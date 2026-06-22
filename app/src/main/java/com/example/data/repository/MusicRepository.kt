package com.example.data.repository

import com.example.data.local.LikedSongEntity
import com.example.data.local.RecentlyPlayedEntity
import com.example.data.remote.*
import kotlinx.coroutines.flow.Flow

interface MusicRepository {
    // Remote Music APIs
    fun getChartTracks(): Flow<NetworkResult<List<DeezerTrack>>>
    fun getChartAlbums(): Flow<NetworkResult<List<DeezerAlbum>>>
    fun getChartArtists(): Flow<NetworkResult<List<DeezerArtist>>>
    fun getGenres(): Flow<NetworkResult<List<DeezerGenre>>>
    fun getRadios(): Flow<NetworkResult<List<DeezerRadio>>>
    fun search(query: String): Flow<NetworkResult<List<DeezerTrack>>>
    fun getTrackDetail(id: String): Flow<NetworkResult<DeezerTrack>>
    fun getAlbumDetail(id: String): Flow<NetworkResult<DeezerAlbum>>
    fun getArtistDetail(id: String): Flow<NetworkResult<DeezerArtist>>
    fun getArtistTopTracks(id: String): Flow<NetworkResult<List<DeezerTrack>>>

    // Local Liked Songs Room APIs
    fun getAllLikedSongs(): Flow<List<LikedSongEntity>>
    suspend fun likeSong(song: LikedSongEntity)
    suspend fun unlikeSong(id: String)
    fun isSongLikedFlow(id: String): Flow<Boolean>
    suspend fun isSongLiked(id: String): Boolean

    // Local Recently Played Room APIs
    fun getRecentlyPlayed(): Flow<List<RecentlyPlayedEntity>>
    suspend fun insertRecentlyPlayed(song: RecentlyPlayedEntity)
    suspend fun clearRecentlyPlayed()
}
