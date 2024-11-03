package ir.example.androidsocket.ui

import android.content.ComponentName
import android.content.Intent
import android.content.ServiceConnection
import android.net.Uri
import android.os.Build
import android.os.IBinder
import androidx.activity.ComponentActivity
import androidx.lifecycle.viewModelScope
import com.example.androidSocket.R
import dagger.hilt.android.lifecycle.HiltViewModel
import ir.example.androidsocket.Constants
import ir.example.androidsocket.client.SocketClientForegroundService
import ir.example.androidsocket.SocketConnectionListener
import ir.example.androidsocket.ui.base.BaseViewModel
import ir.example.androidsocket.utils.clientLog
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject


@HiltViewModel
internal class ClientViewModel @Inject constructor() : BaseViewModel() {


    var clientMessage = MutableStateFlow("")
        private set

    var fileUrl: MutableStateFlow<Uri?> = MutableStateFlow(null)
        private set

    var fileProgress = MutableStateFlow<Int?>(null)
        private set

    var serverMessage = MutableStateFlow("")
        private set

    var waitingForServerConfirmation = MutableStateFlow<Boolean?>(null)
        private set

    var serverIp = MutableStateFlow<String>("")
        private set

    var serverIpError = MutableStateFlow(false)
        private set

    var serverPort = MutableStateFlow<String>("")
        private set

    var serverPortError = MutableStateFlow(false)
        private set

    var socketStatus = MutableStateFlow(Constants.SocketStatus.DISCONNECTED)
        private set

    var isConnecting = MutableStateFlow(false)
        private set

    var isServiceBound = MutableStateFlow<Boolean>(false)
        private set

    private var clientForegroundService: SocketClientForegroundService? = null

    val clientConnectionListener = object : SocketConnectionListener {

        override fun onStart() {
            clientLog(
                "clientConnectionListener onStart()"
            )
        }

        override fun onConnected() {
            clientLog("clientConnectionListener onConnected")
            onEvent(ClientEvent.SetLoading(false))
            onEvent(ClientEvent.SetIsConnecting(false))
            onEvent(ClientEvent.SetSocketConnectionStatus(Constants.SocketStatus.CONNECTED))
        }

        override fun onMessage(messageContentType: Int?, message: String?) {
            clientLog("clientConnectionListener onMessage : $message", "progressCheck")
            onEvent(ClientEvent.SetLoading(false))
            setWaitingForServer(false)
            onEvent(ClientEvent.SetServerMessage(message ?: ""))
        }

        override fun onProgressUpdate(progress: Int) {
            clientLog("clientConnectionListener onProgressUpdate : $progress", "progressCheck")
            setFileProgress(progress)
            if (fileProgress.value == 100)
                setFileProgress(null)
        }

        override fun onDisconnected(code: Int?, reason: String?) {
            clientLog(
                "clientConnectionListener onDisconnected : $code $reason"
            )
            emitMessageValue(R.string.disconnected_error_message, reason)
            onEvent(ClientEvent.SetSocketConnectionStatus(Constants.SocketStatus.DISCONNECTED))
            //onEvent(ClientEvent.SetLoading(false))
            onEvent(ClientEvent.SetIsConnecting(false))
        }

        override fun onError(exception: Exception?) {
            clientLog(
                "clientConnectionListener ClientActivity onError  ${exception?.message}"
            )
            emitMessageValue(R.string.error_message, exception?.message ?: "")
            //onEvent(ClientEvent.SetLoading(false))
            onEvent(ClientEvent.SetIsConnecting(false))
            onEvent(ClientEvent.SetSocketConnectionStatus(Constants.SocketStatus.DISCONNECTED))
        }

        override fun onException(exception: Exception?) {
            clientLog(
                "clientConnectionListener onException() ${exception?.message}"
            )
            // onEvent(ClientEvent.SetLoading(false))
            onEvent(ClientEvent.SetIsConnecting(false))
            emitMessageValue(R.string.error_message, exception?.message ?: "")
        }
    }
    private var serviceConnection: ServiceConnection? = object : ServiceConnection {
        override fun onServiceConnected(className: ComponentName, service: IBinder) {
            val binder = service as SocketClientForegroundService.LocalBinder
            clientLog("serviceConnection onServiceConnected")
            clientForegroundService = binder.getService()
            clientForegroundService?.registerConnectionListener(clientConnectionListener)
            isServiceBound.value = true

        }

        override fun onServiceDisconnected(arg0: ComponentName) {
            clientForegroundService?.unregisterConnectionListener(clientConnectionListener)
            isServiceBound.value = false
        }
    }

