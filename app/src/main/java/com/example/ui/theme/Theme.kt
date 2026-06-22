package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme =
  darkColorScheme(
    primary = SpotifyGreen,
    secondary = LightGray,
    tertiary = SpotifyGreen,
    background = PureBlack,
    surface = DarkGray,
    onPrimary = TextWhite,
    onSecondary = TextWhite,
    onTertiary = TextWhite,
    onBackground = TextWhite,
    onSurface = TextWhite,
    surfaceVariant = LightGray,
    onSurfaceVariant = TextGray
  )

private val LightColorScheme = DarkColorScheme // Always premium dark mode for Spotify aesthetic

@Composable
fun MyApplicationTheme(
  darkTheme: Boolean = true, // Force dark mode as per Spotify theme
  dynamicColor: Boolean = false, // Force Spotify custom palette rather than dynamic device colors
  content: @Composable () -> Unit,
) {
  val colorScheme = DarkColorScheme

  MaterialTheme(colorScheme = colorScheme, typography = Typography, content = content)
}
