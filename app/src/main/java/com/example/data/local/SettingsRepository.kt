package com.example.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.data.model.AppLanguage
import com.example.data.model.ThemeMode
import com.example.data.model.UserSettings
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

private val Context.snapTubeDataStore: DataStore<Preferences> by preferencesDataStore(name = "snaptube_settings")

class SettingsRepository(private val context: Context) {

    private object Keys {
        val THEME_MODE = stringPreferencesKey("theme_mode")
        val LANGUAGE = stringPreferencesKey("language")
        val DEFAULT_VIDEO_QUALITY = stringPreferencesKey("default_video_quality")
        val DEFAULT_AUDIO_QUALITY = stringPreferencesKey("default_audio_quality")
        val DOWNLOAD_FOLDER_URI = stringPreferencesKey("download_folder_uri")
        val DOWNLOAD_FOLDER_LABEL = stringPreferencesKey("download_folder_label")
        val MAX_CONCURRENT = intPreferencesKey("max_concurrent_downloads")
        val WIFI_ONLY = booleanPreferencesKey("wifi_only")
        val NOTIFICATIONS_ENABLED = booleanPreferencesKey("notifications_enabled")
    }

    val settingsFlow: Flow<UserSettings> = context.snapTubeDataStore.data.map { prefs ->
        UserSettings(
            themeMode = ThemeMode.fromKey(prefs[Keys.THEME_MODE]),
            language = AppLanguage.fromCode(prefs[Keys.LANGUAGE]),
            defaultVideoQuality = prefs[Keys.DEFAULT_VIDEO_QUALITY] ?: "720p HD",
            defaultAudioQuality = prefs[Keys.DEFAULT_AUDIO_QUALITY] ?: "192 kbps",
            downloadFolderUri = prefs[Keys.DOWNLOAD_FOLDER_URI],
            downloadFolderLabel = prefs[Keys.DOWNLOAD_FOLDER_LABEL],
            maxConcurrentDownloads = (prefs[Keys.MAX_CONCURRENT] ?: 2).coerceIn(1, 4),
            wifiOnly = prefs[Keys.WIFI_ONLY] ?: false,
            notificationsEnabled = prefs[Keys.NOTIFICATIONS_ENABLED] ?: true
        )
    }

    suspend fun getSnapshot(): UserSettings = settingsFlow.first()

    suspend fun setThemeMode(mode: ThemeMode) {
        context.snapTubeDataStore.edit { it[Keys.THEME_MODE] = mode.key }
    }

    suspend fun setLanguage(language: AppLanguage) {
        context.snapTubeDataStore.edit { it[Keys.LANGUAGE] = language.code }
    }

    suspend fun setDefaultVideoQuality(quality: String) {
        context.snapTubeDataStore.edit { it[Keys.DEFAULT_VIDEO_QUALITY] = quality }
    }

    suspend fun setDefaultAudioQuality(quality: String) {
        context.snapTubeDataStore.edit { it[Keys.DEFAULT_AUDIO_QUALITY] = quality }
    }

    suspend fun setDownloadFolder(uri: String?, label: String?) {
        context.snapTubeDataStore.edit { prefs ->
            if (uri == null) {
                prefs.remove(Keys.DOWNLOAD_FOLDER_URI)
                prefs.remove(Keys.DOWNLOAD_FOLDER_LABEL)
            } else {
                prefs[Keys.DOWNLOAD_FOLDER_URI] = uri
                if (label != null) {
                    prefs[Keys.DOWNLOAD_FOLDER_LABEL] = label
                }
            }
        }
    }

    suspend fun setMaxConcurrentDownloads(limit: Int) {
        context.snapTubeDataStore.edit { it[Keys.MAX_CONCURRENT] = limit.coerceIn(1, 4) }
    }

    suspend fun setWifiOnly(enabled: Boolean) {
        context.snapTubeDataStore.edit { it[Keys.WIFI_ONLY] = enabled }
    }

    suspend fun setNotificationsEnabled(enabled: Boolean) {
        context.snapTubeDataStore.edit { it[Keys.NOTIFICATIONS_ENABLED] = enabled }
    }
}
