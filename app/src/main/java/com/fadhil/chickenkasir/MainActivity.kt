package com.fadhil.chickenkasir

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.fadhil.chickenkasir.adapter.MenuAdapter
import com.fadhil.chickenkasir.firebase.FirebaseHelper
import com.fadhil.chickenkasir.model.Menu
import com.fadhil.chickenkasir.utils.CartManager
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainActivity : AppCompatActivity() {

    private lateinit var btnCart: Button
    private lateinit var rvMenu: RecyclerView
    private lateinit var adapter: MenuAdapter
    private lateinit var chipGroup: ChipGroup
    private val allMenuList = mutableListOf<Menu>()
    private val filteredMenuList = mutableListOf<Menu>()
    private var isFirstLoad = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        rvMenu = findViewById(R.id.rvMenu)
        btnCart = findViewById(R.id.btnCart)
        chipGroup = findViewById(R.id.chipGroupKategori)
        val btnKelolaMenu = findViewById<Button>(R.id.btnKelolaMenu)

        // Setup adapter DULU sebelum load data
        adapter = MenuAdapter(filteredMenuList) { menu ->
            CartManager.addItem(menu)
            updateCartButton()
        }

        rvMenu.layoutManager = GridLayoutManager(this, 2)
        rvMenu.adapter = adapter

        // Load data SEKALI di onCreate
        lifecycleScope.launch {
            loadKategoriChips()
            loadMenu()
            withContext(Dispatchers.Main) {
                filterByKategori("Semua")
                isFirstLoad = false
            }
        }

        setupChipListener()
        updateCartButton()

        // Tombol Profil
        findViewById<Button>(R.id.btnProfil)?.setOnClickListener {
            startActivity(Intent(this, ProfileActivity::class.java))
        }

        btnCart.setOnClickListener {
            if (CartManager.getTotalItem() > 0) {
                val intent = Intent(this, CartActivity::class.java)
                startActivity(intent)
            }
        }

        btnKelolaMenu.setOnClickListener {
            val intent = Intent(this, KelolaMenuActivity::class.java)
            startActivity(intent)
        }
    }

    override fun onResume() {
        super.onResume()

        // Skip reload kalo first load (udah di-load di onCreate)
        if (isFirstLoad) {
            return
        }

        android.util.Log.d("MAIN_DEBUG", "onResume - reloading data")

        // Reload HANYA setelah balik dari KelolaMenuActivity
        lifecycleScope.launch {
            loadMenu()
            loadKategoriChips()

            withContext(Dispatchers.Main) {
                val selectedKategori = getSelectedKategori()
                android.util.Log.d("MAIN_DEBUG", "onResume - filtering by: $selectedKategori")
                filterByKategori(selectedKategori)
                updateCartButton()
            }
        }
    }

    private suspend fun loadMenu() {
        android.util.Log.d("MAIN_DEBUG", "loadMenu START")

        allMenuList.clear()
        val menuFromDb = FirebaseHelper.getAllMenu()
        allMenuList.addAll(menuFromDb)

        android.util.Log.d("MAIN_DEBUG", "loadMenu - loaded ${menuFromDb.size} items")
    }

    private suspend fun loadKategoriChips() {
        val kategoriFromDb = FirebaseHelper.getAllKategori()

        withContext(Dispatchers.Main) {
            val currentSelectedId = chipGroup.checkedChipId

            chipGroup.removeAllViews()

            val chipSemua = Chip(this@MainActivity)
            chipSemua.text = "Semua"
            chipSemua.isCheckable = true
            chipSemua.isChecked = (currentSelectedId == 999 || currentSelectedId == -1)
            chipSemua.id = 999
            chipSemua.setChipBackgroundColorResource(android.R.color.holo_orange_light)
            chipGroup.addView(chipSemua)

            kategoriFromDb.forEachIndexed { index, kategori ->
                val chip = Chip(this@MainActivity)
                chip.text = kategori.nama
                chip.isCheckable = true
                chip.id = index + 1000
                chip.isChecked = (currentSelectedId == index + 1000)
                chipGroup.addView(chip)
            }
        }
    }

    private fun setupChipListener() {
        chipGroup.setOnCheckedChangeListener { group, checkedId ->
            val selectedChip = group.findViewById<Chip>(checkedId)
            val kategori = selectedChip?.text?.toString() ?: "Semua"

            android.util.Log.d("MAIN_DEBUG", "Chip clicked: $kategori")

            filterByKategori(kategori)
        }
    }

    private fun getSelectedKategori(): String {
        val selectedChipId = chipGroup.checkedChipId
        val selectedChip = chipGroup.findViewById<Chip>(selectedChipId)
        return selectedChip?.text?.toString() ?: "Semua"
    }

    private fun filterByKategori(kategori: String) {
        android.util.Log.d("MAIN_DEBUG", "filterByKategori START - kategori: $kategori")
        android.util.Log.d("MAIN_DEBUG", "allMenuList size: ${allMenuList.size}")
        android.util.Log.d("MAIN_DEBUG", "filteredMenuList size BEFORE clear: ${filteredMenuList.size}")

        // CRITICAL: Clear DULU sebelum add
        filteredMenuList.clear()

        if (kategori == "Semua") {
            filteredMenuList.addAll(allMenuList)
        } else {
            filteredMenuList.addAll(allMenuList.filter { it.kategori == kategori })
        }

        android.util.Log.d("MAIN_DEBUG", "filteredMenuList size AFTER: ${filteredMenuList.size}")

        adapter.notifyDataSetChanged()
    }

    private fun updateCartButton() {
        val totalItem = CartManager.getTotalItem()
        btnCart.text = "Keranjang ($totalItem)"
    }
}