package com.example.utils

import android.net.Uri
import android.util.Log
import com.example.BuildConfig
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.IOException
import java.util.concurrent.TimeUnit

object YouTubeAudioResolver {
    private const val TAG = "YouTubeAudioResolver"
    
    private val client = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .build()

    // Public Piped API instances to search and retrieve direct stream URLs
    private val PIPED_INSTANCES = listOf(
        "https://pipedapi.kavin.rocks",
        "https://pipedapi.adminforge.de",
        "https://pipedapi.syncord.xyz",
        "https://pipedapi.mha.fi",
        "https://cf.pipedapi.kavin.rocks",
        "https://pipedapi.suyu.sh"
    )

    /**
     * Resolves a full audio stream URL for a given song title and artist.
     * Guaranteed to run in Dispatchers.IO.
     * Prioritizes official YouTube API Search, then falls back to Piped search.
     * Prioritizes Cobalt API stream proxies to skip YouTube's client IP-bound 403 restrictions.
     */
    suspend fun resolveStreamUrl(title: String, artist: String): String? = withContext(Dispatchers.IO) {
        val query = "$title $artist"
        
        val apiKey = try {
            BuildConfig.YOUTUBE_API_KEY
        } catch (e: Exception) {
            null
        }

        var videoId: String? = null

        // 1. If non-empty official YouTube API key is available, use it first for reliable results
        if (!apiKey.isNullOrBlank() && 
            apiKey != "AIzaSyD6duWsiFsiDXizi5PpNRyqPVTEHGil2so_PLACEHOLDER" && 
            apiKey != "AIzaSyD6duWsiFsiDXizi5PpNRyqPVTEHGil2so"
        ) {
            Log.d(TAG, "Attempting search via official YouTube API for query: '$query'")
            videoId = searchYouTubeVideo(query, apiKey)
        }

        // 2. Fallback to decentralized Piped search if API key was empty, failed, or was a placeholder
        if (videoId == null) {
            Log.d(TAG, "Official search unavailable or failed; executing keyless Piped search for query: '$query'")
            videoId = searchYouTubeVideoViaPiped(query)
        }

        if (videoId == null) {
            Log.e(TAG, "Could not find any YouTube video ID for track: $title by $artist")
            return@withContext null
        }
        
        Log.d(TAG, "Target YouTube video ID resolved: $videoId. Resolving playable stream links...")

        // 3. Resolve the video ID to a streaming stream link.
        // First, prefer Cobalt API, which proxies direct YouTube streams to prevent client IP - bound 403 error.
        val streamUrl = getStreamFromCobalt(videoId)
        if (streamUrl != null) {
            return@withContext streamUrl
        }

        // Fallback to Piped stream URL
        Log.w(TAG, "Cobalt resolution failed; fallback to Piped streams (Warning: might fail due to IP bind restrictions)...")
        return@withContext getStreamFromPiped(videoId)
    }

    private fun getStreamFromCobalt(videoId: String): String? {
        val videoUrl = "https://www.youtube.com/watch?v=$videoId"
        
        // Main public Cobalt instances to try in sequence (v10 root POST)
        val cobaltInstances = listOf(
            "https://api.cobalt.tools/",
            "https://cobalt.api.ryboflaven.com/",
            "https://cobalt.lonami.dev/",
            "https://cobalt.pervokursnik.by/",
            "https://api.cobalt.lunes.host/"
        )
        
        for (instance in cobaltInstances) {
            val jsonRequestBody = Gson().toJson(CobaltRequest(url = videoUrl))
            val requestBody = jsonRequestBody.toRequestBody("application/json; charset=utf-8".toMediaTypeOrNull())
            
            val request = Request.Builder()
                .url(instance)
                .addHeader("Accept", "application/json")
                .addHeader("Content-Type", "application/json")
                .post(requestBody)
                .build()
                
            try {
                client.newCall(request).execute().use { response ->
                    val bodyString = response.body?.string() ?: ""
                    if (response.isSuccessful) {
                        if (bodyString.isNotBlank()) {
                            val cobaltResponse = Gson().fromJson(bodyString, CobaltResponse::class.java)
                            
                            val status = cobaltResponse.status
                            val url = cobaltResponse.url
                            
                            if ((status == "stream" || status == "redirect" || status == "success" || status == "tunnel") && !url.isNullOrBlank()) {
                                Log.d(TAG, "Successfully resolved player stream URL via Cobalt instance $instance: $url")
                                return url
                            } else {
                                Log.w(TAG, "Cobalt instance $instance returned non-stream status: $status. Body: $bodyString")
                            }
                        }
                    } else {
                        Log.w(TAG, "Cobalt instance $instance failed with HTTP status ${response.code}. Body: $bodyString")
                    }
                }
            } catch (e: Exception) {
                Log.w(TAG, "Failed resolving stream via Cobalt instance $instance: ${e.message}")
            }
        }
        return null
    }

