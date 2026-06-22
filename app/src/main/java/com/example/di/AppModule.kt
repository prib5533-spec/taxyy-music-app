package com.example.di

import android.content.Context
import androidx.room.Room
import com.example.data.local.AppDatabase
import com.example.data.local.LikedSongDao
import com.example.data.local.RecentlyPlayedDao
import com.example.data.remote.DeezerApiService
import com.example.data.repository.MusicRepository
import com.example.data.repository.MusicRepositoryImpl
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "music_stream_db"
        ).fallbackToDestructiveMigration()
            .build()
    }

    @Provides
    @Singleton
    fun provideLikedSongDao(database: AppDatabase): LikedSongDao {
        return database.likedSongDao()
    }

    @Provides
    @Singleton
    fun provideRecentlyPlayedDao(database: AppDatabase): RecentlyPlayedDao {
        return database.recentlyPlayedDao()
    }

    @Provides
    @Singleton
    fun provideMusicRepository(
        apiService: DeezerApiService,
        likedSongDao: LikedSongDao,
        recentlyPlayedDao: RecentlyPlayedDao
    ): MusicRepository {
        return MusicRepositoryImpl(apiService, likedSongDao, recentlyPlayedDao)
    }
}
