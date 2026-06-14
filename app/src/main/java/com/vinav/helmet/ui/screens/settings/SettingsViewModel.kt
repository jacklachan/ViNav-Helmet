package com.vinav.helmet.ui.screens.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vinav.helmet.data.PreferencesManager
import com.vinav.helmet.data.RideHistoryDao
import com.vinav.helmet.data.SavedPlaceDao
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val preferencesManager: PreferencesManager,
    private val savedPlaceDao: SavedPlaceDao,
    private val rideHistoryDao: RideHistoryDao
) : ViewModel() {

    val autoReconnect: StateFlow<Boolean> = preferencesManager.autoReconnect
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), true)

    val distanceUnit: StateFlow<String> = preferencesManager.distanceUnit
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "metric")

    val navRefreshInterval: StateFlow<Int> = preferencesManager.navRefreshInterval
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 3000)

    val debugMode: StateFlow<Boolean> = preferencesManager.debugMode
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    val homeName: StateFlow<String> = preferencesManager.homeName
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "")

    fun setAutoReconnect(enabled: Boolean) { viewModelScope.launch { preferencesManager.setAutoReconnect(enabled) } }
    fun setDistanceUnit(unit: String) { viewModelScope.launch { preferencesManager.setDistanceUnit(unit) } }
    fun setNavRefreshInterval(ms: Int) { viewModelScope.launch { preferencesManager.setNavRefreshInterval(ms) } }
    fun setDebugMode(enabled: Boolean) { viewModelScope.launch { preferencesManager.setDebugMode(enabled) } }
    fun clearRideHistory() { viewModelScope.launch { rideHistoryDao.clearAll() } }
    fun clearRecentPlaces() { viewModelScope.launch { savedPlaceDao.clearRecent() } }
}
