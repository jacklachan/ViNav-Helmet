package com.vinav.helmet.util

import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.media.session.MediaController
import android.media.session.MediaSessionManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.telecom.TelecomManager
import android.util.Log
import com.vinav.helmet.media.TrackInfo
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class ViNavService : NotificationListenerService() {

    companion object {
        private val _currentTrack = MutableStateFlow(TrackInfo())
        val currentTrack: StateFlow<TrackInfo> = _currentTrack

        private var instance: ViNavService? = null

        fun isRunning(): Boolean = instance != null
        
        fun skipNext() {
            instance?.controlMedia { it.transportControls.skipToNext() }
        }
        
        fun skipPrevious() {
            instance?.controlMedia { it.transportControls.skipToPrevious() }
        }
        
        fun togglePausePlay() {
            instance?.controlMedia {
                val state = it.playbackState?.state
                if (state == android.media.session.PlaybackState.STATE_PLAYING) {
                    it.transportControls.pause()
                } else {
                    it.transportControls.play()
                }
            }
        }

        fun answerCall(context: Context) {
            try {
                val telecomManager = context.getSystemService(Context.TELECOM_SERVICE) as TelecomManager
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                    telecomManager.acceptRingingCall()
                }
            } catch (e: Exception) {
                Log.e("ViNavService", "Error answering call: ${e.message}")
            }
        }
    }

    private lateinit var mediaSessionManager: MediaSessionManager

    override fun onCreate() {
        super.onCreate()
        instance = this
        mediaSessionManager = getSystemService(Context.MEDIA_SESSION_SERVICE) as MediaSessionManager
    }

    override fun onDestroy() {
        super.onDestroy()
        instance = null
    }

    override fun onNotificationPosted(sbn: StatusBarNotification?) {
        updateMediaInfo()
    }

    override fun onNotificationRemoved(sbn: StatusBarNotification?) {
        updateMediaInfo()
    }

    private fun updateMediaInfo() {
        val controllers = mediaSessionManager.getActiveSessions(ComponentName(this, ViNavService::class.java))
        val activeController = controllers.firstOrNull() // Pick the first active session (Spotify, etc)
        
        activeController?.let { controller ->
            val metadata = controller.metadata
            val playbackState = controller.playbackState
            
            if (metadata != null) {
                val title = metadata.getString(android.media.MediaMetadata.METADATA_KEY_TITLE) ?: ""
                val artist = metadata.getString(android.media.MediaMetadata.METADATA_KEY_ARTIST) ?: ""
                val isPlaying = playbackState?.state == android.media.session.PlaybackState.STATE_PLAYING
                
                _currentTrack.value = TrackInfo(title, artist, isPlaying)
            }
        }
    }

    private fun controlMedia(action: (MediaController) -> Unit) {
        val controllers = mediaSessionManager.getActiveSessions(ComponentName(this, ViNavService::class.java))
        controllers.firstOrNull()?.let { action(it) }
    }
}
