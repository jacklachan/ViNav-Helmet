package com.vinav.helmet.model

data class NavCommand(
    val type: String = "nav",
    val arrow: ArrowDirection,
    val distanceM: Int,
    val instruction: String,
    val stepIndex: Int = 0,
    val totalSteps: Int = 0,
    val etaMin: Int = 0,
    val endLat: Double? = null,
    val endLng: Double? = null
)
