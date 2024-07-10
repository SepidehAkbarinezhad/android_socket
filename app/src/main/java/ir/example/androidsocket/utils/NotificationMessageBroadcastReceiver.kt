package ir.example.androidsocket.utils

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import ir.example.androidsocket.Constants

class NotificationMessageBroadcastReceiver(
    private val onMessageReceivedAction: () -> Unit = {},
) : BroadcastReceiver() {

    override fun onReceive(context: Context?, intent: Intent?) {
        serverLog(message = "NotificationBroadcastReceiver onReceive")

        val packageName = context?.packageName
        packageName?.let {
            when (intent?.action) {
                Constants.ActionCode.NotificationMessage.title -> {
                    onMessageReceivedAction()
                }

                else -> {}
            }
        }
    }

}
