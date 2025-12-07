package com.fadhil.chickenkasir.utils

import com.fadhil.chickenkasir.model.Menu

object CartManager {
    val cartList = mutableListOf<CartItem>()

    fun addItem(menu: Menu) {
        val existingItem = cartList.find { it.menu.id == menu.id }
        if (existingItem != null) {
            existingItem.jumlah++
        } else {
            cartList.add(CartItem(menu, 1))
        }
    }

    fun removeItem(menu: Menu) {
        cartList.removeAll { it.menu.id == menu.id }
    }

    fun getTotalItem(): Int {
        return cartList.sumOf { it.jumlah }
    }

    fun getTotal(): Int {
        return cartList.sumOf { it.getSubtotal() }
    }

    fun clear() {
        cartList.clear()
    }
}