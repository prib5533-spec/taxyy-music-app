package com.example.media

import com.example.data.remote.DeezerTrack
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PlayerController @Inject constructor(
    private val serviceConnection: MusicServiceConnection
) {
    val isConnected: StateFlow<Boolean> = serviceConnection.isConnected
    val isPlaying: StateFlow<Boolean> = serviceConnection.isPlaying
    val currentTrack: StateFlow<DeezerTrack?> = serviceConnection.currentTrack
    val currentPosition: StateFlow<Long> = serviceConnection.currentPosition
    val duration: StateFlow<Long> = serviceConnection.duration
    val currentQueue: StateFlow<List<DeezerTrack>> = serviceConnection.currentQueue

    fun playTrack(track: DeezerTrack, queue: List<DeezerTrack> = emptyList()) {
        serviceConnection.playTrack(track, queue)
    }

    fun play() {
        serviceConnection.play()
    }

    fun pause() {
        serviceConnection.pause()
    }

    fun skipToNext() {
        serviceConnection.skipToNext()
    }

    fun skipToPrevious() {
        serviceConnection.skipToPrevious()
    }

    fun seekTo(positionMs: Long) {
        serviceConnection.seekTo(positionMs)
    }
}
