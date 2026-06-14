package com.vinav.helmet.media

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

data class TrackInfo(
    val title: String = "",
    val artist: String = "",
    val isPlaying: Boolean = false
)

interface MediaController {
    val currentTrack: StateFlow<TrackInfo>
    fun connect()
    fun disconnect()
    fun play()
    fun pause()
    fun next()
    fun previous()
}
