package com.vinav.helmet.ui.screens.savedplaces

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vinav.helmet.data.RideHistoryDao
import com.vinav.helmet.data.SavedPlaceDao
import com.vinav.helmet.model.RideHistory
import com.vinav.helmet.model.SavedPlace
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SavedPlacesViewModel @Inject constructor(
    private val savedPlaceDao: SavedPlaceDao,
    private val rideHistoryDao: RideHistoryDao
) : ViewModel() {

    val home: StateFlow<SavedPlace?> = savedPlaceDao.getHomeFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val favorites: StateFlow<List<SavedPlace>> = savedPlaceDao.getFavorites()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val recent: StateFlow<List<SavedPlace>> = savedPlaceDao.getRecent()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val rideHistory: StateFlow<List<RideHistory>> = rideHistoryDao.getRecentRides()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun deletePlace(place: SavedPlace) {
        viewModelScope.launch { savedPlaceDao.delete(place) }
    }

    fun toggleFavorite(place: SavedPlace) {
        viewModelScope.launch { savedPlaceDao.update(place.copy(isFavorite = !place.isFavorite)) }
    }
}
