package com.fadhil.chickenkasir.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "menu")
data class MenuEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val nama: String,
    val harga: Int,
    val kategori: String
)