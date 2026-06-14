package com.vinav.helmet.ui.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vinav.helmet.bluetooth.BluetoothConnectionManager
import com.vinav.helmet.bluetooth.HelmetRepository
import com.vinav.helmet.data.PreferencesManager
import com.vinav.helmet.data.SavedPlaceDao
import com.vinav.helmet.maps.NavigationRepository
import com.vinav.helmet.media.MediaController
import com.vinav.helmet.media.TrackInfo
import com.vinav.helmet.model.ConnectionState
import com.vinav.helmet.model.HelmetStatus
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val helmetRepository: HelmetRepository,
    private val connectionManager: BluetoothConnectionManager,
    private val navigationRepository: NavigationRepository,
    private val mediaController: MediaController,
    private val preferencesManager: PreferencesManager,
    private val savedPlaceDao: SavedPlaceDao
) : ViewModel() {

    val connectionState: StateFlow<ConnectionState> = helmetRepository.connectionState
    val helmetStatus: StateFlow<HelmetStatus> = helmetRepository.helmetStatus
    val isNavigating: StateFlow<Boolean> = navigationRepository.isNavigating
    val currentTrack: StateFlow<TrackInfo> = mediaController.currentTrack

    // Pull Home status from the database
    val homeName: StateFlow<String> = savedPlaceDao.getHomeFlow()
        .map { it?.name ?: "" }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "")

    init {
        viewModelScope.launch {
            val autoReconnect = preferencesManager.autoReconnect.stateIn(viewModelScope).value
            if (autoReconnect) connectionManager.startAutoReconnect()
        }
        mediaController.connect()
    }

    fun onPlay() = mediaController.play()
    fun onPause() = mediaController.pause()
    fun onNext() = mediaController.next()
    fun onPrevious() = mediaController.previous()

    fun navigateHome(originLat: Double, originLng: Double) {
        viewModelScope.launch {
            val home = savedPlaceDao.getHome()
            if (home != null) {
                navigationRepository.fetchAndStartRoute(
                    originLat, originLng, home.latitude, home.longitude, home.name, home.address
                )
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        mediaController.disconnect()
    }
}
