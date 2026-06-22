package com.example.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.LikedSongEntity
import com.example.data.local.RecentlyPlayedEntity
import com.example.data.remote.DeezerArtist
import com.example.data.remote.DeezerTrack
import com.example.data.repository.MusicRepository
import com.example.media.PlayerController
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LibraryViewModel @Inject constructor(
    private val repository: MusicRepository,
    private val playerController: PlayerController
) : ViewModel() {

    val likedSongs: StateFlow<List<LikedSongEntity>> = repository.getAllLikedSongs()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    private val _selectedFilter = MutableStateFlow("All")
    val selectedFilter: StateFlow<String> = _selectedFilter

    val filters = listOf("All", "Liked Songs", "Playlists", "Artists")

    fun setFilter(filter: String) {
        _selectedFilter.value = filter
    }

    fun playLikedSong(song: LikedSongEntity, allSongs: List<LikedSongEntity>) {
        val convertedTrack = convertLikedSongToDeezerTrack(song)
        val convertedQueue = allSongs.map { convertLikedSongToDeezerTrack(it) }
        
        playerController.playTrack(convertedTrack, convertedQueue)
        
        viewModelScope.launch {
            val recentlyPlayed = RecentlyPlayedEntity(
                id = song.id,
                trackId = song.id,
                title = song.title,
                artistId = song.artistId,
                artistName = song.artistName,
                artistPictureUrl = song.artistPictureUrl,
                albumId = song.albumId,
                albumName = song.albumName,
                coverUrl = song.coverUrl,
                duration = song.duration,
                previewUrl = song.previewUrl
            )
            repository.insertRecentlyPlayed(recentlyPlayed)
        }
    }

    private fun convertLikedSongToDeezerTrack(song: LikedSongEntity): DeezerTrack {
        return DeezerTrack(
            id = song.id,
            title = song.title,
            duration = song.duration,
            preview = song.previewUrl,
            artist = DeezerArtist(
                id = song.artistId,
                name = song.artistName,
                picture = song.artistPictureUrl,
                pictureSmall = null,
                pictureMedium = song.artistPictureUrl,
                pictureBig = null,
                nbFan = null,
                nbAlbum = null
            ),
            album = null, // Detail endpoints might resolve this, keeping null is fine
            cover = song.coverUrl
        )
    }
}
