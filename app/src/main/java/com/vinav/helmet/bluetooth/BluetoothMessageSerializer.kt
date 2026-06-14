package com.vinav.helmet.bluetooth

import com.google.gson.Gson
import com.google.gson.JsonObject
import com.vinav.helmet.model.BluetoothMessage
import com.vinav.helmet.model.HelmetStatus
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BluetoothMessageSerializer @Inject constructor() {

    private val gson = Gson()

    fun serialize(message: BluetoothMessage): String {
        val json = JsonObject()
        when (message) {
            is BluetoothMessage.Navigation -> {
                json.addProperty("type", "nav")
                json.addProperty("arrow", message.command.arrow.name)
                json.addProperty("distanceM", message.command.distanceM)
                json.addProperty("instruction", message.command.instruction)
                json.addProperty("stepIndex", message.command.stepIndex)
                json.addProperty("totalSteps", message.command.totalSteps)
                json.addProperty("etaMin", message.command.etaMin)
            }
            is BluetoothMessage.RouteStart -> {
                json.addProperty("type", "route_start")
                json.addProperty("totalSteps", message.totalSteps)
                json.addProperty("destination", message.destinationName)
            }
            is BluetoothMessage.RouteEnd -> {
                json.addProperty("type", "route_end")
            }
            is BluetoothMessage.Ping -> {
                json.addProperty("type", "ping")
            }
            is BluetoothMessage.BatteryRequest -> {
                json.addProperty("type", "battery_request")
            }
            is BluetoothMessage.NavigateHome -> {
                json.addProperty("type", "navigate_home")
                json.addProperty("destination", message.destinationName)
            }
            is BluetoothMessage.SosTrigger -> {
                json.addProperty("type", "sos")
            }
            is BluetoothMessage.MusicCommand -> {
                json.addProperty("type", "music")
                json.addProperty("action", message.action)
            }
            is BluetoothMessage.TestMessage -> {
                json.addProperty("type", "test")
                json.addProperty("content", message.content)
            }
        }
        return gson.toJson(json)
    }

    fun parseIncoming(raw: String): HelmetStatus? {
        return try {
            val obj = gson.fromJson(raw, JsonObject::class.java)
            when (obj.get("type")?.asString) {
                "status" -> HelmetStatus(
                    batteryPercent = obj.get("batteryPercent")?.asInt ?: -1,
                    gpsLock = obj.get("gpsLock")?.asBoolean ?: false,
                    firmware = obj.get("firmware")?.asString ?: "unknown",
                    isConnected = true,
                    lastMessage = raw
                )
                else -> HelmetStatus(isConnected = true, lastMessage = raw)
            }
        } catch (e: Exception) {
            HelmetStatus(isConnected = true, lastMessage = raw)
        }
    }
}
