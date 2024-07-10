package ir.example.androidsocket.ui.server

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.ConnectivityManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.lifecycle.lifecycleScope
import dagger.hilt.android.AndroidEntryPoint
import ir.example.androidsocket.Constants
import ir.example.androidsocket.MainApplication
import ir.example.androidsocket.utils.ConnectionTypeManager
import ir.example.androidsocket.utils.NotificationMessageBroadcastReceiver
import ir.example.androidsocket.utils.serverLog
import kotlinx.coroutines.launch


@AndroidEntryPoint
class ServerActivity : ComponentActivity() {

    private val viewModel: ServerViewModel by viewModels()
    private lateinit var ConnectivityBroadcastReceiver: BroadcastReceiver
    private lateinit var notificationMessageReceiver: NotificationMessageBroadcastReceiver


    override fun onCreate(savedInstanceState: Bundle?) {

        val connectionTypeManager = ConnectionTypeManager(this)

        super.onCreate(savedInstanceState)

        setClientMessageBroadcastReceiver()
        setConnectivityBroadcastReceiver(connectionTypeManager)

        setContent {
            ServerComposable(
                viewModel = viewModel,
                connectionTypeManager = connectionTypeManager
            )
        }
    }

    override fun onDestroy() {
        serverLog("ServerActivity onDestroy ")
        lifecycleScope.launch {
            viewModel.performCleanup()
        }
        unregisterReceiver(ConnectivityBroadcastReceiver)
        super.onDestroy()
    }


    override fun onResume() {
        (application as? MainApplication)?.notifyAppForeground()
        super.onResume()
    }

    override fun onPause() {
        (application as? MainApplication)?.notifyAppBackground()
        super.onPause()
    }

    /**
     * set a broadcastReceiver to react to the action which is sent by the notification containing client's message
     * */
    @RequiresApi(Build.VERSION_CODES.O)
    private fun setClientMessageBroadcastReceiver() {
        notificationMessageReceiver = NotificationMessageBroadcastReceiver(
            onMessageReceivedAction = {
                serverLog(message = "notificationReceiver onMessageReceivedAction ")
                //handle the tasks you want to be done in case of message is sent by client
            }
        )

        // Register the receiver with specific actions
        val intentFilter = IntentFilter().apply {
            addAction(Constants.ActionCode.NotificationMessage.title)
        }


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // For Android Oreo (API level 26) and above
            registerReceiver(notificationMessageReceiver, intentFilter, RECEIVER_NOT_EXPORTED)
        } else {
            // For older Android versions (before Oreo), use Context.registerReceiver without flags
            registerReceiver(notificationMessageReceiver, intentFilter)
        }
    }


    /**
     * set a broadcastReceiver to react to the connection type changes
     * */
    private fun setConnectivityBroadcastReceiver(connectionTypeManager: ConnectionTypeManager) {
        ConnectivityBroadcastReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                connectionTypeManager.checkConnectionStatus()
            }
        }

        val intentFilter = IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION)
        registerReceiver(ConnectivityBroadcastReceiver, intentFilter)

    }
}

