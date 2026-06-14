package com.vinav.helmet.ui.screens.connect

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vinav.helmet.bluetooth.BluetoothConnectionManager
import com.vinav.helmet.bluetooth.BluetoothDeviceScanner
import com.vinav.helmet.bluetooth.ScannedDevice
import com.vinav.helmet.model.ConnectionState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ConnectHelmetViewModel @Inject constructor(
    private val scanner: BluetoothDeviceScanner,
    private val connectionManager: BluetoothConnectionManager
) : ViewModel() {

    val connectionState: StateFlow<ConnectionState> = connectionManager.connectionState

    private val _devices = MutableStateFlow<List<ScannedDevice>>(emptyList())
    val devices: StateFlow<List<ScannedDevice>> = _devices.asStateFlow()

    private val _isScanning = MutableStateFlow(false)
    val isScanning: StateFlow<Boolean> = _isScanning.asStateFlow()

    private val _logs = MutableStateFlow<List<String>>(emptyList())
    val logs: StateFlow<List<String>> = _logs.asStateFlow()

    private var scanJob: Job? = null

    fun startScan() {
        // Clear previous list to show only currently available devices
        _devices.value = emptyList()
        _isScanning.value = true
        addLog("Scanning for available devices...")

        scanJob?.cancel()
        scanJob = viewModelScope.launch {
            scanner.scanForDevices().collect { device ->
                val current = _devices.value.toMutableList()
                if (current.none { it.address == device.address }) {
                    // Only add if it has a name (filter out nameless background signals)
                    if (device.name != "Unknown Device") {
                        current.add(device)
                        _devices.value = current
                        addLog("Found: ${device.name}")
                    }
                }
            }
        }
    }

    fun stopScan() {
        scanJob?.cancel()
        _isScanning.value = false
        addLog("Scan stopped")
    }

    fun connect(device: ScannedDevice) {
        viewModelScope.launch {
            addLog("Connecting to ${device.name}...")
            val result = connectionManager.connect(device.address)
            addLog(if (result) "Connected!" else "Connection failed")
        }
    }

    fun disconnect() {
        viewModelScope.launch {
            connectionManager.disconnect()
            addLog("Disconnected")
        }
    }

    fun isBluetoothEnabled(): Boolean = scanner.isBluetoothEnabled()

    private fun addLog(message: String) {
        val current = _logs.value.toMutableList()
        current.add(0, message)
        if (current.size > 50) current.removeAt(current.lastIndex)
        _logs.value = current
    }
}
