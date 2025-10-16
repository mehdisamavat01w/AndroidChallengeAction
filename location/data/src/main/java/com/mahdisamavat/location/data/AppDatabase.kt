package com.mahdisamavat.location.data

import androidx.room.Database
import androidx.room.RoomDatabase
import com.mahdisamavat.location.data.dao.LocationDao
import com.mahdisamavat.location.data.entity.LocationEntity

@Database(
    entities = [LocationEntity::class],
    version = 1,
    exportSchema = true
)
abstract class AppDatabase : RoomDatabase() {
    

    abstract fun locationDao(): LocationDao
    
    companion object {
        const val DATABASE_NAME = "location_database"
    }
}
