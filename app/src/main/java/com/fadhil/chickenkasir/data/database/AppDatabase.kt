package com.fadhil.chickenkasir.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.fadhil.chickenkasir.data.dao.MenuDao
import com.fadhil.chickenkasir.data.dao.TransaksiDao
import com.fadhil.chickenkasir.data.entity.MenuEntity
import com.fadhil.chickenkasir.data.entity.TransaksiDetailEntity
import com.fadhil.chickenkasir.data.entity.TransaksiEntity

@Database(
    entities = [MenuEntity::class, TransaksiEntity::class, TransaksiDetailEntity::class],
    version = 2
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun menuDao(): MenuDao
    abstract fun transaksiDao(): TransaksiDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "chicken_kasir_db"
                )
                    .fallbackToDestructiveMigration()
                    .allowMainThreadQueries()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}