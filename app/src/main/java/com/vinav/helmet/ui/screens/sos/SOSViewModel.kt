package com.vinav.helmet.ui.screens.sos

import android.Manifest
import android.content.pm.PackageManager
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vinav.helmet.data.PreferencesManager
import com.vinav.helmet.model.EmergencyContact
import com.vinav.helmet.sos.SosRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SOSViewModel @Inject constructor(
    private val sosRepository: SosRepository,
    private val preferencesManager: PreferencesManager
) : ViewModel() {

    val contacts: StateFlow<List<EmergencyContact>> = preferencesManager.emergencyContacts
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _sosMessage = MutableStateFlow("")
    val sosMessage: StateFlow<String> = _sosMessage.asStateFlow()

    fun buildMessage(lat: Double, lng: Double) {
        _sosMessage.value = sosRepository.buildSosMessage(lat, lng)
    }

    fun getSmsIntent(phone: String) = sosRepository.getSmsIntent(phone, _sosMessage.value)
    fun getWhatsAppIntent(phone: String) = sosRepository.getWhatsAppIntent(phone, _sosMessage.value)
    fun getCallIntent(phone: String) = sosRepository.getCallIntent(phone)

    fun triggerSos(lat: Double, lng: Double) {
        viewModelScope.launch { sosRepository.triggerSos(lat, lng) }
    }

    fun saveContact(contact: EmergencyContact) {
        viewModelScope.launch {
            val current = contacts.value.toMutableList()
            val idx = current.indexOfFirst { it.id == contact.id }
            if (idx >= 0) current[idx] = contact else current.add(contact)
            preferencesManager.setEmergencyContacts(current)
        }
    }

    fun removeContact(id: Int) {
        viewModelScope.launch {
            preferencesManager.setEmergencyContacts(contacts.value.filter { it.id != id })
        }
    }
}
