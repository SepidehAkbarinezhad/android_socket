package ir.example.androidsocket.ui

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.androidSocket.R
import dagger.hilt.android.AndroidEntryPoint
import ir.example.androidsocket.ui.base.PermissionDialog
import ir.example.androidsocket.utils.clientLog


@AndroidEntryPoint
class ClientActivity : ComponentActivity() {

    private val viewModel: ClientViewModel by viewModels()

    @SuppressLint("SuspiciousIndentation")
    override fun onCreate(savedInstanceState: Bundle?) {
        clientLog("ClientActivity onCreate()")
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        val activity = this@ClientActivity
        var requestPermissionLauncher: ActivityResultLauncher<String> =
            registerForActivityResult(
                ActivityResultContracts.RequestPermission()
            ) { isGranted: Boolean ->
                if (isGranted) {
                    // Permission is granted. Continue the action or workflow in your app
                    clientLog("ActivityResult permission isGranted")
                    viewModel.setOpenNotificationPermissionDialog(false)
                    viewModel.onEvent(ClientEvent.StartClientService(activity))

                } else {
                    clientLog("ActivityResult permission isNotGranted")
                    viewModel.setOpenNotificationPermissionDialog(true)
                }
            }


        setContent {

            val openPermissionDialog by viewModel.openNotificationPermissionDialog.collectAsStateWithLifecycle(
                initialValue = false
            )

            LaunchedEffect(Unit) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    when {
                        ContextCompat.checkSelfPermission(
                            activity,
                            Manifest.permission.POST_NOTIFICATIONS
                        ) == PackageManager.PERMISSION_GRANTED -> {
                            viewModel.onEvent(ClientEvent.StartClientService(activity))
                        }

                        ActivityCompat.shouldShowRequestPermissionRationale(
                            activity, Manifest.permission.POST_NOTIFICATIONS
                        ) -> {
                            /*
                            * true if the user has previously denied the permission request .
                            * explain to the user why your app requires this permission for a specific feature to behave as expected
                            * and what features are disabled if it's declined.
                            * */
                            viewModel.setOpenNotificationPermissionDialog(true)

                        }

                        else -> {
                            // The registered ActivityResultCallback gets the result of this request.
                            viewModel.setOpenNotificationPermissionDialog(false)
                            requestPermissionLauncher.launch(
                                Manifest.permission.POST_NOTIFICATIONS
                            )
                        }
                    }
                } else {
                    viewModel.onEvent(ClientEvent.StartClientService(activity))
                }

            }

            Box(Modifier.fillMaxSize()) {
                ClientCompose(
                    viewModel = viewModel,
                ) { event -> viewModel.onEvent(event) }

                if (openPermissionDialog) {
                    clientLog("showPermissionDialog")
                    PermissionDialog(
                        modifier = Modifier.align(Alignment.Center),
                        permissionReason = R.string.notification_permission_reason,
                        onDismissRequest = {
                            viewModel.setOpenNotificationPermissionDialog(false)
                            finish()
                        }
                    ) {
                        requestPermissionLauncher.launch(
                            Manifest.permission.POST_NOTIFICATIONS
                        )
                    }
                }
            }
        }
    }

    override fun onStart() {
        clientLog("ClientActivity onStart()")
        super.onStart()
    }

    override fun onStop() {
        clientLog("ClientActivity onStop()")
        super.onStop()
    }


    override fun onDestroy() {
        clientLog("ClientActivity onDestroy()")
        viewModel.performCleanup()
        super.onDestroy()
    }

}

