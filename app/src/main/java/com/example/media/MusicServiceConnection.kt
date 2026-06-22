package com.example.media

import android.content.ComponentName
import android.content.Context
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.Player
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import com.example.data.remote.DeezerTrack
import com.example.utils.YouTubeAudioResolver
import com.google.common.util.concurrent.ListenableFuture
import com.google.common.util.concurrent.MoreExecutors
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MusicServiceConnection @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val _isConnected = MutableStateFlow(false)
    val isConnected: StateFlow<Boolean> = _isConnected

    private val _isPlaying = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean> = _isPlaying

    private val _currentTrack = MutableStateFlow<DeezerTrack?>(null)
    val currentTrack: StateFlow<DeezerTrack?> = _currentTrack

    private val _currentPosition = MutableStateFlow(0L)
    val currentPosition: StateFlow<Long> = _currentPosition

    private val _duration = MutableStateFlow(0L)
    val duration: StateFlow<Long> = _duration

    private val _currentQueue = MutableStateFlow<List<DeezerTrack>>(emptyList())
    val currentQueue: StateFlow<List<DeezerTrack>> = _currentQueue

    private var mediaController: MediaController? = null
    private var controllerFuture: ListenableFuture<MediaController>? = null
    
    private val scope = CoroutineScope(Dispatchers.Main + SupervisorJob())
    private var resolveJob: Job? = null

    init {
        val sessionToken = SessionToken(context, ComponentName(context, MusicService::class.java))
        controllerFuture = MediaController.Builder(context, sessionToken).buildAsync()
        controllerFuture?.addListener({
            try {
                mediaController = controllerFuture?.get()?.apply {
                    addListener(PlayerListener())
                    _isConnected.value = true
                    updatePlaybackState()
                }
                startPositionTracker()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }, MoreExecutors.directExecutor())
    }

    fun playTrack(track: DeezerTrack, queue: List<DeezerTrack> = emptyList()) {
        val controller = mediaController ?: return
        _currentTrack.value = track
        _currentQueue.value = queue.ifEmpty { listOf(track) }

        controller.stop()
        controller.clearMediaItems()
        
        val mediaItems = _currentQueue.value.map { item ->
            val artworkUri = item.album?.coverMedium ?: item.cover ?: ""
            MediaItem.Builder()
                .setMediaId(item.id)
                .setUri(item.preview)
                .setMediaMetadata(
                    MediaMetadata.Builder()
                        .setTitle(item.title)
                        .setArtist(item.artist.name)
                        .setArtworkUri(android.net.Uri.parse(artworkUri))
                        .build()
                )
                .build()
        }
        
        controller.addMediaItems(mediaItems)
        val index = _currentQueue.value.indexOfFirst { it.id == track.id }.coerceAtLeast(0)
        controller.seekTo(index, 0)
        controller.prepare()
        controller.play()

        // Asynchronously resolve YouTube audio stream for the current track and switch seamlessly!
        resolveTrackStreamInBackground(track)
    }

    private fun resolveTrackStreamInBackground(track: DeezerTrack) {
        val controller = mediaController ?: return
        resolveJob?.cancel()
        resolveJob = scope.launch {
            try {
                // Instantly notify searching status in the UI
                android.widget.Toast.makeText(context, "Mencari versi full song dari YouTube...", android.widget.Toast.LENGTH_SHORT).show()
                
                val resolvedUrl = YouTubeAudioResolver.resolveStreamUrl(track.title, track.artist.name)
                if (resolvedUrl != null) {
                    val currentIndex = controller.currentMediaItemIndex
                    if (currentIndex >= 0 && currentIndex < controller.mediaItemCount) {
                        val currentItem = controller.getMediaItemAt(currentIndex)
                        // Ensure the player is still on the same track before replacing
                        if (currentItem.mediaId == track.id) {
                            val currentPosition = controller.currentPosition
                            val isCurrentlyPlaying = controller.isPlaying
                            
                            val artworkUri = track.album?.coverMedium ?: track.cover ?: ""
                            val updatedMediaItem = MediaItem.Builder()
                                .setMediaId(track.id)
                                .setUri(resolvedUrl)
                                .setMediaMetadata(
                                    MediaMetadata.Builder()
                                        .setTitle(track.title)
                                        .setArtist(track.artist.name)
                                        .setArtworkUri(android.net.Uri.parse(artworkUri))
                                        .build()
                                )
                                .build()
                            
                            controller.replaceMediaItem(currentIndex, updatedMediaItem)
                            controller.seekTo(currentIndex, currentPosition)
                            controller.prepare() // Crucial for Media3: re-prepare the player to stream the new URL!
                            if (isCurrentlyPlaying) {
                                controller.play()
                            }
                            android.widget.Toast.makeText(context, "Berhasil! Memainkan versi FULL song.", android.widget.Toast.LENGTH_SHORT).show()
                        }
                    }
                } else {
                    android.widget.Toast.makeText(context, "Gagal melacak full song. Memainkan preview 30 detik.", android.widget.Toast.LENGTH_LONG).show()
                }
            } catch (e: Exception) {
                e.printStackTrace()
                android.widget.Toast.makeText(context, "Terjadi kesalahan saat melacak streaming.", android.widget.Toast.LENGTH_SHORT).show()
            }
        }
    }

    fun play() {
        mediaController?.play()
    }

    fun pause() {
        mediaController?.pause()
    }

    fun skipToNext() {
        val controller = mediaController ?: return
        if (controller.hasNextMediaItem()) {
            controller.seekToNextMediaItem()
        } else {
            // Circle back to first track
            if (_currentQueue.value.isNotEmpty()) {
                controller.seekTo(0, 0)
                controller.play()
            }
        }
    }

    fun skipToPrevious() {
        val controller = mediaController ?: return
        if (controller.hasPreviousMediaItem()) {
            controller.seekToPreviousMediaItem()
        } else {
            controller.seekTo(0)
        }
    }

    fun seekTo(positionMs: Long) {
        mediaController?.seekTo(positionMs)
    }

    private fun startPositionTracker() {
        scope.launch {
            while (isActive) {
                if (_isPlaying.value) {
                    _currentPosition.value = mediaController?.currentPosition ?: 0L
                    _duration.value = mediaController?.duration ?: 0L
                }
                delay(1000)
            }
        }
    }

    internal fun updatePlaybackState() {
        val controller = mediaController ?: return
        _isPlaying.value = controller.isPlaying
        _duration.value = controller.duration.coerceAtLeast(0L)
        _currentPosition.value = controller.currentPosition.coerceAtLeast(0L)
        
        val currentMediaItem = controller.currentMediaItem
        if (currentMediaItem != null) {
            val trackId = currentMediaItem.mediaId
            val matchingTrack = _currentQueue.value.find { it.id == trackId }
            if (matchingTrack != null) {
                _currentTrack.value = matchingTrack
            }
        }
    }

    private inner class PlayerListener : Player.Listener {
        override fun onIsPlayingChanged(isPlaying: Boolean) {
            _isPlaying.value = isPlaying
            updatePlaybackState()
        }

        override fun onPlaybackStateChanged(playbackState: Int) {
            updatePlaybackState()
        }

        override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
            updatePlaybackState()
            
            // Trigger seamless background stream resolution for the transitioned track
            if (mediaItem != null) {
                val trackId = mediaItem.mediaId
                val matchingTrack = _currentQueue.value.find { it.id == trackId }
                if (matchingTrack != null) {
                    val currentUri = mediaItem.localConfiguration?.uri?.toString() ?: ""
                    // Only fetch if it hasn't been fetched (i.e. is still pointing to 30s Deezer preview URL)
                    if (currentUri == matchingTrack.preview) {
                        resolveTrackStreamInBackground(matchingTrack)
                    }
                }
            }
        }
    }
}
