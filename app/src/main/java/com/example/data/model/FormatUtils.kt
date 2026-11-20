package com.example.data.model

import java.net.URI
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.max

object FormatUtils {

    private val urlRegex = Regex(
        pattern = "(https?://[\\w\\-._~:/?#\\[\\]@!$&'()*+,;=%]+)",
        option = RegexOption.IGNORE_CASE
    )

    fun extractFirstHttpUrl(rawText: String?): String? {
        if (rawText.isNullOrBlank()) return null
        val match = urlRegex.find(rawText.trim()) ?: return null
        return match.value.trimEnd('.', ',', ')', ']', '"', '\'')
    }

    fun isValidHttpUrl(input: String): Boolean {
        val trimmed = input.trim()
        if (!trimmed.startsWith("http://", ignoreCase = true) &&
            !trimmed.startsWith("https://", ignoreCase = true)
        ) {
            return false
        }
        return runCatching {
            val uri = URI(trimmed)
            !uri.host.isNullOrBlank()
        }.getOrDefault(false)
    }

    fun formatBytes(bytes: Long): String {
        if (bytes < 0L) return ""
        val kb = 1024.0
        val mb = kb * 1024.0
        val gb = mb * 1024.0
        return when {
            bytes >= gb -> String.format(Locale.US, "%.2f GB", bytes / gb)
            bytes >= mb -> String.format(Locale.US, "%.1f MB", bytes / mb)
            bytes >= kb -> String.format(Locale.US, "%.0f KB", bytes / kb)
            else -> "$bytes B"
        }
    }

    fun formatSpeed(bytesPerSec: Long): String {
        if (bytesPerSec <= 0L) return "0 KB/s"
        val kb = bytesPerSec / 1024.0
        val mb = kb / 1024.0
        return when {
            mb >= 1.0 -> String.format(Locale.US, "%.1f MB/s", mb)
            kb >= 1.0 -> String.format(Locale.US, "%.0f KB/s", kb)
            else -> "$bytesPerSec B/s"
        }
    }

    fun formatEta(seconds: Long): String {
        if (seconds <= 0L) return ""
        val hrs = seconds / 3600
        val mins = (seconds % 3600) / 60
        val secs = seconds % 60
        return when {
            hrs > 0 -> String.format(Locale.US, "%dh %02dm", hrs, mins)
            mins > 0 -> String.format(Locale.US, "%dm %02ds", mins, secs)
            else -> "${max(1L, secs)}s"
        }
    }

    fun formatDuration(seconds: Long): String {
        if (seconds <= 0L) return ""
        val hrs = seconds / 3600
        val mins = (seconds % 3600) / 60
        val secs = seconds % 60
        return if (hrs > 0) {
            String.format(Locale.US, "%d:%02d:%02d", hrs, mins, secs)
        } else {
            String.format(Locale.US, "%02d:%02d", mins, secs)
        }
    }

    fun formatDate(timestampMillis: Long): String {
        val formatter = SimpleDateFormat("MMM d, yyyy", Locale.getDefault())
        return formatter.format(Date(timestampMillis))
    }

    fun sanitizeFileName(raw: String, extension: String): String {
        val base = raw
            .replace(Regex("[\\\\/:*?\"<>|\\n\\r]"), "_")
            .replace(Regex("\\s+"), " ")
            .trim()
            .take(80)
            .ifBlank { "SnapTube_${System.currentTimeMillis()}" }

        val cleanExt = extension.lowercase().trimStart('.')
        return if (base.endsWith(".$cleanExt", ignoreCase = true)) {
            base
        } else {
            "$base.$cleanExt"
        }
    }
}
