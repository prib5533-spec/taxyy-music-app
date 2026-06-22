package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
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
import com.example.data.remote.DeezerGenre
import com.example.data.remote.DeezerTrack
import com.example.data.remote.NetworkResult
import com.example.ui.components.*
import com.example.ui.theme.DarkGray
import com.example.ui.theme.Shapes
import com.example.ui.theme.SpotifyGreen
import com.example.ui.viewmodel.SearchViewModel
import com.example.utils.Constants

@Composable
fun SearchScreen(
    viewModel: SearchViewModel,
    onArtistClick: (String) -> Unit,
    onAlbumClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val searchQuery by viewModel.searchQuery.collectAsState()
    val searchResultState by viewModel.searchResultState.collectAsState()
    val genresState by viewModel.genresState.collectAsState()

    // Predefined mood colors for the grid
    val categoryColors = remember {
        listOf(
            Color(0xFFE1306C), Color(0xFFC13584), Color(0xFF833AB4), Color(0xFF405DE6),
            Color(0xFF5851DB), Color(0xFF1DB954), Color(0xFFFF6F00), Color(0xFFFF3D00),
            Color(0xFFD500F9), Color(0xFF00E5FF), Color(0xFF00E676), Color(0xFF00B0FF),
            Color(0xFF2979FF), Color(0xFF651FFF), Color(0xFFFF1744), Color(0xFF76FF03)
        )
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 90.dp) // Space for persistent MiniPlayer
        ) {
            // Heading Title
            Text(
                text = "Search",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 20.dp)
            )

            // Search Bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { viewModel.onSearchQueryChanged(it) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .clip(Shapes.large),
                placeholder = {
                    Text(
                        text = "What do you want to listen to?",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.Gray
                    )
                },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Search icon",
                        tint = Color.Gray
                    )
                },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { viewModel.onSearchQueryChanged("") }) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Clear search",
                                tint = Color.Gray
                            )
                        }
                    }
                },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = SpotifyGreen,
                    unfocusedBorderColor = Color.Transparent,
                    focusedContainerColor = DarkGray,
                    unfocusedContainerColor = DarkGray,
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White
                ),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Scrollable Content
            if (searchQuery.trim().isEmpty()) {
                // Genres / Moods categories
                LazyColumn(
                    modifier = Modifier.weight(1f)
                ) {
                    // Trending Search Chips
                    item {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = "Trending Searches",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.SemiBold,
                                color = Color.White
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            
                            // Horizontal flow equivalent using Rows to prevent wrapping errors
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                viewModel.trendingSearches.take(3).forEach { trend ->
                                    TrendingChip(
                                        text = trend,
                                        onClick = { viewModel.onSearchQueryChanged(trend) }
                                    )
                                }
                            }
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                viewModel.trendingSearches.drop(3).forEach { trend ->
                                    TrendingChip(
                                        text = trend,
                                        onClick = { viewModel.onSearchQueryChanged(trend) }
                                    )
                                }
                            }
                        }
                    }

                    item {
                        Text(
                            text = "Browse All Categories",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold,
                            color = Color.White,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                        )
                    }

                    // Grid Categories
                    when (genresState) {
                        is NetworkResult.Loading -> {
                            item { LoadingIndicator(isFullScreen = false) }
                        }
                        is NetworkResult.Error -> {
                            item {
                                Text(
                                    text = "Unable to load categories",
                                    color = Color.Gray,
                                    modifier = Modifier.padding(16.dp)
                                )
                            }
                        }
                        is NetworkResult.Success -> {
                            val genres = (genresState as NetworkResult.Success<List<DeezerGenre>>).data
                            item {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(450.dp)
                                        .padding(horizontal = 8.dp)
                                ) {
                                    LazyVerticalGrid(
                                        columns = GridCells.Fixed(2),
                                        contentPadding = PaddingValues(8.dp),
                                        verticalArrangement = Arrangement.spacedBy(12.dp),
                                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                                    ) {
                                        itemsIndexed(genres) { idx, genre ->
                                            val color = categoryColors[idx % categoryColors.size]
                                            GenreCategoryCard(
                                                genre = genre,
                                                backgroundColor = color,
                                                onClick = { viewModel.onSearchQueryChanged(genre.name) }
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            } else {
                // Live Search results
                when (searchResultState) {
                    is NetworkResult.Loading -> {
                        LoadingIndicator(isFullScreen = false)
                    }
                    is NetworkResult.Error -> {
                        ErrorState(
                            message = (searchResultState as NetworkResult.Error).message ?: "Error searching tracks",
                            onRetry = { viewModel.onSearchQueryChanged(searchQuery) }
                        )
                    }
                    is NetworkResult.Success -> {
                        val tracks = (searchResultState as NetworkResult.Success<List<DeezerTrack>>).data
                        if (tracks.isEmpty()) {
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "No tracks found for \"$searchQuery\"",
                                    color = Color.Gray,
                                    style = MaterialTheme.typography.bodyLarge
                                )
                            }
                        } else {
                            LazyColumn(
                                modifier = Modifier.weight(1f)
                            ) {
                                items(tracks) { track ->
                                    TrackItem(
                                        track = track,
                                        onTrackClick = { viewModel.playTrack(track, tracks) }
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

@Composable
fun TrendingChip(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        onClick = onClick,
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        color = DarkGray,
        contentColor = Color.White
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
        )
    }
}

@Composable
fun GenreCategoryCard(
    genre: DeezerGenre,
    backgroundColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(96.dp)
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(backgroundColor, backgroundColor.copy(alpha = 0.7f))
                ),
                shape = Shapes.medium
            )
            .clickable { onClick() }
            .padding(12.dp)
    ) {
        Text(
            text = genre.name,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = Color.White,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.align(Alignment.TopStart).width(100.dp)
        )

        AsyncImage(
            model = genre.pictureMedium ?: Constants.GENRE_IMAGE_FALLBACK,
            contentDescription = genre.name,
            modifier = Modifier
                .size(60.dp)
                .align(Alignment.BottomEnd)
                .clip(Shapes.large),
            contentScale = ContentScale.Crop
        )
    }
}
