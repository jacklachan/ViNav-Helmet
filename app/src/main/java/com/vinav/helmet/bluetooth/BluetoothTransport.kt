package com.vinav.helmet.bluetooth

import com.vinav.helmet.model.ConnectionState
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

interface BluetoothTransport {
    val connectionState: StateFlow<ConnectionState>
    val incomingMessages: Flow<String>
    suspend fun connect(address: String): Boolean
    suspend fun disconnect()
    suspend fun send(data: String): Boolean
    fun isConnected(): Boolean
}
