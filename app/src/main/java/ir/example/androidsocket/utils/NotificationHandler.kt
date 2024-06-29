package ir.example.androidsocket.utils

import android.Manifest
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.pm.PackageManager
import android.media.RingtoneManager
import android.os.Build
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationManagerCompat
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


    fun displayNotification(
        context: Context,
        notificationId: Int,
        title: String,
        message: String,
        onContentIntent: (Context) -> PendingIntent?
    ) {
        val notification =
            createNotification(
                title = title,
                message = message,
                onContentIntent = onContentIntent
            )

        val notificationManager = NotificationManagerCompat.from(context)

        //checks whether the app has the necessary permission to post notifications
        if (ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }
        notificationManager.notify(notificationId, notification)
    }


}