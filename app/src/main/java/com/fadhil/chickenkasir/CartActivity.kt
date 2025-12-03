package com.fadhil.chickenkasir

import android.os.Bundle
import android.text.InputType
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.fadhil.chickenkasir.adapter.CartAdapter
import com.fadhil.chickenkasir.data.database.AppDatabase
import com.fadhil.chickenkasir.data.entity.TransaksiDetailEntity
import com.fadhil.chickenkasir.data.entity.TransaksiEntity
import com.fadhil.chickenkasir.utils.CartManager

class CartActivity : AppCompatActivity() {

    private lateinit var tvTotal: TextView
    private lateinit var adapter: CartAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_cart)

        val rvCart = findViewById<RecyclerView>(R.id.rvCart)
        tvTotal = findViewById(R.id.tvTotal)
        val btnBayar = findViewById<Button>(R.id.btnBayar)

        adapter = CartAdapter(CartManager.cartList) {
            updateTotal()
        }

        rvCart.layoutManager = LinearLayoutManager(this)
        rvCart.adapter = adapter

        updateTotal()

        btnBayar.setOnClickListener {
            if (CartManager.cartList.isEmpty()) {
                Toast.makeText(this, "Keranjang kosong", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            showPaymentDialog()
        }
    }

    private fun updateTotal() {
        val total = CartManager.getTotal()
        tvTotal.text = "Rp ${String.format("%,d", total)}"
    }

    private fun showPaymentDialog() {
        val total = CartManager.getTotal()

        val builder = AlertDialog.Builder(this)
        builder.setTitle("Pembayaran")
        builder.setMessage("Total: Rp ${String.format("%,d", total)}")

        val input = EditText(this)
        input.hint = "Masukkan uang bayar"
        input.inputType = InputType.TYPE_CLASS_NUMBER
        builder.setView(input)

        builder.setPositiveButton("Bayar") { _, _ ->
            val uangBayar = input.text.toString().toIntOrNull() ?: 0
            if (uangBayar >= total) {
                val kembalian = uangBayar - total
                saveTransaksi(total, uangBayar, kembalian)
            } else {
                Toast.makeText(this, "Uang tidak cukup", Toast.LENGTH_SHORT).show()
            }
        }

        builder.setNegativeButton("Batal", null)
        builder.show()
    }

    private fun saveTransaksi(total: Int, uangBayar: Int, kembalian: Int) {
        val db = AppDatabase.getDatabase(this)
        val transaksiDao = db.transaksiDao()

        val transaksi = TransaksiEntity(
            tanggal = System.currentTimeMillis(),
            total = total,
            uangBayar = uangBayar,
            kembalian = kembalian
        )

        val transaksiId = transaksiDao.insertTransaksi(transaksi)

        CartManager.cartList.forEach { item ->
            val detail = TransaksiDetailEntity(
                transaksiId = transaksiId.toInt(),
                menuId = item.menu.id,
                namaMenu = item.menu.nama,
                harga = item.menu.harga,
                jumlah = item.jumlah,
                subtotal = item.getSubtotal()
            )
            transaksiDao.insertTransaksiDetail(detail)
        }

        CartManager.clear()

        Toast.makeText(this, "Transaksi berhasil!\nKembalian: Rp ${String.format("%,d", kembalian)}", Toast.LENGTH_LONG).show()
        finish()
    }
}