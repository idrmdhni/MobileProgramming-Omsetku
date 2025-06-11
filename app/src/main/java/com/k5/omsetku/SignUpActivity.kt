package com.k5.omsetku

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.util.Patterns
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
            val name =  binding.inputName.text.toString().trim()
            val email = binding.inputEmail.text.toString().trim()
            val password = binding.inputPw.text.toString().trim()
            val confirmPassword = binding.inputConfirmPw.text.toString().trim()

            if (name.isEmpty() || email.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
                Toast.makeText(this, "Input cannot be empty!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (password != confirmPassword) {
                Toast.makeText(this, "Password not match!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (password.length < 8) {
                Toast.makeText(this, "The password must be at least 8 characters!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            auth.createUserWithEmailAndPassword(email, password).
                addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        val user: FirebaseUser? = auth.currentUser

                        user?.let { firebaseUser ->
                            val userUid = firebaseUser.uid
                            val userData = hashMapOf(
                                "name" to name,
                                "email" to email
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