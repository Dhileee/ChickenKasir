package com.fadhil.chickenkasir.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "transaksi")
data class TransaksiEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val tanggal: Long,
    val total: Int,
    val uangBayar: Int,
    val kembalian: Int
)