package com.vinav.helmet.media

import android.content.Context
import com.vinav.helmet.bluetooth.HelmetRepository
import com.vinav.helmet.util.ViNavService
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SpotifyMediaController @Inject constructor(
    @ApplicationContext private val context: Context,
    private val helmetRepository: HelmetRepository
) : MediaController {

    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    // Now uses the universal track info from ViNavService
    override val currentTrack: StateFlow<TrackInfo> = ViNavService.currentTrack

    override fun connect() {
        // ViNavService starts automatically when permission is granted
    }

    override fun disconnect() {
    }

    override fun play() {
        ViNavService.togglePausePlay()
        scope.launch { helmetRepository.sendMusicCommand("play") }
    }

    override fun pause() {
        ViNavService.togglePausePlay()
        scope.launch { helmetRepository.sendMusicCommand("pause") }
    }

    override fun next() {
        ViNavService.skipNext()
        scope.launch { helmetRepository.sendMusicCommand("next") }
    }

    override fun previous() {
        ViNavService.skipPrevious()
        scope.launch { helmetRepository.sendMusicCommand("prev") }
    }
    
    fun answerCall() {
        ViNavService.answerCall(context)
    }
}
