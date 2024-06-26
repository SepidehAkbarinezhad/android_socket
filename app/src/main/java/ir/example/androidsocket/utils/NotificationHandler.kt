package ir.example.androidsocket.utils

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.media.RingtoneManager
import android.os.Build
import com.example.androidSocket.R

class NotificationHandler(val context: Context, private val channelId: String) {

    fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "SocketChannel",
                NotificationManager.IMPORTANCE_DEFAULT
            )
            val manager = context.getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }
    }

    fun createNotification(
        message: String,
        title: String = "android socket",
        onContentIntent: (Context) -> PendingIntent?
    ): Notification {
        serverLog("createNotification")
        return setNotification(
            builder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                Notification.Builder(context, channelId)
            else Notification.Builder(context),
            message = message,
            title = title,
            onContentIntent = onContentIntent
        )
    }

    private fun setNotification(
        builder: Notification.Builder,
        message: String,
        title: String,
        onContentIntent: (Context) -> PendingIntent?
    ): Notification {
        serverLog("setNotification")
        val soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)

        val notificationBuilder = builder.setContentTitle(title)
            .setContentText(message)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setSound(soundUri)

        notificationBuilder.setContentIntent(onContentIntent(context)).setAutoCancel(true)

        return notificationBuilder.build()
    }


}