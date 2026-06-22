package com.example.data.remote

import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface DeezerApiService {
    @GET("search")
    suspend fun search(
        @Query("q") query: String
    ): DeezerSearchResponse

    @GET("track/{id}")
    suspend fun getTrack(
        @Path("id") id: String
    ): DeezerTrack

    @GET("album/{id}")
    suspend fun getAlbum(
        @Path("id") id: String
    ): DeezerAlbum

    @GET("artist/{id}")
    suspend fun getArtist(
        @Path("id") id: String
    ): DeezerArtist

    @GET("artist/{id}/top")
    suspend fun getArtistTopTracks(
        @Path("id") id: String
    ): DeezerTrackListResponse

    @GET("chart/0/tracks")
    suspend fun getChartTracks(): DeezerTrackListResponse

    @GET("chart/0/albums")
    suspend fun getChartAlbums(): DeezerAlbumListResponse

    @GET("chart/0/artists")
    suspend fun getChartArtists(): DeezerArtistListResponse

    @GET("genre")
    suspend fun getGenreList(): DeezerGenreListResponse

    @GET("radio")
    suspend fun getRadios(): DeezerRadioListResponse
}
