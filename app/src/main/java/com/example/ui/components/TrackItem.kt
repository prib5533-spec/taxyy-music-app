package com.example.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.data.remote.DeezerTrack
import com.example.ui.theme.Shapes
import com.example.utils.Constants
import com.example.utils.TimeFormatter

@Composable
fun TrackItem(
    track: DeezerTrack,
    modifier: Modifier = Modifier,
    onTrackClick: (DeezerTrack) -> Unit,
    showDuration: Boolean = true,
    trailingContent: @Composable (() -> Unit)? = null
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onTrackClick(track) }
            .padding(horizontal = 10.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        AsyncImage(
            model = track.album?.coverMedium ?: track.cover ?: Constants.TRACK_IMAGE_FALLBACK,
            contentDescription = "Track album cover",
            modifier = Modifier
                .size(40.dp)
                .clip(Shapes.medium),
            contentScale = ContentScale.Crop
        )
        
        Spacer(modifier = Modifier.width(10.dp))
        
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = track.title,
                style = MaterialTheme.typography.bodyLarge,
                color = Color.White,
                maxLines = 1,
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = track.artist.name,
                style = MaterialTheme.typography.bodyMedium,
                color = Color.Gray,
                maxLines = 1,
            )
        }
        
        if (trailingContent != null) {
            trailingContent()
        } else if (showDuration) {
            Text(
                text = TimeFormatter.formatSecondsToMinutes(track.duration),
                style = MaterialTheme.typography.bodyMedium,
                color = Color.Gray
            )
        } else {
            IconButton(onClick = {}) {
                Icon(
                    imageVector = Icons.Default.MoreVert,
                    contentDescription = "Track options",
                    tint = Color.Gray
                )
            }
        }
    }
}
