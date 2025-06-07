package com.k5.omsetku

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.LinearLayout
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.graphics.toColorInt
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.k5.omsetku.databinding.ActivitySignUpBinding
import com.k5.omsetku.utils.FirebaseUtils

class SignUpActivity : AppCompatActivity() {
    private var _binding: ActivitySignUpBinding? = null
    private val binding get() = _binding!!
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    @Suppress("DEPRECATION")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        _binding = ActivitySignUpBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseUtils.auth
        db = FirebaseUtils.db

        window.statusBarColor = "#205072".toColorInt()
        window.navigationBarColor = "#205072".toColorInt()
        window.decorView.systemUiVisibility = 0

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.signup)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        val backToLogin: LinearLayout = findViewById(R.id.back_to_login)

        backToLogin.setOnClickListener {
            val intent = Intent(this, LogInActivity::class.java)
            startActivity(intent)
        }
        binding.btnSignup.setOnClickListener {
            val inputEmail = binding.inputEmail.text.toString().trim()
            val inputName =  binding.inputName.text.toString().trim()
            val inputPassword = binding.inputPw.text.toString().trim()
            val inputConfirmPassword = binding.inputConfirmPw.text.toString().trim()

            if (inputName.isEmpty() || inputEmail.isEmpty() || inputPassword.isEmpty() || inputConfirmPassword.isEmpty()) {
                Toast.makeText(this, "Input tidak boleh kosong!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (inputPassword != inputConfirmPassword) {
                Toast.makeText(this, "Password tidak sama!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (inputPassword.length < 8) {
                Toast.makeText(this, "Password minimal 8 karakter!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            auth.createUserWithEmailAndPassword(inputEmail, inputPassword).
                addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        val user: FirebaseUser? = auth.currentUser

                        user?.let { firebaseUser ->
                            val userUid = firebaseUser.uid
                            val userData = hashMapOf(
                                "name" to inputName,
                                "email" to inputEmail
                            )

                            db.collection("users").document(userUid)
                                .set(userData)
                                .addOnSuccessListener {
                                    Toast.makeText(this, "Akun telah berhasil dibuat!", Toast.LENGTH_SHORT).show()
                                    val intent = Intent(this, LogInActivity::class.java)
                                    startActivity(intent)
                                    finish()
                                }
                                .addOnFailureListener { e ->
                                    Toast.makeText(this, "Registrasi berhasil, namun ada kesalahan dengan keterangan ${e.message}!", Toast.LENGTH_SHORT).show()
                                    firebaseUser.delete()
                                }
                        }
                    } else {
                        Toast.makeText(this, "Registrasi gagal!: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                    }
                }
        }
    }
}