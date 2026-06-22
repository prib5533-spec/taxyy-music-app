package com.example.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.data.remote.DeezerAlbum
import com.example.ui.theme.Shapes

@Composable
fun AlbumCard(
    album: DeezerAlbum,
    modifier: Modifier = Modifier,
    onAlbumClick: (DeezerAlbum) -> Unit
) {
    Column(
        modifier = modifier
            .width(115.dp)
            .clickable { onAlbumClick(album) }
            .padding(4.dp)
    ) {
        AsyncImage(
            model = album.coverMedium,
            contentDescription = "Album cover",
            modifier = Modifier
                .size(105.dp)
                .clip(Shapes.medium),
            contentScale = ContentScale.Crop
        )
        
        Spacer(modifier = Modifier.height(4.dp))
        
        Text(
            text = album.title,
            style = MaterialTheme.typography.bodyLarge,
            color = Color.White,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        
        Spacer(modifier = Modifier.height(2.dp))
        
        Text(
            text = album.artist?.name ?: "Unknown Artist",
            style = MaterialTheme.typography.bodySmall,
            color = Color.Gray,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}
