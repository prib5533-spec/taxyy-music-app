package com.example.ui.navigation

import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LibraryMusic
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.LibraryMusic
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.example.ui.theme.DarkGray
import com.example.ui.theme.SpotifyGreen

sealed class NavigationItem(
    val route: String,
    val title: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector
) {
    object Home : NavigationItem("home", "Home", Icons.Filled.Home, Icons.Outlined.Home)
    object Search : NavigationItem("search", "Search", Icons.Filled.Search, Icons.Outlined.Search)
    object Library : NavigationItem("library", "Library", Icons.Filled.LibraryMusic, Icons.Outlined.LibraryMusic)
    object Profile : NavigationItem("profile", "Profile", Icons.Filled.Person, Icons.Outlined.Person)
}

@Composable
fun BottomNavBar(
    navController: NavController,
    modifier: Modifier = Modifier
) {
    val items = listOf(
        NavigationItem.Home,
        NavigationItem.Search,
        NavigationItem.Library,
        NavigationItem.Profile
    )

    NavigationBar(
        modifier = modifier.navigationBarsPadding(),
        containerColor = DarkGray,
        contentColor = Color.White
    ) {
        val navBackStackEntry = navController.currentBackStackEntryAsState()
        val currentRoute = navBackStackEntry.value?.destination?.route

        items.forEach { item ->
            val isSelected = currentRoute == item.route
            NavigationBarItem(
                selected = isSelected,
                onClick = {
                    if (currentRoute != item.route) {
                        navController.navigate(item.route) {
                            // Pop up to the start destination of the graph to
                            // avoid building up a large stack of destinations.
                            // If the start destination was splash (which has been cleared), pop up to "home".
                            val destinationRoute = navController.graph.startDestinationRoute ?: "home"
                            val popRoute = if (destinationRoute == "splash") "home" else destinationRoute
                            popUpTo(popRoute) {
                                saveState = true
                            }
                            // Avoid multiple copies of the same destination when
                            // reselecting the same item
                            launchSingleTop = true
                            // Restore state when reselecting a previously selected item
                            restoreState = true
                        }
                    }
                },
                icon = {
                    Icon(
                        imageVector = if (isSelected) item.selectedIcon else item.unselectedIcon,
                        contentDescription = item.title
                    )
                },
                label = {
                    Text(text = item.title)
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = SpotifyGreen,
                    unselectedIconColor = Color.Gray,
                    selectedTextColor = SpotifyGreen,
                    unselectedTextColor = Color.Gray,
                    indicatorColor = Color.Transparent // transparent rounded pill container as per spotify aesthetic
                )
            )
        }
    }
}
