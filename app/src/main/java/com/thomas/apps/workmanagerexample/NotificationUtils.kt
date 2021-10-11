package com.thomas.apps.workmanagerexample

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationChannelGroup
import android.app.NotificationManager
import android.content.Context
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.thomas.apps.workmanagerexample.model.WorkLog
import java.time.Instant
import java.time.ZoneId

object NotificationUtils {
    const val CHANNEL_ID = "Log Channel ID3"
    const val CHANNEL_NAME = "Log Channel3 "
    const val CHANNEL_DESCRIPTION = "aa"
    const val NOTIFICATION_GROUP_ID = "LOG_NOTIFICATION_GROUP_ID"
    const val NOTIFICATION_GROUP_NAME = "Log notification group"

    private const val NOTIFICATION_ID = 100

    private fun Context.buildNotification(log: WorkLog): Notification {
        val title = if (log.runFail) "Run fail" else "Run success"
        val time = Instant.now().atZone(ZoneId.systemDefault()).toLocalDateTime()
        val content = "Time: $time"
        val bigContent = log.toString()

        val builder = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.mipmap.notification)
            .setContentTitle(title)
            .setContentText(content)
//            .setStyle(
//                NotificationCompat.BigTextStyle()
//                    .bigText(bigContent)
//            )
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setChannelId(CHANNEL_ID)
            .setGroup(NOTIFICATION_GROUP_ID)
            .setCategory(NotificationCompat.CATEGORY_PROGRESS)
        return builder.build()
    }

    fun Context.createLogNotificationChannel() {
        createNotificationChannel(
            CHANNEL_ID,
            CHANNEL_NAME,
            CHANNEL_DESCRIPTION,
            NotificationCompat.PRIORITY_DEFAULT
        )
    }

    fun Context.createLogNotificationChannelGroup() {
        createNotificationGroup(
            NOTIFICATION_GROUP_ID,
            NOTIFICATION_GROUP_NAME
        )
    }


    private fun Context.createNotificationChannel(
        channelId: String,
        channelName: String,
        description: String,
        importance: Int
    ) {
        val channel = NotificationChannel(channelId, channelName, importance).apply {
            this.description = description
            this.enableVibration(true)
            this.enableLights(true)
            this.group = NOTIFICATION_GROUP_ID
        }
        // Register the channel with the system
        val notificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(channel)
    }

    private fun Context.createNotificationGroup(
        groupId: String,
        groupName: String,
    ) {
        val group = NotificationChannelGroup(groupId, groupName)
        // Register the channel with the system
        val notificationManager: NotificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannelGroup(group)
    }

    fun Context.showNotification(log: WorkLog) {
        with(NotificationManagerCompat.from(this)) {
            // notificationId is a unique int for each notification that you must define
            notify(log.id.toInt(), buildNotification(log))
        }
    }
}