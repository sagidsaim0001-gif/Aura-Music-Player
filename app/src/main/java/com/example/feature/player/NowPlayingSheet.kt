package com.example.feature.player

import androidx.compose.ui.draw.blur
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.media3.common.Player
import coil.compose.AsyncImage
import com.example.core.ui.MusicPlayerViewModel
import com.example.data.repository.AudioRepository

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NowPlayingSheet(
    viewModel: MusicPlayerViewModel,
    onDismiss: () -> Unit
) {
    val currentSong by viewModel.audioHandler.currentSong.collectAsStateWithLifecycle()
    val isPlaying by viewModel.audioHandler.isPlaying.collectAsStateWithLifecycle()
    val progress by viewModel.audioHandler.progress.collectAsStateWithLifecycle()
    val duration by viewModel.audioHandler.duration.collectAsStateWithLifecycle()
    val favorites by viewModel.favorites.collectAsStateWithLifecycle()
    val playbackSpeed by viewModel.audioHandler.playbackSpeed.collectAsStateWithLifecycle()
    val repeatMode by viewModel.audioHandler.repeatMode.collectAsStateWithLifecycle()
    val shuffleMode by viewModel.audioHandler.shuffleModeEnabled.collectAsStateWithLifecycle()
    val currentSongInfo by viewModel.currentSongInfo.collectAsStateWithLifecycle()
    val blurBg by viewModel.blurBackground.collectAsStateWithLifecycle()

    val isFavorite = currentSong?.let { song -> favorites.any { it.songId == song.id } } ?: false

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        modifier = Modifier.fillMaxSize(),
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = MaterialTheme.colorScheme.background
    ) {
        if (currentSong != null) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(24.dp))
                
                // Album Art
                Box(
                    modifier = Modifier
                        .aspectRatio(1f)
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(32.dp))
                ) {
                    if (blurBg) {
                        AsyncImage(
                            model = AudioRepository.getAlbumArtUri(currentSong!!.albumId),
                            contentDescription = null,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .fillMaxSize()
                                .blur(if (blurBg) 24.dp else 0.dp),
                            colorFilter = androidx.compose.ui.graphics.ColorFilter.tint(
                                androidx.compose.ui.graphics.Color.Black.copy(alpha = 0.3f),
                                androidx.compose.ui.graphics.BlendMode.Darken
                            )
                        )
                    }

                    AsyncImage(
                        model = AudioRepository.getAlbumArtUri(currentSong!!.albumId),
                        contentDescription = "Album Art",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(if (blurBg) 24.dp else 0.dp)
                            .clip(RoundedCornerShape(if (blurBg) 16.dp else 32.dp))
                    )
                }
                
                Spacer(modifier = Modifier.height(32.dp))
                
                // Track Info & Favorite
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = currentSong!!.title,
                            style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = currentSong!!.artist,
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    
                    IconButton(onClick = { viewModel.toggleFavorite(currentSong!!.id) }) {
                        Icon(
                            imageVector = if (isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                            contentDescription = "Favorite",
                            tint = if (isFavorite) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                        )
                    }
                    IconButton(onClick = { /* Set as Ringtone */ }) {
                        Icon(androidx.compose.material.icons.Icons.Default.Notifications, contentDescription = "Set as Ringtone")
                    }
                    IconButton(onClick = { /* Show Details */ viewModel.showSongInfoDialog(currentSong!!) }) {
                        Icon(androidx.compose.material.icons.Icons.Default.Info, contentDescription = "Song Info")
                    }
                }
                
                Spacer(modifier = Modifier.height(24.dp))
                
                // Progress Segment
                var sliderValue by remember(progress) { mutableFloatStateOf(progress.toFloat()) }
                var isDragging by remember { mutableStateOf(false) }

                Slider(
                    value = if (isDragging) sliderValue else progress.toFloat(),
                    onValueChange = { 
                        isDragging = true
                        sliderValue = it 
                    },
                    onValueChangeFinished = {
                        isDragging = false
                        viewModel.seekTo(sliderValue.toLong())
                    },
                    valueRange = 0f..(duration.coerceAtLeast(1L).toFloat()),
                    colors = SliderDefaults.colors(
                        thumbColor = MaterialTheme.colorScheme.primary,
                        activeTrackColor = MaterialTheme.colorScheme.primary,
                        inactiveTrackColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                )
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(text = formatDuration(if (isDragging) sliderValue.toLong() else progress), style = MaterialTheme.typography.labelMedium)
                    Text(text = formatDuration(duration), style = MaterialTheme.typography.labelMedium)
                }
                
                Spacer(modifier = Modifier.height(24.dp))
                
                // Controls (Shuffle, Fast Rewind, Play/Pause, Fast Forward, Repeat)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = { viewModel.toggleShuffleMode() }) {
                        Icon(
                            Icons.Default.Shuffle, 
                            "Shuffle",
                            tint = if (shuffleMode) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                        )
                    }
                    
                    IconButton(
                        onClick = { viewModel.skipPrevious() },
                        modifier = Modifier.size(56.dp)
                    ) {
                        Icon(Icons.Default.SkipPrevious, "Previous", modifier = Modifier.size(36.dp))
                    }
                    
                    FilledIconButton(
                        onClick = { viewModel.togglePlayPause() },
                        modifier = Modifier.size(72.dp),
                        shape = CircleShape
                    ) {
                        Icon(
                            imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                            contentDescription = "Play/Pause",
                            modifier = Modifier.size(40.dp)
                        )
                    }
                    
                    IconButton(
                        onClick = { viewModel.skipNext() },
                        modifier = Modifier.size(56.dp)
                    ) {
                        Icon(Icons.Default.SkipNext, "Next", modifier = Modifier.size(36.dp))
                    }
                    
                    IconButton(onClick = { viewModel.toggleRepeatMode() }) {
                        val repeatIcon = when (repeatMode) {
                            Player.REPEAT_MODE_ALL -> Icons.Default.Repeat
                            Player.REPEAT_MODE_ONE -> Icons.Default.RepeatOne
                            else -> Icons.Default.Repeat
                        }
                        val repeatTint = if (repeatMode != Player.REPEAT_MODE_OFF) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                        Icon(repeatIcon, "Repeat", tint = repeatTint)
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Secondary Controls: Fast Rewind, Speed, Fast Forward
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = { viewModel.seekBack() }) {
                        Icon(Icons.Default.FastRewind, "Rewind 10s")
                    }

                    TextButton(onClick = { 
                        val nextSpeed = when (playbackSpeed) {
                            1.0f -> 1.5f
                            1.5f -> 2.0f
                            2.0f -> 0.5f
                            else -> 1.0f
                        }
                        viewModel.setPlaybackSpeed(nextSpeed)
                    }) {
                        Text("${playbackSpeed}x", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground)
                    }

                    IconButton(onClick = { viewModel.seekForward() }) {
                        Icon(Icons.Default.FastForward, "Forward 10s")
                    }
                }
            }
        }
    }
    
    if (currentSongInfo != null) {
        SongInfoDialog(song = currentSongInfo!!, onDismiss = { viewModel.dismissSongInfoDialog() })
    }
}

private fun formatDuration(ms: Long): String {
    val totalSeconds = ms / 1000
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return String.format("%02d:%02d", minutes, seconds)
}

@Composable
fun SongInfoDialog(song: com.example.core.model.Song, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Track Details") },
        text = {
            Column {
                Text("Title: ${song.title}")
                Spacer(modifier = Modifier.height(8.dp))
                Text("Artist: ${song.artist}")
                Spacer(modifier = Modifier.height(8.dp))
                Text("Path: ${song.data}")
                Spacer(modifier = Modifier.height(8.dp))
                Text("Size: ${song.size / 1024 / 1024} MB")
                Spacer(modifier = Modifier.height(8.dp))
                Text("Duration: ${formatDuration(song.duration)}")
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("OK")
            }
        }
    )
}
