package com.example.core.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.core.model.*
import com.example.data.media.AudioHandler
import com.example.data.repository.AudioRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay

sealed class SortOption {
    object Title : SortOption()
    object DateAdded : SortOption()
    object Duration : SortOption()
    object Size : SortOption()
}

enum class ThemeOption {
    SYSTEM, LIGHT, DARK
}

class MusicPlayerViewModel(
    private val repository: AudioRepository,
    val audioHandler: AudioHandler
) : ViewModel() {

    val songs = repository.songs
    val albums = repository.albums
    val artists = repository.artists
    
    val playlists = repository.playlists.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())
    val favorites = repository.favorites.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())
    val recentlyPlayed = repository.recentlyPlayed.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())
    
    val searchQuery = MutableStateFlow("")
    val sortOption = MutableStateFlow<SortOption>(SortOption.Title)
    val currentTheme = MutableStateFlow(ThemeOption.DARK) // Hardcoded dark by default originally

    fun setTheme(theme: ThemeOption) {
        currentTheme.value = theme
    }

    val gaplessPlayback = MutableStateFlow(false)
    fun toggleGaplessPlayback() {
        gaplessPlayback.value = !gaplessPlayback.value
    }

    val keepScreenAwake = MutableStateFlow(false)
    fun toggleKeepScreenAwake() {
        keepScreenAwake.value = !keepScreenAwake.value
    }

    val resumeLastPlayed = MutableStateFlow(true)
    fun toggleResumeLastPlayed() {
        resumeLastPlayed.value = !resumeLastPlayed.value
    }

    val autoPlayNext = MutableStateFlow(true)
    fun toggleAutoPlayNext() {
        autoPlayNext.value = !autoPlayNext.value
    }

    val showLockScreenArt = MutableStateFlow(true)
    fun toggleShowLockScreenArt() {
        showLockScreenArt.value = !showLockScreenArt.value
    }

    val volumeNormalization = MutableStateFlow(false)
    fun toggleVolumeNormalization() {
        volumeNormalization.value = !volumeNormalization.value
    }

    val crossfade = MutableStateFlow(false)
    fun toggleCrossfade() {
        crossfade.value = !crossfade.value
    }

    val hiddenFolders = MutableStateFlow<List<String>>(emptyList())
    
    val shakeToSkip = MutableStateFlow(false)
    fun toggleShakeToSkip() {
        shakeToSkip.value = !shakeToSkip.value
    }

    val fadeOutOnPause = MutableStateFlow(false)
    fun toggleFadeOutOnPause() {
        fadeOutOnPause.value = !fadeOutOnPause.value
    }

    val stopOnDisconnect = MutableStateFlow(true)
    fun toggleStopOnDisconnect() {
        stopOnDisconnect.value = !stopOnDisconnect.value
    }

    val hqAudio = MutableStateFlow(false)
    fun toggleHqAudio() {
        hqAudio.value = !hqAudio.value
    }

    val showLyricsLockScreen = MutableStateFlow(false)
    fun toggleShowLyricsLockScreen() {
        showLyricsLockScreen.value = !showLyricsLockScreen.value
    }

    val autoDownloadLyrics = MutableStateFlow(false)
    fun toggleAutoDownloadLyrics() {
        autoDownloadLyrics.value = !autoDownloadLyrics.value
    }

    val showVisualizer = MutableStateFlow(true)
    fun toggleShowVisualizer() {
        showVisualizer.value = !showVisualizer.value
    }

    val blurBackground = MutableStateFlow(true)
    fun toggleBlurBackground() {
        blurBackground.value = !blurBackground.value
    }

    val carModeEnabled = MutableStateFlow(false)
    fun toggleCarMode() {
        carModeEnabled.value = !carModeEnabled.value
    }

    val djModeEnabled = MutableStateFlow(false)
    fun toggleDjMode() {
        djModeEnabled.value = !djModeEnabled.value
    }

    val scrobbleToLastFm = MutableStateFlow(false)
    fun toggleScrobbleToLastFm() {
        scrobbleToLastFm.value = !scrobbleToLastFm.value
    }

    val autoHideNavBar = MutableStateFlow(false)
    fun toggleAutoHideNavBar() {
        autoHideNavBar.value = !autoHideNavBar.value
    }

    val respectAudioFocus = MutableStateFlow(true)
    fun toggleRespectAudioFocus() {
        respectAudioFocus.value = !respectAudioFocus.value
    }

    val skipSilences = MutableStateFlow(false)
    fun toggleSkipSilences() {
        skipSilences.value = !skipSilences.value
    }

    val monoAudio = MutableStateFlow(false)
    fun toggleMonoAudio() {
        monoAudio.value = !monoAudio.value
    }

    val showBatteryWarnings = MutableStateFlow(true)
    fun toggleShowBatteryWarnings() {
        showBatteryWarnings.value = !showBatteryWarnings.value
    }

    val currentSongInfo = MutableStateFlow<com.example.core.model.Song?>(null)
    fun showSongInfoDialog(song: com.example.core.model.Song) {
        currentSongInfo.value = song
    }
    fun dismissSongInfoDialog() {
        currentSongInfo.value = null
    }

    val displayedSongs = combine(songs, searchQuery, sortOption) { songList, query, sort ->
        var list = songList
        if (query.isNotBlank()) {
            list = list.filter { it.title.contains(query, true) || it.artist.contains(query, true) }
        }
        when (sort) {
            is SortOption.Title -> list.sortedBy { it.title }
            is SortOption.DateAdded -> list.sortedByDescending { it.dateAdded }
            is SortOption.Duration -> list.sortedByDescending { it.duration }
            is SortOption.Size -> list.sortedByDescending { it.size }
        }
    }.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    private var sleepTimerJob: Job? = null
    val sleepTimerRemaining = MutableStateFlow<Long?>(null)

    fun startSleepTimer(minutes: Int) {
        sleepTimerJob?.cancel()
        sleepTimerJob = viewModelScope.launch {
            var remaining = minutes * 60L
            while(remaining > 0) {
                sleepTimerRemaining.value = remaining
                delay(1000)
                remaining--
            }
            sleepTimerRemaining.value = null
            if (audioHandler.isPlaying.value) {
                audioHandler.playPause()
            }
        }
    }
    
    fun clearSleepTimer() {
        sleepTimerJob?.cancel()
        sleepTimerRemaining.value = null
    }

    fun setPlaybackSpeed(speed: Float) = audioHandler.setPlaybackSpeed(speed)
    
    fun toggleRepeatMode() {
        val current = audioHandler.repeatMode.value
        val nextMode = when (current) {
            androidx.media3.common.Player.REPEAT_MODE_OFF -> androidx.media3.common.Player.REPEAT_MODE_ALL
            androidx.media3.common.Player.REPEAT_MODE_ALL -> androidx.media3.common.Player.REPEAT_MODE_ONE
            else -> androidx.media3.common.Player.REPEAT_MODE_OFF
        }
        audioHandler.setRepeatMode(nextMode)
    }

    fun toggleShuffleMode() {
        audioHandler.setShuffleModeEnabled(!audioHandler.shuffleModeEnabled.value)
    }

    fun seekForward() = audioHandler.seekForward()
    fun seekBack() = audioHandler.seekBack()
    
    fun loadAudioFiles() {
        viewModelScope.launch {
            repository.scanAudioFiles()
        }
    }

    fun playSong(song: Song, contextList: List<Song> = songs.value) {
        viewModelScope.launch {
            val startIndex = contextList.indexOf(song).coerceAtLeast(0)
            audioHandler.playMediaItems(contextList, startIndex)
        }
    }

    fun togglePlayPause() = audioHandler.playPause()
    fun skipNext() = audioHandler.skipToNext()
    fun skipPrevious() = audioHandler.skipToPrevious()
    fun seekTo(position: Long) = audioHandler.seekTo(position)

    fun toggleFavorite(songId: Long) {
        viewModelScope.launch {
            val isFav = favorites.value.any { it.songId == songId }
            repository.toggleFavorite(songId, isFav)
        }
    }

    fun createPlaylist(name: String) {
        viewModelScope.launch {
            repository.createPlaylist(name)
        }
    }

    override fun onCleared() {
        super.onCleared()
        audioHandler.release()
    }
}

class MusicPlayerViewModelFactory(
    private val repository: AudioRepository,
    private val audioHandler: AudioHandler
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MusicPlayerViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return MusicPlayerViewModel(repository, audioHandler) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
