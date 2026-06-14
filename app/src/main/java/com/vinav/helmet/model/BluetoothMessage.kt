package com.vinav.helmet.model

sealed class BluetoothMessage {
    data class Navigation(val command: NavCommand) : BluetoothMessage()
    data class RouteStart(val totalSteps: Int, val destinationName: String) : BluetoothMessage()
    data object RouteEnd : BluetoothMessage()
    data object Ping : BluetoothMessage()
    data object BatteryRequest : BluetoothMessage()
    data class NavigateHome(val destinationName: String) : BluetoothMessage()
    data object SosTrigger : BluetoothMessage()
    data class MusicCommand(val action: String) : BluetoothMessage()
    data class TestMessage(val content: String) : BluetoothMessage()
}
