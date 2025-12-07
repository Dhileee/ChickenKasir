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
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch

class LoginActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var etUsername: EditText
    private lateinit var etPassword: EditText
    private lateinit var btnLogin: Button
    private lateinit var tvDaftar: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        auth = FirebaseAuth.getInstance()

        // Cek apakah user sudah login
        if (auth.currentUser != null) {
            startActivity(Intent(this, MainActivity::class.java))
            finish()
            return
        }

        etUsername = findViewById(R.id.etUsername)
        etPassword = findViewById(R.id.etPassword)
        btnLogin = findViewById(R.id.btnLogin)
        tvDaftar = findViewById(R.id.tvDaftar)

        btnLogin.setOnClickListener {
            val usernameOrEmail = etUsername.text.toString().trim()
            val password = etPassword.text.toString()

            when {
                usernameOrEmail.isEmpty() -> {
                    Toast.makeText(this, "Username/Email harus diisi", Toast.LENGTH_SHORT).show()
                }
                password.isEmpty() -> {
                    Toast.makeText(this, "Password harus diisi", Toast.LENGTH_SHORT).show()
                }
                else -> {
                    loginUser(usernameOrEmail, password)
                }
            }
        }

        tvDaftar.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }
    }

    private fun loginUser(usernameOrEmail: String, password: String) {
        btnLogin.isEnabled = false
        Toast.makeText(this, "Login...", Toast.LENGTH_SHORT).show()

        // Cek apakah input email atau username
        if (usernameOrEmail.contains("@")) {
            // Langsung login pake email
            loginWithEmail(usernameOrEmail, password)
        } else {
            // Cari email berdasarkan username
            lifecycleScope.launch {
                val user = FirebaseHelper.getUserByUsername(usernameOrEmail.lowercase())

                if (user != null) {
                    runOnUiThread {
                        loginWithEmail(user.email, password)
                    }
                } else {
                    runOnUiThread {
                        btnLogin.isEnabled = true
                        Toast.makeText(this@LoginActivity, "Username tidak ditemukan", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

    private fun loginWithEmail(email: String, password: String) {
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    Toast.makeText(this, "Login berhasil!", Toast.LENGTH_SHORT).show()
                    startActivity(Intent(this, MainActivity::class.java))
                    finish()
                } else {
                    btnLogin.isEnabled = true
                    val errorMessage = when {
                        task.exception?.message?.contains("no user record") == true ->
                            "Email tidak terdaftar"
                        task.exception?.message?.contains("password is invalid") == true ->
                            "Password salah"
                        task.exception?.message?.contains("network error") == true ->
                            "Tidak ada koneksi internet"
                        else ->
                            "Login gagal: ${task.exception?.message}"
                    }
                    Toast.makeText(this, errorMessage, Toast.LENGTH_LONG).show()
                }
            }
    }
}