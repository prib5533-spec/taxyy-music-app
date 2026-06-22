package com.example.data.repository

import com.example.data.local.LikedSongDao
import com.example.data.local.LikedSongEntity
import com.example.data.local.RecentlyPlayedDao
import com.example.data.local.RecentlyPlayedEntity
import com.example.data.remote.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MusicRepositoryImpl @Inject constructor(
    private val apiService: DeezerApiService,
    private val likedSongDao: LikedSongDao,
    private val recentlyPlayedDao: RecentlyPlayedDao
) : MusicRepository {

    override fun getChartTracks(): Flow<NetworkResult<List<DeezerTrack>>> = flow {
        emit(NetworkResult.Loading)
        try {
            val response = apiService.getChartTracks()
            emit(NetworkResult.Success(response.data))
        } catch (e: Exception) {
            emit(NetworkResult.Error(e, e.localizedMessage))
        }
    }.flowOn(Dispatchers.IO)

    override fun getChartAlbums(): Flow<NetworkResult<List<DeezerAlbum>>> = flow {
        emit(NetworkResult.Loading)
        try {
            val response = apiService.getChartAlbums()
            emit(NetworkResult.Success(response.data))
        } catch (e: Exception) {
            emit(NetworkResult.Error(e, e.localizedMessage))
        }
    }.flowOn(Dispatchers.IO)

    override fun getChartArtists(): Flow<NetworkResult<List<DeezerArtist>>> = flow {
        emit(NetworkResult.Loading)
        try {
            val response = apiService.getChartArtists()
            emit(NetworkResult.Success(response.data))
        } catch (e: Exception) {
            emit(NetworkResult.Error(e, e.localizedMessage))
        }
    }.flowOn(Dispatchers.IO)

    override fun getGenres(): Flow<NetworkResult<List<DeezerGenre>>> = flow {
        emit(NetworkResult.Loading)
        try {
            val response = apiService.getGenreList()
            emit(NetworkResult.Success(response.data))
        } catch (e: Exception) {
            emit(NetworkResult.Error(e, e.localizedMessage))
        }
    }.flowOn(Dispatchers.IO)

    override fun getRadios(): Flow<NetworkResult<List<DeezerRadio>>> = flow {
        emit(NetworkResult.Loading)
        try {
            val response = apiService.getRadios()
            emit(NetworkResult.Success(response.data))
        } catch (e: Exception) {
            emit(NetworkResult.Error(e, e.localizedMessage))
        }
    }.flowOn(Dispatchers.IO)

    override fun search(query: String): Flow<NetworkResult<List<DeezerTrack>>> = flow {
        emit(NetworkResult.Loading)
        try {
            val response = apiService.search(query)
            emit(NetworkResult.Success(response.data))
        } catch (e: Exception) {
            emit(NetworkResult.Error(e, e.localizedMessage))
        }
    }.flowOn(Dispatchers.IO)

    override fun getTrackDetail(id: String): Flow<NetworkResult<DeezerTrack>> = flow {
        emit(NetworkResult.Loading)
        try {
            val response = apiService.getTrack(id)
            emit(NetworkResult.Success(response))
        } catch (e: Exception) {
            emit(NetworkResult.Error(e, e.localizedMessage))
        }
    }.flowOn(Dispatchers.IO)

    override fun getAlbumDetail(id: String): Flow<NetworkResult<DeezerAlbum>> = flow {
        emit(NetworkResult.Loading)
        try {
            val response = apiService.getAlbum(id)
            emit(NetworkResult.Success(response))
        } catch (e: Exception) {
            emit(NetworkResult.Error(e, e.localizedMessage))
        }
    }.flowOn(Dispatchers.IO)

    override fun getArtistDetail(id: String): Flow<NetworkResult<DeezerArtist>> = flow {
        emit(NetworkResult.Loading)
        try {
            val response = apiService.getArtist(id)
            emit(NetworkResult.Success(response))
        } catch (e: Exception) {
            emit(NetworkResult.Error(e, e.localizedMessage))
        }
    }.flowOn(Dispatchers.IO)

    override fun getArtistTopTracks(id: String): Flow<NetworkResult<List<DeezerTrack>>> = flow {
        emit(NetworkResult.Loading)
        try {
            val response = apiService.getArtistTopTracks(id)
            emit(NetworkResult.Success(response.data))
        } catch (e: Exception) {
            emit(NetworkResult.Error(e, e.localizedMessage))
        }
    }.flowOn(Dispatchers.IO)

    // Room Liked Songs
    override fun getAllLikedSongs(): Flow<List<LikedSongEntity>> = likedSongDao.getAllLikedSongs()

    override suspend fun likeSong(song: LikedSongEntity) {
        likedSongDao.insertLikedSong(song)
    }

    override suspend fun unlikeSong(id: String) {
        likedSongDao.deleteLikedSong(id)
    }

    override fun isSongLikedFlow(id: String): Flow<Boolean> = likedSongDao.isSongLikedFlow(id)

    override suspend fun isSongLiked(id: String): Boolean = likedSongDao.isSongLiked(id)

    // Room Recently Played
    override fun getRecentlyPlayed(): Flow<List<RecentlyPlayedEntity>> = recentlyPlayedDao.getRecentlyPlayed()

    override suspend fun insertRecentlyPlayed(song: RecentlyPlayedEntity) {
        recentlyPlayedDao.insertRecentlyPlayed(song)
    }

    override suspend fun clearRecentlyPlayed() {
        recentlyPlayedDao.clearAll()
    }
}
