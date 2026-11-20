package com.example.notification

import android.Manifest
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.example.MainActivity
import com.example.R
import com.example.data.local.DownloadTaskEntity
import com.example.data.model.FormatUtils

class NotificationHelper(private val context: Context) {

    fun createChannels() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val shareChannel = NotificationChannel(
            CHANNEL_SHARE,
            context.getString(R.string.notif_channel_share),
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = context.getString(R.string.notif_channel_share_desc)
        }

        val progressChannel = NotificationChannel(
            CHANNEL_PROGRESS,
            context.getString(R.string.notif_channel_progress),
            NotificationManager.IMPORTANCE_LOW
        ).apply {
            description = context.getString(R.string.notif_channel_progress_desc)
            setShowBadge(false)
        }

        val completeChannel = NotificationChannel(
            CHANNEL_COMPLETE,
            context.getString(R.string.notif_channel_complete),
            NotificationManager.IMPORTANCE_DEFAULT
        ).apply {
            description = context.getString(R.string.notif_channel_complete_desc)
        }

        manager.createNotificationChannels(listOf(shareChannel, progressChannel, completeChannel))
    }

    fun hasNotificationPermission(): Boolean {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) return true
        return ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.POST_NOTIFICATIONS
        ) == PackageManager.PERMISSION_GRANTED
    }

    fun showShareDetectedNotification(sharedUrl: String) {
        if (!hasNotificationPermission()) return

        val openIntent = Intent(context, MainActivity::class.java).apply {
            action = MainActivity.ACTION_OPEN_SHARED_URL
            putExtra(MainActivity.EXTRA_SHARED_URL, sharedUrl)
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val openPending = PendingIntent.getActivity(
            context,
            NOTIFICATION_ID_SHARE,
            openIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val dismissIntent = Intent(context, NotificationActionReceiver::class.java).apply {
            action = NotificationActionReceiver.ACTION_DISMISS_SHARE
            putExtra(NotificationActionReceiver.EXTRA_NOTIFICATION_ID, NOTIFICATION_ID_SHARE)
        }
        val dismissPending = PendingIntent.getBroadcast(
            context,
            NOTIFICATION_ID_SHARE + 1,
            dismissIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_SHARE)
            .setSmallIcon(R.drawable.ic_notification_snaptube)
            .setContentTitle(context.getString(R.string.notif_share_title))
            .setContentText(context.getString(R.string.notif_share_body))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(openPending)
            .addAction(0, context.getString(R.string.notif_action_open_download), openPending)
            .addAction(0, context.getString(R.string.notif_action_dismiss), dismissPending)
            .build()

        runCatching {
            NotificationManagerCompat.from(context).notify(NOTIFICATION_ID_SHARE, notification)
        }
    }

    fun cancelShareNotification() {
        NotificationManagerCompat.from(context).cancel(NOTIFICATION_ID_SHARE)
    }

    fun buildProgressNotification(primaryTask: DownloadTaskEntity?, activeCount: Int): Notification {
        val openManagerIntent = Intent(context, MainActivity::class.java).apply {
            action = MainActivity.ACTION_OPEN_DOWNLOADS
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val contentPending = PendingIntent.getActivity(
            context,
            NOTIFICATION_ID_FOREGROUND,
            openManagerIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val builder = NotificationCompat.Builder(context, CHANNEL_PROGRESS)
            .setSmallIcon(R.drawable.ic_notification_snaptube)
            .setOngoing(true)
            .setOnlyAlertOnce(true)
            .setContentIntent(contentPending)

        if (primaryTask == null) {
            builder
                .setContentTitle(context.getString(R.string.app_name))
                .setContentText(context.getString(R.string.notif_active_summary, activeCount.coerceAtLeast(1)))
                .setProgress(0, 0, true)
            return builder.build()
        }

        val percent = primaryTask.progressPercent
        val downloadedStr = FormatUtils.formatBytes(primaryTask.downloadedBytes)
        val totalStr = if (primaryTask.totalBytes > 0) " / ${FormatUtils.formatBytes(primaryTask.totalBytes)}" else ""
        val speedStr = if (primaryTask.speedBytesPerSec > 0) " • ${FormatUtils.formatSpeed(primaryTask.speedBytesPerSec)}" else ""
        val extraCount = if (activeCount > 1) " (+${activeCount - 1})" else ""

        val pauseIntent = Intent(context, NotificationActionReceiver::class.java).apply {
            action = NotificationActionReceiver.ACTION_PAUSE
            putExtra(NotificationActionReceiver.EXTRA_TASK_ID, primaryTask.id)
        }
        val pausePending = PendingIntent.getBroadcast(
            context,
            (primaryTask.id.toInt() * 10) + 1,
            pauseIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val cancelIntent = Intent(context, NotificationActionReceiver::class.java).apply {
            action = NotificationActionReceiver.ACTION_CANCEL
            putExtra(NotificationActionReceiver.EXTRA_TASK_ID, primaryTask.id)
        }
        val cancelPending = PendingIntent.getBroadcast(
            context,
            (primaryTask.id.toInt() * 10) + 2,
            cancelIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        builder
            .setContentTitle("${primaryTask.title}$extraCount")
            .setContentText("$percent% • $downloadedStr$totalStr$speedStr")
            .setProgress(100, percent, primaryTask.totalBytes <= 0L)
            .addAction(0, context.getString(R.string.action_pause), pausePending)
            .addAction(0, context.getString(R.string.action_cancel), cancelPending)

        return builder.build()
    }

    fun updateForegroundNotification(primaryTask: DownloadTaskEntity?, activeCount: Int) {
        if (!hasNotificationPermission()) return
        runCatching {
            NotificationManagerCompat.from(context).notify(
                NOTIFICATION_ID_FOREGROUND,
                buildProgressNotification(primaryTask, activeCount)
            )
        }
    }

    fun showDownloadCompletedNotification(task: DownloadTaskEntity) {
        if (!hasNotificationPermission()) return

        val openLibraryIntent = Intent(context, MainActivity::class.java).apply {
            action = MainActivity.ACTION_OPEN_COMPLETED
            putExtra(MainActivity.EXTRA_COMPLETED_TASK_ID, task.id)
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            NOTIFICATION_ID_COMPLETE_BASE + task.id.toInt(),
            openLibraryIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_COMPLETE)
            .setSmallIcon(R.drawable.ic_notification_snaptube)
            .setContentTitle(context.getString(R.string.notif_complete_title))
            .setContentText("${task.title} • ${FormatUtils.formatBytes(task.totalBytes)}")
            .setStyle(
                NotificationCompat.BigTextStyle()
                    .bigText("${context.getString(R.string.notif_complete_body)}\n${task.fileName}")
            )
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        runCatching {
            NotificationManagerCompat.from(context).notify(
                NOTIFICATION_ID_COMPLETE_BASE + task.id.toInt(),
                notification
            )
        }
    }

    fun showDownloadFailedNotification(task: DownloadTaskEntity, reason: String) {
        if (!hasNotificationPermission()) return

        val openDownloadsIntent = Intent(context, MainActivity::class.java).apply {
            action = MainActivity.ACTION_OPEN_DOWNLOADS
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            NOTIFICATION_ID_FAILED_BASE + task.id.toInt(),
            openDownloadsIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_COMPLETE)
            .setSmallIcon(R.drawable.ic_notification_snaptube)
            .setContentTitle(context.getString(R.string.notif_failed_title))
            .setContentText("${task.title}: $reason")
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        runCatching {
            NotificationManagerCompat.from(context).notify(
                NOTIFICATION_ID_FAILED_BASE + task.id.toInt(),
                notification
            )
        }
    }

    companion object {
        const val CHANNEL_SHARE = "snaptube_share_detected"
        const val CHANNEL_PROGRESS = "snaptube_download_progress"
        const val CHANNEL_COMPLETE = "snaptube_download_complete"

        const val NOTIFICATION_ID_FOREGROUND = 1001
        const val NOTIFICATION_ID_SHARE = 1002
        const val NOTIFICATION_ID_COMPLETE_BASE = 2000
        const val NOTIFICATION_ID_FAILED_BASE = 4000
    }
}
