package com.fadhil.chickenkasir.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "transaksi_detail")
data class TransaksiDetailEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val transaksiId: Int,
    val menuId: Int,
    val namaMenu: String,
    val harga: Int,
    val jumlah: Int,
    val subtotal: Int
)