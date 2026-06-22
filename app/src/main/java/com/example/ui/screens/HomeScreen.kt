package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.AutoAwesome
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
import com.example.data.local.RecentlyPlayedEntity
import com.example.data.remote.*
import com.example.ui.components.*
import com.example.ui.theme.DarkGray
import com.example.ui.theme.LightGray
import com.example.ui.theme.Shapes
import com.example.ui.theme.SpotifyGreen
import com.example.ui.viewmodel.HomeViewModel
import com.example.utils.Constants
import java.util.Calendar

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: HomeViewModel,
    onArtistClick: (String) -> Unit,
    onAlbumClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val chartTracksState by viewModel.chartTracksState.collectAsState()
    val chartAlbumsState by viewModel.chartAlbumsState.collectAsState()
    val chartArtistsState by viewModel.chartArtistsState.collectAsState()
    val radiosState by viewModel.radiosState.collectAsState()
    val recentlyPlayed by viewModel.recentlyPlayedState.collectAsState()
    val isRefreshing by viewModel.isRefreshing.collectAsState()

    val scrollState = rememberScrollState()

    // Dynamically calculate greeting based on hour of day
    val greeting = remember {
        val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        when (hour) {
            in 0..11 -> "Good Morning"
            in 12..16 -> "Good Afternoon"
            else -> "Good Evening"
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(bottom = 90.dp) // Leave space for persistent miniplayer
        ) {
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 20.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    AsyncImage(
                        model = Constants.DEFAULT_USER_AVATAR,
                        contentDescription = "Profile character avatar",
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape),
                        contentScale = ContentScale.Crop
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = greeting,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }
                
                IconButton(
                    onClick = { viewModel.refresh() }
                ) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "Refresh content",
                        tint = SpotifyGreen
                    )
                }
            }

            // Featured Hero Banner
            if (chartTracksState is NetworkResult.Success) {
                val songs = (chartTracksState as NetworkResult.Success<List<DeezerTrack>>).data
                if (songs.isNotEmpty()) {
                    FeaturedHeroBanner(
                        track = songs.first(),
                        onPlayClick = { viewModel.playTrack(songs.first(), songs) }
                    )
                }
            }

            // Recently Played Section
            if (recentlyPlayed.isNotEmpty()) {
                SectionHeader(title = "Recently Played")
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(recentlyPlayed) { item ->
                        RecentlyPlayedItemCard(
                            entity = item,
                            onClick = {
                                // Re-convert local item to raw track and play
                                val mockTrack = DeezerTrack(
                                    id = item.trackId,
                                    title = item.title,
                                    duration = item.duration,
                                    preview = item.previewUrl,
                                    artist = DeezerArtist(
                                        id = item.artistId,
                                        name = item.artistName,
                                        picture = item.artistPictureUrl,
                                        pictureSmall = null,
                                        pictureMedium = item.artistPictureUrl,
                                        pictureBig = null,
                                        nbFan = null,
                                        nbAlbum = null
                                    ),
                                    album = null,
                                    cover = item.coverUrl
                                )
                                viewModel.playTrack(mockTrack)
                            }
                        )
                    }
                }
            }

            // Popular Songs Section
            when (chartTracksState) {
                is NetworkResult.Loading -> {
                    SectionHeader(title = "Popular Songs")
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(modifier = Modifier.fillMaxWidth().height(60.dp).shimmerEffect()) {}
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(modifier = Modifier.fillMaxWidth().height(60.dp).shimmerEffect()) {}
                    }
                }
                is NetworkResult.Error -> {
                    ErrorState(
                        message = (chartTracksState as NetworkResult.Error).message ?: "Unable to fetch charts",
                        onRetry = { viewModel.refresh() }
                    )
                }
                is NetworkResult.Success -> {
                    val tracks = (chartTracksState as NetworkResult.Success<List<DeezerTrack>>).data
                    HorizontalScrollSection(
                        title = "Popular Songs",
                        items = tracks.take(15)
                    ) { track ->
                        Card(
                            modifier = Modifier
                                .width(150.dp)
                                .clickable { viewModel.playTrack(track, tracks) }
                                .padding(4.dp),
                            colors = CardDefaults.cardColors(containerColor = DarkGray)
                        ) {
                            Column(modifier = Modifier.padding(8.dp)) {
                                AsyncImage(
                                    model = track.album?.coverMedium ?: track.cover ?: Constants.TRACK_IMAGE_FALLBACK,
                                    contentDescription = "Cover",
                                    modifier = Modifier
                                        .size(134.dp)
                                        .clip(Shapes.large),
                                    contentScale = ContentScale.Crop
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = track.title,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = Color.White,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = track.artist.name,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color.Gray,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }
                }
            }

            // Popular Albums Section
            when (chartAlbumsState) {
                is NetworkResult.Loading -> {
                    HorizontalScrollSection(
                        title = "Featured Albums",
                        items = listOf(1, 2, 3, 4)
                    ) {
                        Box(modifier = Modifier.size(118.dp).shimmerEffect())
                    }
                }
                is NetworkResult.Success -> {
                    val albums = (chartAlbumsState as NetworkResult.Success<List<DeezerAlbum>>).data
                    HorizontalScrollSection(
                        title = "Featured Albums",
                        items = albums
                    ) { album ->
                        AlbumCard(
                            album = album,
                            onAlbumClick = { onAlbumClick(album.id) }
                        )
                    }
                }
                else -> {}
            }

            // Popular Artists Section
            when (chartArtistsState) {
                is NetworkResult.Loading -> {
                    HorizontalScrollSection(
                        title = "Trending Artists",
                        items = listOf(1, 2, 3, 4)
                    ) {
                        Box(modifier = Modifier.size(90.dp).clip(CircleShape).shimmerEffect())
                    }
                }
                is NetworkResult.Success -> {
                    val artists = (chartArtistsState as NetworkResult.Success<List<DeezerArtist>>).data
                    HorizontalScrollSection(
                        title = "Trending Artists",
                        items = artists
                    ) { artist ->
                        ArtistCard(
                            artist = artist,
                            onArtistClick = { onArtistClick(artist.id) }
                        )
                    }
                }
                else -> {}
            }

            // New Releases Section (using Deezer API's radios as mood categories)
            when (radiosState) {
                is NetworkResult.Success -> {
                    val radios = (radiosState as NetworkResult.Success<List<DeezerRadio>>).data
                    HorizontalScrollSection(
                        title = "New Releases & Radios",
                        items = radios
                    ) { radio ->
                        Column(
                            modifier = Modifier
                                .width(120.dp)
                                .clickable { }
                                .padding(8.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            AsyncImage(
                                model = radio.pictureMedium ?: Constants.GENRE_IMAGE_FALLBACK,
                                contentDescription = radio.title,
                                modifier = Modifier
                                    .size(100.dp)
                                    .clip(Shapes.large),
                                contentScale = ContentScale.Crop
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = radio.title,
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.White,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
                else -> {}
            }
        }
    }
}

@Composable
fun FeaturedHeroBanner(
    track: DeezerTrack,
    onPlayClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(200.dp)
            .padding(16.dp)
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(LightGray, DarkGray)
                ),
                shape = Shapes.large
            )
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AsyncImage(
                model = track.album?.coverMedium ?: track.cover ?: Constants.TRACK_IMAGE_FALLBACK,
                contentDescription = null,
                modifier = Modifier
                    .size(120.dp)
                    .clip(Shapes.large),
                contentScale = ContentScale.Crop
            )
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.AutoAwesome,
                        contentDescription = null,
                        tint = SpotifyGreen,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "NEW HOT TRACK",
                        style = MaterialTheme.typography.labelSmall,
                        color = SpotifyGreen,
                        fontWeight = FontWeight.Bold
                    )
                }
                
                Spacer(modifier = Modifier.height(4.dp))
                
                Text(
                    text = track.title,
                    style = MaterialTheme.typography.titleLarge,
                    color = Color.White,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                
                Text(
                    text = track.artist.name,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.Gray,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                
                Spacer(modifier = Modifier.height(12.dp))
                
                Button(
                    onClick = onPlayClick,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = SpotifyGreen,
                        contentColor = Color.Black
                    ),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
                    modifier = Modifier.height(36.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = "Play button",
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Play Now", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun RecentlyPlayedItemCard(
    entity: RecentlyPlayedEntity,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .width(110.dp)
            .clickable { onClick() }
            .padding(6.dp)
    ) {
        AsyncImage(
            model = entity.coverUrl,
            contentDescription = "Cover art",
            modifier = Modifier
                .size(98.dp)
                .clip(Shapes.large),
            contentScale = ContentScale.Crop
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = entity.title,
            style = MaterialTheme.typography.bodyMedium,
            color = Color.White,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        Text(
            text = entity.artistName,
            style = MaterialTheme.typography.bodySmall,
            color = Color.Gray,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}
