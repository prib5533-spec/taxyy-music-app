package com.example.data.remote

import com.google.gson.annotations.SerializedName

data class DeezerArtist(
    @SerializedName("id") val id: String,
    @SerializedName("name") val name: String,
    @SerializedName("picture") val picture: String?,
    @SerializedName("picture_small") val pictureSmall: String?,
    @SerializedName("picture_medium") val pictureMedium: String?,
    @SerializedName("picture_big") val pictureBig: String?,
    @SerializedName("nb_fan") val nbFan: Int?,
    @SerializedName("nb_album") val nbAlbum: Int?
)

data class DeezerAlbum(
    @SerializedName("id") val id: String,
    @SerializedName("title") val title: String,
    @SerializedName("cover") val cover: String?,
    @SerializedName("cover_small") val coverSmall: String?,
    @SerializedName("cover_medium") val coverMedium: String?,
    @SerializedName("cover_big") val coverBig: String?,
    @SerializedName("artist") val artist: DeezerArtist?,
    @SerializedName("release_date") val releaseDate: String?,
    @SerializedName("tracks") val tracks: DeezerTrackListResponse?
)

data class DeezerTrack(
    @SerializedName("id") val id: String,
    @SerializedName("title") val title: String,
    @SerializedName("duration") val duration: Int,
    @SerializedName("preview") val preview: String,
    @SerializedName("artist") val artist: DeezerArtist,
    @SerializedName("album") val album: DeezerAlbum?,
    @SerializedName("cover") val cover: String?
)

data class DeezerGenre(
    @SerializedName("id") val id: String,
    @SerializedName("name") val name: String,
    @SerializedName("picture") val picture: String?,
    @SerializedName("picture_medium") val pictureMedium: String?
)

data class DeezerPlaylist(
    @SerializedName("id") val id: String,
    @SerializedName("title") val title: String,
    @SerializedName("picture") val picture: String?,
    @SerializedName("picture_medium") val pictureMedium: String?,
    @SerializedName("nb_tracks") val nbTracks: Int?,
    @SerializedName("tracks") val tracks: DeezerTrackListResponse?
)

data class DeezerTrackListResponse(
    @SerializedName("data") val data: List<DeezerTrack>
)

data class DeezerSearchResponse(
    @SerializedName("data") val data: List<DeezerTrack>
)

data class DeezerAlbumListResponse(
    @SerializedName("data") val data: List<DeezerAlbum>
)

data class DeezerArtistListResponse(
    @SerializedName("data") val data: List<DeezerArtist>
)

data class DeezerGenreListResponse(
    @SerializedName("data") val data: List<DeezerGenre>
)

data class DeezerRadio(
    @SerializedName("id") val id: String,
    @SerializedName("title") val title: String,
    @SerializedName("picture") val picture: String?,
    @SerializedName("picture_medium") val pictureMedium: String?
)

data class DeezerRadioListResponse(
    @SerializedName("data") val data: List<DeezerRadio>
)
