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
import kotlinx.coroutines.launch

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
    
    init {
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