    fun onEvent(event: ClientEvent) {
        when (event) {
            is ClientEvent.StartClientService -> {
                if (!isServiceBound.value) {
                    try {
                        serviceConnection?.let { connection ->
                            val serviceIntent =
                                Intent(event.context, SocketClientForegroundService::class.java)
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                                event.context.startForegroundService(serviceIntent)
                            } else {
                                event.context.startService(serviceIntent)
                            }
                            event.context.bindService(
                                serviceIntent,
                                connection,
                                ComponentActivity.BIND_AUTO_CREATE
                            )
                            isServiceBound.value = true

                        } ?: throw Exception()

                    } catch (e: Exception) {
                        clientLog("startSocketService  catch : ${e.message}")

                    }
                }

            }

            is ClientEvent.SetLoading -> {
                loading.value = event.value
            }

            is ClientEvent.SetIsConnecting -> {
                isConnecting.value = event.value
            }

            is ClientEvent.SetServerIp -> {
                serverIp.value = event.ip
            }

            is ClientEvent.SetServerPort -> serverPort.value = event.port
            is ClientEvent.SetSocketConnectionStatus -> socketStatus.value = event.status
            is ClientEvent.SetClientMessage -> {
                clientMessage.value = event.message
                if (clientMessage.value.isEmpty()) {
                    setWaitingForServer(null)
                }
            }

            is ClientEvent.SetFileUrl -> {
                fileUrl.value = event.uri
            }

            is ClientEvent.SetServerMessage -> {
                clientLog("ClientEvent.SetServerMessage ${waitingForServerConfirmation.value}")
                serverMessage.value = event.message
            }

            ClientEvent.OnConnectToServer -> {
                serverIpError.value = serverIp.value.isEmpty()
                serverPortError.value = serverPort.value.isEmpty()

                if (!serverIpError.value && !serverPortError.value && isServiceBound.value) {
                    clientForegroundService?.let { service ->
                        service.connectWebSocket(
                            selectedProtocol.value,
                            ip = serverIp.value,
                            port = serverPort.value
                        )
                    }
                }
            }

            ClientEvent.OnDisconnectFromServer -> clientForegroundService?.closeClientSocket()
            is ClientEvent.SendMessageToServer -> {
                clientLog("SocketClientForegroundService SendMessageToServer")

                setWaitingForServer(true)
                viewModelScope.launch {
                    delay(1000)
                    fileUrl.value?.let {
                        clientLog("SocketClientForegroundService SendMessageToServer 1")
                        clientForegroundService?.sendFile(it)
                    } ?: clientForegroundService?.sendMessageWithTimeout(message = event.message)
                }

            }

            is ClientEvent.SetProtocolType -> selectedProtocol.value = when (event.type) {
                Constants.ProtocolType.TCP.title -> Constants.ProtocolType.TCP
                else -> Constants.ProtocolType.WEBSOCKET
            }

            ClientEvent.ResetClientMessage -> {
                onEvent(ClientEvent.SetClientMessage(""))
                onEvent(ClientEvent.SetFileUrl(null))
                setFileProgress(null)
            }
        }
    }

    private fun setFileProgress(progress: Int?) {
        fileProgress.value = progress
    }

    private fun setWaitingForServer(waiting: Boolean?) {
        waitingForServerConfirmation.value = waiting
    }

    fun performCleanup() {
        clientLog("performCleanup()")
        try {
            clientForegroundService?.let { foregroundService ->
                serviceConnection?.let { serviceConnection ->
                    //stopping the service makes it automatically unbinds all clients that are bound to it
                    foregroundService.stopSelf()
                    isServiceBound.value = false
                }
            }

        } catch (e: Exception) {
            // Handle exceptions
            clientLog("performCleanup catch: ${e.message}")
        }

    }

}