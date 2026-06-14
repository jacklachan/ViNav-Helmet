package com.vinav.helmet.ui.screens.route

import android.Manifest
import android.content.pm.PackageManager
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
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.Polyline
import com.google.maps.android.compose.rememberCameraPositionState
import com.vinav.helmet.ui.components.ViNavTopBar
import com.vinav.helmet.ui.theme.ViNavPrimary
import com.vinav.helmet.ui.theme.ViNavSuccess
import com.vinav.helmet.ui.theme.ViNavTextSecondary

@Composable
fun RouteScreen(
    onBack: () -> Unit,
    onNavigateToActiveNav: () -> Unit,
    viewModel: RouteViewModel = hiltViewModel()
) {
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val searchResults by viewModel.searchResults.collectAsStateWithLifecycle()
    val selectedPlace by viewModel.selectedPlace.collectAsStateWithLifecycle()
    val routePolyline by viewModel.routePolyline.collectAsStateWithLifecycle()
    val routeDistance by viewModel.routeDistance.collectAsStateWithLifecycle()
    val routeDuration by viewModel.routeDuration.collectAsStateWithLifecycle()
    val isSending by viewModel.isSending.collectAsStateWithLifecycle()
    val sendResult by viewModel.sendResult.collectAsStateWithLifecycle()

    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }
    val fusedLocation = remember { LocationServices.getFusedLocationProviderClient(context) }
    
    // Default to Bangalore center if no GPS yet
    var originLat by remember { mutableDoubleStateOf(12.9716) }
    var originLng by remember { mutableDoubleStateOf(77.5946) }

    var hasLocPerm by remember {
        mutableStateOf(ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED)
    }
    val permLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { hasLocPerm = it }

    LaunchedEffect(hasLocPerm) {
        if (hasLocPerm) {
            try {
                fusedLocation.lastLocation.addOnSuccessListener { loc ->
                    if (loc != null) { originLat = loc.latitude; originLng = loc.longitude }
                }
            } catch (_: SecurityException) {}
        } else {
            permLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        }
    }

    LaunchedEffect(sendResult) {
        sendResult?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearSendResult()
            if (it.contains("sent")) onNavigateToActiveNav()
        }
    }

    val cameraPos = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(
            selectedPlace?.let { LatLng(it.lat, it.lng) } ?: LatLng(originLat, originLng), 
            if (selectedPlace != null) 15f else 12f
        )
    }
    
    // Smooth camera update when location changes
    LaunchedEffect(originLat, originLng, selectedPlace) {
        val target = selectedPlace?.let { LatLng(it.lat, it.lng) } ?: LatLng(originLat, originLng)
        cameraPos.position = CameraPosition.fromLatLngZoom(target, if (selectedPlace != null) 15f else 12f)
    }

    Scaffold(
        topBar = { ViNavTopBar("New Route", onBack = onBack) },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Search Bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { viewModel.onSearchQueryChanged(it) },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Search destination…") },
                leadingIcon = { Icon(Icons.Filled.Search, null) },
                trailingIcon = {
                    IconButton(onClick = { viewModel.searchPlaces(originLat, originLng) }) {
                        Icon(Icons.Filled.Search, "Search", tint = ViNavPrimary)
                    }
                },
                shape = RoundedCornerShape(12.dp),
                singleLine = true,
                keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(imeAction = androidx.compose.ui.text.input.ImeAction.Search),
                keyboardActions = androidx.compose.foundation.text.KeyboardActions(onSearch = { viewModel.searchPlaces(originLat, originLng) }),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = ViNavPrimary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.surfaceVariant
                )
            )

            // Search Results
            if (searchResults.isNotEmpty()) {
                LazyColumn(modifier = Modifier.height(200.dp)) {
                    items(searchResults) { place ->
                        Card(
                            onClick = {
                                viewModel.selectPlace(place)
                                viewModel.previewRoute(originLat, originLng)
                            },
                            modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text(place.name, style = MaterialTheme.typography.bodyLarge)
                                Text(place.address, style = MaterialTheme.typography.bodySmall, color = ViNavTextSecondary)
                            }
                        }
                    }
                }
            }

            // Map Preview
            GoogleMap(
                modifier = Modifier.fillMaxWidth().height(250.dp),
                cameraPositionState = cameraPos,
                properties = MapProperties(isMyLocationEnabled = hasLocPerm)
            ) {
                selectedPlace?.let {
                    Marker(state = MarkerState(LatLng(it.lat, it.lng)), title = it.name)
                }
                if (routePolyline.isNotEmpty()) {
                    Polyline(points = routePolyline, color = ViNavPrimary, width = 8f)
                }
            }

            // Route Summary
            if (routeDistance.isNotEmpty()) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text("Distance: $routeDistance", style = MaterialTheme.typography.bodyLarge)
                            Text("Duration: $routeDuration", style = MaterialTheme.typography.bodyMedium, color = ViNavTextSecondary)
                        }
                        Row {
                            IconButton(onClick = { viewModel.saveAsFavorite() }) {
                                Icon(Icons.Filled.Bookmark, "Favorite", tint = ViNavPrimary)
                            }
                            IconButton(onClick = { viewModel.setAsHome() }) {
                                Icon(Icons.Filled.Home, "Set Home", tint = ViNavSuccess)
                            }
                        }
                    }
                }
            }

            // Send to Helmet
            if (selectedPlace != null) {
                Button(
                    onClick = { viewModel.sendRouteToHelmet(originLat, originLng) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    enabled = !isSending,
                    colors = ButtonDefaults.buttonColors(containerColor = ViNavSuccess)
                ) {
                    Icon(Icons.Filled.Send, null, Modifier.padding(end = 8.dp))
                    Text(if (isSending) "Sending…" else "Send to Helmet")
                }
            }

            Spacer(Modifier.height(16.dp))
        }
    }
}
