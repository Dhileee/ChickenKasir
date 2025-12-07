package com.fadhil.chickenkasir

import android.content.Intent
import android.os.Bundle
import android.text.TextWatcher
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.RadioGroup
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.fadhil.chickenkasir.adapter.CartAdapter
import com.fadhil.chickenkasir.firebase.FirebaseHelper
import com.fadhil.chickenkasir.model.Transaksi
import com.fadhil.chickenkasir.model.TransaksiItem
import com.fadhil.chickenkasir.utils.CartManager
import kotlinx.coroutines.launch
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

        val dialogView = layoutInflater.inflate(R.layout.dialog_payment, null)
        val tvTotalDialog = dialogView.findViewById<TextView>(R.id.tvTotalDialog)
        val rgMetodePembayaran = dialogView.findViewById<RadioGroup>(R.id.rgMetodePembayaran)
        val etUangBayar = dialogView.findViewById<EditText>(R.id.etUangBayar)
        val tvKembalian = dialogView.findViewById<TextView>(R.id.tvKembalian)

        tvTotalDialog.text = "Total: Rp ${String.format("%,d", total)}"

        rgMetodePembayaran.setOnCheckedChangeListener { _, checkedId ->
            when (checkedId) {
                R.id.rbCash -> {
                    etUangBayar.isEnabled = true
                    etUangBayar.setText("")
                    etUangBayar.hint = "Uang Bayar"
                    tvKembalian.visibility = View.VISIBLE
                    tvKembalian.text = "Kembalian: Rp 0"
                }
                R.id.rbQRIS, R.id.rbTransfer -> {
                    etUangBayar.isEnabled = false
                    etUangBayar.setText(total.toString())
                    tvKembalian.visibility = View.GONE
                }
            }
        }

        val dialog = AlertDialog.Builder(this)
            .setTitle("Pembayaran")
            .setView(dialogView)
            .setPositiveButton("Bayar") { _, _ ->
                val metodePembayaran = when (rgMetodePembayaran.checkedRadioButtonId) {
                    R.id.rbCash -> "Cash"
                    R.id.rbQRIS -> "QRIS"
                    R.id.rbTransfer -> "Transfer"
                    else -> "Cash"
                }

                when (metodePembayaran) {
                    "Cash" -> {
                        val uangBayar = etUangBayar.text.toString().toIntOrNull() ?: 0
                        if (uangBayar >= total) {
                            val kembalian = uangBayar - total
                            saveTransaksi(total, uangBayar, kembalian, metodePembayaran)
                        } else {
                            Toast.makeText(this, "Uang tidak cukup", Toast.LENGTH_SHORT).show()
                        }
                    }
                    "QRIS" -> {
                        showQRISDialog(total)
                    }
                    "Transfer" -> {
                        showTransferDialog(total)
                    }
                }
            }
            .setNegativeButton("Batal", null)
            .create()

        dialog.show()

        // Listener untuk auto-calculate kembalian SAAT INPUT
        etUangBayar.addTextChangedListener(object : android.text.TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: android.text.Editable?) {
                if (rgMetodePembayaran.checkedRadioButtonId == R.id.rbCash) {
                    val uangBayar = s.toString().toIntOrNull() ?: 0
                    val kembalian = uangBayar - total
                    if (kembalian >= 0) {
                        tvKembalian.text = "Kembalian: Rp ${String.format("%,d", kembalian)}"
                        tvKembalian.setTextColor(resources.getColor(android.R.color.holo_green_dark, null))
                    } else {
                        tvKembalian.text = "Uang kurang: Rp ${String.format("%,d", kotlin.math.abs(kembalian))}"
                        tvKembalian.setTextColor(resources.getColor(android.R.color.holo_red_dark, null))
                    }
                }
            }
        })
    }

    private fun showQRISDialog(total: Int) {
        val dialogView = layoutInflater.inflate(R.layout.dialog_qris, null)
        val tvTotalQRIS = dialogView.findViewById<TextView>(R.id.tvTotalQRIS)
        val ivQrisCode = dialogView.findViewById<ImageView>(R.id.ivQrisCode)
        val btnKonfirmasi = dialogView.findViewById<Button>(R.id.btnKonfirmasiQRIS)

        tvTotalQRIS.text = "Total: Rp ${String.format("%,d", total)}"

        // Load QRIS image dari user profile
        val currentUser = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser
        if (currentUser != null) {
            lifecycleScope.launch {
                val user = com.fadhil.chickenkasir.firebase.FirebaseHelper.getUser(currentUser.uid)

                runOnUiThread {
                    if (user != null && user.qrisImageUrl.isNotEmpty()) {
                        // Load QR QRIS dari Firebase Storage
                        com.bumptech.glide.Glide.with(this@CartActivity)
                            .load(user.qrisImageUrl)
                            .into(ivQrisCode)
                    } else {
                        // Placeholder kalo belum upload
                        ivQrisCode.setImageResource(android.R.drawable.ic_menu_gallery)
                    }
                }
            }
        }

        val dialog = AlertDialog.Builder(this)
            .setView(dialogView)
            .setCancelable(false)
            .create()

        dialog.show()

        btnKonfirmasi.setOnClickListener {
            dialog.dismiss()
            saveTransaksi(total, total, 0, "QRIS")
        }
    }

    private fun showTransferDialog(total: Int) {
        val dialogView = layoutInflater.inflate(R.layout.dialog_transfer, null)
        val tvTotalTransfer = dialogView.findViewById<TextView>(R.id.tvTotalTransfer)
        val tvNomorRekening = dialogView.findViewById<TextView>(R.id.tvNomorRekening)
        val tvNamaPemilik = dialogView.findViewById<TextView>(R.id.tvNamaPemilik)
        val btnKonfirmasi = dialogView.findViewById<Button>(R.id.btnKonfirmasiTransfer)

        tvTotalTransfer.text = "Total: Rp ${String.format("%,d", total)}"

        // Load rekening dari user profile
        val currentUser = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser
        if (currentUser != null) {
            lifecycleScope.launch {
                val user = com.fadhil.chickenkasir.firebase.FirebaseHelper.getUser(currentUser.uid)

                runOnUiThread {
                    if (user != null && user.nomorRekening.isNotEmpty()) {
                        tvNomorRekening.text = user.nomorRekening
                        tvNamaPemilik.text = "a.n. ${user.namaRekening}"
                    } else {
                        tvNomorRekening.text = "Belum diatur (set di Profil)"
                        tvNamaPemilik.text = ""
                    }
                }
            }
        }

        val dialog = AlertDialog.Builder(this)
            .setView(dialogView)
            .setCancelable(false)
            .create()

        dialog.show()

        btnKonfirmasi.setOnClickListener {
            dialog.dismiss()
            saveTransaksi(total, total, 0, "Transfer")
        }
    }

    private fun saveTransaksi(total: Int, uangBayar: Int, kembalian: Int, metodePembayaran: String) {
        android.util.Log.d("CART_DEBUG", "===== saveTransaksi START =====")
        android.util.Log.d("CART_DEBUG", "total: $total, uangBayar: $uangBayar, kembalian: $kembalian, metode: $metodePembayaran")

        val cartItems = CartManager.cartList.toList()
        android.util.Log.d("CART_DEBUG", "cartItems size: ${cartItems.size}")

        val items = cartItems.map { item ->
            TransaksiItem(
                menuId = item.menu.id,
                namaMenu = item.menu.nama,
                harga = item.menu.harga,
                jumlah = item.jumlah,
                subtotal = item.getSubtotal()
            )
        }

        val transaksi = Transaksi(
            tanggal = System.currentTimeMillis(),
            total = total,
            uangBayar = uangBayar,
            kembalian = kembalian,
            metodePembayaran = metodePembayaran,
            items = items
        )

        android.util.Log.d("CART_DEBUG", "Launching coroutine to save to Firebase...")

        lifecycleScope.launch {
            android.util.Log.d("CART_DEBUG", "Inside coroutine, calling FirebaseHelper.addTransaksi...")

            val transaksiId = FirebaseHelper.addTransaksi(transaksi)

            android.util.Log.d("CART_DEBUG", "Transaksi saved, ID: $transaksiId")

            CartManager.clear()

            android.util.Log.d("CART_DEBUG", "Cart cleared, running on UI thread...")

            runOnUiThread {
                android.util.Log.d("CART_DEBUG", "On UI thread, calling showStrukDialog...")
                showStrukDialog(transaksiId, cartItems, total, uangBayar, kembalian, metodePembayaran)
            }
        }
    }

    private fun showStrukDialog(
        transaksiId: String,
        cartItems: List<com.fadhil.chickenkasir.utils.CartItem>,
        total: Int,
        uangBayar: Int,
        kembalian: Int,
        metodePembayaran: String
    ) {
        android.util.Log.d("STRUK_DEBUG", "===== showStrukDialog START =====")
        android.util.Log.d("STRUK_DEBUG", "transaksiId: $transaksiId")
        android.util.Log.d("STRUK_DEBUG", "cartItems size: ${cartItems.size}")
        android.util.Log.d("STRUK_DEBUG", "total: $total")
        android.util.Log.d("STRUK_DEBUG", "metodePembayaran: $metodePembayaran")

        try {
            val intent = Intent(this, StrukActivity::class.java)
            android.util.Log.d("STRUK_DEBUG", "Intent created for StrukActivity")

            val gson = com.google.gson.Gson()
            val cartItemsJson = gson.toJson(cartItems)
            android.util.Log.d("STRUK_DEBUG", "cartItemsJson length: ${cartItemsJson.length}")
            android.util.Log.d("STRUK_DEBUG", "cartItemsJson: $cartItemsJson")

            intent.putExtra("transaksiId", transaksiId)
            intent.putExtra("cartItems", cartItemsJson)
            intent.putExtra("total", total)
            intent.putExtra("uangBayar", uangBayar)
            intent.putExtra("kembalian", kembalian)
            intent.putExtra("metodePembayaran", metodePembayaran)

            android.util.Log.d("STRUK_DEBUG", "All extras added to intent")
            android.util.Log.d("STRUK_DEBUG", "Calling startActivity...")

            startActivity(intent)

            android.util.Log.d("STRUK_DEBUG", "startActivity called successfully")
            android.util.Log.d("STRUK_DEBUG", "Calling finish() on CartActivity")

            finish()

            android.util.Log.d("STRUK_DEBUG", "===== showStrukDialog END =====")

        } catch (e: Exception) {
            android.util.Log.e("STRUK_DEBUG", "ERROR in showStrukDialog: ${e.message}")
            e.printStackTrace()
            Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }
}