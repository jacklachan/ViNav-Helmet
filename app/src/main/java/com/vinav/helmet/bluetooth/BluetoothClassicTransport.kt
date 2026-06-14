package com.vinav.helmet.bluetooth

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothSocket
import com.vinav.helmet.model.ConnectionState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BluetoothClassicTransport @Inject constructor() : BluetoothTransport {

    companion object {
        val SPP_UUID: UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB")
    }

    private val _connectionState = MutableStateFlow(ConnectionState.DISCONNECTED)
    override val connectionState: StateFlow<ConnectionState> = _connectionState.asStateFlow()

    private val _incomingMessages = MutableSharedFlow<String>(replay = 0, extraBufferCapacity = 64)
    override val incomingMessages: Flow<String> = _incomingMessages.asSharedFlow()

    private var socket: BluetoothSocket? = null
    private var inputStream: InputStream? = null
    private var outputStream: OutputStream? = null
    private var readJob: Job? = null
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    @SuppressLint("MissingPermission")
    override suspend fun connect(address: String): Boolean = withContext(Dispatchers.IO) {
        try {
            _connectionState.value = ConnectionState.CONNECTING
            val adapter = BluetoothAdapter.getDefaultAdapter() ?: run {
                _connectionState.value = ConnectionState.ERROR
                return@withContext false
            }
            adapter.cancelDiscovery()
            val device = adapter.getRemoteDevice(address)
            val btSocket = device.createRfcommSocketToServiceRecord(SPP_UUID)
            btSocket.connect()
            socket = btSocket
            inputStream = btSocket.inputStream
            outputStream = btSocket.outputStream
            _connectionState.value = ConnectionState.CONNECTED
            startReading()
            true
        } catch (e: IOException) {
            _connectionState.value = ConnectionState.ERROR
            cleanup()
            false
        } catch (e: SecurityException) {
            _connectionState.value = ConnectionState.ERROR
            cleanup()
            false
        }
    }

    override suspend fun disconnect() {
        _connectionState.value = ConnectionState.DISCONNECTED
        cleanup()
    }

    override suspend fun send(data: String): Boolean = withContext(Dispatchers.IO) {
        try {
            val os = outputStream ?: return@withContext false
            os.write(data.toByteArray(Charsets.UTF_8))
            os.write('\n'.code)
            os.flush()
            true
        } catch (e: IOException) {
            _connectionState.value = ConnectionState.ERROR
            false
        }
    }

    override fun isConnected(): Boolean = _connectionState.value == ConnectionState.CONNECTED

    private fun startReading() {
        readJob?.cancel()
        readJob = scope.launch {
            val buffer = ByteArray(1024)
            val sb = StringBuilder()
            while (isActive) {
                try {
                    val ins = inputStream ?: break
                    val bytes = ins.read(buffer)
                    if (bytes > 0) {
                        val chunk = String(buffer, 0, bytes, Charsets.UTF_8)
                        sb.append(chunk)
                        while (sb.contains('\n')) {
                            val idx = sb.indexOf('\n')
                            val line = sb.substring(0, idx).trim()
                            sb.delete(0, idx + 1)
                            if (line.isNotEmpty()) {
                                _incomingMessages.emit(line)
                            }
                        }
                    }
                } catch (e: IOException) {
                    if (isActive) _connectionState.value = ConnectionState.ERROR
                    break
                }
            }
        }
    }

    private fun cleanup() {
        readJob?.cancel()
        readJob = null
        try { inputStream?.close() } catch (_: IOException) {}
        try { outputStream?.close() } catch (_: IOException) {}
        try { socket?.close() } catch (_: IOException) {}
        inputStream = null
        outputStream = null
        socket = null
    }
}
