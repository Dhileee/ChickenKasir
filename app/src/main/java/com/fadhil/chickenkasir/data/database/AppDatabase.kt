package com.fadhil.chickenkasir.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.fadhil.chickenkasir.data.dao.MenuDao
import com.fadhil.chickenkasir.data.entity.MenuEntity

@Database(entities = [MenuEntity::class], version = 1)
abstract class AppDatabase : RoomDatabase() {
    abstract fun menuDao(): MenuDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "chicken_kasir_db"
                ).allowMainThreadQueries().build()
                INSTANCE = instance
                instance
            }
        }
    }
}