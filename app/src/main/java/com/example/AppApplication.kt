package com.example

import android.app.Application
import androidx.room.Room
import com.example.data.local.AuraDatabase
import com.example.data.repository.AudioRepository

interface AppContainer {
    val audioRepository: AudioRepository
}

class DefaultAppContainer(private val application: Application) : AppContainer {
    private val database by lazy {
        Room.databaseBuilder(application, AuraDatabase::class.java, "aura_db").build()
    }

    override val audioRepository: AudioRepository by lazy {
        AudioRepository(application, database.auraDao())
    }
}

class AppApplication : Application() {
    lateinit var container: AppContainer

    override fun onCreate() {
        super.onCreate()
        container = DefaultAppContainer(this)
    }
}
