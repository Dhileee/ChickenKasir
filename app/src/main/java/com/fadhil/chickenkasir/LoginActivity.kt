package com.fadhil.chickenkasir

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.textfield.TextInputEditText

class LoginActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        val etUsername = findViewById<TextInputEditText>(R.id.etUsername)
        val etPassword = findViewById<TextInputEditText>(R.id.etPassword)
        val btnLogin = findViewById<Button>(R.id.btnLogin)

        btnLogin.setOnClickListener {
            val username = etUsername.text.toString().trim()
            val password = etPassword.text.toString().trim()

            // Validasi kosong
            if (username.isEmpty()) {
                etUsername.error = "Username tidak boleh kosong"
                etUsername.requestFocus()
                return@setOnClickListener
            }

            if (password.isEmpty()) {
                etPassword.error = "Password tidak boleh kosong"
                etPassword.requestFocus()
                return@setOnClickListener
            }

            // Validasi hardcode (username: admin, password: admin123)
            if (username == "admin" && password == "admin123") {
                Toast.makeText(this, "Login berhasil!", Toast.LENGTH_SHORT).show()

                // Intent ke MainActivity
                val intent = Intent(this, MainActivity::class.java)
                startActivity(intent)
                finish() // Tutup LoginActivity biar gak bisa back
            } else {
                Toast.makeText(this, "Username atau password salah!", Toast.LENGTH_SHORT).show()
            }
        }
    }
}