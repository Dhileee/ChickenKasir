package com.fadhil.chickenkasir

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.fadhil.chickenkasir.firebase.FirebaseHelper
import com.fadhil.chickenkasir.model.User
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch

class RegisterActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var etNama: EditText
    private lateinit var etUsername: EditText
    private lateinit var etEmail: EditText
    private lateinit var etPassword: EditText
    private lateinit var etPasswordConfirm: EditText
    private lateinit var btnDaftar: Button
    private lateinit var tvSudahPunyaAkun: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        auth = FirebaseAuth.getInstance()

        etNama = findViewById(R.id.etNamaRegister)
        etUsername = findViewById(R.id.etUsernameRegister)
        etEmail = findViewById(R.id.etEmailRegister)
        etPassword = findViewById(R.id.etPasswordRegister)
        etPasswordConfirm = findViewById(R.id.etPasswordConfirm)
        btnDaftar = findViewById(R.id.btnDaftar)
        tvSudahPunyaAkun = findViewById(R.id.tvSudahPunyaAkun)

        btnDaftar.setOnClickListener {
            val nama = etNama.text.toString().trim()
            val username = etUsername.text.toString().trim().lowercase()
            val email = etEmail.text.toString().trim()
            val password = etPassword.text.toString()
            val passwordConfirm = etPasswordConfirm.text.toString()

            when {
                nama.isEmpty() -> {
                    Toast.makeText(this, "Nama harus diisi", Toast.LENGTH_SHORT).show()
                }
                username.isEmpty() -> {
                    Toast.makeText(this, "Username harus diisi", Toast.LENGTH_SHORT).show()
                }
                username.contains(" ") -> {
                    Toast.makeText(this, "Username tidak boleh ada spasi", Toast.LENGTH_SHORT).show()
                }
                username.length < 3 -> {
                    Toast.makeText(this, "Username minimal 3 karakter", Toast.LENGTH_SHORT).show()
                }
                email.isEmpty() -> {
                    Toast.makeText(this, "Email harus diisi", Toast.LENGTH_SHORT).show()
                }
                password.isEmpty() -> {
                    Toast.makeText(this, "Password harus diisi", Toast.LENGTH_SHORT).show()
                }
                password.length < 6 -> {
                    Toast.makeText(this, "Password minimal 6 karakter", Toast.LENGTH_SHORT).show()
                }
                password != passwordConfirm -> {
                    Toast.makeText(this, "Password tidak cocok", Toast.LENGTH_SHORT).show()
                }
                else -> {
                    checkUsernameAndRegister(nama, username, email, password)
                }
            }
        }

        tvSudahPunyaAkun.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }
    }

    private fun checkUsernameAndRegister(nama: String, username: String, email: String, password: String) {
        btnDaftar.isEnabled = false
        Toast.makeText(this, "Memeriksa username...", Toast.LENGTH_SHORT).show()

        lifecycleScope.launch {
            val existingUser = FirebaseHelper.getUserByUsername(username)

            if (existingUser != null) {
                runOnUiThread {
                    btnDaftar.isEnabled = true
                    Toast.makeText(this@RegisterActivity, "Username sudah dipakai", Toast.LENGTH_SHORT).show()
                }
            } else {
                registerUser(nama, username, email, password)
            }
        }
    }

    private fun registerUser(nama: String, username: String, email: String, password: String) {
        android.util.Log.d("REGISTER_DEBUG", "registerUser START")
        android.util.Log.d("REGISTER_DEBUG", "email: $email, username: $username")

        Toast.makeText(this, "Mendaftar...", Toast.LENGTH_SHORT).show()

        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                android.util.Log.d("REGISTER_DEBUG", "Firebase auth complete, success: ${task.isSuccessful}")

                if (task.isSuccessful) {
                    android.util.Log.d("REGISTER_DEBUG", "Auth successful, creating user in Firestore...")

                    val firebaseUser = auth.currentUser
                    if (firebaseUser != null) {
                        val user = User(
                            id = firebaseUser.uid,
                            nama = nama,
                            username = username,
                            email = email,
                            role = "kasir"
                        )

                        android.util.Log.d("REGISTER_DEBUG", "User object created, saving to Firestore...")

                        lifecycleScope.launch {
                            try {
                                FirebaseHelper.createUser(user)

                                android.util.Log.d("REGISTER_DEBUG", "User saved to Firestore successfully")

                                kotlinx.coroutines.delay(300)

                                runOnUiThread {
                                    Toast.makeText(
                                        this@RegisterActivity,
                                        "Registrasi berhasil! Silakan login dengan username.",
                                        Toast.LENGTH_LONG
                                    ).show()

                                    android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
                                        val intent = Intent(this@RegisterActivity, LoginActivity::class.java)
                                        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                                        startActivity(intent)
                                        finish()
                                    }, 500)
                                }
                            } catch (e: Exception) {
                                android.util.Log.e("REGISTER_DEBUG", "Error saving user: ${e.message}")
                                e.printStackTrace()

                                runOnUiThread {
                                    btnDaftar.isEnabled = true
                                    Toast.makeText(
                                        this@RegisterActivity,
                                        "Error menyimpan data: ${e.message}",
                                        Toast.LENGTH_LONG
                                    ).show()
                                }
                            }
                        }
                    } else {
                        android.util.Log.e("REGISTER_DEBUG", "Firebase user is null!")
                        btnDaftar.isEnabled = true
                        Toast.makeText(this, "Error: User null", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    btnDaftar.isEnabled = true

                    android.util.Log.e("REGISTER_DEBUG", "Auth failed: ${task.exception?.message}")

                    val errorMessage = when {
                        task.exception?.message?.contains("email address is already in use") == true ->
                            "Email sudah terdaftar"
                        task.exception?.message?.contains("network error") == true ->
                            "Tidak ada koneksi internet"
                        else ->
                            "Registrasi gagal: ${task.exception?.message}"
                    }
                    Toast.makeText(this, errorMessage, Toast.LENGTH_LONG).show()
                }
            }
    }
}