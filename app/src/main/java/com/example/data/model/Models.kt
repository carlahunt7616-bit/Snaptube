package com.example.data.model

enum class ThemeMode(val key: String) {
    SYSTEM("system"),
    LIGHT("light"),
    DARK("dark");

    companion object {
        fun fromKey(key: String?): ThemeMode =
            entries.firstOrNull { it.key == key } ?: LIGHT
    }
}

enum class AppLanguage(val code: String) {
    ENGLISH("en"),
    BENGALI("bn");

    companion object {
        fun fromCode(code: String?): AppLanguage =
            entries.firstOrNull { it.code == code } ?: ENGLISH
    }
}

enum class MediaType(val key: String) {
    VIDEO("VIDEO"),
    AUDIO("AUDIO");

    companion object {
        fun fromKey(key: String?): MediaType =
            entries.firstOrNull { it.key == key } ?: VIDEO
    }
}

enum class MediaFormat(val ext: String, val mimeType: String) {
    MP4("mp4", "video/mp4"),
    WEBM("webm", "video/webm"),
    MP3("mp3", "audio/mpeg"),
    M4A("m4a", "audio/mp4"),
    OGG("ogg", "audio/ogg");

    companion object {
        fun fromExtension(ext: String): MediaFormat {
            val clean = ext.lowercase().trimStart('.')
            return entries.firstOrNull { it.ext == clean } ?: MP4
        }

        fun fromMime(mime: String?): MediaFormat {
            val lower = mime?.lowercase().orEmpty()
            return when {
                lower.contains("audio/mpeg") || lower.contains("audio/mp3") -> MP3
                lower.contains("audio/mp4") || lower.contains("audio/m4a") || lower.contains("audio/aac") -> M4A
                lower.contains("audio/ogg") || lower.contains("audio/opus") -> OGG
                lower.contains("video/webm") -> WEBM
                else -> MP4
            }
        }
    }
}

enum class DownloadStatus(val key: String) {
    QUEUED("QUEUED"),
    DOWNLOADING("DOWNLOADING"),
    PAUSED("PAUSED"),
    COMPLETED("COMPLETED"),
    FAILED("FAILED"),
    CANCELED("CANCELED");

    val isActiveGroup: Boolean
        get() = this == QUEUED || this == DOWNLOADING || this == PAUSED || this == FAILED

    companion object {
        fun fromKey(key: String?): DownloadStatus =
            entries.firstOrNull { it.key == key } ?: FAILED
    }
}

enum class PlatformType(
    val id: String,
    val displayName: String,
    val shortBadge: String,
    val accentHex: Long,
    val hosts: List<String>
) {
    YOUTUBE("youtube", "YouTube", "YT", 0xFFFF0033, listOf("youtube.com", "youtu.be", "m.youtube.com")),
    FACEBOOK("facebook", "Facebook", "FB", 0xFF1877F2, listOf("facebook.com", "fb.watch", "m.facebook.com")),
    INSTAGRAM("instagram", "Instagram", "IG", 0xFFE1306C, listOf("instagram.com", "instagr.am")),
    TIKTOK("tiktok", "TikTok", "TT", 0xFF00F2EA, listOf("tiktok.com", "vm.tiktok.com")),
    X_TWITTER("x_twitter", "X / Twitter", "X", 0xFF1DA1F2, listOf("x.com", "twitter.com", "t.co")),
    PINTEREST("pinterest", "Pinterest", "PI", 0xFFE60023, listOf("pinterest.com", "pin.it")),
    REDDIT("reddit", "Reddit", "RD", 0xFFFF4500, listOf("reddit.com", "redd.it", "v.redd.it")),
    VIMEO("vimeo", "Vimeo", "VM", 0xFF1AB7EA, listOf("vimeo.com")),
    DAILYMOTION("dailymotion", "Dailymotion", "DM", 0xFF0066DC, listOf("dailymotion.com", "dai.ly")),
    DIRECT("direct", "Direct Media", "URL", 0xFF6C3CE9, emptyList());

    companion object {
        fun fromId(id: String?): PlatformType =
            entries.firstOrNull { it.id == id } ?: DIRECT
    }
}

data class MediaOption(
    val id: String,
    val mediaType: MediaType,
    val format: MediaFormat,
    val qualityLabel: String,
    val downloadUrl: String,
    val estimatedBytes: Long = -1L,
    val supportsRange: Boolean = false,
    val isExtraFormat: Boolean = false
)

data class AnalyzedMedia(
    val sourceUrl: String,
    val platform: PlatformType,
    val title: String,
    val thumbnailUrl: String? = null,
    val durationLabel: String? = null,
    val primarySizeBytes: Long = -1L,
    val options: List<MediaOption> = emptyList(),
    val isSupportedForDownload: Boolean = true,
    val unsupportedReason: String? = null
)

data class UserSettings(
    val themeMode: ThemeMode = ThemeMode.LIGHT,
    val language: AppLanguage = AppLanguage.ENGLISH,
    val defaultVideoQuality: String = "720p HD",
    val defaultAudioQuality: String = "192 kbps",
    val downloadFolderUri: String? = null,
    val downloadFolderLabel: String? = null,
    val maxConcurrentDownloads: Int = 2,
    val wifiOnly: Boolean = false,
    val notificationsEnabled: Boolean = true
)

enum class SortOption {
    NEWEST, NAME, SIZE
}

enum class LegalDocumentType {
    PRIVACY_POLICY,
    TERMS_AND_CONDITIONS,
    DISCLAIMER
}

data class SampleMediaItem(
    val title: String,
    val subtitle: String,
    val url: String,
    val mediaType: MediaType,
    val badge: String
)

object QualityPresets {
    val videoQualities = listOf("144p", "240p", "360p", "480p", "720p HD", "1080p Full HD", "2K", "4K")
    val audioQualities = listOf("96 kbps", "128 kbps", "192 kbps", "256 kbps", "320 kbps")
}
