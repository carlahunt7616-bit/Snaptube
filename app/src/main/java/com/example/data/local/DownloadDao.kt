package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface DownloadDao {

    @Query("SELECT * FROM download_tasks ORDER BY createdAt DESC")
    fun observeAllTasks(): Flow<List<DownloadTaskEntity>>

    @Query("SELECT * FROM download_tasks WHERE status IN ('QUEUED', 'DOWNLOADING', 'PAUSED', 'FAILED') ORDER BY createdAt DESC")
    fun observeActiveTasks(): Flow<List<DownloadTaskEntity>>

    @Query("SELECT * FROM download_tasks WHERE status = 'COMPLETED' ORDER BY completedAt DESC, createdAt DESC")
    fun observeCompletedTasks(): Flow<List<DownloadTaskEntity>>

    @Query("SELECT * FROM download_tasks WHERE id = :id LIMIT 1")
    suspend fun getById(id: Long): DownloadTaskEntity?

    @Query("SELECT * FROM download_tasks WHERE mediaUrl = :mediaUrl AND qualityLabel = :qualityLabel AND status IN ('QUEUED', 'DOWNLOADING', 'PAUSED') LIMIT 1")
    suspend fun findActiveDuplicate(mediaUrl: String, qualityLabel: String): DownloadTaskEntity?

    @Query("SELECT * FROM download_tasks WHERE status = 'QUEUED' ORDER BY createdAt ASC")
    suspend fun getQueuedTasks(): List<DownloadTaskEntity>

    @Query("SELECT * FROM download_tasks WHERE status = 'DOWNLOADING'")
    suspend fun getDownloadingTasks(): List<DownloadTaskEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(task: DownloadTaskEntity): Long

    @Update
    suspend fun update(task: DownloadTaskEntity)

    @Query(
        """
        UPDATE download_tasks
        SET downloadedBytes = :downloadedBytes,
            totalBytes = :totalBytes,
            speedBytesPerSec = :speedBytesPerSec,
            etaSeconds = :etaSeconds,
            status = :status,
            supportsResume = :supportsResume
        WHERE id = :id
        """
    )
    suspend fun updateProgress(
        id: Long,
        downloadedBytes: Long,
        totalBytes: Long,
        speedBytesPerSec: Long,
        etaSeconds: Long,
        status: String,
        supportsResume: Boolean
    )

    @Query(
        """
        UPDATE download_tasks
        SET status = :status,
            speedBytesPerSec = 0,
            etaSeconds = -1,
            errorMessage = :errorMessage
        WHERE id = :id
        """
    )
    suspend fun updateStatus(id: Long, status: String, errorMessage: String? = null)

    @Query(
        """
        UPDATE download_tasks
        SET status = 'COMPLETED',
            downloadedBytes = :finalSize,
            totalBytes = :finalSize,
            speedBytesPerSec = 0,
            etaSeconds = 0,
            localUri = :localUri,
            fileName = :fileName,
            errorMessage = NULL,
            completedAt = :completedAt
        WHERE id = :id
        """
    )
    suspend fun markCompleted(
        id: Long,
        finalSize: Long,
        localUri: String,
        fileName: String,
        completedAt: Long
    )

    @Query("UPDATE download_tasks SET title = :newTitle, fileName = :newFileName, localUri = :newUri WHERE id = :id")
    suspend fun updateFileMetadata(id: Long, newTitle: String, newFileName: String, newUri: String?)

    @Query("DELETE FROM download_tasks WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("DELETE FROM download_tasks WHERE status IN ('COMPLETED', 'FAILED', 'CANCELED')")
    suspend fun clearHistory()
}
