package com.vinav.helmet.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.doublePreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.vinav.helmet.model.EmergencyContact
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "vinav_prefs")

@Singleton
class PreferencesManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val gson = Gson()

    companion object {
        val LAST_CONNECTED_DEVICE = stringPreferencesKey("last_connected_device")
        val AUTO_RECONNECT = booleanPreferencesKey("auto_reconnect")
        val DISTANCE_UNIT = stringPreferencesKey("distance_unit")
        val NAV_REFRESH_INTERVAL = intPreferencesKey("nav_refresh_interval_ms")
        val HOME_LAT = doublePreferencesKey("home_lat")
        val HOME_LNG = doublePreferencesKey("home_lng")
        val HOME_NAME = stringPreferencesKey("home_name")
        val HOME_ADDRESS = stringPreferencesKey("home_address")
        val EMERGENCY_CONTACTS = stringPreferencesKey("emergency_contacts_json")
        val DEBUG_MODE = booleanPreferencesKey("debug_mode")
    }

    val lastConnectedDevice: Flow<String> = context.dataStore.data.map { it[LAST_CONNECTED_DEVICE] ?: "" }
    val autoReconnect: Flow<Boolean> = context.dataStore.data.map { it[AUTO_RECONNECT] ?: true }
    val distanceUnit: Flow<String> = context.dataStore.data.map { it[DISTANCE_UNIT] ?: "metric" }
    val navRefreshInterval: Flow<Int> = context.dataStore.data.map { it[NAV_REFRESH_INTERVAL] ?: 3000 }
    val homeName: Flow<String> = context.dataStore.data.map { it[HOME_NAME] ?: "" }
    val homeAddress: Flow<String> = context.dataStore.data.map { it[HOME_ADDRESS] ?: "" }
    val homeLat: Flow<Double> = context.dataStore.data.map { it[HOME_LAT] ?: 0.0 }
    val homeLng: Flow<Double> = context.dataStore.data.map { it[HOME_LNG] ?: 0.0 }
    val debugMode: Flow<Boolean> = context.dataStore.data.map { it[DEBUG_MODE] ?: false }

    val emergencyContacts: Flow<List<EmergencyContact>> = context.dataStore.data.map {
        val json = it[EMERGENCY_CONTACTS] ?: "[]"
        try {
            val type = object : TypeToken<List<EmergencyContact>>() {}.type
            gson.fromJson(json, type) ?: emptyList()
        } catch (_: Exception) { emptyList() }
    }

    suspend fun setLastConnectedDevice(address: String) { context.dataStore.edit { it[LAST_CONNECTED_DEVICE] = address } }
    suspend fun setAutoReconnect(enabled: Boolean) { context.dataStore.edit { it[AUTO_RECONNECT] = enabled } }
    suspend fun setDistanceUnit(unit: String) { context.dataStore.edit { it[DISTANCE_UNIT] = unit } }
    suspend fun setNavRefreshInterval(ms: Int) { context.dataStore.edit { it[NAV_REFRESH_INTERVAL] = ms } }
    suspend fun setDebugMode(enabled: Boolean) { context.dataStore.edit { it[DEBUG_MODE] = enabled } }

    suspend fun setHome(name: String, address: String, lat: Double, lng: Double) {
        context.dataStore.edit {
            it[HOME_NAME] = name
            it[HOME_ADDRESS] = address
            it[HOME_LAT] = lat
            it[HOME_LNG] = lng
        }
    }

    suspend fun setEmergencyContacts(contacts: List<EmergencyContact>) {
        context.dataStore.edit { it[EMERGENCY_CONTACTS] = gson.toJson(contacts) }
    }
}
