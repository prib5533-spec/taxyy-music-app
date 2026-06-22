package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.DownloadDone
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.outlined.ArrowDownward
import androidx.compose.material.icons.outlined.Download
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.data.remote.DeezerAlbum
import com.example.data.remote.DeezerArtist
import com.example.data.remote.DeezerTrack
import com.example.data.remote.NetworkResult
import com.example.ui.components.ErrorState
import com.example.ui.components.LoadingIndicator
import com.example.ui.components.TrackItem
import com.example.ui.theme.Shapes
import com.example.ui.theme.SpotifyGreen
import com.example.ui.viewmodel.AlbumViewModel
import com.example.utils.Constants

@Composable
fun AlbumScreen(
    albumId: String,
    viewModel: AlbumViewModel,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val albumDetailState by viewModel.albumDetailState.collectAsState()

    var isDownloaded by remember { mutableStateOf(false) }

    LaunchedEffect(albumId) {
        viewModel.loadAlbum(albumId)
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        when (albumDetailState) {
            is NetworkResult.Loading -> {
                LoadingIndicator()
            }
            is NetworkResult.Error -> {
                ErrorState(
                    message = (albumDetailState as NetworkResult.Error).message ?: "Unable to fetch album tracks.",
                    onRetry = { viewModel.loadAlbum(albumId) }
                )
            }
            is NetworkResult.Success -> {
                val album = (albumDetailState as NetworkResult.Success<DeezerAlbum>).data
                val rawTracks = album.tracks?.data ?: emptyList()

                // Inject parent album data into nested tracks so clicking track from album retains cover art
                val tracks = remember(album, rawTracks) {
                    rawTracks.map { track ->
                        track.copy(
                            album = album,
                            cover = album.coverMedium ?: track.cover ?: ""
                        )
                    }
                }

                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(bottom = 90.dp) // persistent miniplayer footprint
                ) {
                    // Header Cover Detail
                    item {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .statusBarsPadding()
                                .padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            // Top navig bar row
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.Start
                            ) {
                                IconButton(
                                    onClick = onBackClick,
                                    modifier = Modifier
                                        .background(Color.DarkGray.copy(alpha = 0.5f), shape = CircleShape)
                                        .size(36.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.ArrowBack,
                                        contentDescription = "Back",
                                        tint = Color.White
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            // Cover Album square
                            AsyncImage(
                                model = album.coverMedium ?: Constants.TRACK_IMAGE_FALLBACK,
                                contentDescription = "Album cover art",
                                modifier = Modifier
                                    .size(200.dp)
                                    .clip(Shapes.large),
                                contentScale = ContentScale.Crop
                            )

                            Spacer(modifier = Modifier.height(20.dp))

                            // Metadata details
                            Text(
                                text = album.title,
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis
                            )

                            Spacer(modifier = Modifier.height(6.dp))

                            Text(
                                text = "Album by ${album.artist?.name ?: "Unknown Artist"}",
                                style = MaterialTheme.typography.bodyLarge,
                                color = Color.Gray,
                                fontWeight = FontWeight.Medium
                            )

                            Spacer(modifier = Modifier.height(4.dp))

                            Text(
                                text = "${album.releaseDate ?: "2024"} • ${tracks.size} tracks",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.Gray
                            )
                        }
                    }

                    // Action elements (Play, Shuffle, Download All)
                    item {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                IconButton(onClick = { isDownloaded = !isDownloaded }) {
                                    Icon(
                                        imageVector = if (isDownloaded) Icons.Filled.DownloadDone else Icons.Outlined.Download,
                                        contentDescription = "Download album",
                                        tint = if (isDownloaded) SpotifyGreen else Color.White,
                                        modifier = Modifier.size(28.dp)
                                    )
                                }

                                IconButton(onClick = {}) {
                                    Icon(
                                        imageVector = Icons.Outlined.FavoriteBorder,
                                        contentDescription = "Like album",
                                        tint = Color.White,
                                        modifier = Modifier.size(28.dp)
                                    )
                                }
                            }

                            if (tracks.isNotEmpty()) {
                                FloatingActionButton(
                                    onClick = { viewModel.playTrack(tracks.first(), tracks) },
                                    containerColor = SpotifyGreen,
                                    contentColor = Color.Black,
                                    shape = CircleShape,
                                    modifier = Modifier.size(52.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.PlayArrow,
                                        contentDescription = "Play Album"
                                    )
                                }
                            }
                        }
                    }

                    // Tracks rows
                    itemsIndexed(tracks) { index, track ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { viewModel.playTrack(track, tracks) }
                                .padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = (index + 1).toString(),
                                style = MaterialTheme.typography.bodyLarge,
                                color = SpotifyGreen,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier
                                    .width(42.dp)
                                    .padding(start = 16.dp),
                                onTextLayout = {}
                            )

                            // Render TrackItem internally for nesting cover art properly etc.
                            TrackItem(
                                track = track,
                                onTrackClick = { viewModel.playTrack(track, tracks) },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }
        }
    }
}
