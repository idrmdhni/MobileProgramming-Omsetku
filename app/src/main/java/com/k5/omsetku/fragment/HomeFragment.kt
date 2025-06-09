package com.k5.omsetku.fragment

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AlertDialog
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.k5.omsetku.LogInActivity
import com.k5.omsetku.R
import com.k5.omsetku.databinding.FragmentHomeBinding
import com.k5.omsetku.utils.LoadFragment
import kotlin.jvm.java

class HomeFragment : Fragment() {
    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private var userProfileListener: ListenerRegistration? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val callback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                showExitConfirmationDialog()
            }
        }
        requireActivity().onBackPressedDispatcher.addCallback(this, callback)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        val account: ImageView = binding.account

        account.setOnClickListener { LoadFragment.loadChildFragment(
            parentFragmentManager,
            R.id.host_fragment,
            AccountFragment()
        ) }

    }

    override fun onResume() {
        super.onResume()
        // Mulai mendengarkan data saat fragment aktif
        setupFirestoreListeners()
    }

    override fun onPause() {
        super.onPause()
        // Hentikan mendengarkan data saat fragment tidak aktif
        removeFirestoreListeners()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        removeFirestoreListeners() // Pastikan listener dilepas juga saat view dihancurkan
        _binding = null
    }

    @SuppressLint("SetTextI18n")
    private fun setupFirestoreListeners() {
        val currentUser = auth.currentUser
        if (currentUser != null) {
            val userUid = currentUser.uid

            // Listener untuk profil pengguna
            userProfileListener = db.collection("users").document(userUid)
                .addSnapshotListener { documentSnapshot, e ->
                    if (e != null) {
                        Toast.makeText(requireContext(), "Failed to load profile: ${e.message}", Toast.LENGTH_SHORT).show()
                        return@addSnapshotListener
                    }

                    if (documentSnapshot != null && documentSnapshot.exists()) {
                        val name = documentSnapshot.getString("name")
                        val email = documentSnapshot.getString("email")
                        binding.accountName.text = name ?: email
                    } else {
                        binding.accountName.text = "Anonym!"
                    }
                }
        } else {
            // Jika tidak ada pengguna yang login, arahkan kembali ke LoginActivity
            Toast.makeText(requireContext(), "Anda harus login.", Toast.LENGTH_SHORT).show()
            startActivity(Intent(requireContext(), LogInActivity::class.java))
            requireActivity().finish() // Penting: tutup activity saat ini
        }
    }

    private fun removeFirestoreListeners() {
        userProfileListener?.remove() // Hentikan listener profil
        userProfileListener = null
    }

    private fun showExitConfirmationDialog() {
        AlertDialog.Builder(requireContext()) // Menggunakan requireContext() untuk mendapatkan Context
            .setTitle("Exit Confirmation")
            .setMessage("Are you sure want to exit the application?")
            .setPositiveButton("Yes") { dialog, which ->
                // Tutup semua Activity dan keluar dari aplikasi
                activity?.finishAffinity()
            }
            .setNegativeButton("No") { dialog, which ->
                dialog.dismiss() // Tutup dialog
            }
            .show()
    }
}