    private fun searchYouTubeVideoViaPiped(query: String): String? {
        val encodedQuery = Uri.encode(query)
        for (instance in PIPED_INSTANCES) {
            val url = "$instance/search?q=$encodedQuery&filter=all"
            val request = Request.Builder()
                .url(url)
                .build()

            try {
                client.newCall(request).execute().use { response ->
                    if (response.isSuccessful) {
                        val bodyString = response.body?.string() ?: return@use
                        val searchResponse = Gson().fromJson(bodyString, PipedInstanceSearchResponse::class.java)
                        
                        // Look for a stream/video type item containing a watch URL
                        val matchedItem = searchResponse.items?.firstOrNull { item ->
                            val isStream = item.type == "stream" || item.type == "video"
                            val hasWatchUrl = item.url?.contains("/watch?v=") == true
                            isStream && hasWatchUrl
                        }
                        
                        if (matchedItem != null) {
                            val videoId = matchedItem.url?.substringAfter("/watch?v=")?.substringBefore("&")
                            if (!videoId.isNullOrBlank()) {
                                Log.d(TAG, "Successfully found videoId '$videoId' via $instance")
                                return videoId
                            }
                        }
                    } else {
                        Log.w(TAG, "Search on Piped instance $instance failed with status code ${response.code}")
                    }
                }
            } catch (e: Exception) {
                Log.w(TAG, "Failed searching via Piped instance $instance: ${e.message}")
            }
        }
        return null
    }

    private fun searchYouTubeVideo(query: String, apiKey: String): String? {
        val encodedQuery = Uri.encode(query)
        val url = "https://www.googleapis.com/youtube/v3/search?part=snippet&q=$encodedQuery&type=video&maxResults=1&key=$apiKey"

        val request = Request.Builder()
            .url(url)
            .build()

        try {
            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    Log.e(TAG, "YouTube API search failed or returned status ${response.code}")
                    return null
                }

                val bodyString = response.body?.string() ?: return null
                val searchResponse = Gson().fromJson(bodyString, YouTubeSearchResponse::class.java)
                return searchResponse.items?.firstOrNull()?.id?.videoId
            }
        } catch (e: Exception) {
            Log.e(TAG, "Exception during YouTube API search for query: $query", e)
        }
        return null
    }

    private fun getStreamFromPiped(videoId: String): String? {
        for (instance in PIPED_INSTANCES) {
            val url = "$instance/streams/$videoId"
            val request = Request.Builder()
                .url(url)
                .build()

            try {
                client.newCall(request).execute().use { response ->
                    if (response.isSuccessful) {
                        val bodyString = response.body?.string() ?: return@use
                        val streamResponse = Gson().fromJson(bodyString, PipedStreamResponse::class.java)
                        
                        // Pick the best quality audio stream, prioritizing higher bitrate or any available valid audio stream
                        val audioStreamUrl = streamResponse.audioStreams
                            ?.filter { !it.url.isNullOrBlank() }
                            ?.maxByOrNull { it.bitrate ?: 0 }
                            ?.url
                        
                        if (audioStreamUrl != null) {
                            Log.d(TAG, "Successfully resolved audiostream URL from $instance")
                            return audioStreamUrl
                        }
                    } else {
                        Log.w(TAG, "Piped stream request to $instance failed with code ${response.code}")
                    }
                }
            } catch (e: Exception) {
                Log.w(TAG, "Failed to get audio stream from Piped instance $instance: ${e.message}")
            }
        }
        Log.e(TAG, "Failed to resolve stream URL from all known Piped instances")
        return null
    }
}

// Gson Data Models for parsing
private data class CobaltRequest(
    val url: String,
    val downloadMode: String = "audio"
)
private data class CobaltResponse(
    val status: String?,
    val url: String?,
    val text: String?
)

private data class PipedInstanceSearchResponse(val items: List<PipedInstanceSearchItem>?)
private data class PipedInstanceSearchItem(val url: String?, val type: String?, val title: String?)

private data class YouTubeSearchResponse(val items: List<YouTubeSearchItem>?)
private data class YouTubeSearchItem(val id: YouTubeVideoId?)
private data class YouTubeVideoId(val videoId: String?)

private data class PipedStreamResponse(val audioStreams: List<PipedAudioStream>?)
private data class PipedAudioStream(val url: String?, val bitrate: Int?)
