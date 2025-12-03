package com.fadhil.chickenkasir.utils

import com.fadhil.chickenkasir.data.entity.MenuEntity

object CartManager {
    val cartList = mutableListOf<CartItem>()

    fun addItem(menu: MenuEntity) {
        val existing = cartList.find { it.menu.id == menu.id }
        if (existing != null) {
            existing.jumlah++
        } else {
            cartList.add(CartItem(menu, 1))
        }
    }

    fun getTotalItem(): Int = cartList.sumOf { it.jumlah }

    fun getTotal(): Int = cartList.sumOf { it.getSubtotal() }

    fun clear() {
        cartList.clear()
    }
}