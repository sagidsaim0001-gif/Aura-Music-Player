package com.example.data.media

import android.content.ComponentName
import android.content.Context
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.Player
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import com.example.core.model.Song
import com.example.data.repository.AudioRepository
import com.google.common.util.concurrent.ListenableFuture
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class AudioHandler(private val context: Context, private val repository: AudioRepository) {
    private var mediaController: MediaController? = null
    private var controllerFuture: ListenableFuture<MediaController>? = null
    private val scope = CoroutineScope(Dispatchers.Main)

    private val _currentSong = MutableStateFlow<Song?>(null)
    val currentSong = _currentSong.asStateFlow()

    private val _isPlaying = MutableStateFlow(false)
    val isPlaying = _isPlaying.asStateFlow()

    private val _progress = MutableStateFlow(0L)
    val progress = _progress.asStateFlow()
    
    private val _duration = MutableStateFlow(0L)
    val duration = _duration.asStateFlow()

    private val _playbackSpeed = MutableStateFlow(1.0f)
    val playbackSpeed = _playbackSpeed.asStateFlow()
    
    private val _repeatMode = MutableStateFlow(Player.REPEAT_MODE_OFF)
    val repeatMode = _repeatMode.asStateFlow()

    private val _shuffleModeEnabled = MutableStateFlow(false)
    val shuffleModeEnabled = _shuffleModeEnabled.asStateFlow()

    init {
        initializeController()
        startTrackingProgress()
    }

    private fun initializeController() {
        val sessionToken = SessionToken(context, ComponentName(context, PlaybackService::class.java))
        controllerFuture = MediaController.Builder(context, sessionToken).buildAsync()
        
        controllerFuture?.addListener({
            mediaController = controllerFuture?.get()
            mediaController?.addListener(object : Player.Listener {
                override fun onIsPlayingChanged(isPlaying: Boolean) {
                    _isPlaying.value = isPlaying
                }

                override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
                    mediaItem?.mediaId?.toLongOrNull()?.let { songId ->
                        scope.launch {
                            val song = repository.songs.value.find { it.id == songId }
                            _currentSong.value = song
                            song?.let {
                                repository.addRecentlyPlayed(it.id)
                            }
                        }
                    }
                    _duration.value = mediaController?.duration?.coerceAtLeast(0L) ?: 0L
                }

                override fun onRepeatModeChanged(repeatMode: Int) {
                    _repeatMode.value = repeatMode
                }

                override fun onShuffleModeEnabledChanged(shuffleModeEnabled: Boolean) {
                    _shuffleModeEnabled.value = shuffleModeEnabled
                }
            })
        }, context.mainExecutor)
    }

    suspend fun playMediaItems(songs: List<Song>, startIndex: Int = 0) {
        val controller = mediaController ?: return
        
        val items = songs.map { song ->
            MediaItem.Builder()
                .setMediaId(song.id.toString())
                .setUri(AudioRepository.getSongUri(song.id))
                .setMediaMetadata(
                    MediaMetadata.Builder()
                        .setTitle(song.title)
                        .setArtist(song.artist)
                        .setAlbumTitle(song.album)
                        .setArtworkUri(AudioRepository.getAlbumArtUri(song.albumId))
                        .build()
                )
                .build()
        }
        
        controller.setMediaItems(items, startIndex, 0L)
        controller.prepare()
        controller.play()
    }

    fun playPause() {
        val controller = mediaController ?: return
        if (controller.isPlaying) {
            controller.pause()
        } else {
            controller.play()
        }
    }

    fun skipToNext() {
        mediaController?.seekToNext()
    }

    fun skipToPrevious() {
        mediaController?.seekToPrevious()
    }

    fun seekTo(position: Long) {
        mediaController?.seekTo(position)
    }

    fun seekForward() {
        mediaController?.seekForward()
    }

    fun seekBack() {
        mediaController?.seekBack()
    }

    fun setRepeatMode(repeatMode: Int) {
        mediaController?.repeatMode = repeatMode
    }

    fun setShuffleModeEnabled(shuffleModeEnabled: Boolean) {
        mediaController?.shuffleModeEnabled = shuffleModeEnabled
    }

    fun setPlaybackSpeed(speed: Float) {
        _playbackSpeed.value = speed
        mediaController?.setPlaybackSpeed(speed)
    }

    private fun startTrackingProgress() {
        scope.launch {
            while (true) {
                if (_isPlaying.value) {
                    mediaController?.let {
                        _progress.value = it.currentPosition.coerceAtLeast(0L)
                        _duration.value = it.duration.coerceAtLeast(0L)
                    }
                }
                delay(1000L)
            }
        }
    }

    fun release() {
        controllerFuture?.let { MediaController.releaseFuture(it) }
    }
}
