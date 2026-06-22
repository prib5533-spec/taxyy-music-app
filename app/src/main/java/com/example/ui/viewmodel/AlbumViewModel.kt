package com.example.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.RecentlyPlayedEntity
import com.example.data.remote.DeezerAlbum
import com.example.data.remote.DeezerTrack
import com.example.data.remote.NetworkResult
import com.example.data.repository.MusicRepository
import com.example.media.PlayerController
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AlbumViewModel @Inject constructor(
    private val repository: MusicRepository,
    private val playerController: PlayerController
) : ViewModel() {

    private val _albumDetailState = MutableStateFlow<NetworkResult<DeezerAlbum>>(NetworkResult.Loading)
    val albumDetailState: StateFlow<NetworkResult<DeezerAlbum>> = _albumDetailState

    fun loadAlbum(albumId: String) {
        viewModelScope.launch {
            repository.getAlbumDetail(albumId).collect { _albumDetailState.value = it }
        }
    }

    fun playTrack(track: DeezerTrack, queue: List<DeezerTrack> = emptyList()) {
        playerController.playTrack(track, queue)
        viewModelScope.launch {
            val recentlyPlayed = RecentlyPlayedEntity(
                id = track.id,
                trackId = track.id,
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
            repository.insertRecentlyPlayed(recentlyPlayed)
        }
    }
}
