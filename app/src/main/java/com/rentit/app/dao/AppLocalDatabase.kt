package com.rentit.app.dao

import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.rentit.app.base.MyApplication
import com.rentit.app.models.apartment.Apartment

// Room database repository for local data persistence.
@Database(entities = [Apartment:: class], version = 2)
abstract class AppLocalDbRepository: RoomDatabase() {
    abstract fun apartmentDao(): ApartmentDao
}

/**
 * Singleton object providing access to the local Room database.
 * Database is lazily initialized on first access for optimal performance.
 */
object AppLocalDatabase {
    // Lazy initialization ensures database is created only when first accessed
    val db: AppLocalDbRepository by lazy {
        // Get application context, fail fast if not available
        val context = MyApplication.Globals.appContext
            ?: throw IllegalStateException("Application context not available")

        Room.databaseBuilder(
            context,
            AppLocalDbRepository::class.java,
            "dbFileName.db"
        )
            // If the schema changes, just delete the whole database file and start over with an empty one
            .fallbackToDestructiveMigration()
            .build()
    }
}