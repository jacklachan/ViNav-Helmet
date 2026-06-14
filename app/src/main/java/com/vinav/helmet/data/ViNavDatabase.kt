package com.vinav.helmet.data

import androidx.room.Database
import androidx.room.RoomDatabase
import com.vinav.helmet.model.RideHistory
import com.vinav.helmet.model.SavedPlace

@Database(
    entities = [SavedPlace::class, RideHistory::class],
    version = 1,
    exportSchema = false
)
abstract class ViNavDatabase : RoomDatabase() {
    abstract fun savedPlaceDao(): SavedPlaceDao
    abstract fun rideHistoryDao(): RideHistoryDao
}
