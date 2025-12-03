package com.fadhil.chickenkasir.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.fadhil.chickenkasir.data.entity.TransaksiDetailEntity
import com.fadhil.chickenkasir.data.entity.TransaksiEntity

@Dao
interface TransaksiDao {
    @Insert
    fun insertTransaksi(transaksi: TransaksiEntity): Long

    @Insert
    fun insertTransaksiDetail(detail: TransaksiDetailEntity)

    @Query("SELECT * FROM transaksi ORDER BY tanggal DESC")
    fun getAllTransaksi(): List<TransaksiEntity>

    @Query("SELECT * FROM transaksi_detail WHERE transaksiId = :transaksiId")
    fun getDetailByTransaksi(transaksiId: Int): List<TransaksiDetailEntity>
}