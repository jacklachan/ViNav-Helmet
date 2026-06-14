package com.vinav.helmet.ui.screens.savedplaces

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
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Home
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import com.vinav.helmet.ui.components.ViNavTopBar
import com.vinav.helmet.ui.theme.ViNavPrimary
import com.vinav.helmet.ui.theme.ViNavSuccess
import com.vinav.helmet.ui.theme.ViNavTextSecondary

@Composable
fun SavedPlacesScreen(
    onBack: () -> Unit,
    viewModel: SavedPlacesViewModel = hiltViewModel()
) {
    val home by viewModel.home.collectAsStateWithLifecycle()
    val favorites by viewModel.favorites.collectAsStateWithLifecycle()
    val recent by viewModel.recent.collectAsStateWithLifecycle()
    val rides by viewModel.rideHistory.collectAsStateWithLifecycle()

    Scaffold(topBar = { ViNavTopBar("Saved Places", onBack = onBack) }) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding).padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Home
            item {
                Text("Home", style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(top = 8.dp))
            }
            item {
                if (home != null) {
                    Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
                        Row(modifier = Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Filled.Home, null, tint = ViNavSuccess, modifier = Modifier.padding(end = 12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(home!!.name, style = MaterialTheme.typography.bodyLarge)
                                Text(home!!.address, style = MaterialTheme.typography.bodySmall, color = ViNavTextSecondary)
                            }
                        }
                    }
                } else {
                    Text("No home set — set one in Route screen", style = MaterialTheme.typography.bodyMedium, color = ViNavTextSecondary)
                }
            }

            // Favorites
            if (favorites.isNotEmpty()) {
                item { Text("Favorites", style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(top = 12.dp)) }
                items(favorites) { place ->
                    Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
                        Row(modifier = Modifier.fillMaxWidth().padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Filled.Bookmark, null, tint = ViNavPrimary, modifier = Modifier.padding(end = 12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(place.name, style = MaterialTheme.typography.bodyLarge)
                                Text(place.address, style = MaterialTheme.typography.bodySmall, color = ViNavTextSecondary)
                            }
                            IconButton(onClick = { viewModel.toggleFavorite(place) }) {
                                Icon(Icons.Filled.BookmarkBorder, "Unfavorite")
                            }
                            IconButton(onClick = { viewModel.deletePlace(place) }) {
                                Icon(Icons.Filled.Delete, "Delete", tint = MaterialTheme.colorScheme.error)
                            }
                        }
                    }
                }
            }

            // Recent
            if (recent.isNotEmpty()) {
                item { Text("Recent", style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(top = 12.dp)) }
                items(recent) { place ->
                    Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
                        Row(modifier = Modifier.fillMaxWidth().padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(place.name, style = MaterialTheme.typography.bodyLarge)
                                Text(place.address, style = MaterialTheme.typography.bodySmall, color = ViNavTextSecondary)
                            }
                            IconButton(onClick = { viewModel.toggleFavorite(place) }) {
                                Icon(if (place.isFavorite) Icons.Filled.Bookmark else Icons.Filled.BookmarkBorder, "Favorite", tint = ViNavPrimary)
                            }
                        }
                    }
                }
            }

            // Ride History
            if (rides.isNotEmpty()) {
                item { Text("Ride History", style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(top = 12.dp)) }
                items(rides) { ride ->
                    Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
                        Row(modifier = Modifier.fillMaxWidth().padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Filled.History, null, tint = ViNavTextSecondary, modifier = Modifier.padding(end = 12.dp))
                            Column {
                                Text(ride.destinationName, style = MaterialTheme.typography.bodyLarge)
                                Text("${ride.distanceText} · ${ride.durationText}", style = MaterialTheme.typography.bodySmall, color = ViNavTextSecondary)
                            }
                        }
                    }
                }
            }
            item { Spacer(Modifier.height(16.dp)) }
        }
    }
}
