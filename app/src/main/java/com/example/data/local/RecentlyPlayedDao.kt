package com.example.data.local

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface RecentlyPlayedDao {
    @Query("SELECT * FROM recently_played ORDER BY timestamp DESC LIMIT 20")
    fun getRecentlyPlayed(): Flow<List<RecentlyPlayedEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecentlyPlayedRaw(song: RecentlyPlayedEntity)

    @Query("DELETE FROM recently_played WHERE id NOT IN (SELECT id FROM recently_played ORDER BY timestamp DESC LIMIT 20)")
    suspend fun pruneRecentlyPlayed()

    @Transaction
    suspend fun insertRecentlyPlayed(song: RecentlyPlayedEntity) {
        insertRecentlyPlayedRaw(song)
        pruneRecentlyPlayed()
    }

    @Query("DELETE FROM recently_played")
    suspend fun clearAll()
}
