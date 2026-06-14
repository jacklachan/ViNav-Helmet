package com.vinav.helmet.maps

import android.content.Context
import android.location.Location
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.gson.Gson
import com.vinav.helmet.bluetooth.HelmetRepository
import com.vinav.helmet.data.PreferencesManager
import com.vinav.helmet.data.RideHistoryDao
import com.vinav.helmet.model.NavCommand
import com.vinav.helmet.model.RideHistory
import com.vinav.helmet.util.TtsManager
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.*

@Singleton
class NavigationRepository @Inject constructor(
    @ApplicationContext private val context: Context,
    private val directionsApi: DirectionsApiService,
    private val routeParser: RouteParser,
    private val helmetRepository: HelmetRepository,
    private val rideHistoryDao: RideHistoryDao,
    private val preferencesManager: PreferencesManager,
    private val ttsManager: TtsManager
) {
    private val gson = Gson()
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private val fusedLocation = LocationServices.getFusedLocationProviderClient(context)

    private val _activeRoute = MutableStateFlow<List<NavCommand>>(emptyList())
    val activeRoute: StateFlow<List<NavCommand>> = _activeRoute.asStateFlow()

    private val _currentStepIndex = MutableStateFlow(0)
    val currentStepIndex: StateFlow<Int> = _currentStepIndex.asStateFlow()

    private val _routeDistanceText = MutableStateFlow("")
    val routeDistanceText: StateFlow<String> = _routeDistanceText.asStateFlow()

    private val _routeDurationText = MutableStateFlow("")
    val routeDurationText: StateFlow<String> = _routeDurationText.asStateFlow()

    private val _polylinePoints = MutableStateFlow("")
    val polylinePoints: StateFlow<String> = _polylinePoints.asStateFlow()

    private val _isNavigating = MutableStateFlow(false)
    val isNavigating: StateFlow<Boolean> = _isNavigating.asStateFlow()

    private var navigationJob: Job? = null
    
    // Store polyline points for dynamic distance calculation
    private var decodedPolyline: List<Pair<Double, Double>> = emptyList()

    suspend fun fetchAndStartRoute(
        originLat: Double, originLng: Double,
        destLat: Double, destLng: Double,
        destinationName: String, destinationAddress: String
    ): Boolean {
        val response = directionsApi.getDirections(originLat, originLng, destLat, destLng) ?: return false
        val parsed = routeParser.parseDirectionsResponse(response) ?: return false

        _activeRoute.value = parsed.commands
        _currentStepIndex.value = 0
        _routeDistanceText.value = parsed.distanceText
        _routeDurationText.value = parsed.durationText
        _polylinePoints.value = parsed.polylinePoints
        decodedPolyline = routeParser.decodePolyline(parsed.polylinePoints)

        rideHistoryDao.insert(
            RideHistory(
                destinationName = destinationName,
                destinationAddress = destinationAddress,
                destinationLat = destLat, destinationLng = destLng,
                originLat = originLat, originLng = originLng,
                routeJson = gson.toJson(parsed.commands),
                totalSteps = parsed.commands.size,
                distanceText = parsed.distanceText,
                durationText = parsed.durationText
            )
        )

        helmetRepository.sendRouteStart(parsed.commands.size, destinationName)
        if (parsed.commands.isNotEmpty()) {
            val firstCmd = parsed.commands[0]
            helmetRepository.sendNavCommand(firstCmd)
            speakInstruction(firstCmd)
        }
        _isNavigating.value = true
        startPeriodicNavUpdates()
        return true
    }

    private fun startPeriodicNavUpdates() {
        navigationJob?.cancel()
        navigationJob = scope.launch {
            val interval = 1000L // 1 second update for distance
            while (isActive && _isNavigating.value) {
                try {
                    updateCurrentDistance()
                } catch (e: Exception) {
                    // Fail silently
                }
                delay(interval)
            }
        }
    }

    private suspend fun updateCurrentDistance() {
        // Get current location
        val location = try {
            fusedLocation.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, null).await()
        } catch (_: SecurityException) { null } ?: return

        val commands = _activeRoute.value
        val idx = _currentStepIndex.value
        if (idx >= commands.size) return

        val currentCmd = commands[idx]
        
        // Find the destination of the CURRENT step to calculate distance to turn
        val destLat = currentCmd.endLat ?: decodedPolyline.lastOrNull()?.first ?: return
        val destLng = currentCmd.endLng ?: decodedPolyline.lastOrNull()?.second ?: return
        
        val distanceToTurn = calculateDistance(location.latitude, location.longitude, destLat, destLng)
        
        // Update the distance in the command and send to helmet
        val updatedCmd = currentCmd.copy(distanceM = distanceToTurn.toInt())
        
        // Update internal state so the UI reflects the distance
        val newlyUpdatedRoute = commands.toMutableList()
        newlyUpdatedRoute[idx] = updatedCmd
        _activeRoute.value = newlyUpdatedRoute
        
        // Send to helmet
        helmetRepository.sendNavCommand(updatedCmd)
        
        // Auto-advance to next step if we are within 20 meters of the turn
        if (distanceToTurn < 20.0 && idx < commands.size - 1) {
            advanceStep()
        }
    }

    private fun calculateDistance(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        val r = 6371000.0 // meters
        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)
        val a = sin(dLat / 2) * sin(dLat / 2) +
                cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) *
                sin(dLon / 2) * sin(dLon / 2)
        val c = 2 * atan2(sqrt(a), sqrt(1 - a))
        return r * c
    }

    fun advanceStep() {
        val newIdx = _currentStepIndex.value + 1
        if (newIdx < _activeRoute.value.size) {
            _currentStepIndex.value = newIdx
            val nextCmd = _activeRoute.value[newIdx]
            scope.launch { 
                helmetRepository.sendNavCommand(nextCmd)
                speakInstruction(nextCmd)
            }
        } else {
            endNavigation()
        }
    }

    private fun speakInstruction(command: NavCommand) {
        val text = if (command.distanceM > 0) {
            "In ${command.distanceM} meters, ${command.instruction}"
        } else {
            command.instruction
        }
        ttsManager.speak(text)
    }

    fun endNavigation() {
        _isNavigating.value = false
        navigationJob?.cancel()
        ttsManager.speak("You have arrived at your destination.")
        scope.launch { helmetRepository.sendRouteEnd() }
    }

    suspend fun resendCurrentStep(): Boolean {
        val commands = _activeRoute.value
        val idx = _currentStepIndex.value
        return if (idx < commands.size) {
            val cmd = commands[idx]
            speakInstruction(cmd)
            helmetRepository.sendNavCommand(cmd)
        } else false
    }

    suspend fun resendFullRoute(): Boolean {
        val commands = _activeRoute.value
        if (commands.isEmpty()) return false
        helmetRepository.sendRouteStart(commands.size, "Resend")
        for (cmd in commands) {
            helmetRepository.sendNavCommand(cmd)
            delay(100)
        }
        return true
    }

    fun getCurrentCommand(): NavCommand? {
        val commands = _activeRoute.value
        val idx = _currentStepIndex.value
        return if (idx < commands.size) commands[idx] else null
    }
}
