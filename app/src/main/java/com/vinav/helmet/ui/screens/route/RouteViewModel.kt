package com.vinav.helmet.ui.screens.route

import android.annotation.SuppressLint
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.maps.model.LatLng
import com.vinav.helmet.data.SavedPlaceDao
import com.vinav.helmet.maps.DirectionsApiService
import com.vinav.helmet.maps.NavigationRepository
import com.vinav.helmet.maps.RouteParser
import com.vinav.helmet.model.SavedPlace
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.math.*

data class PlaceResult(val name: String, val address: String, val lat: Double, val lng: Double, val distance: Double = 0.0)

@HiltViewModel
class RouteViewModel @Inject constructor(
    private val navigationRepository: NavigationRepository,
    private val directionsApi: DirectionsApiService,
    private val routeParser: RouteParser,
    private val savedPlaceDao: SavedPlaceDao
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _searchResults = MutableStateFlow<List<PlaceResult>>(emptyList())
    val searchResults: StateFlow<List<PlaceResult>> = _searchResults.asStateFlow()

    private val _selectedPlace = MutableStateFlow<PlaceResult?>(null)
    val selectedPlace: StateFlow<PlaceResult?> = _selectedPlace.asStateFlow()

    private val _routePolyline = MutableStateFlow<List<LatLng>>(emptyList())
    val routePolyline: StateFlow<List<LatLng>> = _routePolyline.asStateFlow()

    private val _routeDistance = MutableStateFlow("")
    val routeDistance: StateFlow<String> = _routeDistance.asStateFlow()

    private val _routeDuration = MutableStateFlow("")
    val routeDuration: StateFlow<String> = _routeDuration.asStateFlow()

    private val _isSending = MutableStateFlow(false)
    val isSending: StateFlow<Boolean> = _isSending.asStateFlow()

    private val _sendResult = MutableStateFlow<String?>(null)
    val sendResult: StateFlow<String?> = _sendResult.asStateFlow()

    fun onSearchQueryChanged(query: String) {
        _searchQuery.value = query
    }

    fun searchPlaces(lat: Double, lng: Double) {
        viewModelScope.launch {
            val query = _searchQuery.value
            if (query.length < 3) return@launch
            
            Log.d("ViNav", "Searching for: $query at $lat, $lng")
            val result = directionsApi.searchPlaces(query, lat, lng)
            
            if (result == null) {
                Log.e("ViNav", "Places API returned NULL")
                _sendResult.value = "Search Failed"
                return@launch
            }

            val results = result.getAsJsonArray("results")?.map { r ->
                val obj = r.asJsonObject
                val loc = obj.getAsJsonObject("geometry")?.getAsJsonObject("location")
                val pLat = loc?.get("lat")?.asDouble ?: 0.0
                val pLng = loc?.get("lng")?.asDouble ?: 0.0
                PlaceResult(
                    name = obj.get("name")?.asString ?: "",
                    address = obj.get("formatted_address")?.asString ?: "",
                    lat = pLat,
                    lng = pLng,
                    distance = calculateDistance(lat, lng, pLat, pLng)
                )
            }?.sortedBy { it.distance } ?: emptyList() // Sort by distance from user
            
            _searchResults.value = results
        }
    }

    private fun calculateDistance(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        val r = 6371 // km
        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)
        val a = sin(dLat / 2) * sin(dLat / 2) +
                cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) *
                sin(dLon / 2) * sin(dLon / 2)
        val c = 2 * atan2(sqrt(a), sqrt(1 - a))
        return r * c
    }

    fun selectPlace(place: PlaceResult) {
        _selectedPlace.value = place
        _searchResults.value = emptyList()
        _searchQuery.value = place.name
    }

    @SuppressLint("MissingPermission")
    fun previewRoute(originLat: Double, originLng: Double) {
        viewModelScope.launch {
            val dest = _selectedPlace.value ?: return@launch
            val response = directionsApi.getDirections(originLat, originLng, dest.lat, dest.lng)
            response?.let {
                val parsed = routeParser.parseDirectionsResponse(it)
                parsed?.let { p ->
                    _routeDistance.value = p.distanceText
                    _routeDuration.value = p.durationText
                    _routePolyline.value = routeParser.decodePolyline(p.polylinePoints)
                        .map { (la, ln) -> LatLng(la, ln) }
                }
            }
        }
    }

    fun sendRouteToHelmet(originLat: Double, originLng: Double) {
        viewModelScope.launch {
            _isSending.value = true
            val dest = _selectedPlace.value ?: run {
                _sendResult.value = "No destination selected"
                _isSending.value = false
                return@launch
            }
            val success = navigationRepository.fetchAndStartRoute(
                originLat, originLng, dest.lat, dest.lng, dest.name, dest.address
            )
            _sendResult.value = if (success) "Route sent to helmet!" else "Failed to send route"
            _isSending.value = false

            savedPlaceDao.insert(
                SavedPlace(name = dest.name, address = dest.address, latitude = dest.lat, longitude = dest.lng)
            )
        }
    }

    fun saveAsFavorite() {
        viewModelScope.launch {
            val dest = _selectedPlace.value ?: return@launch
            savedPlaceDao.insert(
                SavedPlace(name = dest.name, address = dest.address, latitude = dest.lat, longitude = dest.lng, isFavorite = true)
            )
        }
    }

    fun setAsHome() {
        viewModelScope.launch {
            val dest = _selectedPlace.value ?: return@launch
            savedPlaceDao.clearHome()
            savedPlaceDao.insert(
                SavedPlace(name = dest.name, address = dest.address, latitude = dest.lat, longitude = dest.lng, isHome = true)
            )
        }
    }

    fun clearSendResult() { _sendResult.value = null }
}
