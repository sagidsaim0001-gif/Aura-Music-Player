package com.example.feature.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.example.core.ui.MusicPlayerViewModel
import com.example.core.ui.ThemeOption

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: MusicPlayerViewModel,
    navController: NavController
) {
    var showSleepTimerDialog by remember { mutableStateOf(false) }
    var showThemeDialog by remember { mutableStateOf(false) }
    val sleepTimerRemaining by viewModel.sleepTimerRemaining.collectAsStateWithLifecycle()
    val currentTheme by viewModel.currentTheme.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
        ) {
            SettingsItem(
                icon = Icons.Default.Palette,
                title = "Theme",
                subtitle = when (currentTheme) {
                    ThemeOption.SYSTEM -> "System Default"
                    ThemeOption.LIGHT -> "Light Mode"
                    ThemeOption.DARK -> "Dark Mode"
                }
            ) {
                showThemeDialog = true
            }
            SettingsItem(
                icon = Icons.Default.Equalizer,
                title = "Equalizer",
                subtitle = "Adjust audio frequencies (Available soon)"
            ) {
                // TBD
            }
            SettingsItem(
                icon = Icons.Default.Timer,
                title = "Sleep Timer",
                subtitle = if (sleepTimerRemaining != null) "Stops in ${sleepTimerRemaining!! / 60}m ${sleepTimerRemaining!! % 60}s" else "Stop playback automatically"
            ) {
                showSleepTimerDialog = true
            }

            val gapless by viewModel.gaplessPlayback.collectAsStateWithLifecycle()
            SettingsToggleItem(
                icon = Icons.Default.Audiotrack,
                title = "Gapless Playback",
                subtitle = "Eliminate silence between tracks",
                checked = gapless,
                onCheckedChange = { viewModel.toggleGaplessPlayback() }
            )

            val screenAwake by viewModel.keepScreenAwake.collectAsStateWithLifecycle()
            SettingsToggleItem(
                icon = Icons.Default.WbSunny,
                title = "Keep Screen Awake",
                subtitle = "Prevent device from sleeping during playback",
                checked = screenAwake,
                onCheckedChange = { viewModel.toggleKeepScreenAwake() }
            )

            val resumeLast by viewModel.resumeLastPlayed.collectAsStateWithLifecycle()
            SettingsToggleItem(
                icon = Icons.Default.Restore,
                title = "Resume Last Played",
                subtitle = "Start playing from where you left off",
                checked = resumeLast,
                onCheckedChange = { viewModel.toggleResumeLastPlayed() }
            )

            val autoPlay by viewModel.autoPlayNext.collectAsStateWithLifecycle()
            SettingsToggleItem(
                icon = Icons.Default.PlaylistPlay,
                title = "Auto-Play Next",
                subtitle = "Automatically play next related track",
                checked = autoPlay,
                onCheckedChange = { viewModel.toggleAutoPlayNext() }
            )

            val lockScreenArt by viewModel.showLockScreenArt.collectAsStateWithLifecycle()
            SettingsToggleItem(
                icon = Icons.Default.Lock,
                title = "Show Album Art on Lock Screen",
                subtitle = "Display current track art when device is locked",
                checked = lockScreenArt,
                onCheckedChange = { viewModel.toggleShowLockScreenArt() }
            )

            val shakeToSkip by viewModel.shakeToSkip.collectAsStateWithLifecycle()
            SettingsToggleItem(
                icon = Icons.Default.Vibration,
                title = "Shake to Skip",
                subtitle = "Shake device to play next track",
                checked = shakeToSkip,
                onCheckedChange = { viewModel.toggleShakeToSkip() }
            )

            val fadeOutOnPause by viewModel.fadeOutOnPause.collectAsStateWithLifecycle()
            SettingsToggleItem(
                icon = Icons.Default.Transform,
                title = "Fade Out on Pause",
                subtitle = "Gradually reduce volume before pausing",
                checked = fadeOutOnPause,
                onCheckedChange = { viewModel.toggleFadeOutOnPause() }
            )

            val volumeNorm by viewModel.volumeNormalization.collectAsStateWithLifecycle()
            SettingsToggleItem(
                icon = Icons.Default.VolumeUp,
                title = "Volume Normalization",
                subtitle = "Keep volume consistent across tracks",
                checked = volumeNorm,
                onCheckedChange = { viewModel.toggleVolumeNormalization() }
            )

            val crossfade by viewModel.crossfade.collectAsStateWithLifecycle()
            SettingsToggleItem(
                icon = Icons.Default.Animation,
                title = "Crossfade",
                subtitle = "Smoothly transition between songs",
                checked = crossfade,
                onCheckedChange = { viewModel.toggleCrossfade() }
            )

            val stopOnDisconnect by viewModel.stopOnDisconnect.collectAsStateWithLifecycle()
            SettingsToggleItem(
                icon = Icons.Default.Headphones,
                title = "Stop on Disconnect",
                subtitle = "Pause playback when headphones are removed",
                checked = stopOnDisconnect,
                onCheckedChange = { viewModel.toggleStopOnDisconnect() }
            )

            val hqAudio by viewModel.hqAudio.collectAsStateWithLifecycle()
            SettingsToggleItem(
                icon = Icons.Default.HighQuality,
                title = "High-Quality Audio",
                subtitle = "Prefer higher bitrate (if available)",
                checked = hqAudio,
                onCheckedChange = { viewModel.toggleHqAudio() }
            )

            val showVisualizer by viewModel.showVisualizer.collectAsStateWithLifecycle()
            SettingsToggleItem(
                icon = Icons.Default.GraphicEq,
                title = "Audio Visualizer",
                subtitle = "Show visualizer in Now Playing",
                checked = showVisualizer,
                onCheckedChange = { viewModel.toggleShowVisualizer() }
            )

            val autoDownloadLyrics by viewModel.autoDownloadLyrics.collectAsStateWithLifecycle()
            SettingsToggleItem(
                icon = Icons.Default.Download,
                title = "Auto-Download Lyrics",
                subtitle = "Fetch lyrics over Wi-Fi when available",
                checked = autoDownloadLyrics,
                onCheckedChange = { viewModel.toggleAutoDownloadLyrics() }
            )

            val showLyricsLockScreen by viewModel.showLyricsLockScreen.collectAsStateWithLifecycle()
            SettingsToggleItem(
                icon = Icons.Default.Lyrics,
                title = "Lyrics on Lock Screen",
                subtitle = "Display lyrics when device is locked",
                checked = showLyricsLockScreen,
                onCheckedChange = { viewModel.toggleShowLyricsLockScreen() }
            )

            val blurBg by viewModel.blurBackground.collectAsStateWithLifecycle()
            SettingsToggleItem(
                icon = Icons.Default.BlurOn,
                title = "Blur Background",
                subtitle = "Apply blur effect to album art in Now Playing",
                checked = blurBg,
                onCheckedChange = { viewModel.toggleBlurBackground() }
            )

            val carMode by viewModel.carModeEnabled.collectAsStateWithLifecycle()
            SettingsToggleItem(
                icon = Icons.Default.DirectionsCar,
                title = "Car Mode",
                subtitle = "Simplified UI while driving",
                checked = carMode,
                onCheckedChange = { viewModel.toggleCarMode() }
            )

            val djMode by viewModel.djModeEnabled.collectAsStateWithLifecycle()
            SettingsToggleItem(
                icon = Icons.Default.MusicNote,
                title = "DJ Mode / Transitions",
                subtitle = "Smart transitions between different genres",
                checked = djMode,
                onCheckedChange = { viewModel.toggleDjMode() }
            )

            val scrobble by viewModel.scrobbleToLastFm.collectAsStateWithLifecycle()
            SettingsToggleItem(
                icon = Icons.Default.Sync,
                title = "Scrobble to Last.fm",
                subtitle = "Record your listening history",
                checked = scrobble,
                onCheckedChange = { viewModel.toggleScrobbleToLastFm() }
            )

            val autoHide by viewModel.autoHideNavBar.collectAsStateWithLifecycle()
            SettingsToggleItem(
                icon = Icons.Default.Fullscreen,
                title = "Auto-hide Navigation",
                subtitle = "Hide bottom navigation when scrolling",
                checked = autoHide,
                onCheckedChange = { viewModel.toggleAutoHideNavBar() }
            )

            val audioFocus by viewModel.respectAudioFocus.collectAsStateWithLifecycle()
            SettingsToggleItem(
                icon = Icons.Default.VolumeMute,
                title = "Respect Audio Focus",
                subtitle = "Pause playback when other apps play audio",
                checked = audioFocus,
                onCheckedChange = { viewModel.toggleRespectAudioFocus() }
            )

            val skipSilence by viewModel.skipSilences.collectAsStateWithLifecycle()
            SettingsToggleItem(
                icon = Icons.Default.FastForward,
                title = "Skip Silences",
                subtitle = "Automatically skip silent parts in tracks",
                checked = skipSilence,
                onCheckedChange = { viewModel.toggleSkipSilences() }
            )

            val mono by viewModel.monoAudio.collectAsStateWithLifecycle()
            SettingsToggleItem(
                icon = Icons.Default.Hearing,
                title = "Mono Audio",
                subtitle = "Combine left and right channels to play in both ears",
                checked = mono,
                onCheckedChange = { viewModel.toggleMonoAudio() }
            )

            SettingsItem(
                icon = Icons.Default.FolderOff,
                title = "Hidden Folders",
                subtitle = "Manage excluded audio directories"
            ) {
                // TBD
            }
            SettingsItem(
                icon = Icons.Default.Info,
                title = "About",
                subtitle = "Aura Music Player v1.0.0"
            ) {
                // TBD
            }
        }

        if (showSleepTimerDialog) {
            AlertDialog(
                onDismissRequest = { showSleepTimerDialog = false },
                title = { Text("Sleep Timer") },
                text = {
                    Column {
                        Text("Select time to stop playback:")
                        Spacer(Modifier.height(16.dp))
                        val options = listOf(5, 10, 15, 30, 45, 60, 120)
                        LazyColumn(modifier = Modifier.heightIn(max = 200.dp)) {
                            items(options.size) { index ->
                                val mins = options[index]
                                Text(
                                    text = "$mins minutes",
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            viewModel.startSleepTimer(mins)
                                            showSleepTimerDialog = false
                                        }
                                        .padding(vertical = 12.dp, horizontal = 8.dp)
                                )
                            }
                        }
                    }
                },
                confirmButton = {
                    TextButton(onClick = { showSleepTimerDialog = false }) {
                        Text("Cancel")
                    }
                },
                dismissButton = {
                    if (sleepTimerRemaining != null) {
                        TextButton(onClick = {
                            viewModel.clearSleepTimer()
                            showSleepTimerDialog = false
                        }) {
                            Text("Turn Off Timer")
                        }
                    }
                }
            )
        }

        if (showThemeDialog) {
            AlertDialog(
                onDismissRequest = { showThemeDialog = false },
                title = { Text("Choose Theme") },
                text = {
                    Column {
                        ThemeOptionDialogItem(
                            text = "System Default",
                            selected = currentTheme == ThemeOption.SYSTEM,
                            onClick = { viewModel.setTheme(ThemeOption.SYSTEM); showThemeDialog = false }
                        )
                        ThemeOptionDialogItem(
                            text = "Light Mode",
                            selected = currentTheme == ThemeOption.LIGHT,
                            onClick = { viewModel.setTheme(ThemeOption.LIGHT); showThemeDialog = false }
                        )
                        ThemeOptionDialogItem(
                            text = "Dark Mode",
                            selected = currentTheme == ThemeOption.DARK,
                            onClick = { viewModel.setTheme(ThemeOption.DARK); showThemeDialog = false }
                        )
                    }
                },
                confirmButton = {
                    TextButton(onClick = { showThemeDialog = false }) {
                        Text("Cancel")
                    }
                }
            )
        }
    }
}

@Composable
fun ThemeOptionDialogItem(
    text: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(vertical = 12.dp, horizontal = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RadioButton(selected = selected, onClick = null)
        Spacer(modifier = Modifier.width(16.dp))
        Text(text)
    }
}

@Composable
fun SettingsItem(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(28.dp)
        )
        Spacer(modifier = Modifier.width(16.dp))
        Column {
            Text(text = title, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.SemiBold)
            Text(text = subtitle, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
fun SettingsToggleItem(
    icon: ImageVector,
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onCheckedChange(!checked) }
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(28.dp)
        )
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(text = title, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.SemiBold)
            Text(text = subtitle, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange
        )
    }
}
