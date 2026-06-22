package com.example.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.RecentlyPlayedEntity
import com.example.data.remote.*
import com.example.data.repository.MusicRepository
import com.example.media.PlayerController
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val repository: MusicRepository,
    private val playerController: PlayerController
) : ViewModel() {

    private val _chartTracksState = MutableStateFlow<NetworkResult<List<DeezerTrack>>>(NetworkResult.Loading)
    val chartTracksState: StateFlow<NetworkResult<List<DeezerTrack>>> = _chartTracksState

    private val _chartAlbumsState = MutableStateFlow<NetworkResult<List<DeezerAlbum>>>(NetworkResult.Loading)
    val chartAlbumsState: StateFlow<NetworkResult<List<DeezerAlbum>>> = _chartAlbumsState

    private val _chartArtistsState = MutableStateFlow<NetworkResult<List<DeezerArtist>>>(NetworkResult.Loading)
    val chartArtistsState: StateFlow<NetworkResult<List<DeezerArtist>>> = _chartArtistsState

    private val _radiosState = MutableStateFlow<NetworkResult<List<DeezerRadio>>>(NetworkResult.Loading)
    val radiosState: StateFlow<NetworkResult<List<DeezerRadio>>> = _radiosState

    val recentlyPlayedState: StateFlow<List<RecentlyPlayedEntity>> = repository.getRecentlyPlayed()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing

    init {
        loadHomeContent()
    }

    fun refresh() {
        viewModelScope.launch {
            _isRefreshing.value = true
            loadHomeContent()
            _isRefreshing.value = false
        }
    }

    private fun loadHomeContent() {
        viewModelScope.launch {
            repository.getChartTracks().collect { _chartTracksState.value = it }
        }
        viewModelScope.launch {
            repository.getChartAlbums().collect { _chartAlbumsState.value = it }
        }
        viewModelScope.launch {
            repository.getChartArtists().collect { _chartArtistsState.value = it }
        }
        viewModelScope.launch {
            repository.getRadios().collect { _radiosState.value = it }
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
