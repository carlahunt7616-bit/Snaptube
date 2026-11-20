package com.example.download

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import com.example.data.local.DownloadRepository
import com.example.data.local.DownloadTaskEntity
import com.example.data.local.SettingsRepository
import com.example.data.model.AnalyzedMedia
import com.example.data.model.DownloadStatus
import com.example.data.model.FormatUtils
import com.example.data.model.MediaOption
import com.example.notification.NotificationHelper
import com.example.storage.MediaStorageManager
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.FileOutputStream
import java.io.IOException
import java.util.concurrent.ConcurrentHashMap

sealed class EnqueueResult {
    data class Enqueued(val taskId: Long) : EnqueueResult()
    data object DuplicateActive : EnqueueResult()
    data object WifiRequired : EnqueueResult()
}

class DownloadEngine(
    private val context: Context,
    private val okHttpClient: OkHttpClient,
    private val downloadRepository: DownloadRepository,
    private val settingsRepository: SettingsRepository,
    private val storageManager: MediaStorageManager,
    private val notificationHelper: NotificationHelper
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val schedulerMutex = Mutex()
    private val activeJobs = ConcurrentHashMap<Long, Job>()
    private val activeTaskSnapshots = ConcurrentHashMap<Long, DownloadTaskEntity>()
    private val pausedFlags = ConcurrentHashMap<Long, Boolean>()
    private val canceledFlags = ConcurrentHashMap<Long, Boolean>()

    private val _activeCount = MutableStateFlow(0)
    val activeCount: StateFlow<Int> = _activeCount.asStateFlow()

    fun recoverInterruptedTasks() {
        scope.launch {
            val staleDownloading = downloadRepository.getDownloadingTasks()
            for (task in staleDownloading) {
                val partFile = storageManager.getPartialFile(task.id)
                if (partFile.exists() && partFile.length() > 0L && task.supportsResume) {
                    downloadRepository.updateStatus(task.id, DownloadStatus.PAUSED.key, null)
                } else {
                    downloadRepository.updateStatus(
                        task.id,
                        DownloadStatus.PAUSED.key,
                        "Paused after app restart"
                    )
                }
            }
            scheduleNext()
        }
    }

    suspend fun enqueueDownload(media: AnalyzedMedia, option: MediaOption): EnqueueResult {
        val existing = downloadRepository.findActiveDuplicate(option.downloadUrl, option.qualityLabel)
        if (existing != null) {
            return EnqueueResult.DuplicateActive
        }

        val settings = settingsRepository.getSnapshot()
        if (settings.wifiOnly && !isOnWifi()) {
            return EnqueueResult.WifiRequired
        }

        val fileName = FormatUtils.sanitizeFileName(media.title, option.format.ext)
        val entity = DownloadTaskEntity(
            sourceUrl = media.sourceUrl,
            mediaUrl = option.downloadUrl,
            title = media.title,
            fileName = fileName,
            platformId = media.platform.id,
            thumbnailUrl = media.thumbnailUrl,
            mediaType = option.mediaType.key,
            formatExt = option.format.ext,
            qualityLabel = option.qualityLabel,
            totalBytes = option.estimatedBytes,
            downloadedBytes = 0L,
            status = DownloadStatus.QUEUED.key,
            supportsResume = option.supportsRange,
            createdAt = System.currentTimeMillis()
        )
        val taskId = downloadRepository.insert(entity)
        scheduleNext()
        return EnqueueResult.Enqueued(taskId)
    }

    fun pauseDownload(taskId: Long) {
        pausedFlags[taskId] = true
        activeJobs[taskId]?.cancel()
        scope.launch {
            downloadRepository.updateStatus(taskId, DownloadStatus.PAUSED.key, null)
            activeTaskSnapshots.remove(taskId)
            refreshServiceNotification()
            scheduleNext()
        }
    }

    fun resumeDownload(taskId: Long) {
        scope.launch {
            val settings = settingsRepository.getSnapshot()
            if (settings.wifiOnly && !isOnWifi()) {
                downloadRepository.updateStatus(
                    taskId,
                    DownloadStatus.PAUSED.key,
                    "Waiting for Wi-Fi connection"
                )
                return@launch
            }
            pausedFlags.remove(taskId)
            canceledFlags.remove(taskId)
            downloadRepository.updateStatus(taskId, DownloadStatus.QUEUED.key, null)
            scheduleNext()
        }
    }

    fun retryDownload(taskId: Long) {
        scope.launch {
            pausedFlags.remove(taskId)
            canceledFlags.remove(taskId)
            val partFile = storageManager.getPartialFile(taskId)
            val task = downloadRepository.getById(taskId)
            if (task != null && !task.supportsResume && partFile.exists()) {
                partFile.delete()
            }
            downloadRepository.updateStatus(taskId, DownloadStatus.QUEUED.key, null)
            scheduleNext()
        }
    }

    fun cancelDownload(taskId: Long) {
        canceledFlags[taskId] = true
        activeJobs[taskId]?.cancel()
        scope.launch {
            storageManager.getPartialFile(taskId).delete()
            activeTaskSnapshots.remove(taskId)
            downloadRepository.deleteById(taskId)
            refreshServiceNotification()
            scheduleNext()
        }
    }

    fun onConcurrencyLimitChanged() {
        scope.launch { scheduleNext() }
    }

    fun getPrimaryActiveTask(): DownloadTaskEntity? =
        activeTaskSnapshots.values.minByOrNull { it.createdAt }

    private suspend fun scheduleNext() {
        schedulerMutex.withLock {
            val settings = settingsRepository.getSnapshot()
            val maxConcurrent = settings.maxConcurrentDownloads.coerceIn(1, 4)
            val queued = downloadRepository.getQueuedTasks()

            for (task in queued) {
                if (activeJobs.size >= maxConcurrent) break
                if (activeJobs.containsKey(task.id)) continue

                if (settings.wifiOnly && !isOnWifi()) {
                    downloadRepository.updateStatus(
                        task.id,
                        DownloadStatus.PAUSED.key,
                        "Wi-Fi Only mode active"
                    )
                    continue
                }

                startTaskJob(task)
            }
            _activeCount.value = activeJobs.size
            if (activeJobs.isNotEmpty()) {
                DownloadService.start(context)
            } else {
                DownloadService.stop(context)
            }
        }
    }

    private fun startTaskJob(initialTask: DownloadTaskEntity) {
        val taskId = initialTask.id
        pausedFlags.remove(taskId)
        canceledFlags.remove(taskId)

        val job = scope.launch {
            try {
                executeRealDownload(initialTask)
            } catch (ce: CancellationException) {
                if (canceledFlags.remove(taskId) == true) {
                    storageManager.getPartialFile(taskId).delete()
                } else if (pausedFlags.remove(taskId) == true) {
                    downloadRepository.updateStatus(taskId, DownloadStatus.PAUSED.key, null)
                }
                throw ce
            } catch (e: Exception) {
                val message = e.message?.take(140) ?: "Network error during download"
                downloadRepository.updateStatus(taskId, DownloadStatus.FAILED.key, message)
                val settings = settingsRepository.getSnapshot()
                if (settings.notificationsEnabled) {
                    val latest = downloadRepository.getById(taskId) ?: initialTask
                    notificationHelper.showDownloadFailedNotification(latest, message)
                }
            } finally {
                activeJobs.remove(taskId)
                activeTaskSnapshots.remove(taskId)
                _activeCount.value = activeJobs.size
                refreshServiceNotification()
                scheduleNext()
            }
        }
        activeJobs[taskId] = job
    }

    private suspend fun executeRealDownload(task: DownloadTaskEntity) {
        val partFile = storageManager.getPartialFile(task.id)
        var existingBytes = if (partFile.exists()) partFile.length() else 0L

        val requestBuilder = Request.Builder()
            .url(task.mediaUrl)
            .header(
                "User-Agent",
                "Mozilla/5.0 (Linux; Android 14; Mobile) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/131.0.0.0 Mobile Safari/537.36"
            )

        if (existingBytes > 0L) {
            requestBuilder.header("Range", "bytes=$existingBytes-")
        }

        val call = okHttpClient.newCall(requestBuilder.build())
        call.execute().use { response ->
            if (!response.isSuccessful && response.code != 206) {
                if (response.code == 416 && existingBytes > 0L) {
                    // Partial file mismatch; reset and restart cleanly
                    partFile.delete()
                    existingBytes = 0L
                }
                throw IOException("Server returned HTTP ${response.code}")
            }

            val isResumed = response.code == 206 && existingBytes > 0L
            if (!isResumed && existingBytes > 0L) {
                // Server ignored Range header; start fresh from byte 0
                partFile.delete()
                existingBytes = 0L
            }

            val body = response.body ?: throw IOException("Empty response body")
            val contentLength = body.contentLength()
            val totalBytes = when {
                isResumed && contentLength > 0L -> existingBytes + contentLength
                contentLength > 0L -> contentLength
                task.totalBytes > 0L -> task.totalBytes
                else -> -1L
            }
            val acceptsRange = response.code == 206 ||
                response.header("Accept-Ranges")?.contains("bytes", ignoreCase = true) == true

            downloadRepository.updateProgress(
                id = task.id,
                downloadedBytes = existingBytes,
                totalBytes = totalBytes,
                speedBytesPerSec = 0L,
                etaSeconds = -1L,
                status = DownloadStatus.DOWNLOADING.key,
                supportsResume = acceptsRange
            )

            var downloaded = existingBytes
            var lastEmitTime = System.currentTimeMillis()
            var lastEmitBytes = downloaded
            var smoothedSpeed = 0L
            val buffer = ByteArray(16 * 1024)

            body.byteStream().use { input ->
                FileOutputStream(partFile, isResumed).use { output ->
                    while (true) {
                        if (pausedFlags[task.id] == true || canceledFlags[task.id] == true) {
                            call.cancel()
                            throw CancellationException("User paused or canceled")
                        }

                        val read = input.read(buffer)
                        if (read == -1) break

                        output.write(buffer, 0, read)
                        downloaded += read

                        val now = System.currentTimeMillis()
                        val elapsedMs = now - lastEmitTime
                        if (elapsedMs >= PROGRESS_THROTTLE_MS) {
                            val deltaBytes = downloaded - lastEmitBytes
                            val instantSpeed = (deltaBytes * 1000L) / elapsedMs.coerceAtLeast(1L)
                            smoothedSpeed = if (smoothedSpeed == 0L) {
                                instantSpeed
                            } else {
                                ((smoothedSpeed * 7) + (instantSpeed * 3)) / 10
                            }
                            val eta = if (totalBytes > downloaded && smoothedSpeed > 0L) {
                                (totalBytes - downloaded) / smoothedSpeed
                            } else {
                                -1L
                            }

                            downloadRepository.updateProgress(
                                id = task.id,
                                downloadedBytes = downloaded,
                                totalBytes = totalBytes,
                                speedBytesPerSec = smoothedSpeed,
                                etaSeconds = eta,
                                status = DownloadStatus.DOWNLOADING.key,
                                supportsResume = acceptsRange
                            )

                            val snapshot = task.copy(
                                downloadedBytes = downloaded,
                                totalBytes = totalBytes,
                                speedBytesPerSec = smoothedSpeed,
                                etaSeconds = eta,
                                status = DownloadStatus.DOWNLOADING.key,
                                supportsResume = acceptsRange
                            )
                            activeTaskSnapshots[task.id] = snapshot
                            refreshServiceNotification()

                            lastEmitTime = now
                            lastEmitBytes = downloaded
                        }
                    }
                    output.flush()
                }
            }

            // Finalize into MediaStore or user-selected SAF folder
            val settings = settingsRepository.getSnapshot()
            val saved = storageManager.saveCompletedFile(
                sourcePartFile = partFile,
                fileName = task.fileName,
                format = task.formatEnum,
                safTreeUriString = settings.downloadFolderUri
            ).getOrThrow()

            val finalSize = if (downloaded > 0L) downloaded else totalBytes.coerceAtLeast(0L)
            val completedAt = System.currentTimeMillis()
            downloadRepository.markCompleted(
                id = task.id,
                finalSize = finalSize,
                localUri = saved.first,
                fileName = saved.second,
                completedAt = completedAt
            )

            if (settings.notificationsEnabled) {
                val completedTask = task.copy(
                    status = DownloadStatus.COMPLETED.key,
                    downloadedBytes = finalSize,
                    totalBytes = finalSize,
                    localUri = saved.first,
                    fileName = saved.second,
                    completedAt = completedAt
                )
                notificationHelper.showDownloadCompletedNotification(completedTask)
            }
        }
    }

    private fun refreshServiceNotification() {
        val count = activeJobs.size
        if (count > 0) {
            notificationHelper.updateForegroundNotification(getPrimaryActiveTask(), count)
        }
    }

    fun isOnWifi(): Boolean {
        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = cm.activeNetwork ?: return false
        val caps = cm.getNetworkCapabilities(network) ?: return false
        return caps.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) ||
            caps.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET)
    }

    companion object {
        private const val PROGRESS_THROTTLE_MS = 400L
    }
}
