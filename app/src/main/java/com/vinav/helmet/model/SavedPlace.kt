package com.vinav.helmet.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "saved_places")
data class SavedPlace(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val address: String,
    val latitude: Double,
    val longitude: Double,
    val isHome: Boolean = false,
    val isFavorite: Boolean = false,
    val lastUsedTimestamp: Long = System.currentTimeMillis()
)
