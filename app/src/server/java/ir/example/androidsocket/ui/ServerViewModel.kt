package ir.example.androidsocket.ui

import android.app.PendingIntent
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Build
import android.os.IBinder
import androidx.activity.ComponentActivity
import androidx.lifecycle.viewModelScope
import com.example.androidSocket.R
import dagger.hilt.android.lifecycle.HiltViewModel
import ir.example.androidsocket.Constants
import ir.example.androidsocket.Constants.CLIENT_MESSAGE_NOTIFICATION_ID
import ir.example.androidsocket.Constants.MessageConstantType.MESSAGE_TYPE_FILE_CONTENT
import ir.example.androidsocket.Constants.MessageConstantType.MESSAGE_TYPE_TEXT_CONTENT
import ir.example.androidsocket.MainApplication
import ir.example.androidsocket.SocketConnectionListener
import ir.example.androidsocket.socket.SocketServerForegroundService
import ir.example.androidsocket.ui.base.BaseViewModel
import ir.example.androidsocket.utils.IpAddressManager
import ir.example.androidsocket.utils.serverLog
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import javax.inject.Inject


@HiltViewModel
internal class ServerViewModel @Inject constructor() : BaseViewModel() {

    var openStoragePermissionDialog = MutableStateFlow(false)

    var clientMessage = MutableStateFlow("")
        private set

    var fileProgress = MutableStateFlow<Int?>(null)
        private set

    var fileIsSaved = MutableStateFlow<Boolean>(false)
        private set

    var wifiServerIp = MutableStateFlow("")
        private set

    var ethernetServerIp = MutableStateFlow("")
        private set

    var socketStatus = MutableStateFlow(Constants.SocketStatus.DISCONNECTED)
        private set


    var isServiceBound = MutableStateFlow<Boolean>(false)
        private set


    private var serverForgroundService: SocketServerForegroundService? = null

    val socketConnectionListener = object : SocketConnectionListener {
        override fun onStart() {
            serverLog("SocketConnectionListener onStart")
            onEvent(ServerEvent.SetLoading(false))
        }

        override fun onConnected() {
            serverLog("SocketConnectionListener onConnected")
            socketStatus.value = Constants.SocketStatus.CONNECTED
            onEvent(ServerEvent.SetLoading(false))
        }

        override fun onMessage(messageContentType: Int?, message: String?) {
            serverLog("SocketConnectionListener onMessage:  $message", "progressCheck")
            when (messageContentType) {
                MESSAGE_TYPE_TEXT_CONTENT -> {
                    onEvent(ServerEvent.SetClientMessage(message ?: ""))
                    createNotificationFromClientMessage(message = message)
                    serverForgroundService?.sendMessageWithTimeout("message is received by server")
                }

                MESSAGE_TYPE_FILE_CONTENT -> {
                    serverLog("MESSAGE_TYPE_FILE_CONTENT", "progressCheck")
                    onEvent(ServerEvent.SetFileIsSaved(true))
                    emitMessageValue(R.string.file_message_saved)
                }
            }
        }

        override fun onProgressUpdate(progress: Int) {
            serverLog("SocketConnectionListener onProgressUpdate:  $progress", "progressCheck")
            fileProgress.value = progress
            if (progress == 100) {
                emitMessageValue(R.string.file_message_received)
                createNotificationFromClientMessage(message = "a file message is received")
                serverForgroundService?.sendMessageWithTimeout("message is received by server")
                fileProgress.value = null
            }

        }

        override fun onDisconnected(code: Int?, reason: String?) {
            serverLog("SocketConnectionListener onDisconnected: $reason")
            emitMessageValue(R.string.disconnected_error_message, reason)
            socketStatus.value = Constants.SocketStatus.DISCONNECTED

        }

        override fun onError(exception: Exception?) {
            serverLog("SocketConnectionListener onError:  ${exception?.message}")

            /*socketStatus should not changed to disconnected because in some cases such as when creating notification from client
            *message got error on >=31 ,the socket was connected but onError was called.
            */
            emitMessageValue(R.string.error_message, exception?.message)
        }

        override fun onException(exception: Exception?) {
            serverLog("SocketConnectionListener onException: ${exception?.message}")
            emitMessageValue(R.string.error_message, exception?.message)
            onEvent(ServerEvent.SetLoading(false))
        }

    }

