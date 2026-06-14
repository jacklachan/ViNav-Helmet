package com.vinav.helmet.sos

import android.content.Context
import android.content.Intent
import android.net.Uri
import com.vinav.helmet.bluetooth.HelmetRepository
import com.vinav.helmet.data.PreferencesManager
import com.vinav.helmet.model.EmergencyContact
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.first
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SosRepository @Inject constructor(
    @ApplicationContext private val context: Context,
    private val preferencesManager: PreferencesManager,
    private val helmetRepository: HelmetRepository
) {
    suspend fun getContacts(): List<EmergencyContact> =
        preferencesManager.emergencyContacts.first()

    fun buildSosMessage(latitude: Double, longitude: Double): String {
        val mapsLink = "https://maps.google.com/?q=$latitude,$longitude"
        return "\uD83D\uDEA8 EMERGENCY - I need help!\nMy live location: $mapsLink\n\u2014 Sent via ViNav Helmet App"
    }

    fun getSmsIntent(phone: String, message: String): Intent =
        Intent(Intent.ACTION_VIEW).apply {
            data = Uri.parse("sms:$phone")
            putExtra("sms_body", message)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }

    fun getWhatsAppIntent(phone: String, message: String): Intent {
        val encoded = Uri.encode(message)
        return Intent(Intent.ACTION_VIEW).apply {
            data = Uri.parse("https://wa.me/$phone?text=$encoded")
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
    }

    fun getCallIntent(phone: String): Intent =
        Intent(Intent.ACTION_DIAL).apply {
            data = Uri.parse("tel:$phone")
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }

    suspend fun triggerSos(latitude: Double, longitude: Double) {
        helmetRepository.sendSosTrigger()
    }
}
