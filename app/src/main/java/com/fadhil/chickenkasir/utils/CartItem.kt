package com.fadhil.chickenkasir.utils

import com.fadhil.chickenkasir.model.Menu

data class CartItem(
    val menu: Menu,
    var jumlah: Int = 1
) {
    fun getSubtotal(): Int = menu.harga * jumlah
}