    private var serviceConnection: ServiceConnection? = object : ServiceConnection {
        override fun onServiceConnected(className: ComponentName, service: IBinder) {
            serverLog("serverConnectionListener onServiceConnected")
            val binder = service as SocketServerForegroundService.LocalBinder
            serverForgroundService = binder.getService()
            serverForgroundService?.registerConnectionListener(socketConnectionListener)
            isServiceBound.value = true
        }

        override fun onServiceDisconnected(arg0: ComponentName) {
            serverLog("serverConnectionListener onServiceDisconnected")

            /*This method is not guaranteed to be called when you call unbindService.
             It is more commonly used in scenarios where the service process is unexpectedly
             terminated or crashes.*/

        }
    }

    fun setOpenStoragePermissionDialog(value: Boolean) {
        openStoragePermissionDialog.value = value
    }

    fun startServerService(context: Context) {
        serverLog("startServerService() ${isServiceBound.value}")
        if (!isServiceBound.value) {
            try {
                serverLog("startServerService() try")
                serviceConnection?.let { connection ->
                    val serviceIntent = Intent(context, SocketServerForegroundService::class.java)
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        serverLog("startServerService() Build.VERSION_CODES.O")
                        context.startForegroundService(serviceIntent)
                    } else {
                        serverLog("startServerService() Build.VERSION_CODES.O else")
                        context.startService(serviceIntent)
                    }
                    context.bindService(
                        serviceIntent,
                        connection,
                        ComponentActivity.BIND_AUTO_CREATE
                    )
                    isServiceBound.value = true
                } ?: throw Exception()

            } catch (e: Exception) {
                serverLog("startSocketService  catch: ${e.message}")
            }
        }
    }

    fun onEvent(event: ServerEvent) {
        when (event) {
            is ServerEvent.SetLoading -> loading.value = event.value
            is ServerEvent.SetSocketConnectionStatus -> socketStatus.value = event.status
            is ServerEvent.SetClientMessage -> clientMessage.value = event.message
            is ServerEvent.GetWifiIpAddress -> wifiServerIp.value =
                IpAddressManager.getLocalIpAddress(event.context).first ?: ""

            is ServerEvent.GetLanIpAddress -> ethernetServerIp.value =
                IpAddressManager.getLocalIpAddress(event.context).second ?: ""

            is ServerEvent.SetProtocolType -> {
                serverLog("SetProtocolType  ${event.type}")
                //if the selected protocol doesn't differ from previous one, return
                if (selectedProtocol.value.title == event.type)
                    return
                serverLog("SetProtocolType 1")
                // before running socket on new selected protocol type , close the previous
                serverForgroundService?.closeServerSocket()
                selectedProtocol.value = when (event.type) {
                    Constants.ProtocolType.TCP.title -> Constants.ProtocolType.TCP
                    else -> Constants.ProtocolType.WEBSOCKET
                }
                serverForgroundService?.startSocketServer(selectedProtocol.value)
            }
            is ServerEvent.SetFileIsSaved -> fileIsSaved.value = event.saved
        }
    }

    private fun createNotificationFromClientMessage(message: String?) {
        if (!message.isNullOrEmpty())
            serverForgroundService?.displayNotification(
                notificationId = CLIENT_MESSAGE_NOTIFICATION_ID,
                title = Constants.ActionCode.NotificationMessage.title,
                message = message,
                onContentIntent = { context ->
                    //will be triggered when the user taps on the notification
                    if (!MainApplication.isAppInForeground) {
                        val intent = Intent(context, ServerActivity::class.java)
                        intent.action = Constants.ActionCode.NotificationMessage.title
                        PendingIntent.getActivity(
                            context,
                            0,
                            intent,
                            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
                        )
                    } else null
                }
            )
    }

    fun performCleanup() {
        serverLog("performCleanup()")
        try {
            serverForgroundService?.let { foregroundService ->
                serviceConnection?.let {
                    //stopping the service makes it automatically unbinds all clients that are bound to it
                    foregroundService.stopSelf()
                    isServiceBound.value = false
                }
            }
        } catch (e: Exception) {
            serverLog("performCleanup catch: ${e.message}")
        }
    }

}