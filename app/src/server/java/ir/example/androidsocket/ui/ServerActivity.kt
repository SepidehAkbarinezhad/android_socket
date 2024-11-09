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
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.WindowCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.androidSocket.R
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import dagger.hilt.android.AndroidEntryPoint
import ir.example.androidsocket.Constants
import ir.example.androidsocket.MainApplication
import ir.example.androidsocket.ui.base.BaseUiEvent
import ir.example.androidsocket.ui.base.PermissionDialog
import ir.example.androidsocket.ui.theme.AndroidSocketTheme
import ir.example.androidsocket.utils.ConnectionTypeManager
import ir.example.androidsocket.utils.NotificationMessageBroadcastReceiver
import ir.example.androidsocket.utils.serverLog


@AndroidEntryPoint
class ServerActivity : ComponentActivity() {

    private val viewModel: ServerViewModel by viewModels()
    private lateinit var connectivityBroadcastReceiver: BroadcastReceiver
    private lateinit var notificationMessageReceiver: NotificationMessageBroadcastReceiver

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        val connectionTypeManager = ConnectionTypeManager(this)

        val activity = this@ServerActivity
        var requestNotificationPermissionLauncher: ActivityResultLauncher<String> =
            registerForActivityResult(
                ActivityResultContracts.RequestPermission()
            ) { isGranted: Boolean ->
                if (isGranted) {
                    viewModel.setOpenNotificationPermissionDialog(false)
                    viewModel.setNotificationGranted(true)
                } else {
                    viewModel.setOpenNotificationPermissionDialog(true)
                }
            }

        var requestStoragePermissionLauncher: ActivityResultLauncher<String> =
            registerForActivityResult(
                ActivityResultContracts.RequestPermission()
            ) { isGranted ->
                if (isGranted) {
                    viewModel.setOpenStoragePermissionDialog(false)
                    viewModel.startServerService(activity)
                } else {
                    viewModel.setOpenStoragePermissionDialog(true)
                }
            }
        setClientMessageBroadcastReceiver()
        setConnectivityBroadcastReceiver(connectionTypeManager)
        setContent {

            val openNotificationPermissionDialog by viewModel.openNotificationPermissionDialog.collectAsStateWithLifecycle(
                initialValue = false
            )

            val notificationPermissionGranted by viewModel.notificationPermissionGranted.collectAsStateWithLifecycle(
                initialValue = false
            )

            val openStoragePermissionDialog by viewModel.openStoragePermissionDialog.collectAsStateWithLifecycle(
                initialValue = false
            )
            val uiEvent by viewModel.uiEvent.collectAsStateWithLifecycle(initialValue = BaseUiEvent.None)
            val loading by viewModel.loading
            LaunchedEffect(Unit) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    /**
                     *  handles the logic to determine whether the app already has the permission, whether it needs to show a rationale, or whether it should request the permission.
                     * **/
                    when {
                        ContextCompat.checkSelfPermission(
                            activity,
                            Manifest.permission.POST_NOTIFICATIONS
                        ) == PackageManager.PERMISSION_GRANTED -> {
                            viewModel.setNotificationGranted(true)
                        }

                        ActivityCompat.shouldShowRequestPermissionRationale(
                            activity, Manifest.permission.POST_NOTIFICATIONS
                        ) -> {
                            /**
                             * true if the user has previously denied the permission request .
                             * explain to the user why your app requires this permission for a specific feature to behave as expected
                             * and what features are disabled if it's declined.
                             * */
                            viewModel.setOpenNotificationPermissionDialog(true)

                        }

                        else -> {
                            // The registered ActivityResultCallback gets the result of this request.
                            viewModel.setOpenNotificationPermissionDialog(false)
                            requestNotificationPermissionLauncher.launch(
                                Manifest.permission.POST_NOTIFICATIONS
                            )
                        }
                    }
                }

            }
            LaunchedEffect(notificationPermissionGranted) {
                if (notificationPermissionGranted) {
                    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
                        when {
                            ContextCompat.checkSelfPermission(
                                activity,
                                Manifest.permission.WRITE_EXTERNAL_STORAGE
                            ) == PackageManager.PERMISSION_GRANTED -> {
                                viewModel.startServerService(activity)
                            }

                            ActivityCompat.shouldShowRequestPermissionRationale(
                                activity, Manifest.permission.WRITE_EXTERNAL_STORAGE
                            ) -> {
                                viewModel.setOpenStoragePermissionDialog(true)
                            }

                            else -> {
                                // The registered ActivityResultCallback gets the result of this request.
                                viewModel.setOpenStoragePermissionDialog(false)
                                requestStoragePermissionLauncher.launch(
                                    Manifest.permission.WRITE_EXTERNAL_STORAGE
                                )
                            }
                        }
                    } else {
                        viewModel.startServerService(activity)
                    }
                }
            }

            AndroidSocketTheme(uiEvent = uiEvent, displayProgressBar = loading) {
                val systemUiController = rememberSystemUiController()
                systemUiController.setNavigationBarColor(MaterialTheme.colorScheme.primary)
                Box(
                    Modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.background)
                ) {
                    ServerComposable(
                        viewModel = viewModel,
                        connectionTypeManager = connectionTypeManager,
                        onEvent = { viewModel.onEvent(it) }
                    )
                    if (openNotificationPermissionDialog) {
                        PermissionDialog(
                            modifier = Modifier.align(Alignment.Center),
                            permissionReason = R.string.notification_permission_reason,
                            onDismissRequest = {
                                viewModel.setOpenNotificationPermissionDialog(false)
                                finish()
                            }
                        ) {
                            viewModel.setOpenNotificationPermissionDialog(false)
                            requestNotificationPermissionLauncher.launch(
                                Manifest.permission.POST_NOTIFICATIONS
                            )
                        }
                    }
                    if (openStoragePermissionDialog) {
                        PermissionDialog(
                            modifier = Modifier.align(Alignment.Center),
                            permissionReason = R.string.storage_permission_reason,
                            onDismissRequest = {
                                viewModel.setOpenStoragePermissionDialog(false)
                                finish()
                            }
                        ) {
                            viewModel.setOpenStoragePermissionDialog(false)
                            requestStoragePermissionLauncher.launch(
                                Manifest.permission.WRITE_EXTERNAL_STORAGE
                            )
                        }
                    }
                }
            }

        }
    }

    override fun onDestroy() {
        viewModel.performCleanup()
        unregisterReceiver(connectivityBroadcastReceiver)
        unregisterReceiver(notificationMessageReceiver)
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
                //handle the tasks you want to be done in case of message is sent from client

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
        connectivityBroadcastReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                connectionTypeManager.checkConnectionStatus()
            }
        }

        val intentFilter = IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION)
        registerReceiver(connectivityBroadcastReceiver, intentFilter)
    }
}

