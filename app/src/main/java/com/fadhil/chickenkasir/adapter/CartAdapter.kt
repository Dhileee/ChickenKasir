package com.fadhil.chickenkasir.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.fadhil.chickenkasir.R
import com.fadhil.chickenkasir.utils.CartItem

class CartAdapter(
    private val cartList: MutableList<CartItem>,
    private val onUpdateTotal: () -> Unit
) : RecyclerView.Adapter<CartAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvNamaCart: TextView = view.findViewById(R.id.tvNamaCart)
        val tvHargaCart: TextView = view.findViewById(R.id.tvHargaCart)
        val tvJumlah: TextView = view.findViewById(R.id.tvJumlah)
        val btnPlus: Button = view.findViewById(R.id.btnPlus)
        val btnMinus: Button = view.findViewById(R.id.btnMinus)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_cart, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = cartList[position]
        holder.tvNamaCart.text = item.menu.nama
        holder.tvHargaCart.text = "Rp ${String.format("%,d", item.menu.harga)}"
        holder.tvJumlah.text = item.jumlah.toString()

        holder.btnPlus.setOnClickListener {
            item.jumlah++
            holder.tvJumlah.text = item.jumlah.toString()
            onUpdateTotal()
        }

        holder.btnMinus.setOnClickListener {
            if (item.jumlah > 1) {
                item.jumlah--
                holder.tvJumlah.text = item.jumlah.toString()
            } else {
                cartList.removeAt(position)
                notifyItemRemoved(position)
                notifyItemRangeChanged(position, cartList.size)
            }
            onUpdateTotal()
        }
    }

    override fun getItemCount() = cartList.size
}