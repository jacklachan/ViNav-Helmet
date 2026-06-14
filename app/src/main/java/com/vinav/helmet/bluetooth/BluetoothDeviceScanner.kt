package com.vinav.helmet.bluetooth

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import javax.inject.Inject
import javax.inject.Singleton

data class ScannedDevice(
    val name: String,
    val address: String,
    val bonded: Boolean
)

@Singleton
class BluetoothDeviceScanner @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val adapter: BluetoothAdapter? = BluetoothAdapter.getDefaultAdapter()

    @SuppressLint("MissingPermission")
    fun getPairedDevices(): List<ScannedDevice> {
        return try {
            adapter?.bondedDevices?.map {
                ScannedDevice(
                    name = it.name ?: "Unknown",
                    address = it.address,
                    bonded = true
                )
            } ?: emptyList()
        } catch (_: SecurityException) {
            emptyList()
        }
    }

    @SuppressLint("MissingPermission")
    fun scanForDevices(): Flow<ScannedDevice> = callbackFlow {
        val receiver = object : BroadcastReceiver() {
            @SuppressLint("MissingPermission")
            override fun onReceive(ctx: Context, intent: Intent) {
                when (intent.action) {
                    BluetoothDevice.ACTION_FOUND -> {
                        @Suppress("DEPRECATION")
                        val device = intent.getParcelableExtra<BluetoothDevice>(BluetoothDevice.EXTRA_DEVICE)
                        device?.let {
                            try {
                                trySend(
                                    ScannedDevice(
                                        name = it.name ?: "Unknown Device",
                                        address = it.address,
                                        bonded = it.bondState == BluetoothDevice.BOND_BONDED
                                    )
                                )
                            } catch (_: SecurityException) {}
                        }
                    }
                }
            }
        }

        val filter = IntentFilter().apply {
            addAction(BluetoothDevice.ACTION_FOUND)
            addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED)
        }
        context.registerReceiver(receiver, filter)
        try { adapter?.startDiscovery() } catch (_: SecurityException) {}

        awaitClose {
            try { adapter?.cancelDiscovery() } catch (_: SecurityException) {}
            try { context.unregisterReceiver(receiver) } catch (_: Exception) {}
        }
    }

    fun isBluetoothEnabled(): Boolean = adapter?.isEnabled == true
}
