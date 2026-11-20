package com.example.download

import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.IBinder
import androidx.core.content.ContextCompat
import com.example.SnapTubeApplication
import com.example.notification.NotificationHelper

class DownloadService : Service() {

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val app = applicationContext as SnapTubeApplication
        if (intent?.action == ACTION_STOP) {
            stopForeground(STOP_FOREGROUND_REMOVE)
            stopSelf()
            return START_NOT_STICKY
        }

        val notification = app.notificationHelper.buildProgressNotification(
            primaryTask = app.downloadEngine.getPrimaryActiveTask(),
            activeCount = app.downloadEngine.activeCount.value.coerceAtLeast(1)
        )

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            startForeground(
                NotificationHelper.NOTIFICATION_ID_FOREGROUND,
                notification,
                ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC
            )
        } else {
            startForeground(NotificationHelper.NOTIFICATION_ID_FOREGROUND, notification)
        }

        return START_NOT_STICKY
    }

    companion object {
        private const val ACTION_START = "com.example.snaptube.service.START"
        private const val ACTION_STOP = "com.example.snaptube.service.STOP"

        fun start(context: Context) {
            val intent = Intent(context, DownloadService::class.java).apply {
                action = ACTION_START
            }
            runCatching {
                ContextCompat.startForegroundService(context, intent)
            }
        }

        fun stop(context: Context) {
            val intent = Intent(context, DownloadService::class.java).apply {
                action = ACTION_STOP
            }
            runCatching {
                context.stopService(intent)
            }
        }
    }
}
