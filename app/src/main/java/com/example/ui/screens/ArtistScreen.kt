package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.data.remote.DeezerArtist
import com.example.data.remote.DeezerTrack
import com.example.data.remote.NetworkResult
import com.example.ui.components.ErrorState
import com.example.ui.components.LoadingIndicator
import com.example.ui.components.TrackItem
import com.example.ui.theme.DarkGray
import com.example.ui.theme.Shapes
import com.example.ui.theme.SpotifyGreen
import com.example.ui.viewmodel.ArtistViewModel
import com.example.utils.Constants
import java.text.NumberFormat
import java.util.Locale

@Composable
fun ArtistScreen(
    artistId: String,
    viewModel: ArtistViewModel,
    onBackClick: () -> Unit,
    onArtistClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val artistDetailState by viewModel.artistDetailState.collectAsState()
    val artistTopTracksState by viewModel.artistTopTracksState.collectAsState()

    var isFollowing by remember { mutableStateOf(false) }

    LaunchedEffect(artistId) {
        viewModel.loadArtist(artistId)
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        when (artistDetailState) {
            is NetworkResult.Loading -> {
                LoadingIndicator()
            }
            is NetworkResult.Error -> {
                ErrorState(
                    message = (artistDetailState as NetworkResult.Error).message ?: "Unable to load artist profile.",
                    onRetry = { viewModel.loadArtist(artistId) }
                )
            }
            is NetworkResult.Success -> {
                val artist = (artistDetailState as NetworkResult.Success<DeezerArtist>).data

                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(bottom = 90.dp) // space for miniplayer
                ) {
                    // Jumbo Artist Banner Header
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(250.dp)
                        ) {
                            AsyncImage(
                                model = artist.pictureBig ?: artist.pictureMedium ?: Constants.ARTIST_IMAGE_FALLBACK,
                                contentDescription = "Artist Banner",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                            
                            // Dim bottom gradient overlay
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(
                                        brush = Brush.verticalGradient(
                                            colors = listOf(Color.Transparent, Color.Black),
                                            startY = 200f
                                        )
                                    )
                            )

                            // Title, Stats, and Action Buttons inside Banner
                            Column(
                                modifier = Modifier
                                    .align(Alignment.BottomStart)
                                    .padding(16.dp)
                            ) {
                                Text(
                                    text = artist.name,
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                
                                val fansCountFormatted = remember(artist.nbFan) {
                                    val count = artist.nbFan ?: 1450203
                                    NumberFormat.getNumberInstance(Locale.US).format(count)
                                }
                                
                                Text(
                                    text = "$fansCountFormatted fans",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = Color.LightGray
                                )
                            }

                            // Circular back button
                            IconButton(
                                onClick = onBackClick,
                                modifier = Modifier
                                    .statusBarsPadding()
                                    .padding(8.dp)
                                    .background(Color.Black.copy(alpha = 0.5f), shape = CircleShape)
                                    .size(40.dp)
                                    .align(Alignment.TopStart)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ArrowBack,
                                    contentDescription = "Go back",
                                    tint = Color.White
                                )
                            }
                        }
                    }

                    // Artist controls (Play and Follow buttons)
                    item {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Button(
                                onClick = { isFollowing = !isFollowing },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (isFollowing) Color.Transparent else SpotifyGreen,
                                    contentColor = if (isFollowing) Color.White else Color.Black
                                ),
                                border = if (isFollowing) BorderStroke(1.dp, Color.White) else null,
                                shape = RoundedCornerShape(16.dp)
                            ) {
                                Text(
                                    text = if (isFollowing) "Following" else "Follow",
                                    style = MaterialTheme.typography.labelLarge,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            if (artistTopTracksState is NetworkResult.Success) {
                                val topSongs = (artistTopTracksState as NetworkResult.Success<List<DeezerTrack>>).data
                                if (topSongs.isNotEmpty()) {
                                    FloatingActionButton(
                                        onClick = { viewModel.playTrack(topSongs.first(), topSongs) },
                                        containerColor = SpotifyGreen,
                                        contentColor = Color.Black,
                                        shape = CircleShape,
                                        modifier = Modifier.size(52.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.PlayArrow,
                                            contentDescription = "Shuffle artist tracks"
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // Top Songs Segment Label
                    item {
                        Text(
                            text = "Popular Songs",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                        )
                    }

                    // Top 5 tracks list
                    when (artistTopTracksState) {
                        is NetworkResult.Loading -> {
                            item { LoadingIndicator(isFullScreen = false) }
                        }
                        is NetworkResult.Error -> {
                            item {
                                Text(
                                    text = "Unable to load popular tracks.",
                                    color = Color.Gray,
                                    modifier = Modifier.padding(16.dp)
                                )
                            }
                        }
                        is NetworkResult.Success -> {
                            val topSongs = (artistTopTracksState as NetworkResult.Success<List<DeezerTrack>>).data
                            itemsIndexed(topSongs.take(5)) { index, song ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { viewModel.playTrack(song, topSongs) }
                                        .padding(vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = (index + 1).toString(),
                                        style = MaterialTheme.typography.bodyLarge,
                                        fontWeight = FontWeight.Bold,
                                        color = SpotifyGreen,
                                        modifier = Modifier
                                            .width(42.dp)
                                            .padding(start = 16.dp),
                                        onTextLayout = {}
                                    )
                                    
                                    // Render TrackItem internally to keep indentation clean
                                    TrackItem(
                                        track = song,
                                        onTrackClick = { viewModel.playTrack(song, topSongs) },
                                        modifier = Modifier.weight(1f)
                                    )
                                }
                            }
                        }
                    }

                    // Discography and Related section
                    item {
                        Column(
                            modifier = Modifier.padding(16.dp)
                        ) {
                            Text(
                                    text = "Artist Details",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = DarkGray)
                            ) {
                                Column(
                                    modifier = Modifier.padding(16.dp)
                                ) {
                                    Text(
                                        text = "About the Artist",
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = SpotifyGreen
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "${artist.name} is a highly accomplished performing creator with a global catalog on Deezer streaming platforms, reaching multiple chart lists.",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = Color.Gray
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
