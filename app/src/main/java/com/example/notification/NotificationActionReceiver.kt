package com.example.notification

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationManagerCompat
import com.example.SnapTubeApplication

class NotificationActionReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val app = context.applicationContext as? SnapTubeApplication ?: return
        when (intent.action) {
            ACTION_PAUSE -> {
                val taskId = intent.getLongExtra(EXTRA_TASK_ID, -1L)
                if (taskId > 0L) app.downloadEngine.pauseDownload(taskId)
            }
            ACTION_RESUME -> {
                val taskId = intent.getLongExtra(EXTRA_TASK_ID, -1L)
                if (taskId > 0L) app.downloadEngine.resumeDownload(taskId)
            }
            ACTION_CANCEL -> {
                val taskId = intent.getLongExtra(EXTRA_TASK_ID, -1L)
                if (taskId > 0L) app.downloadEngine.cancelDownload(taskId)
            }
            ACTION_DISMISS_SHARE -> {
                val notifId = intent.getIntExtra(EXTRA_NOTIFICATION_ID, NotificationHelper.NOTIFICATION_ID_SHARE)
                NotificationManagerCompat.from(context).cancel(notifId)
            }
        }
    }

    companion object {
        const val ACTION_PAUSE = "com.example.snaptube.action.PAUSE"
        const val ACTION_RESUME = "com.example.snaptube.action.RESUME"
        const val ACTION_CANCEL = "com.example.snaptube.action.CANCEL"
        const val ACTION_DISMISS_SHARE = "com.example.snaptube.action.DISMISS_SHARE"
        const val EXTRA_TASK_ID = "extra_task_id"
        const val EXTRA_NOTIFICATION_ID = "extra_notification_id"
    }
}
