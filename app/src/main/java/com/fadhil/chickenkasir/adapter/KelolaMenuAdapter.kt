package com.fadhil.chickenkasir.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.fadhil.chickenkasir.R
import com.fadhil.chickenkasir.model.Menu

class KelolaMenuAdapter(
    private val menuList: List<Menu>,
    private val onEdit: (Menu) -> Unit,
    private val onDelete: (Menu) -> Unit
) : RecyclerView.Adapter<KelolaMenuAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvNama: TextView = view.findViewById(R.id.tvNamaKelola)
        val tvHarga: TextView = view.findViewById(R.id.tvHargaKelola)
        val tvKategori: TextView = view.findViewById(R.id.tvKategoriKelola)
        val btnEdit: Button = view.findViewById(R.id.btnEdit)
        val btnHapus: Button = view.findViewById(R.id.btnHapus)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_kelola_menu, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val menu = menuList[position]
        holder.tvNama.text = menu.nama
        holder.tvHarga.text = "Rp ${String.format("%,d", menu.harga)}"
        holder.tvKategori.text = "Kategori: ${menu.kategori}"

        holder.btnEdit.setOnClickListener {
            onEdit(menu)
        }

        holder.btnHapus.setOnClickListener {
            onDelete(menu)
        }
    }

    override fun getItemCount() = menuList.size
}