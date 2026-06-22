package com.example.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.RecentlyPlayedEntity
import com.example.data.remote.DeezerArtist
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
class ArtistViewModel @Inject constructor(
    private val repository: MusicRepository,
    private val playerController: PlayerController
) : ViewModel() {

    private val _artistDetailState = MutableStateFlow<NetworkResult<DeezerArtist>>(NetworkResult.Loading)
    val artistDetailState: StateFlow<NetworkResult<DeezerArtist>> = _artistDetailState

    private val _artistTopTracksState = MutableStateFlow<NetworkResult<List<DeezerTrack>>>(NetworkResult.Loading)
    val artistTopTracksState: StateFlow<NetworkResult<List<DeezerTrack>>> = _artistTopTracksState

    fun loadArtist(artistId: String) {
        viewModelScope.launch {
            repository.getArtistDetail(artistId).collect { _artistDetailState.value = it }
        }
        viewModelScope.launch {
            repository.getArtistTopTracks(artistId).collect { _artistTopTracksState.value = it }
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
