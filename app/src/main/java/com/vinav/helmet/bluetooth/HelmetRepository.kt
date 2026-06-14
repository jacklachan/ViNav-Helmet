package com.vinav.helmet.bluetooth

import android.content.Context
import android.util.Log
import com.vinav.helmet.data.SavedPlaceDao
import com.vinav.helmet.maps.NavigationRepository
import com.vinav.helmet.model.ArrowDirection
import com.vinav.helmet.model.BluetoothMessage
import com.vinav.helmet.model.ConnectionState
import com.vinav.helmet.model.HelmetStatus
import com.vinav.helmet.model.NavCommand
import com.vinav.helmet.sos.SosRepository
import com.vinav.helmet.util.ViNavService
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Provider
import javax.inject.Singleton
import com.google.gson.Gson
import com.google.gson.JsonObject

@Singleton
class HelmetRepository @Inject constructor(
    @ApplicationContext private val context: Context,
    private val connectionManager: BluetoothConnectionManager,
    private val serializer: BluetoothMessageSerializer,
    private val navigationRepository: Provider<NavigationRepository>,
    private val sosRepository: Provider<SosRepository>,
    private val savedPlaceDao: Provider<SavedPlaceDao>
) {
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private val gson = Gson()

    val connectionState: StateFlow<ConnectionState> = connectionManager.connectionState

    private val _helmetStatus = MutableStateFlow(HelmetStatus())
    val helmetStatus: StateFlow<HelmetStatus> = _helmetStatus.asStateFlow()

    private val _lastSentMessage = MutableStateFlow("")
    val lastSentMessage: StateFlow<String> = _lastSentMessage.asStateFlow()

    private val _lastReceivedMessage = MutableStateFlow("")
    val lastReceivedMessage: StateFlow<String> = _lastReceivedMessage.asStateFlow()

    init {
        connectionManager.incomingMessages.onEach { raw ->
            _lastReceivedMessage.value = raw
            try {
                handleIncomingRaw(raw)
            } catch (e: Exception) {
                Log.e("HelmetRepo", "Crash prevented in message handler", e)
            }
        }.launchIn(scope)
    }

    private fun handleIncomingRaw(raw: String) {
        val obj = try { gson.fromJson(raw, JsonObject::class.java) } catch (e: Exception) { null } ?: return
        val type = obj.get("type")?.asString ?: return
        
        when (type) {
            "status" -> {
                _helmetStatus.value = HelmetStatus(
                    batteryPercent = obj.get("batteryPercent")?.asInt ?: -1,
                    isConnected = true
                )
            }
            "music" -> {
                if (obj.get("action")?.asString == "toggle") {
                    scope.launch(Dispatchers.Main) { ViNavService.togglePausePlay() }
                }
            }
            "call" -> {
                if (obj.get("action")?.asString == "answer") {
                    scope.launch(Dispatchers.Main) { ViNavService.answerCall(context) }
                }
            }
            "sos" -> {
                if (obj.get("action")?.asString == "trigger") {
                    scope.launch { sosRepository.get().triggerSos(0.0, 0.0) }
                }
            }
            "nav" -> {
                if (obj.get("action")?.asString == "home") {
                    triggerGoHome()
                }
            }
        }
    }

    private fun triggerGoHome() {
        scope.launch {
            try {
                val home = savedPlaceDao.get().getHome()
                if (home != null) {
                    navigationRepository.get().fetchAndStartRoute(
                        originLat = 12.9716, 
                        originLng = 77.5946, 
                        destLat = home.latitude, 
                        destLng = home.longitude, 
                        destinationName = home.name, 
                        destinationAddress = home.address
                    )
                }
            } catch (e: Exception) {
                Log.e("HelmetRepo", "Error triggering home", e)
            }
        }
    }

    suspend fun sendMessage(message: BluetoothMessage): Boolean {
        val rawString = when (message) {
            is BluetoothMessage.Navigation -> {
                val dirChar = when(message.command.arrow) {
                    ArrowDirection.LEFT -> "L"
                    ArrowDirection.RIGHT -> "R"
                    ArrowDirection.UTURN -> "U"
                    ArrowDirection.ARRIVED -> "A"
                    else -> "F"
                }
                "$dirChar,${message.command.distanceM}"
            }
            is BluetoothMessage.RouteStart -> "F,0"
            is BluetoothMessage.RouteEnd -> "A,0"
            else -> ""
        }
        
        if (rawString.isEmpty()) return false
        
        _lastSentMessage.value = rawString
        return connectionManager.send(rawString)
    }

    suspend fun sendNavCommand(command: NavCommand): Boolean =
        sendMessage(BluetoothMessage.Navigation(command))

    suspend fun sendRouteStart(totalSteps: Int, destination: String): Boolean =
        sendMessage(BluetoothMessage.RouteStart(totalSteps, destination))

    suspend fun sendRouteEnd(): Boolean =
        sendMessage(BluetoothMessage.RouteEnd)

    suspend fun requestBattery(): Boolean { return true }
    suspend fun sendPing(): Boolean { return true }
    suspend fun sendSosTrigger(): Boolean { return true }
    suspend fun sendMusicCommand(action: String): Boolean { return true }
    suspend fun sendTestMessage(content: String): Boolean { return true }

    fun isConnected(): Boolean = connectionManager.isConnected()
}
