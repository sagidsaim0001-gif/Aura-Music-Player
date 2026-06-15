package com.example

import android.Manifest
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.core.navigation.*
import com.example.core.ui.MusicPlayerViewModel
import com.example.core.ui.MusicPlayerViewModelFactory
import com.example.data.media.AudioHandler
import com.example.ui.theme.MyApplicationTheme
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.example.feature.home.HomeScreen
import com.example.feature.songs.SongsScreen
import com.example.feature.player.NowPlayingSheet
import com.example.feature.components.MiniPlayer
import com.example.feature.albums.AlbumsScreen
import com.example.feature.playlists.PlaylistsScreen
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Album
import androidx.compose.material.icons.filled.QueueMusic

class MainActivity : ComponentActivity() {
  private lateinit var audioHandler: AudioHandler

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()
    
    val appContainer = (application as AppApplication).container
    audioHandler = AudioHandler(this, appContainer.audioRepository)
    
    setContent {
      MyApplicationTheme(darkTheme = true) { // Dark-first design
        val viewModel: MusicPlayerViewModel = viewModel(
            factory = MusicPlayerViewModelFactory(appContainer.audioRepository, audioHandler)
        )
        
        AuraMusicApp(viewModel)
      }
    }
  }
}

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun AuraMusicApp(viewModel: MusicPlayerViewModel) {
    val permission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        Manifest.permission.READ_MEDIA_AUDIO
    } else {
        Manifest.permission.READ_EXTERNAL_STORAGE
    }
    
    val permissionState = rememberPermissionState(permission)
    
    if (permissionState.status.isGranted) {
        MainScreen(viewModel)
    } else {
        PermissionRequestScreen(
            onRequest = { permissionState.launchPermissionRequest() }
        )
    }
}

@Composable
fun PermissionRequestScreen(onRequest: () -> Unit) {
    Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        Column(
            modifier = Modifier.fillMaxSize().padding(24.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                "Aura Music",
                style = MaterialTheme.typography.displayMedium,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                "We need audio permissions to find your music files offline.",
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(32.dp))
            Button(onClick = onRequest) {
                Text("Grant Permission")
            }
        }
    }
}

@Composable
fun MainScreen(viewModel: MusicPlayerViewModel) {
    val navController = rememberNavController()
    var showNowPlaying by remember { mutableStateOf(false) }
    
    LaunchedEffect(Unit) {
        viewModel.loadAudioFiles()
    }
    
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = {
            Column(modifier = Modifier.fillMaxWidth()) {
                MiniPlayer(
                    viewModel = viewModel,
                    onExpand = { showNowPlaying = true }
                )
                NavigationBar {
                    val navBackStackEntry by navController.currentBackStackEntryAsState()
                    val currentRoute = navBackStackEntry?.destination?.route
                    
                    NavigationBarItem(
                        selected = currentRoute?.contains("HomeRoute") == true,
                        onClick = { navController.navigate(HomeRoute) { launchSingleTop = true; restoreState = true } },
                        icon = { Icon(Icons.Default.Home, contentDescription = "Home") },
                        label = { Text("Home") }
                    )
                    NavigationBarItem(
                        selected = currentRoute?.contains("SongsRoute") == true,
                        onClick = { navController.navigate(SongsRoute) { launchSingleTop = true; restoreState = true } },
                        icon = { Icon(Icons.Default.MusicNote, contentDescription = "Songs") },
                        label = { Text("Songs") }
                    )
                    NavigationBarItem(
                        selected = currentRoute?.contains("AlbumsRoute") == true,
                        onClick = { navController.navigate(AlbumsRoute) { launchSingleTop = true; restoreState = true } },
                        icon = { Icon(Icons.Default.Album, contentDescription = "Albums") },
                        label = { Text("Albums") }
                    )
                    NavigationBarItem(
                        selected = currentRoute?.contains("PlaylistsRoute") == true,
                        onClick = { navController.navigate(PlaylistsRoute) { launchSingleTop = true; restoreState = true } },
                        icon = { Icon(Icons.Default.QueueMusic, contentDescription = "Playlists") },
                        label = { Text("Playlists") }
                    )
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = HomeRoute,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable<HomeRoute> {
                HomeScreen(viewModel, navController)
            }
            composable<SongsRoute> {
                SongsScreen(viewModel, navController)
            }
            composable<AlbumsRoute> {
                AlbumsScreen(viewModel, navController)
            }
            composable<PlaylistsRoute> {
                PlaylistsScreen(viewModel, navController)
            }
        }
    }
    
    if (showNowPlaying) {
        NowPlayingSheet(
            viewModel = viewModel,
            onDismiss = { showNowPlaying = false }
        )
    }
}
