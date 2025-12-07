package com.fadhil.chickenkasir.model

data class Menu(
    var id: String = "",
    val nama: String = "",
    val harga: Int = 0,
    val gambar: String = "",
    val imageUrl: String = "",
    val kategori: String = "Reguler",
    val deskripsi: String = ""
)