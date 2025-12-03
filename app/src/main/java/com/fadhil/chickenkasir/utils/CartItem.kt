package com.fadhil.chickenkasir.utils

import com.fadhil.chickenkasir.data.entity.MenuEntity

data class CartItem(
    val menu: MenuEntity,
    var jumlah: Int = 1
) {
    fun getSubtotal(): Int = menu.harga * jumlah
}