package com.vinav.helmet.ui.screens.activenav

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vinav.helmet.bluetooth.HelmetRepository
import com.vinav.helmet.maps.NavigationRepository
import com.vinav.helmet.model.ArrowDirection
import com.vinav.helmet.model.NavCommand
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ActiveNavViewModel @Inject constructor(
    private val navigationRepository: NavigationRepository,
    private val helmetRepository: HelmetRepository
) : ViewModel() {

    val activeRoute = navigationRepository.activeRoute
    val currentStepIndex = navigationRepository.currentStepIndex
    val isNavigating = navigationRepository.isNavigating
    val routeDistance = navigationRepository.routeDistanceText
    val routeDuration = navigationRepository.routeDurationText

    // This is the key fix: Reactive current command
    val currentCommand: StateFlow<NavCommand?> = combine(activeRoute, currentStepIndex) { route, index ->
        if (index < route.size) route[index] else null
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    private val _testResult = MutableStateFlow("")
    val testResult: StateFlow<String> = _testResult.asStateFlow()

    fun advanceStep() = navigationRepository.advanceStep()

    fun endNavigation() = navigationRepository.endNavigation()

    fun resendCurrent() {
        viewModelScope.launch { navigationRepository.resendCurrentStep() }
    }

    fun resendFullRoute() {
        viewModelScope.launch { navigationRepository.resendFullRoute() }
    }

    fun sendTestCommand(arrow: ArrowDirection, distanceM: Int) {
        viewModelScope.launch {
            val cmd = NavCommand(arrow = arrow, distanceM = distanceM, instruction = "${arrow.name} ${distanceM}m")
            val ok = helmetRepository.sendNavCommand(cmd)
            _testResult.value = if (ok) "Sent: ${arrow.name} ${distanceM}m" else "Send failed"
        }
    }
}
