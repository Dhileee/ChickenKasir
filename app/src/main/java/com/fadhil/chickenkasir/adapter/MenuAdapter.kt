package com.fadhil.chickenkasir.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.fadhil.chickenkasir.R
import com.fadhil.chickenkasir.data.entity.MenuEntity

class MenuAdapter(
    private val menuList: List<MenuEntity>,
    private val onAddClick: (MenuEntity) -> Unit
) : RecyclerView.Adapter<MenuAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvNama: TextView = view.findViewById(R.id.tvNama)
        val tvHarga: TextView = view.findViewById(R.id.tvHarga)
        val btnAdd: Button = view.findViewById(R.id.btnAdd)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_menu, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val menu = menuList[position]
        holder.tvNama.text = menu.nama
        holder.tvHarga.text = "Rp ${String.format("%,d", menu.harga)}"
        holder.btnAdd.setOnClickListener {
            onAddClick(menu)
        }
    }

    override fun getItemCount() = menuList.size
}