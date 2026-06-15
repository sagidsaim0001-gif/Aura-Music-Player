package com.example.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.core.model.*

@Database(
    entities = [
        PlaylistEntity::class,
        PlaylistSongEntity::class,
        RecentlyPlayedEntity::class,
        FavoriteEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AuraDatabase : RoomDatabase() {
    abstract fun auraDao(): AuraDao
}
