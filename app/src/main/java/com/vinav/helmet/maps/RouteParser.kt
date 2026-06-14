package com.vinav.helmet.maps

import com.google.gson.JsonObject
import com.vinav.helmet.model.ArrowDirection
import com.vinav.helmet.model.NavCommand
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RouteParser @Inject constructor() {

    data class ParsedRoute(
        val commands: List<NavCommand>,
        val distanceText: String,
        val durationText: String,
        val polylinePoints: String
    )

    fun parseDirectionsResponse(response: JsonObject): ParsedRoute? {
        try {
            val routes = response.getAsJsonArray("routes")
            if (routes == null || routes.size() == 0) return null
            val route = routes[0].asJsonObject
            val legs = route.getAsJsonArray("legs")
            if (legs == null || legs.size() == 0) return null

            val leg = legs[0].asJsonObject
            val steps = leg.getAsJsonArray("steps") ?: return null
            val distanceText = leg.getAsJsonObject("distance")?.get("text")?.asString ?: ""
            val durationText = leg.getAsJsonObject("duration")?.get("text")?.asString ?: ""
            val polyline = route.getAsJsonObject("overview_polyline")?.get("points")?.asString ?: ""

            val commands = mutableListOf<NavCommand>()
            val totalSteps = steps.size()

            for (i in 0 until totalSteps) {
                val step = steps[i].asJsonObject
                val instruction = step.get("html_instructions")?.asString
                    ?.replace(Regex("<[^>]*>"), "") ?: ""
                val distanceM = step.getAsJsonObject("distance")?.get("value")?.asInt ?: 0
                val maneuver = step.get("maneuver")?.asString ?: ""
                val endLoc = step.getAsJsonObject("end_location")
                val endLat = endLoc?.get("lat")?.asDouble
                val endLng = endLoc?.get("lng")?.asDouble

                commands.add(
                    NavCommand(
                        arrow = mapManeuverToArrow(maneuver, instruction),
                        distanceM = distanceM,
                        instruction = instruction,
                        stepIndex = i,
                        totalSteps = totalSteps,
                        endLat = endLat,
                        endLng = endLng
                    )
                )
            }

            commands.add(
                NavCommand(
                    arrow = ArrowDirection.ARRIVED,
                    distanceM = 0,
                    instruction = "You have arrived",
                    stepIndex = totalSteps,
                    totalSteps = totalSteps
                )
            )

            return ParsedRoute(commands, distanceText, durationText, polyline)
        } catch (_: Exception) {
            return null
        }
    }

    private fun mapManeuverToArrow(maneuver: String, instruction: String): ArrowDirection {
        return when {
            maneuver.contains("uturn") -> ArrowDirection.UTURN
            maneuver.contains("slight-left") || maneuver.contains("ramp-left") -> ArrowDirection.SLIGHT_LEFT
            maneuver.contains("slight-right") || maneuver.contains("ramp-right") -> ArrowDirection.SLIGHT_RIGHT
            maneuver.contains("left") -> ArrowDirection.LEFT
            maneuver.contains("right") -> ArrowDirection.RIGHT
            maneuver.contains("straight") || maneuver.contains("merge") -> ArrowDirection.STRAIGHT
            instruction.lowercase().contains("left") -> ArrowDirection.LEFT
            instruction.lowercase().contains("right") -> ArrowDirection.RIGHT
            else -> ArrowDirection.STRAIGHT
        }
    }

    fun decodePolyline(encoded: String): List<Pair<Double, Double>> {
        val poly = mutableListOf<Pair<Double, Double>>()
        var index = 0
        val len = encoded.length
        var lat = 0; var lng = 0
        while (index < len) {
            var b: Int; var shift = 0; var result = 0
            do { b = encoded[index++].code - 63; result = result or ((b and 0x1f) shl shift); shift += 5 } while (b >= 0x20)
            lat += if (result and 1 != 0) (result shr 1).inv() else result shr 1
            shift = 0; result = 0
            do { b = encoded[index++].code - 63; result = result or ((b and 0x1f) shl shift); shift += 5 } while (b >= 0x20)
            lng += if (result and 1 != 0) (result shr 1).inv() else result shr 1
            poly.add(Pair(lat.toDouble() / 1E5, lng.toDouble() / 1E5))
        }
        return poly
    }
}
