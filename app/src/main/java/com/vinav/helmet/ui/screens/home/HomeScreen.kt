package com.vinav.helmet.ui.screens.home

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.google.android.gms.location.LocationServices
import com.vinav.helmet.ui.components.ConnectionCard
import com.vinav.helmet.ui.components.SOSButton
import com.vinav.helmet.ui.components.SpotifyMiniPlayer
import com.vinav.helmet.ui.components.ViNavTopBar
import com.vinav.helmet.ui.theme.ViNavPrimary
import com.vinav.helmet.ui.theme.ViNavSuccess
import com.vinav.helmet.util.ViNavService

@Composable
fun HomeScreen(
    onNavigateToConnect: () -> Unit,
    onNavigateToRoute: () -> Unit,
    onNavigateToActiveNav: () -> Unit,
    onNavigateToSavedPlaces: () -> Unit,
    onNavigateToSOS: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onNavigateToDebug: () -> Unit,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val connectionState by viewModel.connectionState.collectAsStateWithLifecycle()
    val helmetStatus by viewModel.helmetStatus.collectAsStateWithLifecycle()
    val isNavigating by viewModel.isNavigating.collectAsStateWithLifecycle()
    val currentTrack by viewModel.currentTrack.collectAsStateWithLifecycle()
    val homeName by viewModel.homeName.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val fusedLocation = remember { LocationServices.getFusedLocationProviderClient(context) }

    var isServiceRunning by remember { mutableStateOf(ViNavService.isRunning()) }

    // Refresh service status when returning to screen
    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) {
        isServiceRunning = ViNavService.isRunning()
    }

    var hasLocationPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) ==
                    PackageManager.PERMISSION_GRANTED
        )
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { perms ->
        hasLocationPermission = perms[Manifest.permission.ACCESS_FINE_LOCATION] == true
    }

    Scaffold(
        topBar = {
            ViNavTopBar(
                title = "ViNav",
                onBack = null
            )
        },
        floatingActionButton = { SOSButton(onClick = onNavigateToSOS) }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(horizontal = 16.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Connection Card
                ConnectionCard(
                    connectionState = connectionState,
                    helmetStatus = helmetStatus,
                    onClick = onNavigateToConnect
                )

                // Service Setup Card (Shows if Permission Missing)
                if (!isServiceRunning) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFF59E0B).copy(alpha = 0.1f)),
                        shape = RoundedCornerShape(12.dp),
                        border = CardDefaults.outlinedCardBorder()
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("Media & Call Control", fontWeight = FontWeight.Bold)
                            Text("Enable permissions to control Spotify and answer calls from your helmet.", style = MaterialTheme.typography.bodySmall)
                            Spacer(Modifier.height(8.dp))
                            Button(
                                onClick = {
                                    context.startActivity(Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS))
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF59E0B)),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text("Enable Access", color = Color.White)
                            }
                        }
                    }
                }

                // Active Trip Card
                if (isNavigating) {
                    Button(
                        onClick = onNavigateToActiveNav,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = ViNavSuccess)
                    ) {
                        Icon(Icons.Filled.Navigation, "Active Navigation", modifier = Modifier.padding(end = 8.dp))
                        Text("Active Navigation — Tap to View")
                    }
                }

                // Action Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Button(
                        onClick = onNavigateToRoute,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = ViNavPrimary)
                    ) {
                        Icon(Icons.Filled.Route, null, modifier = Modifier.padding(end = 4.dp))
                        Text("New Route")
                    }

                    Button(
                        onClick = {
                            if (!hasLocationPermission) {
                                permissionLauncher.launch(
                                    arrayOf(
                                        Manifest.permission.ACCESS_FINE_LOCATION,
                                        Manifest.permission.ACCESS_COARSE_LOCATION
                                    )
                                )
                                return@Button
                            }
                            try {
                                fusedLocation.lastLocation.addOnSuccessListener { loc ->
                                    if (loc != null) {
                                        viewModel.navigateHome(loc.latitude, loc.longitude)
                                    }
                                }
                            } catch (_: SecurityException) {}
                        },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        enabled = homeName.isNotEmpty()
                    ) {
                        Icon(Icons.Filled.Home, null, modifier = Modifier.padding(end = 4.dp))
                        Text("Go Home")
                    }
                }

                // Saved Places Button
                Button(
                    onClick = onNavigateToSavedPlaces,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Icon(Icons.Filled.Bookmark, null, modifier = Modifier.padding(end = 8.dp))
                    Text("Saved Places")
                }

                // Spotify Mini Player
                SpotifyMiniPlayer(
                    trackInfo = currentTrack,
                    onPlay = viewModel::onPlay,
                    onPause = viewModel::onPause,
                    onNext = viewModel::onNext,
                    onPrevious = viewModel::onPrevious
                )

                Spacer(Modifier.height(80.dp)) // Space for SOS FAB
            }

            // Settings & Debug icons at top right
            Row(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(top = 4.dp, end = 4.dp)
            ) {
                IconButton(onClick = onNavigateToDebug) {
                    Icon(Icons.Filled.BugReport, "Debug", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                IconButton(onClick = onNavigateToSettings) {
                    Icon(Icons.Filled.Settings, "Settings", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
    }
}
