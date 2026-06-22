package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "liked_songs")
data class LikedSongEntity(
    @PrimaryKey val id: String,
    val title: String,
    val artistId: String,
    val artistName: String,
    val artistPictureUrl: String,
    val albumId: String,
    val albumName: String,
    val coverUrl: String,
    val duration: Int,
    val previewUrl: String,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "recently_played")
data class RecentlyPlayedEntity(
    @PrimaryKey val id: String, // same as trackId or auto-generated, we can use trackId
    val trackId: String,
    val title: String,
    val artistId: String,
    val artistName: String,
    val artistPictureUrl: String,
    val albumId: String,
    val albumName: String,
    val coverUrl: String,
    val duration: Int,
    val previewUrl: String,
    val timestamp: Long = System.currentTimeMillis()
)
