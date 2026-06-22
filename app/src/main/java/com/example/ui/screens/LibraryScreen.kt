package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
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
import com.example.data.local.LikedSongEntity
import com.example.ui.theme.DarkGray
import com.example.ui.theme.Shapes
import com.example.ui.theme.SpotifyGreen
import com.example.ui.viewmodel.LibraryViewModel
import com.example.utils.Constants

@Composable
fun LibraryScreen(
    viewModel: LibraryViewModel,
    onArtistClick: (String) -> Unit,
    onAlbumClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val likedSongs by viewModel.likedSongs.collectAsState()
    val selectedFilter by viewModel.selectedFilter.collectAsState()

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 90.dp) // persistent player space
        ) {
            // Heading Label
            Text(
                text = "Your Library",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 20.dp)
            )

            // Horizontal Filters Row
            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(viewModel.filters) { filter ->
                    val isSelected = selectedFilter == filter
                    FilterChipWidget(
                        text = filter,
                        isSelected = isSelected,
                        onClick = { viewModel.setFilter(filter) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Main Listing
            LazyColumn(
                modifier = Modifier.weight(1f)
            ) {
                // If "All" or "Liked Songs" is selected, render our customized Liked Songs banner list
                if (selectedFilter == "All" || selectedFilter == "Liked Songs") {
                    item {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(130.dp)
                                .padding(horizontal = 16.dp, vertical = 8.dp)
                                .clickable { viewModel.setFilter("Liked Songs") },
                            shape = Shapes.large
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(
                                        brush = Brush.linearGradient(
                                            colors = listOf(Color(0xFF450DFC), Color(0xFFC4E0E5))
                                        )
                                    )
                                    .padding(16.dp)
                            ) {
                                Column(
                                    modifier = Modifier.align(Alignment.BottomStart)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Favorite,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(24.dp)
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = "Liked Songs",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                    Text(
                                        text = "${likedSongs.size} songs",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = Color.White.copy(alpha = 0.8f)
                                    )
                                }

                                if (likedSongs.isNotEmpty()) {
                                    FloatingActionButton(
                                        onClick = { viewModel.playLikedSong(likedSongs.first(), likedSongs) },
                                        containerColor = SpotifyGreen,
                                        contentColor = Color.Black,
                                        shape = CircleShape,
                                        modifier = Modifier
                                            .size(48.dp)
                                            .align(Alignment.BottomEnd)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.PlayArrow,
                                            contentDescription = "Play all liked songs"
                                        )
                                    }
                                }
                            }
                        }
                    }

                    if (selectedFilter == "Liked Songs") {
                        if (likedSongs.isEmpty()) {
                            item {
                                EmptyLibraryState(message = "Your liked songs will appear here.")
                            }
                        } else {
                            items(likedSongs) { song ->
                                LikedSongRowItem(
                                    song = song,
                                    onClick = { viewModel.playLikedSong(song, likedSongs) }
                                )
                            }
                        }
                    }
                }

                // If playlists selected or ALL, render mock playlists
                if (selectedFilter == "All" || selectedFilter == "Playlists") {
                    item {
                        Text(
                            text = "Playlists",
                            style = MaterialTheme.typography.titleSmall,
                            color = Color.White,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                        )
                    }

                    item {
                        OfflinePlaylistItem(
                            title = "Driving Chill Hits",
                            songCount = 12,
                            imageUrl = "https://images.unsplash.com/photo-1501386761578-eac5c94b800a?auto=format&fit=crop&w=150&q=80"
                        )
                    }
                    item {
                        OfflinePlaylistItem(
                            title = "Late Night Study Beats",
                            songCount = 28,
                            imageUrl = "https://images.unsplash.com/photo-1470225620780-dba8ba36b745?auto=format&fit=crop&w=150&q=80"
                        )
                    }
                }

                // If artists selected or ALL, render mock followed artists
                if (selectedFilter == "All" || selectedFilter == "Artists") {
                    item {
                        Text(
                            text = "Artists",
                            style = MaterialTheme.typography.titleSmall,
                            color = Color.White,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                        )
                    }

                    item {
                        FollowedArtistItem(
                            name = "The Weeknd",
                            followers = "78M followers",
                            imageUrl = "https://images.unsplash.com/photo-1549834185-bd9f078a5dfe?auto=format&fit=crop&w=150&q=80",
                            onClick = { onArtistClick("1190512") } // Mock Weeknd ID or redirect to standard profile
                        )
                    }

                    item {
                        FollowedArtistItem(
                            name = "Daft Punk",
                            followers = "15M followers",
                            imageUrl = "https://images.unsplash.com/photo-1470225620780-dba8ba36b745?auto=format&fit=crop&w=150&q=80",
                            onClick = { onArtistClick("27") } // Mock Daft Punk ID
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun FilterChipWidget(
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        onClick = onClick,
        modifier = modifier,
        shape = CircleShape,
        color = if (isSelected) SpotifyGreen else DarkGray,
        contentColor = if (isSelected) Color.Black else Color.White
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
        )
    }
}

@Composable
fun LikedSongRowItem(
    song: LikedSongEntity,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        AsyncImage(
            model = song.coverUrl,
            contentDescription = null,
            modifier = Modifier
                .size(48.dp)
                .clip(Shapes.medium),
            contentScale = ContentScale.Crop
        )
        
        Spacer(modifier = Modifier.width(16.dp))
        
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = song.title,
                style = MaterialTheme.typography.bodyLarge,
                color = Color.White,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = song.artistName,
                style = MaterialTheme.typography.bodyMedium,
                color = Color.Gray,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
fun OfflinePlaylistItem(
    title: String,
    songCount: Int,
    imageUrl: String,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        AsyncImage(
            model = imageUrl,
            contentDescription = null,
            modifier = Modifier
                .size(48.dp)
                .clip(Shapes.medium),
            contentScale = ContentScale.Crop
        )
        
        Spacer(modifier = Modifier.width(16.dp))
        
        Column {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                color = Color.White
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = "Playlist • $songCount songs",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.Gray
            )
        }
    }
}

@Composable
fun FollowedArtistItem(
    name: String,
    followers: String,
    imageUrl: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        AsyncImage(
            model = imageUrl,
            contentDescription = null,
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape),
            contentScale = ContentScale.Crop
        )
        
        Spacer(modifier = Modifier.width(16.dp))
        
        Column {
            Text(
                text = name,
                style = MaterialTheme.typography.bodyLarge,
                color = Color.White
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = followers,
                style = MaterialTheme.typography.bodyMedium,
                color = Color.Gray
            )
        }
    }
}

@Composable
fun EmptyLibraryState(
    message: String,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(32.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = message,
            style = MaterialTheme.typography.bodyMedium,
            color = Color.Gray
        )
    }
}
