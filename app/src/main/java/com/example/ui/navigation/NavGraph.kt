package com.example.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.ui.screens.*
import com.example.ui.viewmodel.*

@Composable
fun NavGraph(
    navController: NavHostController,
    modifier: Modifier = Modifier
) {
    NavHost(
        navController = navController,
        startDestination = "splash",
        modifier = modifier
    ) {
        composable("splash") {
            SplashScreen(
                onTimeout = {
                    navController.navigate("home") {
                        popUpTo("splash") { inclusive = true }
                    }
                }
            )
        }
        
        composable("home") {
            val homeViewModel: HomeViewModel = hiltViewModel()
            HomeScreen(
                viewModel = homeViewModel,
                onArtistClick = { artistId ->
                    navController.navigate("artist/$artistId")
                },
                onAlbumClick = { albumId ->
                    navController.navigate("album/$albumId")
                }
            )
        }
        
        composable("search") {
            val searchViewModel: SearchViewModel = hiltViewModel()
            SearchScreen(
                viewModel = searchViewModel,
                onArtistClick = { artistId ->
                    navController.navigate("artist/$artistId")
                },
                onAlbumClick = { albumId ->
                    navController.navigate("album/$albumId")
                }
            )
        }
        
        composable("library") {
            val libraryViewModel: LibraryViewModel = hiltViewModel()
            LibraryScreen(
                viewModel = libraryViewModel,
                onArtistClick = { artistId ->
                    navController.navigate("artist/$artistId")
                },
                onAlbumClick = { albumId ->
                    navController.navigate("album/$albumId")
                }
            )
        }
        
        composable("profile") {
            ProfileScreen()
        }
        
        composable(
            route = "artist/{artistId}",
            arguments = listOf(navArgument("artistId") { type = NavType.StringType })
        ) { backStackEntry ->
            val artistId = backStackEntry.arguments?.getString("artistId") ?: ""
            val artistViewModel: ArtistViewModel = hiltViewModel()
            ArtistScreen(
                artistId = artistId,
                viewModel = artistViewModel,
                onBackClick = { navController.popBackStack() },
                onArtistClick = { id -> navController.navigate("artist/$id") }
            )
        }
        
        composable(
            route = "album/{albumId}",
            arguments = listOf(navArgument("albumId") { type = NavType.StringType })
        ) { backStackEntry ->
            val albumId = backStackEntry.arguments?.getString("albumId") ?: ""
            val albumViewModel: AlbumViewModel = hiltViewModel()
            AlbumScreen(
                albumId = albumId,
                viewModel = albumViewModel,
                onBackClick = { navController.popBackStack() }
            )
        }
    }
}
