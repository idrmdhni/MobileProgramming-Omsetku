package com.k5.omsetku.fragment

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.k5.omsetku.LogInActivity
import com.k5.omsetku.R
import com.k5.omsetku.databinding.FragmentEditProductBinding
import com.k5.omsetku.databinding.FragmentHomeBinding
import kotlin.jvm.java

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [HomeFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class HomeFragment : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private var userProfileListener: ListenerRegistration? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
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

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        val account: ImageView = binding.account

        account.setOnClickListener { loadFragment(AccountFragment()) }

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

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment HomeFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            HomeFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }

    @SuppressLint("CommitTransaction")
    private fun loadFragment(fragment: Fragment) {
        parentFragmentManager.beginTransaction()
            .replace(R.id.host_fragment, fragment)
            .commit()
    }
}