package com.vinav.helmet.ui.screens.sos

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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Message
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.google.android.gms.location.LocationServices
import com.vinav.helmet.model.EmergencyContact
import com.vinav.helmet.ui.components.ViNavTopBar
import com.vinav.helmet.ui.theme.ViNavSOS
import com.vinav.helmet.ui.theme.ViNavTextSecondary

@Composable
fun SOSScreen(
    onBack: () -> Unit,
    viewModel: SOSViewModel = hiltViewModel()
) {
    val contacts by viewModel.contacts.collectAsStateWithLifecycle()
    val sosMessage by viewModel.sosMessage.collectAsStateWithLifecycle()
    val context = LocalContext.current

    var lat by remember { mutableDoubleStateOf(0.0) }
    var lng by remember { mutableDoubleStateOf(0.0) }
    var showConfirm by remember { mutableStateOf(false) }
    var showAddContact by remember { mutableStateOf(false) }
    var newName by remember { mutableStateOf("") }
    var newPhone by remember { mutableStateOf("") }

    val permLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) {}

    LaunchedEffect(Unit) {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            try {
                LocationServices.getFusedLocationProviderClient(context).lastLocation.addOnSuccessListener { loc ->
                    if (loc != null) { lat = loc.latitude; lng = loc.longitude; viewModel.buildMessage(lat, lng) }
                }
            } catch (_: SecurityException) {}
        } else {
            permLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        }
    }

    // Confirm Dialog
    if (showConfirm) {
        AlertDialog(
            onDismissRequest = { showConfirm = false },
            icon = { Icon(Icons.Filled.Warning, null, tint = ViNavSOS, modifier = Modifier.size(48.dp)) },
            title = { Text("Send SOS?", textAlign = TextAlign.Center) },
            text = { Text("This will send your emergency location to all saved contacts.") },
            confirmButton = {
                Button(onClick = {
                    showConfirm = false
                    viewModel.triggerSos(lat, lng)
                    contacts.forEach { c ->
                        try { context.startActivity(viewModel.getSmsIntent(c.phone)) } catch (_: Exception) {}
                    }
                }, colors = ButtonDefaults.buttonColors(containerColor = ViNavSOS)) { Text("SEND SOS") }
            },
            dismissButton = { TextButton(onClick = { showConfirm = false }) { Text("Cancel") } }
        )
    }

    // Add Contact Dialog
    if (showAddContact) {
        AlertDialog(
            onDismissRequest = { showAddContact = false },
            title = { Text("Add Emergency Contact") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(value = newName, onValueChange = { newName = it }, label = { Text("Name") }, singleLine = true)
                    OutlinedTextField(value = newPhone, onValueChange = { newPhone = it }, label = { Text("Phone") }, singleLine = true)
                }
            },
            confirmButton = {
                Button(onClick = {
                    if (newName.isNotEmpty() && newPhone.isNotEmpty()) {
                        viewModel.saveContact(EmergencyContact(id = contacts.size + 1, name = newName, phone = newPhone))
                        newName = ""; newPhone = ""; showAddContact = false
                    }
                }) { Text("Save") }
            },
            dismissButton = { TextButton(onClick = { showAddContact = false }) { Text("Cancel") } }
        )
    }

    Scaffold(topBar = { ViNavTopBar("SOS Emergency", onBack = onBack) }) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding).padding(horizontal = 16.dp).verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Big SOS Button
            Button(
                onClick = { showConfirm = true },
                modifier = Modifier.fillMaxWidth().height(80.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = ViNavSOS)
            ) {
                Icon(Icons.Filled.Warning, null, Modifier.size(32.dp).padding(end = 8.dp))
                Text("SEND SOS", style = MaterialTheme.typography.headlineMedium, color = Color.White)
            }

            // Message Preview
            if (sosMessage.isNotEmpty()) {
                Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
                    Text(sosMessage, modifier = Modifier.padding(16.dp), style = MaterialTheme.typography.bodyMedium)
                }
            }

            // Emergency Contacts
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text("Emergency Contacts", style = MaterialTheme.typography.titleMedium)
                if (contacts.size < 3) {
                    IconButton(onClick = { showAddContact = true }) { Icon(Icons.Filled.Add, "Add Contact") }
                }
            }

            contacts.forEach { contact ->
                Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
                    Row(modifier = Modifier.fillMaxWidth().padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(contact.name, style = MaterialTheme.typography.bodyLarge)
                            Text(contact.phone, style = MaterialTheme.typography.bodySmall, color = ViNavTextSecondary)
                        }
                        IconButton(onClick = { try { context.startActivity(viewModel.getSmsIntent(contact.phone)) } catch (_: Exception) {} }) {
                            Icon(Icons.Filled.Message, "SMS")
                        }
                        IconButton(onClick = { try { context.startActivity(viewModel.getWhatsAppIntent(contact.phone)) } catch (_: Exception) {} }) {
                            Icon(Icons.Filled.Share, "WhatsApp")
                        }
                        IconButton(onClick = { try { context.startActivity(viewModel.getCallIntent(contact.phone)) } catch (_: Exception) {} }) {
                            Icon(Icons.Filled.Call, "Call")
                        }
                        IconButton(onClick = { viewModel.removeContact(contact.id) }) {
                            Icon(Icons.Filled.Delete, "Remove", tint = MaterialTheme.colorScheme.error)
                        }
                    }
                }
            }
            if (contacts.isEmpty()) {
                Text("No emergency contacts added yet", style = MaterialTheme.typography.bodyMedium, color = ViNavTextSecondary)
            }
            Spacer(Modifier.height(16.dp))
        }
    }
}
