package com.fadhil.chickenkasir.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.fadhil.chickenkasir.data.entity.MenuEntity

@Dao
interface MenuDao {
    @Query("SELECT * FROM menu")
    fun getAllMenu(): List<MenuEntity>

    @Insert
    fun insertMenu(menu: MenuEntity)
}