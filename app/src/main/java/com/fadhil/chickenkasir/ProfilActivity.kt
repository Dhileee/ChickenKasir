package com.fadhil.chickenkasir

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.fadhil.chickenkasir.firebase.FirebaseHelper
import com.fadhil.chickenkasir.model.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale


class ProfileActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var storage: FirebaseStorage
    private var selectedQrisUri: Uri? = null
    private var currentUser: User? = null

    private val qrisPickerLauncher = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            selectedQrisUri = it
            findViewById<ImageView>(R.id.ivQrisPreview).setImageURI(it)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        auth = FirebaseAuth.getInstance()
        storage = FirebaseStorage.getInstance()

        val tvAvatarInitial = findViewById<TextView>(R.id.tvAvatarInitial)
        val tvNamaProfile = findViewById<TextView>(R.id.tvNamaProfile)
        val tvRoleProfile = findViewById<TextView>(R.id.tvRoleProfile)
        val tvUsernameProfile = findViewById<TextView>(R.id.tvUsernameProfile)
        val tvEmailProfile = findViewById<TextView>(R.id.tvEmailProfile)
        val tvTanggalDaftar = findViewById<TextView>(R.id.tvTanggalDaftar)
        val ivQrisPreview = findViewById<ImageView>(R.id.ivQrisPreview)
        val etNamaBank = findViewById<EditText>(R.id.etNamaBank)
        val etNomorRekening = findViewById<EditText>(R.id.etNomorRekening)
        val etNamaRekening = findViewById<EditText>(R.id.etNamaRekening)
        val btnUploadQris = findViewById<Button>(R.id.btnUploadQris)
        val btnSimpanPembayaran = findViewById<Button>(R.id.btnSimpanPembayaran)
        val btnBack = findViewById<Button>(R.id.btnBack)
        val btnLogoutProfile = findViewById<Button>(R.id.btnLogoutProfile)

        // Load user data
        val firebaseUser = auth.currentUser
        if (firebaseUser != null) {
            lifecycleScope.launch {
                val user = FirebaseHelper.getUser(firebaseUser.uid)

                if (user != null) {
                    currentUser = user

                    runOnUiThread {
                        // Avatar initial
                        val initial = user.nama.firstOrNull()?.uppercaseChar()?.toString() ?: "U"
                        tvAvatarInitial.text = initial

                        // Data user
                        tvNamaProfile.text = user.nama
                        tvRoleProfile.text = user.role.replaceFirstChar { it.uppercase() }
                        tvUsernameProfile.text = user.username
                        tvEmailProfile.text = user.email

                        // Format tanggal daftar
                        val dateFormat = SimpleDateFormat("dd MMMM yyyy", Locale("id", "ID"))
                        tvTanggalDaftar.text = dateFormat.format(Date(user.tanggalDaftar))

                        // Load QRIS image
                        if (user.qrisImageUrl.isNotEmpty()) {
                            Glide.with(this@ProfileActivity)
                                .load(user.qrisImageUrl)
                                .into(ivQrisPreview)
                        }

                        // Load rekening data
                        if (user.nomorRekening.isNotEmpty()) {
                            // Parse "BCA - 1234567890" jadi bank & nomor
                            val parts = user.nomorRekening.split(" - ")
                            if (parts.size == 2) {
                                etNamaBank.setText(parts[0])
                                etNomorRekening.setText(parts[1])
                            }
                        }

                        if (user.namaRekening.isNotEmpty()) {
                            etNamaRekening.setText(user.namaRekening)
                        }
                    }
                }
            }
        } else {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
            return
        }

        btnUploadQris.setOnClickListener {
            qrisPickerLauncher.launch("image/*")
        }

        btnSimpanPembayaran.setOnClickListener {
            val namaBank = etNamaBank.text.toString().trim()
            val nomorRekening = etNomorRekening.text.toString().trim()
            val namaRekening = etNamaRekening.text.toString().trim()

            if (selectedQrisUri == null && namaBank.isEmpty() && nomorRekening.isEmpty()) {
                Toast.makeText(this, "Isi minimal satu pengaturan", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            savePembayaranSettings(namaBank, nomorRekening, namaRekening)
        }

        btnBack.setOnClickListener {
            finish()
        }

        btnLogoutProfile.setOnClickListener {
            auth.signOut()
            Toast.makeText(this, "Logout berhasil", Toast.LENGTH_SHORT).show()
            val intent = Intent(this, LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        }
    }

    private fun savePembayaranSettings(namaBank: String, nomorRekening: String, namaRekening: String) {
        val user = currentUser ?: return
        val firebaseUser = auth.currentUser ?: return

        Toast.makeText(this, "Menyimpan...", Toast.LENGTH_SHORT).show()

        lifecycleScope.launch {
            try {
                var qrisUrl = user.qrisImageUrl

                // Upload QRIS image kalo ada
                if (selectedQrisUri != null) {
                    val storageRef = storage.reference.child("qris_images/${firebaseUser.uid}.jpg")
                    storageRef.putFile(selectedQrisUri!!).await()
                    qrisUrl = storageRef.downloadUrl.await().toString()
                }

                // Format nomor rekening: "BCA - 1234567890"
                val nomorRekeningFormatted = if (namaBank.isNotEmpty() && nomorRekening.isNotEmpty()) {
                    "$namaBank - $nomorRekening"
                } else {
                    user.nomorRekening
                }

                // Update user di Firestore
                val updatedUser = user.copy(
                    qrisImageUrl = qrisUrl,
                    nomorRekening = nomorRekeningFormatted,
                    namaRekening = namaRekening.ifEmpty { user.namaRekening }
                )

                FirebaseHelper.updateUser(firebaseUser.uid, updatedUser)

                runOnUiThread {
                    Toast.makeText(this@ProfileActivity, "Pengaturan pembayaran tersimpan!", Toast.LENGTH_LONG).show()
                    currentUser = updatedUser
                    selectedQrisUri = null
                }

            } catch (e: Exception) {
                runOnUiThread {
                    Toast.makeText(this@ProfileActivity, "Error: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }
        }
    }
}