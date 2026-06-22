package com.example.ui.screens

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.ui.theme.SpotifyGreen
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(
    onTimeout: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scale = remember { Animatable(0.5f) }

    LaunchedEffect(key1 = true) {
        scale.animateTo(
            targetValue = 1.0f,
            animationSpec = tween(
                durationMillis = 800
            )
        )
        delay(1200) // 800ms anim + 1200ms delay = 2000s total
        onTimeout()
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.scale(scale.value)
        ) {
            // Draw a Spotify-like glowing soundwave logo using a Canvas
            Canvas(modifier = Modifier.size(100.dp)) {
                val barWidth = 10.dp.toPx()
                val barGap = 6.dp.toPx()
                val heights = listOf(40.dp, 75.dp, 90.dp, 60.dp, 30.dp)
                
                var currentX = (size.width - (heights.size * barWidth + (heights.size - 1) * barGap)) / 2f
                
                heights.forEach { h ->
                    val hPx = h.toPx()
                    val startY = (size.height - hPx) / 2f
                    drawRoundRect(
                        color = SpotifyGreen,
                        topLeft = androidx.compose.ui.geometry.Offset(currentX, startY),
                        size = androidx.compose.ui.geometry.Size(barWidth, hPx),
                        cornerRadius = androidx.compose.ui.geometry.CornerRadius(6.dp.toPx())
                    )
                    currentX += barWidth + barGap
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "Music Stream",
                style = MaterialTheme.typography.titleLarge,
                color = Color.White
            )
            
            Spacer(modifier = Modifier.height(4.dp))
            
            Text(
                text = "Pure Dark Streaming",
                style = MaterialTheme.typography.bodyMedium,
                color = SpotifyGreen
            )
        }
    }
}
