package com.vinav.helmet.model

data class HelmetStatus(
    val batteryPercent: Int = -1,
    val gpsLock: Boolean = false,
    val firmware: String = "unknown",
    val isConnected: Boolean = false,
    val lastMessage: String = ""
)
