package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.ui.components.MiniPlayer
import com.example.ui.navigation.BottomNavBar
import com.example.ui.navigation.NavGraph
import com.example.ui.screens.NowPlayingScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.viewmodel.PlayerViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                val navController = rememberNavController()
                val playerViewModel: PlayerViewModel = hiltViewModel()

                val currentTrack by playerViewModel.currentTrack.collectAsState()
                val isPlaying by playerViewModel.isPlaying.collectAsState()
                val currentPosition by playerViewModel.currentPosition.collectAsState()
                val duration by playerViewModel.duration.collectAsState()

                var showFullPlayer by remember { mutableStateOf(false) }

                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentRoute = navBackStackEntry?.destination?.route

                // Determine whether to show the Bottom Bar and Mini Player
                val showBottomBar = currentRoute in listOf("home", "search", "library", "profile")

                // Convert position/duration into an ongoing progress percentage (0.0f - 1.0f)
                val progressValue = remember(currentPosition, duration) {
                    if (duration > 0) currentPosition.toFloat() / duration.toFloat() else 0f
                }

                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    bottomBar = {
                        if (showBottomBar) {
                            BottomNavBar(navController = navController)
                        }
                    },
                    containerColor = Color.Black
                ) { innerPadding ->
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(
                                bottom = if (showBottomBar) 0.dp else innerPadding.calculateBottomPadding()
                            )
                    ) {
                        // Master Screen Pages Router
                        NavGraph(
                            navController = navController,
                            modifier = Modifier.padding(
                                top = innerPadding.calculateTopPadding(),
                                bottom = if (showBottomBar && currentTrack != null) 64.dp else if (showBottomBar) 80.dp else 0.dp
                            )
                        )

                        // Floating Persistent MiniPlayer
                        if (showBottomBar && currentTrack != null) {
                            Box(
                                modifier = Modifier
                                    .align(Alignment.BottomCenter)
                                    .padding(bottom = 88.dp) // Float perfectly above bottom bar
                            ) {
                                MiniPlayer(
                                    currentTrack = currentTrack,
                                    isPlaying = isPlaying,
                                    progress = progressValue,
                                    onPlayPauseClick = { playerViewModel.togglePlayPause() },
                                    onNextClick = { playerViewModel.skipToNext() },
                                    onClick = { showFullPlayer = true }
                                )
                            }
                        }

                        // Sliding Full-Screen NowPlaying Overlay
                        AnimatedVisibility(
                            visible = showFullPlayer,
                            enter = slideInVertically(
                                initialOffsetY = { height -> height },
                                animationSpec = tween(durationMillis = 400)
                            ) + fadeIn(),
                            exit = slideOutVertically(
                                targetOffsetY = { height -> height },
                                animationSpec = tween(durationMillis = 400)
                            ) + fadeOut()
                        ) {
                            NowPlayingScreen(
                                viewModel = playerViewModel,
                                onBackClick = { showFullPlayer = false }
                            )
                        }
                    }
                }
            }
        }
    }
}
