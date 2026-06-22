package com.example.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.RecentlyPlayedEntity
import com.example.data.remote.*
import com.example.data.repository.MusicRepository
import com.example.media.PlayerController
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@OptIn(FlowPreview::class)
@HiltViewModel
class SearchViewModel @Inject constructor(
    private val repository: MusicRepository,
    private val playerController: PlayerController
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery

    private val _genresState = MutableStateFlow<NetworkResult<List<DeezerGenre>>>(NetworkResult.Loading)
    val genresState: StateFlow<NetworkResult<List<DeezerGenre>>> = _genresState

    // Debounced and reactive search Flow
    val searchResultState: StateFlow<NetworkResult<List<DeezerTrack>>> = _searchQuery
        .debounce(500)
        .distinctUntilChanged()
        .flatMapLatest { query ->
            if (query.trim().isEmpty()) {
                flowOf(NetworkResult.Success(emptyList()))
            } else {
                repository.search(query.trim())
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = NetworkResult.Success(emptyList())
        )

    val trendingSearches = listOf("Rock", "Pop", "Jazz", "Hip Hop", "Lofi Beats", "Dance", "Chill Out")

    init {
        loadGenres()
    }

    fun onSearchQueryChanged(newQuery: String) {
        _searchQuery.value = newQuery
    }

    private fun loadGenres() {
        viewModelScope.launch {
            repository.getGenres().collect { _genresState.value = it }
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
