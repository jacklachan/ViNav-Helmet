package com.vinav.helmet.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "ride_history")
data class RideHistory(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val destinationName: String,
    val destinationAddress: String,
    val destinationLat: Double,
    val destinationLng: Double,
    val originLat: Double,
    val originLng: Double,
    val routeJson: String,
    val timestamp: Long = System.currentTimeMillis(),
    val totalSteps: Int = 0,
    val distanceText: String = "",
    val durationText: String = ""
)
