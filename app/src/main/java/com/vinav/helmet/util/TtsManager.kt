package com.vinav.helmet.util

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFocusRequest
import android.media.AudioManager
import android.os.Build
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import dagger.hilt.android.qualifiers.ApplicationContext
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TtsManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private var tts: TextToSpeech? = null
    private var isReady = false
    private val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager

    // Fix: Modern Android requires a listener for audio focus requests
    private val focusChangeListener = AudioManager.OnAudioFocusChangeListener { _ -> }

    init {
        // Explicitly request the Google TTS engine for the familiar Maps voice
        tts = TextToSpeech(context, { status ->
            if (status == TextToSpeech.SUCCESS) {
                setupGoogleVoice()
                isReady = true
                setupProgressListener()
            }
        }, "com.google.android.tts")
    }

    private fun setupGoogleVoice() {
        val locale = Locale.getDefault()
        tts?.language = locale
        
        try {
            val voices = tts?.voices
            if (voices != null) {
                val googleVoice = voices.find { voice ->
                    voice.locale.language == locale.language && 
                    !voice.isNetworkConnectionRequired &&
                    voice.name.contains("google", ignoreCase = true)
                }
                googleVoice?.let { tts?.voice = it }
            }
        } catch (e: Exception) {}
        
        tts?.setSpeechRate(1.0f)
        tts?.setPitch(1.0f)
    }

    private fun setupProgressListener() {
        tts?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
            override fun onStart(utteranceId: String?) {}
            override fun onDone(utteranceId: String?) { abandonFocus() }
            override fun onError(utteranceId: String?) { abandonFocus() }
        })
    }

    fun speak(text: String) {
        if (!isReady) return

        requestHoldFocus()
        val params = android.os.Bundle()
        params.putString(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, "nav_instruction")
        tts?.speak(text, TextToSpeech.QUEUE_FLUSH, params, "nav_instruction")
    }

    private fun requestHoldFocus() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val focusRequest = AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN_TRANSIENT_MAY_DUCK)
                .setAudioAttributes(AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_ASSISTANCE_NAVIGATION_GUIDANCE)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                    .build())
                .setAcceptsDelayedFocusGain(true)
                .setOnAudioFocusChangeListener(focusChangeListener) // This fixes the crash!
                .build()
            audioManager.requestAudioFocus(focusRequest)
        } else {
            @Suppress("DEPRECATION")
            audioManager.requestAudioFocus(focusChangeListener, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN_TRANSIENT_MAY_DUCK)
        }
    }

    private fun abandonFocus() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // Modern Android handles focus abandonment automatically in many ducking scenarios
        } else {
            @Suppress("DEPRECATION")
            audioManager.abandonAudioFocus(focusChangeListener)
        }
    }

    fun stop() {
        tts?.stop()
        tts?.shutdown()
    }
}
