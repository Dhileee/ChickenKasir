package com.fadhil.chickenkasir

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.fadhil.chickenkasir.adapter.MenuAdapter
import com.fadhil.chickenkasir.data.database.AppDatabase
import com.fadhil.chickenkasir.data.entity.MenuEntity
import com.fadhil.chickenkasir.utils.CartManager

class MainActivity : AppCompatActivity() {

    private lateinit var btnCart: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val db = AppDatabase.getDatabase(this)
        val menuDao = db.menuDao()

        // Insert dummy data cuma kalo database kosong
        if (menuDao.getAllMenu().isEmpty()) {
            menuDao.insertMenu(MenuEntity(nama = "Ayam Original", harga = 15000, kategori = "Makanan"))
            menuDao.insertMenu(MenuEntity(nama = "Ayam Geprek", harga = 17000, kategori = "Makanan"))
            menuDao.insertMenu(MenuEntity(nama = "Ayam Bakar", harga = 16000, kategori = "Makanan"))
            menuDao.insertMenu(MenuEntity(nama = "Nasi Putih", harga = 5000, kategori = "Makanan"))
            menuDao.insertMenu(MenuEntity(nama = "Es Teh", harga = 3000, kategori = "Minuman"))
            menuDao.insertMenu(MenuEntity(nama = "Es Jeruk", harga = 5000, kategori = "Minuman"))
        }

        val menuList = menuDao.getAllMenu()

        val rvMenu = findViewById<RecyclerView>(R.id.rvMenu)
        btnCart = findViewById(R.id.btnCart)

        val adapter = MenuAdapter(menuList) { menu ->
            CartManager.addItem(menu)
            updateCartButton()
        }

        rvMenu.layoutManager = GridLayoutManager(this, 2)
        rvMenu.adapter = adapter

        updateCartButton()

        btnCart.setOnClickListener {
            if (CartManager.getTotalItem() > 0) {
                val intent = Intent(this, CartActivity::class.java)
                startActivity(intent)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        updateCartButton()
    }

    private fun updateCartButton() {
        val totalItem = CartManager.getTotalItem()
        btnCart.text = "Keranjang ($totalItem)"
    }
}