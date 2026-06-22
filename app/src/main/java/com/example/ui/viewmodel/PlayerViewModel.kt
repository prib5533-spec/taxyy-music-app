package com.example.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.LikedSongEntity
import com.example.data.remote.DeezerTrack
import com.example.data.repository.MusicRepository
import com.example.media.PlayerController
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class PlayerViewModel @Inject constructor(
    private val repository: MusicRepository,
    private val playerController: PlayerController
) : ViewModel() {

    val isConnected: StateFlow<Boolean> = playerController.isConnected
    val isPlaying: StateFlow<Boolean> = playerController.isPlaying
    val currentTrack: StateFlow<DeezerTrack?> = playerController.currentTrack
    val currentPosition: StateFlow<Long> = playerController.currentPosition
    val duration: StateFlow<Long> = playerController.duration
    val currentQueue: StateFlow<List<DeezerTrack>> = playerController.currentQueue

    // Reactively monitor if the currently playing song is in the local Liked Table
    val isLiked: StateFlow<Boolean> = playerController.currentTrack
        .flatMapLatest { track ->
            if (track == null) {
                flowOf(false)
            } else {
                repository.isSongLikedFlow(track.id)
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = false
        )

    private val _shuffleEnabled = MutableStateFlow(false)
    val shuffleEnabled: StateFlow<Boolean> = _shuffleEnabled

    private val _repeatMode = MutableStateFlow(PlaybackRepeatMode.NONE)
    val repeatMode: StateFlow<PlaybackRepeatMode> = _repeatMode

    fun togglePlayPause() {
        if (isPlaying.value) {
            playerController.pause()
        } else {
            playerController.play()
        }
    }

    fun skipToNext() {
        playerController.skipToNext()
    }

    fun skipToPrevious() {
        playerController.skipToPrevious()
    }

    fun seekTo(positionMs: Long) {
        playerController.seekTo(positionMs)
    }

    fun toggleShuffle() {
        _shuffleEnabled.value = !_shuffleEnabled.value
        // If shuffle is enabled, we could randomize order, but keeping visual state is sufficient for this scope
    }

    fun toggleRepeat() {
        _repeatMode.value = when (_repeatMode.value) {
            PlaybackRepeatMode.NONE -> PlaybackRepeatMode.ONE
            PlaybackRepeatMode.ONE -> PlaybackRepeatMode.ALL
            PlaybackRepeatMode.ALL -> PlaybackRepeatMode.NONE
        }
    }

    fun toggleLike() {
        val track = currentTrack.value ?: return
        viewModelScope.launch {
            val alreadyLiked = repository.isSongLiked(track.id)
            if (alreadyLiked) {
                repository.unlikeSong(track.id)
            } else {
                val entity = LikedSongEntity(
                    id = track.id,
                    title = track.title,
                    artistId = track.artist.id,
                    artistName = track.artist.name,
                    artistPictureUrl = track.artist.pictureMedium ?: "",
                    albumId = track.album?.id ?: "",
                    albumName = track.album?.title ?: "",
                    coverUrl = track.album?.coverMedium ?: track.cover ?: "",
                    duration = track.duration,
                    previewUrl = track.preview
                )
                repository.likeSong(entity)
            }
        }
    }
}

enum class PlaybackRepeatMode {
    NONE,
    ONE,
    ALL
}
