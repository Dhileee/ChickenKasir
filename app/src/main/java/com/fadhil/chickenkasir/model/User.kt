package com.fadhil.chickenkasir.model

data class User(
    var id: String = "",
    val nama: String = "",
    val username: String = "",
    val email: String = "",
    val role: String = "kasir",
    val tanggalDaftar: Long = System.currentTimeMillis(),
    val qrisImageUrl: String = "", // URL gambar QR QRIS
    val nomorRekening: String = "", // Format: "BCA - 1234567890"
    val namaRekening: String = "" // Nama pemilik rekening
)