package com.vinav.helmet.ui.screens.debug

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vinav.helmet.bluetooth.HelmetRepository
import com.vinav.helmet.model.ArrowDirection
import com.vinav.helmet.model.NavCommand
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DebugViewModel @Inject constructor(
    private val helmetRepository: HelmetRepository
) : ViewModel() {

    val connectionState = helmetRepository.connectionState
    val lastSentMessage: StateFlow<String> = helmetRepository.lastSentMessage
    val lastReceivedMessage: StateFlow<String> = helmetRepository.lastReceivedMessage
    val helmetStatus = helmetRepository.helmetStatus

    fun sendTestLeft() = sendTest(ArrowDirection.LEFT, 200)
    fun sendTestRight() = sendTest(ArrowDirection.RIGHT, 80)
    fun sendTestStraight() = sendTest(ArrowDirection.STRAIGHT, 500)
    fun sendTestArrived() = sendTest(ArrowDirection.ARRIVED, 0)

    fun sendPing() { viewModelScope.launch { helmetRepository.sendPing() } }
    fun requestBattery() { viewModelScope.launch { helmetRepository.requestBattery() } }

    fun sendRawTest(text: String) {
        viewModelScope.launch { helmetRepository.sendTestMessage(text) }
    }

    private fun sendTest(arrow: ArrowDirection, dist: Int) {
        viewModelScope.launch {
            helmetRepository.sendNavCommand(
                NavCommand(arrow = arrow, distanceM = dist, instruction = "${arrow.name} ${dist}m", stepIndex = 0, totalSteps = 1)
            )
        }
    }
}
