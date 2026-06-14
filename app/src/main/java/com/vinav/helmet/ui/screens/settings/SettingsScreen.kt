package com.vinav.helmet.ui.screens.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.vinav.helmet.ui.components.ViNavTopBar
import com.vinav.helmet.ui.theme.ViNavError
import com.vinav.helmet.ui.theme.ViNavPrimary
import com.vinav.helmet.ui.theme.ViNavTextSecondary

@Composable
fun SettingsScreen(
    onBack: () -> Unit,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val autoReconnect by viewModel.autoReconnect.collectAsStateWithLifecycle()
    val distanceUnit by viewModel.distanceUnit.collectAsStateWithLifecycle()
    val navRefresh by viewModel.navRefreshInterval.collectAsStateWithLifecycle()
    val debugMode by viewModel.debugMode.collectAsStateWithLifecycle()
    val homeName by viewModel.homeName.collectAsStateWithLifecycle()

    Scaffold(topBar = { ViNavTopBar("Settings", onBack = onBack) }) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding).padding(horizontal = 16.dp).verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Bluetooth
            SectionTitle("Bluetooth")
            SettingRow("Auto Reconnect") {
                Switch(checked = autoReconnect, onCheckedChange = { viewModel.setAutoReconnect(it) },
                    colors = SwitchDefaults.colors(checkedTrackColor = ViNavPrimary))
            }

            // Navigation
            SectionTitle("Navigation")
            SettingRow("Distance Unit") {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(onClick = { viewModel.setDistanceUnit("metric") },
                        colors = if (distanceUnit == "metric") ButtonDefaults.outlinedButtonColors(containerColor = ViNavPrimary.copy(0.2f)) else ButtonDefaults.outlinedButtonColors()
                    ) { Text("Metric") }
                    OutlinedButton(onClick = { viewModel.setDistanceUnit("imperial") },
                        colors = if (distanceUnit == "imperial") ButtonDefaults.outlinedButtonColors(containerColor = ViNavPrimary.copy(0.2f)) else ButtonDefaults.outlinedButtonColors()
                    ) { Text("Imperial") }
                }
            }
            SettingRow("Nav Refresh: ${navRefresh}ms") {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(onClick = { viewModel.setNavRefreshInterval(2000) }) { Text("2s") }
                    OutlinedButton(onClick = { viewModel.setNavRefreshInterval(3000) }) { Text("3s") }
                    OutlinedButton(onClick = { viewModel.setNavRefreshInterval(5000) }) { Text("5s") }
                }
            }

            // Home
            SectionTitle("Home Location")
            Text(
                if (homeName.isNotEmpty()) "Current: $homeName" else "Not set — set via Route screen",
                style = MaterialTheme.typography.bodyMedium,
                color = ViNavTextSecondary
            )

            // Debug
            SectionTitle("Debug")
            SettingRow("Debug Mode") {
                Switch(checked = debugMode, onCheckedChange = { viewModel.setDebugMode(it) },
                    colors = SwitchDefaults.colors(checkedTrackColor = ViNavPrimary))
            }

            // Data
            SectionTitle("Data")
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(onClick = { viewModel.clearRideHistory() }, modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = ViNavError.copy(alpha = 0.8f))
                ) { Text("Clear Ride History") }
                Button(onClick = { viewModel.clearRecentPlaces() }, modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = ViNavError.copy(alpha = 0.8f))
                ) { Text("Clear Recent") }
            }

            // About
            SectionTitle("About")
            Text("ViNav v1.0.0 — Smart Helmet Companion", style = MaterialTheme.typography.bodyMedium, color = ViNavTextSecondary)
            Text("Bluetooth Transport: Classic (SPP/HC-05)", style = MaterialTheme.typography.bodySmall, color = ViNavTextSecondary)

            Spacer(Modifier.height(32.dp))
        }
    }
}

@Composable
private fun SectionTitle(title: String) {
    Text(title, style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(top = 8.dp))
}

@Composable
private fun SettingRow(label: String, content: @Composable () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(label, style = MaterialTheme.typography.bodyLarge, modifier = Modifier.weight(1f))
            content()
        }
    }
}
