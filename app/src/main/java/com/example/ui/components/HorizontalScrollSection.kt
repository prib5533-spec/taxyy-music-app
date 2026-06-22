package com.example.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun <T> HorizontalScrollSection(
    title: String,
    items: List<T>,
    modifier: Modifier = Modifier,
    hasSeeAll: Boolean = false,
    onSeeAllClick: () -> Unit = {},
    itemContent: @Composable (T) -> Unit
) {
    if (items.isEmpty()) return

    Column(modifier = modifier.fillMaxWidth()) {
        SectionHeader(
            title = title,
            hasSeeAll = hasSeeAll,
            onSeeAllClick = onSeeAllClick
        )
        
        LazyRow(
            contentPadding = PaddingValues(horizontal = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            items(items) { item ->
                itemContent(item)
            }
        }
    }
}
