package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.data.model.DownloadStatus
import com.example.data.model.MediaFormat
import com.example.data.model.MediaType
import com.example.data.model.PlatformType

@Entity(tableName = "download_tasks")
data class DownloadTaskEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val sourceUrl: String,
    val mediaUrl: String,
    val title: String,
    val fileName: String,
    val platformId: String,
    val thumbnailUrl: String? = null,
    val mediaType: String,
    val formatExt: String,
    val qualityLabel: String,
    val totalBytes: Long = -1L,
    val downloadedBytes: Long = 0L,
    val speedBytesPerSec: Long = 0L,
    val etaSeconds: Long = -1L,
    val status: String = DownloadStatus.QUEUED.key,
    val localUri: String? = null,
    val supportsResume: Boolean = false,
    val errorMessage: String? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val completedAt: Long? = null
) {
    val statusEnum: DownloadStatus get() = DownloadStatus.fromKey(status)
    val mediaTypeEnum: MediaType get() = MediaType.fromKey(mediaType)
    val formatEnum: MediaFormat get() = MediaFormat.fromExtension(formatExt)
    val platformEnum: PlatformType get() = PlatformType.fromId(platformId)

    val progressFraction: Float
        get() = when {
            statusEnum == DownloadStatus.COMPLETED -> 1f
            totalBytes > 0L -> (downloadedBytes.toFloat() / totalBytes.toFloat()).coerceIn(0f, 1f)
            else -> 0f
        }

    val progressPercent: Int
        get() = (progressFraction * 100).toInt().coerceIn(0, 100)
}
