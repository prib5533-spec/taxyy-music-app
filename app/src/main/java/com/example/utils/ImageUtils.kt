package com.example.utils

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import coil.request.ImageRequest

object ImageUtils {
    @Composable
    fun getCoilRequest(url: String, fallback: String): ImageRequest {
        return ImageRequest.Builder(LocalContext.current)
            .data(url.ifEmpty { fallback })
            .crossfade(true)
            .error(android.R.drawable.ic_menu_report_image)
            .placeholder(android.R.drawable.ic_menu_gallery)
            .build()
    }
}
