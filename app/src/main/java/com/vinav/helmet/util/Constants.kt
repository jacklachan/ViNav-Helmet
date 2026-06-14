package com.vinav.helmet.util

object Constants {
    // Set in local.properties
    val DIRECTIONS_API_KEY = com.vinav.helmet.BuildConfig.DIRECTIONS_API_KEY
    val PLACES_API_KEY = com.vinav.helmet.BuildConfig.PLACES_API_KEY
    const val SPOTIFY_CLIENT_ID = "4f1dada6a2504b4e99013ad5c0a044d6"
    const val SPOTIFY_REDIRECT_URI = "vinav://spotify-callback"

    const val DEFAULT_NAV_REFRESH_MS = 3000
    const val BT_RECONNECT_DELAY_MS = 2000L
    const val MAX_EMERGENCY_CONTACTS = 3
    const val MAX_RECENT_PLACES = 20

    const val DB_NAME = "vinav_database"
}
