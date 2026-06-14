package com.vinav.helmet.bluetooth

import com.vinav.helmet.data.PreferencesManager
import com.vinav.helmet.model.ConnectionState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BluetoothConnectionManager @Inject constructor(
    private val transport: BluetoothTransport,
    private val preferencesManager: PreferencesManager
) {
    val connectionState: StateFlow<ConnectionState> = transport.connectionState
    val incomingMessages: Flow<String> = transport.incomingMessages

    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private var autoReconnectJob: Job? = null

    suspend fun connect(address: String): Boolean {
        val result = transport.connect(address)
        if (result) {
            preferencesManager.setLastConnectedDevice(address)
        }
        return result
    }

    suspend fun disconnect() {
        autoReconnectJob?.cancel()
        transport.disconnect()
    }

    suspend fun send(data: String): Boolean = transport.send(data)

    fun isConnected(): Boolean = transport.isConnected()

    fun startAutoReconnect() {
        autoReconnectJob?.cancel()
        autoReconnectJob = scope.launch {
            while (true) {
                delay(2000)
                val address = preferencesManager.lastConnectedDevice.first()
                val autoReconnect = preferencesManager.autoReconnect.first()
                if (address.isNotEmpty() && autoReconnect && !transport.isConnected()) {
                    transport.connect(address)
                }
                delay(5000)
            }
        }
    }

    fun stopAutoReconnect() {
        autoReconnectJob?.cancel()
    }
}
