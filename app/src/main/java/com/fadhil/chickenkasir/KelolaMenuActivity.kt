package com.fadhil.chickenkasir

import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Spinner
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.fadhil.chickenkasir.adapter.KelolaMenuAdapter
import com.fadhil.chickenkasir.firebase.FirebaseHelper
import com.fadhil.chickenkasir.model.Kategori
import com.fadhil.chickenkasir.model.Menu
import kotlinx.coroutines.launch

class KelolaMenuActivity : AppCompatActivity() {

    private lateinit var rvKelolaMenu: RecyclerView
    private lateinit var btnTambahMenu: Button
    private lateinit var adapter: KelolaMenuAdapter
    private val menuList = mutableListOf<Menu>()
    private val kategoriList = mutableListOf<Kategori>()
    private var selectedImageUri: Uri? = null
    private var currentImagePreview: ImageView? = null

    private val imagePickerLauncher = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            selectedImageUri = it
            currentImagePreview?.setImageURI(it)
            currentImagePreview?.visibility = View.VISIBLE
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_kelola_menu)

        rvKelolaMenu = findViewById(R.id.rvKelolaMenu)
        btnTambahMenu = findViewById(R.id.btnTambahMenu)

        lifecycleScope.launch {
            loadKategori()
            loadMenu()
        }

        adapter = KelolaMenuAdapter(menuList,
            onEdit = { menu -> showEditDialog(menu) },
            onDelete = { menu -> deleteMenu(menu) }
        )

        rvKelolaMenu.layoutManager = LinearLayoutManager(this)
        rvKelolaMenu.adapter = adapter

        btnTambahMenu.setOnClickListener {
            showTambahDialog()
        }
    }

    private suspend fun loadKategori() {
        kategoriList.clear()
        kategoriList.addAll(FirebaseHelper.getAllKategori())
    }

    private suspend fun loadMenu() {
        menuList.clear()
        menuList.addAll(FirebaseHelper.getAllMenu())
        runOnUiThread {
            adapter.notifyDataSetChanged()
        }
    }

    private fun showTambahDialog() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_tambah_menu, null)
        val etNama = dialogView.findViewById<EditText>(R.id.etNamaMenu)
        val etHarga = dialogView.findViewById<EditText>(R.id.etHargaMenu)
        val etDeskripsi = dialogView.findViewById<EditText>(R.id.etDeskripsiMenu)
        val spinnerKategori = dialogView.findViewById<Spinner>(R.id.spinnerKategori)
        val btnTambahKategori = dialogView.findViewById<Button>(R.id.btnTambahKategori)
        val btnHapusKategori = dialogView.findViewById<Button>(R.id.btnHapusKategori)
        val btnPilihGambar = dialogView.findViewById<Button>(R.id.btnPilihGambar)
        val ivPreviewGambar = dialogView.findViewById<ImageView>(R.id.ivPreviewGambar)

        currentImagePreview = ivPreviewGambar
        selectedImageUri = null

        setupSpinner(spinnerKategori)

        btnTambahKategori.setOnClickListener {
            showTambahKategoriDialog(spinnerKategori)
        }

        btnHapusKategori.setOnClickListener {
            if (spinnerKategori.selectedItemPosition >= 0) {
                val selectedKategori = kategoriList[spinnerKategori.selectedItemPosition]
                showHapusKategoriDialog(selectedKategori, spinnerKategori)
            }
        }

        btnPilihGambar.setOnClickListener {
            imagePickerLauncher.launch("image/*")
        }

        AlertDialog.Builder(this)
            .setTitle("Tambah Menu")
            .setView(dialogView)
            .setPositiveButton("Simpan") { _, _ ->
                val nama = etNama.text.toString()
                val harga = etHarga.text.toString().toIntOrNull() ?: 0
                val deskripsi = etDeskripsi.text.toString()
                val kategori = if (spinnerKategori.selectedItemPosition >= 0) {
                    kategoriList[spinnerKategori.selectedItemPosition].nama
                } else {
                    "Reguler"
                }

                if (nama.isNotEmpty() && harga > 0) {
                    lifecycleScope.launch {
                        val menuId = System.currentTimeMillis().toString()

                        val imageUrl = if (selectedImageUri != null) {
                            FirebaseHelper.uploadMenuImage(selectedImageUri!!, menuId)
                        } else {
                            ""
                        }

                        val menu = Menu(
                            nama = nama,
                            harga = harga,
                            gambar = "default",
                            imageUrl = imageUrl,
                            kategori = kategori,
                            deskripsi = deskripsi
                        )

                        FirebaseHelper.addMenu(menu)
                        loadMenu()

                        runOnUiThread {
                            Toast.makeText(this@KelolaMenuActivity, "Menu ditambahkan", Toast.LENGTH_SHORT).show()
                        }
                    }
                } else {
                    Toast.makeText(this, "Isi semua field", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Batal", null)
            .show()
    }

    private fun showEditDialog(menu: Menu) {
        val dialogView = layoutInflater.inflate(R.layout.dialog_tambah_menu, null)
        val etNama = dialogView.findViewById<EditText>(R.id.etNamaMenu)
        val etHarga = dialogView.findViewById<EditText>(R.id.etHargaMenu)
        val etDeskripsi = dialogView.findViewById<EditText>(R.id.etDeskripsiMenu)
        val spinnerKategori = dialogView.findViewById<Spinner>(R.id.spinnerKategori)
        val btnTambahKategori = dialogView.findViewById<Button>(R.id.btnTambahKategori)
        val btnHapusKategori = dialogView.findViewById<Button>(R.id.btnHapusKategori)
        val btnPilihGambar = dialogView.findViewById<Button>(R.id.btnPilihGambar)
        val ivPreviewGambar = dialogView.findViewById<ImageView>(R.id.ivPreviewGambar)

        etNama.setText(menu.nama)
        etHarga.setText(menu.harga.toString())
        etDeskripsi.setText(menu.deskripsi)

        currentImagePreview = ivPreviewGambar
        selectedImageUri = null

        if (menu.imageUrl.isNotEmpty()) {
            // Load existing image preview (optional, butuh Glide)
            ivPreviewGambar.visibility = View.VISIBLE
        }

        setupSpinner(spinnerKategori)

        val kategoriIndex = kategoriList.indexOfFirst { it.nama == menu.kategori }
        if (kategoriIndex >= 0) {
            spinnerKategori.setSelection(kategoriIndex)
        }

        btnTambahKategori.setOnClickListener {
            showTambahKategoriDialog(spinnerKategori)
        }

        btnHapusKategori.setOnClickListener {
            if (spinnerKategori.selectedItemPosition >= 0) {
                val selectedKategori = kategoriList[spinnerKategori.selectedItemPosition]
                showHapusKategoriDialog(selectedKategori, spinnerKategori)
            }
        }

        btnPilihGambar.setOnClickListener {
            imagePickerLauncher.launch("image/*")
        }

        AlertDialog.Builder(this)
            .setTitle("Edit Menu")
            .setView(dialogView)
            .setPositiveButton("Simpan") { _, _ ->
                val nama = etNama.text.toString()
                val harga = etHarga.text.toString().toIntOrNull() ?: 0
                val deskripsi = etDeskripsi.text.toString()
                val kategori = if (spinnerKategori.selectedItemPosition >= 0) {
                    kategoriList[spinnerKategori.selectedItemPosition].nama
                } else {
                    "Reguler"
                }

                if (nama.isNotEmpty() && harga > 0) {
                    lifecycleScope.launch {
                        val imageUrl = if (selectedImageUri != null) {
                            FirebaseHelper.uploadMenuImage(selectedImageUri!!, menu.id)
                        } else {
                            menu.imageUrl
                        }

                        val updatedMenu = Menu(
                            id = menu.id,
                            nama = nama,
                            harga = harga,
                            gambar = menu.gambar,
                            imageUrl = imageUrl,
                            kategori = kategori,
                            deskripsi = deskripsi
                        )

                        FirebaseHelper.updateMenu(updatedMenu.id, updatedMenu)
                        loadMenu()

                        runOnUiThread {
                            Toast.makeText(this@KelolaMenuActivity, "Menu diupdate", Toast.LENGTH_SHORT).show()
                        }
                    }
                } else {
                    Toast.makeText(this, "Isi semua field", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Batal", null)
            .show()
    }

    private fun setupSpinner(spinner: Spinner) {
        val kategoriNames = kategoriList.map { it.nama }
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, kategoriNames)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinner.adapter = adapter
    }

    private fun showTambahKategoriDialog(spinner: Spinner) {
        val input = EditText(this)
        input.hint = "Nama Kategori Baru"

        AlertDialog.Builder(this)
            .setTitle("Tambah Kategori")
            .setView(input)
            .setPositiveButton("Tambah") { _, _ ->
                val namaKategori = input.text.toString()
                if (namaKategori.isNotEmpty()) {
                    lifecycleScope.launch {
                        FirebaseHelper.addKategori(Kategori(nama = namaKategori))
                        loadKategori()

                        runOnUiThread {
                            setupSpinner(spinner)
                            spinner.setSelection(kategoriList.size - 1)
                            Toast.makeText(this@KelolaMenuActivity, "Kategori ditambahkan", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }
            .setNegativeButton("Batal", null)
            .show()
    }

    private fun showHapusKategoriDialog(kategori: Kategori, spinner: Spinner) {
        AlertDialog.Builder(this)
            .setTitle("Hapus Kategori")
            .setMessage("Yakin hapus kategori '${kategori.nama}'?\n\nPeringatan: Menu dengan kategori ini tidak akan terhapus, tapi kategorinya akan hilang.")
            .setPositiveButton("Hapus") { _, _ ->
                lifecycleScope.launch {
                    FirebaseHelper.deleteKategori(kategori.id)
                    loadKategori()

                    runOnUiThread {
                        setupSpinner(spinner)
                        if (kategoriList.isNotEmpty()) {
                            spinner.setSelection(0)
                        }
                        Toast.makeText(this@KelolaMenuActivity, "Kategori dihapus", Toast.LENGTH_SHORT).show()
                    }
                }
            }
            .setNegativeButton("Batal", null)
            .show()
    }

    private fun deleteMenu(menu: Menu) {
        AlertDialog.Builder(this)
            .setTitle("Hapus Menu")
            .setMessage("Yakin hapus ${menu.nama}?")
            .setPositiveButton("Hapus") { _, _ ->
                lifecycleScope.launch {
                    FirebaseHelper.deleteMenu(menu.id)
                    loadMenu()

                    runOnUiThread {
                        Toast.makeText(this@KelolaMenuActivity, "Menu dihapus", Toast.LENGTH_SHORT).show()
                    }
                }
            }
            .setNegativeButton("Batal", null)
            .show()
    }
}