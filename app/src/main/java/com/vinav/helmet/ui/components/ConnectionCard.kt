package com.vinav.helmet.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bluetooth
import androidx.compose.material.icons.filled.BatteryFull
import androidx.compose.material.icons.filled.GpsFixed
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.vinav.helmet.model.ConnectionState
import com.vinav.helmet.model.HelmetStatus
import com.vinav.helmet.ui.theme.ViNavPrimary
import com.vinav.helmet.ui.theme.ViNavSuccess

@Composable
fun ConnectionCard(
    connectionState: ConnectionState,
    helmetStatus: HelmetStatus,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Filled.Bluetooth,
                        contentDescription = "Helmet",
                        tint = if (connectionState == ConnectionState.CONNECTED) ViNavSuccess else ViNavPrimary,
                        modifier = Modifier.size(24.dp)
                    )
                    Text("Helmet", style = MaterialTheme.typography.titleMedium)
                }
                when (connectionState) {
                    ConnectionState.CONNECTED -> ConnectedChip()
                    ConnectionState.CONNECTING -> ConnectingChip()
                    ConnectionState.SCANNING -> ScanningChip()
                    else -> DisconnectedChip()
                }
            }

            if (connectionState == ConnectionState.CONNECTED) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    if (helmetStatus.batteryPercent >= 0) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Filled.BatteryFull, "Battery", modifier = Modifier.size(16.dp), tint = ViNavSuccess)
                            Text("${helmetStatus.batteryPercent}%", style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                    if (helmetStatus.gpsLock) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Filled.GpsFixed, "GPS", modifier = Modifier.size(16.dp), tint = ViNavSuccess)
                            Text("GPS Lock", style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                    if (helmetStatus.firmware != "unknown") {
                        Text("FW: ${helmetStatus.firmware}", style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }
        }
    }
}
