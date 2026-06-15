package com.example.core.navigation

import kotlinx.serialization.Serializable

@Serializable
object HomeRoute

@Serializable
object SongsRoute

@Serializable
object AlbumsRoute

@Serializable
object ArtistsRoute

@Serializable
object PlaylistsRoute

@Serializable
data class PlaylistDetailRoute(val playlistId: Long)

@Serializable
object SettingsRoute
