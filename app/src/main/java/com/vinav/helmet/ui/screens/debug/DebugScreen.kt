package com.vinav.helmet.ui.screens.debug

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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.vinav.helmet.ui.components.ViNavTopBar
import com.vinav.helmet.ui.theme.ViNavPrimary
import com.vinav.helmet.ui.theme.ViNavSuccess
import com.vinav.helmet.ui.theme.ViNavTextSecondary
import com.vinav.helmet.ui.theme.ViNavWarning

@Composable
fun DebugScreen(
    onBack: () -> Unit,
    viewModel: DebugViewModel = hiltViewModel()
) {
    val connectionState by viewModel.connectionState.collectAsStateWithLifecycle()
    val lastSent by viewModel.lastSentMessage.collectAsStateWithLifecycle()
    val lastReceived by viewModel.lastReceivedMessage.collectAsStateWithLifecycle()
    val helmetStatus by viewModel.helmetStatus.collectAsStateWithLifecycle()
    var rawInput by remember { mutableStateOf("") }

    Scaffold(topBar = { ViNavTopBar("Debug Console", onBack = onBack) }) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Connection Status
            Text("Status: ${connectionState.name}", style = MaterialTheme.typography.titleMedium,
                color = when (connectionState.name) { "CONNECTED" -> ViNavSuccess; "ERROR" -> MaterialTheme.colorScheme.error; else -> ViNavWarning })

            // Send Test Arrow Commands
            Text("Send Test Commands", style = MaterialTheme.typography.titleMedium)
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(onClick = { viewModel.sendTestLeft() }, Modifier.weight(1f)) { Text("← LEFT") }
                OutlinedButton(onClick = { viewModel.sendTestRight() }, Modifier.weight(1f)) { Text("→ RIGHT") }
            }
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(onClick = { viewModel.sendTestStraight() }, Modifier.weight(1f)) { Text("↑ STRAIGHT") }
                OutlinedButton(onClick = { viewModel.sendTestArrived() }, Modifier.weight(1f)) { Text("🏁 ARRIVED") }
            }
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(onClick = { viewModel.sendPing() }, Modifier.weight(1f)) { Text("Ping") }
                OutlinedButton(onClick = { viewModel.requestBattery() }, Modifier.weight(1f)) { Text("Battery?") }
            }

            // Raw Input
            Text("Raw Message", style = MaterialTheme.typography.titleMedium)
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = rawInput,
                    onValueChange = { rawInput = it },
                    modifier = Modifier.weight(1f),
                    placeholder = { Text("Raw JSON…") },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = ViNavPrimary)
                )
                IconButton(onClick = { if (rawInput.isNotEmpty()) { viewModel.sendRawTest(rawInput); rawInput = "" } }) {
                    Icon(Icons.Filled.Send, "Send", tint = ViNavPrimary)
                }
            }

            // Last Outbound
            Text("Last Sent (Outbound)", style = MaterialTheme.typography.titleMedium)
            Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
                Text(
                    text = lastSent.ifEmpty { "(none)" },
                    modifier = Modifier.padding(12.dp),
                    fontFamily = FontFamily.Monospace,
                    fontSize = 12.sp,
                    color = ViNavPrimary
                )
            }

            // Last Inbound
            Text("Last Received (Inbound)", style = MaterialTheme.typography.titleMedium)
            Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
                Text(
                    text = lastReceived.ifEmpty { "(none)" },
                    modifier = Modifier.padding(12.dp),
                    fontFamily = FontFamily.Monospace,
                    fontSize = 12.sp,
                    color = ViNavSuccess
                )
            }

            // Helmet Status
            Text("Parsed Helmet Status", style = MaterialTheme.typography.titleMedium)
            Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text("Battery: ${if (helmetStatus.batteryPercent >= 0) "${helmetStatus.batteryPercent}%" else "N/A"}",
                        fontFamily = FontFamily.Monospace, fontSize = 12.sp, color = ViNavTextSecondary)
                    Text("GPS Lock: ${helmetStatus.gpsLock}",
                        fontFamily = FontFamily.Monospace, fontSize = 12.sp, color = ViNavTextSecondary)
                    Text("Firmware: ${helmetStatus.firmware}",
                        fontFamily = FontFamily.Monospace, fontSize = 12.sp, color = ViNavTextSecondary)
                }
            }

            Spacer(Modifier.height(32.dp))
        }
    }
}
