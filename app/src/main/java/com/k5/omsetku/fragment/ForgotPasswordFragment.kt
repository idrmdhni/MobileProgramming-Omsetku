package com.k5.omsetku.fragment

// ChooseItemDialogFragment.kt
import android.app.Dialog
import android.graphics.Color
import android.os.Bundle
import android.util.DisplayMetrics
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.view.WindowManager
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import androidx.core.graphics.drawable.toDrawable
import androidx.core.view.doOnLayout
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.k5.omsetku.databinding.PopupForgotPasswordBinding
import com.k5.omsetku.utils.FirebaseUtils

class ForgotPasswordFragment: DialogFragment() {
    private var _binding: PopupForgotPasswordBinding? = null
    private val binding get() = _binding!!
    private val auth = FirebaseUtils.auth

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState)
        // Mengatur banground fragment menjadi transparan agar lengkungan terlihat
        dialog.window?.setBackgroundDrawable(Color.TRANSPARENT.toDrawable())
        dialog.window?.requestFeature(Window.FEATURE_NO_TITLE) // Tidak ada title bar

        return dialog
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = PopupForgotPasswordBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        view.doOnLayout { measuredView ->
            val height = measuredView.height

            dialog?.window?.let { window ->
                val displayMetrics = DisplayMetrics()
                @Suppress("DEPRECATION")
                activity?.windowManager?.defaultDisplay?.getMetrics(displayMetrics)
                val screenWidth = displayMetrics.widthPixels
                val screenHeight = displayMetrics.heightPixels

                // Atur lebar dialog menjadi sekitar 90% dari lebar layar
                val dialogWidth = (screenWidth * 0.90).toInt()
                // Tinggi dialog akan diatur ke WRAP_CONTENT di XML,

                window.setLayout(dialogWidth, WindowManager.LayoutParams.WRAP_CONTENT)

                // Mengatur posisi dialog sedikit lebih ke atas
                window.setGravity(Gravity.CENTER_HORIZONTAL or Gravity.TOP)
                // Mengatur offset Y untuk menyesuaikan jarak dari atas
                val params = window.attributes
                // 50% dari tinggi layar dari atas - 50% tinggi fragment
                params.y = (screenHeight * 0.5 - height * 0.5).toInt()

                // Mengatur transparansi background di belakang dialog
                params.dimAmount = 0.5f
                window.setAttributes(params)
            }
        }

        setupListeners()

    }

    private fun setupListeners() {
        binding.btnCancel.setOnClickListener {
            dismiss()
        }

        binding.btnSend.setOnClickListener {
            val email = binding.inputEmail.text.toString().trim()
            val password = binding.inputPw.text.toString().trim()
            val confirmPassword = binding.inputConfirmPw.text.toString().trim()

            if (email.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
                Toast.makeText(requireContext(), "Input cannot be empty!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (password != confirmPassword) {
                Toast.makeText(requireContext(), "Password not match!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (password.length < 8) {
                Toast.makeText(requireContext(), "The password must be at least 8 characters!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            sendPasswordResetEmail(email)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun sendPasswordResetEmail(email: String) {
        showLoading(true)

        auth.sendPasswordResetEmail(email)
            .addOnCompleteListener { task ->
                showLoading(false)

                if (task.isSuccessful) {
                    Toast.makeText(requireContext(), "Password reset link has been sent to your email", Toast.LENGTH_LONG).show()

                    dismiss()
                } else {
                    try {
                        throw task.exception!!
                    } catch (e: FirebaseAuthInvalidUserException) {
                        // Error jika email tidak terdaftar di Firebase Auth
                        Toast.makeText(requireContext(), "An error occurred: ${e.message}", Toast.LENGTH_LONG).show()
                    } catch (e: Exception) {
                        // Error lainnya (misal: tidak ada koneksi internet)
                        Toast.makeText(requireContext(), "An error occurred: ${e.message}", Toast.LENGTH_LONG).show()
                    }
                }
            }
    }

    private fun showLoading(isLoading: Boolean) {
        if (isLoading) {
            binding.progressBar.visibility = View.VISIBLE
            binding.btnSend.visibility = View.GONE
            binding.formWrapper.visibility = View.GONE
        } else {
            binding.progressBar.visibility = View.GONE
            binding.btnSend.visibility = View.VISIBLE
            binding.formWrapper.visibility = View.VISIBLE
        }
    }
}
