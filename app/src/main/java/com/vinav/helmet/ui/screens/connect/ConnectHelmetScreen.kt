package com.vinav.helmet.ui.screens.connect

import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bluetooth
import androidx.compose.material.icons.filled.BluetoothConnected
import androidx.compose.material.icons.filled.BluetoothDisabled
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.vinav.helmet.model.ConnectionState
import com.vinav.helmet.ui.components.ViNavTopBar
import com.vinav.helmet.ui.theme.ViNavError
import com.vinav.helmet.ui.theme.ViNavPrimary
import com.vinav.helmet.ui.theme.ViNavSuccess
import com.vinav.helmet.ui.theme.ViNavTextSecondary

@Composable
fun ConnectHelmetScreen(
    onBack: () -> Unit,
    viewModel: ConnectHelmetViewModel = hiltViewModel()
) {
    val connectionState by viewModel.connectionState.collectAsStateWithLifecycle()
    val devices by viewModel.devices.collectAsStateWithLifecycle()
    val isScanning by viewModel.isScanning.collectAsStateWithLifecycle()
    val logs by viewModel.logs.collectAsStateWithLifecycle()

    val btPermLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { perms ->
        if (perms.values.all { it }) viewModel.startScan()
    }

    Scaffold(topBar = { ViNavTopBar("Connect Helmet", onBack = onBack) }) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Scan / Stop Button
            item {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Button(
                        onClick = {
                            if (!viewModel.isBluetoothEnabled()) return@Button
                            val perms = mutableListOf<String>()
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                                perms.add(Manifest.permission.BLUETOOTH_SCAN)
                                perms.add(Manifest.permission.BLUETOOTH_CONNECT)
                            }
                            perms.add(Manifest.permission.ACCESS_FINE_LOCATION)
                            btPermLauncher.launch(perms.toTypedArray())
                        },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        enabled = !isScanning
                    ) {
                        Icon(Icons.Filled.Bluetooth, null, Modifier.padding(end = 4.dp))
                        Text("Scan")
                    }
                    if (isScanning) {
                        Button(
                            onClick = { viewModel.stopScan() },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = ViNavError)
                        ) { Text("Stop") }
                    }
                    if (connectionState == ConnectionState.CONNECTED) {
                        Button(
                            onClick = { viewModel.disconnect() },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = ViNavError)
                        ) { Text("Disconnect") }
                    }
                }
            }

            // Device List
            item {
                Text("Devices", style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(top = 8.dp))
            }

            items(devices) { device ->
                Card(
                    onClick = { viewModel.connect(device) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(device.name, style = MaterialTheme.typography.bodyLarge)
                            Text(device.address, style = MaterialTheme.typography.labelSmall, color = ViNavTextSecondary)
                        }
                        Icon(
                            if (device.bonded) Icons.Filled.BluetoothConnected else Icons.Filled.BluetoothDisabled,
                            null,
                            tint = if (device.bonded) ViNavSuccess else ViNavTextSecondary
                        )
                    }
                }
            }

            // Connection Log
            item {
                Spacer(Modifier.height(8.dp))
                Text("Connection Log", style = MaterialTheme.typography.titleMedium)
            }
            items(logs) { log ->
                Text(log, style = MaterialTheme.typography.bodySmall, color = ViNavTextSecondary)
            }
            item { Spacer(Modifier.height(16.dp)) }
        }
    }
}
