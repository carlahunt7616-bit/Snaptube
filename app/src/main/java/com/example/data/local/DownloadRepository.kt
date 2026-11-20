package com.example.data.local

import kotlinx.coroutines.flow.Flow

class DownloadRepository(private val dao: DownloadDao) {

    val allTasks: Flow<List<DownloadTaskEntity>> = dao.observeAllTasks()
    val activeTasks: Flow<List<DownloadTaskEntity>> = dao.observeActiveTasks()
    val completedTasks: Flow<List<DownloadTaskEntity>> = dao.observeCompletedTasks()

    suspend fun getById(id: Long): DownloadTaskEntity? = dao.getById(id)

    suspend fun findActiveDuplicate(mediaUrl: String, qualityLabel: String): DownloadTaskEntity? =
        dao.findActiveDuplicate(mediaUrl, qualityLabel)

    suspend fun getQueuedTasks(): List<DownloadTaskEntity> = dao.getQueuedTasks()

    suspend fun getDownloadingTasks(): List<DownloadTaskEntity> = dao.getDownloadingTasks()

    suspend fun insert(task: DownloadTaskEntity): Long = dao.insert(task)

    suspend fun update(task: DownloadTaskEntity) = dao.update(task)

    suspend fun updateProgress(
        id: Long,
        downloadedBytes: Long,
        totalBytes: Long,
        speedBytesPerSec: Long,
        etaSeconds: Long,
        status: String,
        supportsResume: Boolean
    ) = dao.updateProgress(id, downloadedBytes, totalBytes, speedBytesPerSec, etaSeconds, status, supportsResume)

    suspend fun updateStatus(id: Long, status: String, errorMessage: String? = null) =
        dao.updateStatus(id, status, errorMessage)

    suspend fun markCompleted(
        id: Long,
        finalSize: Long,
        localUri: String,
        fileName: String,
        completedAt: Long
    ) = dao.markCompleted(id, finalSize, localUri, fileName, completedAt)

    suspend fun updateFileMetadata(id: Long, newTitle: String, newFileName: String, newUri: String?) =
        dao.updateFileMetadata(id, newTitle, newFileName, newUri)

    suspend fun deleteById(id: Long) = dao.deleteById(id)

    suspend fun clearHistory() = dao.clearHistory()
}
