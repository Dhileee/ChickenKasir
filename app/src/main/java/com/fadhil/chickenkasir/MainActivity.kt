package com.fadhil.chickenkasir

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.fadhil.chickenkasir.data.database.AppDatabase
import com.fadhil.chickenkasir.data.entity.MenuEntity

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val db = AppDatabase.getDatabase(this)
        val menuDao = db.menuDao()

        // Insert dummy data cuma kalo database kosong
        if (menuDao.getAllMenu().isEmpty()) {
            menuDao.insertMenu(MenuEntity(nama = "Ayam Original", harga = 15000, kategori = "Makanan"))
            menuDao.insertMenu(MenuEntity(nama = "Ayam Geprek", harga = 17000, kategori = "Makanan"))
            menuDao.insertMenu(MenuEntity(nama = "Nasi Putih", harga = 5000, kategori = "Makanan"))
            menuDao.insertMenu(MenuEntity(nama = "Es Teh", harga = 3000, kategori = "Minuman"))
        }

        val allMenu = menuDao.getAllMenu()
        android.util.Log.d("MainActivity", "Total menu: ${allMenu.size}")
    }
}