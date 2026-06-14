package com.vinav.helmet.maps

import com.google.gson.Gson
import com.google.gson.JsonObject
import com.vinav.helmet.util.Constants
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DirectionsApiService @Inject constructor(
    private val httpClient: OkHttpClient
) {
    companion object {
        const val BASE_URL = "https://maps.googleapis.com/maps/api/directions/json"
    }

    suspend fun getDirections(
        originLat: Double, originLng: Double,
        destLat: Double, destLng: Double
    ): JsonObject? = withContext(Dispatchers.IO) {
        try {
            val url = "$BASE_URL?origin=$originLat,$originLng" +
                    "&destination=$destLat,$destLng" +
                    "&key=${Constants.DIRECTIONS_API_KEY}&mode=driving"
            val request = Request.Builder().url(url).get().build()
            val response = httpClient.newCall(request).execute()
            val body = response.body?.string() ?: return@withContext null
            Gson().fromJson(body, JsonObject::class.java)
        } catch (_: Exception) {
            null
        }
    }

    suspend fun searchPlaces(query: String, lat: Double, lng: Double): JsonObject? = withContext(Dispatchers.IO) {
        try {
            // Using rankby=distance requires removing the radius parameter and adding a type or keyword
            // To keep it simple and effective, we'll use locationbias to prefer nearby results
            val url = "https://maps.googleapis.com/maps/api/place/textsearch/json" +
                    "?query=${java.net.URLEncoder.encode(query, "UTF-8")}" +
                    "&location=$lat,$lng" +
                    "&radius=10000" + // Reduced radius to 10km to prioritize local results
                    "&key=${Constants.PLACES_API_KEY}"

            val request = Request.Builder().url(url).get().build()
            val response = httpClient.newCall(request).execute()
            val body = response.body?.string() ?: return@withContext null
            Gson().fromJson(body, JsonObject::class.java)
        } catch (_: Exception) {
            null
        }
    }
}
