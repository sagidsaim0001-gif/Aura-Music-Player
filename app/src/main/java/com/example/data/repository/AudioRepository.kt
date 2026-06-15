package com.example.data.repository

import android.content.ContentUris
import android.content.Context
import android.net.Uri
import android.provider.MediaStore
import com.example.core.model.*
import com.example.data.local.AuraDao
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.withContext

class AudioRepository(
    private val context: Context,
    private val dao: AuraDao
) {

    private val _songs = MutableStateFlow<List<Song>>(emptyList())
    val songs: StateFlow<List<Song>> = _songs.asStateFlow()

    private val _albums = MutableStateFlow<List<Album>>(emptyList())
    val albums: StateFlow<List<Album>> = _albums.asStateFlow()

    private val _artists = MutableStateFlow<List<Artist>>(emptyList())
    val artists: StateFlow<List<Artist>> = _artists.asStateFlow()

    val playlists = dao.getAllPlaylists()
    val favorites = dao.getFavorites()
    val recentlyPlayed = dao.getRecentlyPlayed()

    suspend fun scanAudioFiles() = withContext(Dispatchers.IO) {
        val songList = mutableListOf<Song>()
        val collection = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
        val projection = arrayOf(
            MediaStore.Audio.Media._ID,
            MediaStore.Audio.Media.TITLE,
            MediaStore.Audio.Media.ARTIST,
            MediaStore.Audio.Media.ALBUM,
            MediaStore.Audio.Media.ALBUM_ID,
            MediaStore.Audio.Media.DURATION,
            MediaStore.Audio.Media.DATA,
            MediaStore.Audio.Media.DATE_ADDED,
            MediaStore.Audio.Media.SIZE
        )
        val selection = "${MediaStore.Audio.Media.IS_MUSIC} != 0 AND ${MediaStore.Audio.Media.DURATION} >= 30000" // ignore <30s
        val sortOrder = "${MediaStore.Audio.Media.TITLE} ASC"

        context.contentResolver.query(
            collection,
            projection,
            selection,
            null,
            sortOrder
        )?.use { cursor ->
            val idColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID)
            val titleColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE)
            val artistColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST)
            val albumColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM)
            val albumIdColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM_ID)
            val durationColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION)
            val dataColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA)
            val dateAddedColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATE_ADDED)
            val sizeColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.SIZE)

            while (cursor.moveToNext()) {
                val id = cursor.getLong(idColumn)
                val title = cursor.getString(titleColumn) ?: "Unknown Title"
                val artist = cursor.getString(artistColumn) ?: "Unknown Artist"
                val album = cursor.getString(albumColumn) ?: "Unknown Album"
                val albumId = cursor.getLong(albumIdColumn)
                val duration = cursor.getLong(durationColumn)
                val data = cursor.getString(dataColumn) ?: ""
                val dateAdded = cursor.getLong(dateAddedColumn)
                val size = cursor.getLong(sizeColumn)

                songList.add(Song(id, title, artist, album, albumId, duration, data, dateAdded, size))
            }
        }
        _songs.value = songList

        // Group into albums
        _albums.value = songList.groupBy { it.albumId }.map { (albumId, songs) ->
            val first = songs.first()
            Album(albumId, first.album, first.artist, songs.size)
        }.sortedBy { it.name }

        // Group into artists
        _artists.value = songList.groupBy { it.artist }.map { (artistName, songs) ->
            val albumCount = songs.distinctBy { it.albumId }.size
            Artist(songs.first().id, artistName, songs.size, albumCount)
        }.sortedBy { it.name }
    }

    suspend fun toggleFavorite(songId: Long, isCurrentlyFavorite: Boolean) {
        if (isCurrentlyFavorite) {
            dao.removeFavorite(songId)
        } else {
            dao.addFavorite(FavoriteEntity(songId))
        }
    }

    fun isFavorite(songId: Long) = dao.isFavorite(songId)

    suspend fun addRecentlyPlayed(songId: Long) {
        dao.addRecentlyPlayed(RecentlyPlayedEntity(songId, System.currentTimeMillis()))
    }
    
    suspend fun createPlaylist(name: String) {
        dao.insertPlaylist(PlaylistEntity(name = name, dateCreated = System.currentTimeMillis()))
    }

    fun getSongsForPlaylist(playlistId: Long): Flow<List<Song>> = dao.getSongsInPlaylist(playlistId).map { ids ->
        val all = _songs.value
        ids.mapNotNull { id -> all.find { it.id == id } }
    }

    companion object {
        fun getAlbumArtUri(albumId: Long): Uri {
            val artworkUri = Uri.parse("content://media/external/audio/albumart")
            return ContentUris.withAppendedId(artworkUri, albumId)
        }
        fun getSongUri(songId: Long): Uri {
            return ContentUris.withAppendedId(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, songId)
        }
    }
}
