package com.example.debt.data.db

import android.content.Context
import androidx.room.AutoMigration
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.debt.data.local.db.DebtorDao
import com.example.debt.data.model.db.DebtEntity

private const val DB_VERSION = 1
private const val DB_NAME = "project_debt"

@Database(
    entities = [
        DebtEntity::class
    ],
    version = DB_VERSION,
    exportSchema = true
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun debtorDao(): DebtorDao

    companion object {
        fun create(context: Context) : AppDatabase{
            return Room.databaseBuilder(
                context,
                AppDatabase::class.java,
                DB_NAME
            )
                .fallbackToDestructiveMigration()
                .build()
        }
    }
}