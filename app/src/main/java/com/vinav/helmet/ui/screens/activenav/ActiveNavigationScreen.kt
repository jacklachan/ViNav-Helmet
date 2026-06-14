package com.vinav.helmet.ui.screens.activenav

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
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.vinav.helmet.model.ArrowDirection
import com.vinav.helmet.ui.components.ArrowIcon
import com.vinav.helmet.ui.components.ViNavTopBar
import com.vinav.helmet.ui.theme.ViNavError
import com.vinav.helmet.ui.theme.ViNavPrimary
import com.vinav.helmet.ui.theme.ViNavTextSecondary

@Composable
fun ActiveNavigationScreen(
    onBack: () -> Unit,
    viewModel: ActiveNavViewModel = hiltViewModel()
) {
    val activeRoute by viewModel.activeRoute.collectAsStateWithLifecycle()
    val currentIndex by viewModel.currentStepIndex.collectAsStateWithLifecycle()
    val isNavigating by viewModel.isNavigating.collectAsStateWithLifecycle()
    val distance by viewModel.routeDistance.collectAsStateWithLifecycle()
    val duration by viewModel.routeDuration.collectAsStateWithLifecycle()
    val testResult by viewModel.testResult.collectAsStateWithLifecycle()
    
    // Use the reactive currentCommand from the ViewModel
    val currentCommand by viewModel.currentCommand.collectAsStateWithLifecycle()

    Scaffold(topBar = { ViNavTopBar("Navigation", onBack = onBack) }) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Big Arrow Display
            if (currentCommand != null) {
                val command = currentCommand!!
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        ArrowIcon(direction = command.arrow, size = 96.dp)
                        Spacer(Modifier.height(12.dp))
                        Text(
                            "${command.distanceM}m",
                            fontSize = 36.sp,
                            color = ViNavPrimary
                        )
                        Text(
                            command.instruction,
                            style = MaterialTheme.typography.bodyLarge,
                            textAlign = TextAlign.Center
                        )
                        Spacer(Modifier.height(8.dp))
                        Text(
                            "Step ${currentIndex + 1} / ${activeRoute.size}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = ViNavTextSecondary
                        )
                    }
                }
            } else {
                Text("No active route", style = MaterialTheme.typography.titleMedium, color = ViNavTextSecondary)
            }

            // Route Info
            if (distance.isNotEmpty()) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                    Text("Distance: $distance", style = MaterialTheme.typography.bodyMedium)
                    Text("ETA: $duration", style = MaterialTheme.typography.bodyMedium)
                }
            }

            // Controls
            if (isNavigating) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(
                        onClick = { viewModel.advanceStep() },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Filled.SkipNext, null, Modifier.padding(end = 4.dp))
                        Text("Next Step")
                    }
                    Button(
                        onClick = { viewModel.resendCurrent() },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Icon(Icons.Filled.Refresh, null, Modifier.padding(end = 4.dp))
                        Text("Resend")
                    }
                }
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(
                        onClick = { viewModel.resendFullRoute() },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp)
                    ) { Text("Resend Full Route") }
                    Button(
                        onClick = { viewModel.endNavigation() },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = ViNavError)
                    ) {
                        Icon(Icons.Filled.Stop, null, Modifier.padding(end = 4.dp))
                        Text("End")
                    }
                }
            }

            // Test Commands
            Text("Test Commands", style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(top = 8.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(onClick = { viewModel.sendTestCommand(ArrowDirection.LEFT, 200) }, Modifier.weight(1f)) { Text("← LEFT 200m") }
                OutlinedButton(onClick = { viewModel.sendTestCommand(ArrowDirection.RIGHT, 80) }, Modifier.weight(1f)) { Text("→ RIGHT 80m") }
            }
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(onClick = { viewModel.sendTestCommand(ArrowDirection.STRAIGHT, 500) }, Modifier.weight(1f)) { Text("↑ STRAIGHT 500m") }
                OutlinedButton(onClick = { viewModel.sendTestCommand(ArrowDirection.ARRIVED, 0) }, Modifier.weight(1f)) { Text("🏁 ARRIVED") }
            }
            if (testResult.isNotEmpty()) {
                Text(testResult, style = MaterialTheme.typography.bodySmall, color = ViNavPrimary)
            }

            Spacer(Modifier.height(16.dp))
        }
    }
}
