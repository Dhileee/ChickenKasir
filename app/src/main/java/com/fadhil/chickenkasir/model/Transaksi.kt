package com.fadhil.chickenkasir.model

data class Transaksi(
    var id: String = "",
    val tanggal: Long = 0,
    val total: Int = 0,
    val uangBayar: Int = 0,
    val kembalian: Int = 0,
    val metodePembayaran: String = "",
    val items: List<TransaksiItem> = emptyList()
)

data class TransaksiItem(
    val menuId: String = "",
    val namaMenu: String = "",
    val harga: Int = 0,
    val jumlah: Int = 0,
    val subtotal: Int = 0
)