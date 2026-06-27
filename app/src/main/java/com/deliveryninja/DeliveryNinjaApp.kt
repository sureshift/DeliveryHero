package com.deliveryninja

import android.app.Application
import androidx.room.Room
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.deliveryninja.data.db.AppDatabase
import com.deliveryninja.data.models.DEFAULT_PLATFORMS
import com.deliveryninja.data.repository.DeliveryRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class DeliveryNinjaApp : Application() {
    lateinit var database: AppDatabase
    lateinit var repository: DeliveryRepository

    private val MIGRATION_1_2 = object : Migration(1, 2) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL("CREATE TABLE IF NOT EXISTS platforms (id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, name TEXT NOT NULL, colorHex TEXT NOT NULL DEFAULT '#FC8019', logoUri TEXT NOT NULL DEFAULT '', isActive INTEGER NOT NULL DEFAULT 1)")
            db.execSQL("CREATE TABLE IF NOT EXISTS restaurants (id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, name TEXT NOT NULL, address TEXT NOT NULL DEFAULT '', lat REAL NOT NULL DEFAULT 0.0, lon REAL NOT NULL DEFAULT 0.0, platformId INTEGER NOT NULL DEFAULT 0, notes TEXT NOT NULL DEFAULT '', createdAt INTEGER NOT NULL)")
            db.execSQL("CREATE TABLE IF NOT EXISTS customers (id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, name TEXT NOT NULL DEFAULT '', address TEXT NOT NULL, lat REAL NOT NULL DEFAULT 0.0, lon REAL NOT NULL DEFAULT 0.0, area TEXT NOT NULL DEFAULT '', notes TEXT NOT NULL DEFAULT '', createdAt INTEGER NOT NULL)")
            db.execSQL("ALTER TABLE deliveries ADD COLUMN restaurantId INTEGER NOT NULL DEFAULT 0")
            db.execSQL("ALTER TABLE deliveries ADD COLUMN restaurantName TEXT NOT NULL DEFAULT ''")
            db.execSQL("ALTER TABLE deliveries ADD COLUMN pickupAddress TEXT NOT NULL DEFAULT ''")
            db.execSQL("ALTER TABLE deliveries ADD COLUMN customerId INTEGER NOT NULL DEFAULT 0")
            db.execSQL("ALTER TABLE deliveries ADD COLUMN customerName TEXT NOT NULL DEFAULT ''")
            db.execSQL("ALTER TABLE deliveries ADD COLUMN dropAddress TEXT NOT NULL DEFAULT ''")
            db.execSQL("ALTER TABLE deliveries ADD COLUMN platformName TEXT NOT NULL DEFAULT ''")
        }
    }

    private val MIGRATION_2_3 = object : Migration(2, 3) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL("ALTER TABLE platforms ADD COLUMN logoUri TEXT NOT NULL DEFAULT ''")
        }
    }

    override fun onCreate() {
        super.onCreate()
        instance = this
        database = Room.databaseBuilder(applicationContext, AppDatabase::class.java, "deliveryninja.db")
            .addMigrations(MIGRATION_1_2, MIGRATION_2_3)
            .fallbackToDestructiveMigration()
            .build()

        repository = DeliveryRepository(
            database.platformDao(), database.restaurantDao(), database.customerDao(),
            database.deliveryDao(), database.shiftDao(), database.goalDao(), database.expenseDao()
        )

        CoroutineScope(Dispatchers.IO).launch {
            if (repository.platformCount() == 0) {
                DEFAULT_PLATFORMS.forEach { repository.addPlatform(it) }
            }
        }
    }

    companion object {
        private var instance: DeliveryNinjaApp? = null
        fun getInstance(): DeliveryNinjaApp = instance!!
    }
}
