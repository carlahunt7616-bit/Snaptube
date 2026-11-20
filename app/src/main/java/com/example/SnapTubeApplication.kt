package com.example

import android.app.Application
import com.example.analyzer.MediaLinkAnalyzer
import com.example.data.local.DownloadRepository
import com.example.data.local.SettingsRepository
import com.example.data.local.SnapTubeDatabase
import com.example.download.DownloadEngine
import com.example.notification.NotificationHelper
import com.example.storage.MediaStorageManager
import okhttp3.OkHttpClient
import java.util.concurrent.TimeUnit

class SnapTubeApplication : Application() {

    lateinit var okHttpClient: OkHttpClient
        private set
    lateinit var database: SnapTubeDatabase
        private set
    lateinit var settingsRepository: SettingsRepository
        private set
    lateinit var downloadRepository: DownloadRepository
        private set
    lateinit var storageManager: MediaStorageManager
        private set
    lateinit var notificationHelper: NotificationHelper
        private set
    lateinit var mediaLinkAnalyzer: MediaLinkAnalyzer
        private set
    lateinit var downloadEngine: DownloadEngine
        private set

    override fun onCreate() {
        super.onCreate()
        okHttpClient = OkHttpClient.Builder()
            .connectTimeout(20, TimeUnit.SECONDS)
            .readTimeout(60, TimeUnit.SECONDS)
            .followRedirects(true)
            .followSslRedirects(true)
            .build()

        database = SnapTubeDatabase.getInstance(this)
        settingsRepository = SettingsRepository(this)
        downloadRepository = DownloadRepository(database.downloadDao())
        storageManager = MediaStorageManager(this)
        notificationHelper = NotificationHelper(this).apply { createChannels() }
        mediaLinkAnalyzer = MediaLinkAnalyzer(okHttpClient)
        downloadEngine = DownloadEngine(
            context = this,
            okHttpClient = okHttpClient,
            downloadRepository = downloadRepository,
            settingsRepository = settingsRepository,
            storageManager = storageManager,
            notificationHelper = notificationHelper
        )
        downloadEngine.recoverInterruptedTasks()
    }
}
