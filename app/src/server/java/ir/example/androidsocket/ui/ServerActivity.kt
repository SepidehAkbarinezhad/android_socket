package ir.example.androidsocket.ui

import android.Manifest
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.net.ConnectivityManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.lifecycleScope
import dagger.hilt.android.AndroidEntryPoint
import ir.example.androidsocket.Constants
import ir.example.androidsocket.MainApplication
import ir.example.androidsocket.ui.base.PermissionDialog
import ir.example.androidsocket.utils.ConnectionTypeManager
import ir.example.androidsocket.utils.NotificationMessageBroadcastReceiver
import ir.example.androidsocket.utils.clientLog
import ir.example.androidsocket.utils.serverLog
import kotlinx.coroutines.launch


@AndroidEntryPoint
class ServerActivity : ComponentActivity() {

    private val viewModel: ServerViewModel by viewModels()
    private lateinit var ConnectivityBroadcastReceiver: BroadcastReceiver
    private lateinit var notificationMessageReceiver: NotificationMessageBroadcastReceiver


    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        val connectionTypeManager = ConnectionTypeManager(this)

        val activity = this@ServerActivity
        var requestPermissionLauncher : ActivityResultLauncher<String> =
            registerForActivityResult(
                ActivityResultContracts.RequestPermission()
            ) { isGranted: Boolean ->
                if (isGranted) {
                    // Permission is granted. Continue the action or workflow in your app
                    clientLog("ActivityResult permission isGranted")
                    viewModel.setOpenPermissionDialog(false)
                    viewModel.startServerService(activity)
                } else {
                    clientLog("ActivityResult permission isNotGranted")
                    viewModel.setOpenPermissionDialog(true)
                }
            }

        setClientMessageBroadcastReceiver()
        setConnectivityBroadcastReceiver(connectionTypeManager)
        setContent {

            val openPermissionDialog by viewModel.openPermissionDialog.collectAsStateWithLifecycle(initialValue = false)

            LaunchedEffect(Unit) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    when {
                        ContextCompat.checkSelfPermission(
                            activity,
                            Manifest.permission.POST_NOTIFICATIONS
                        ) == PackageManager.PERMISSION_GRANTED -> {
                            viewModel.startServerService(activity)
                        }

                        ActivityCompat.shouldShowRequestPermissionRationale(
                            activity, Manifest.permission.POST_NOTIFICATIONS
                        ) -> {
                            /*
                            * true if the user has previously denied the permission request .
                            * explain to the user why your app requires this permission for a specific feature to behave as expected
                            * and what features are disabled if it's declined.
                            * */
                            viewModel.setOpenPermissionDialog(true)

                        }

                        else -> {
                            // The registered ActivityResultCallback gets the result of this request.
                            viewModel.setOpenPermissionDialog(false)
                            requestPermissionLauncher.launch(
                                Manifest.permission.POST_NOTIFICATIONS
                            )
                        }
                    }
                } else {
                    viewModel.startServerService(activity)
                }

            }

            Box(Modifier.fillMaxSize()) {
            ServerComposable(
                viewModel = viewModel,
                connectionTypeManager = connectionTypeManager
            )
            if (openPermissionDialog){
                clientLog("showPermissionDialog")
                PermissionDialog(
                    modifier = Modifier.align(Alignment.Center),
                    onDismissRequest = {
                        viewModel.setOpenPermissionDialog(false)
                        finish() }
                ) {
                    requestPermissionLauncher.launch(
                        Manifest.permission.POST_NOTIFICATIONS
                    )
                }
            }
        }
        }
    }

    override fun onDestroy() {
        serverLog("ServerActivity onDestroy ")
        viewModel.performCleanup()
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

