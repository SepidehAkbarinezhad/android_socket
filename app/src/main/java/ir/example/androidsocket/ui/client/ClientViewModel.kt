package ir.example.androidsocket.ui.client

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Build
import android.os.IBinder
import androidx.activity.ComponentActivity
import com.example.androidSocket.R
import dagger.hilt.android.lifecycle.HiltViewModel
import ir.example.androidsocket.Constants
import ir.example.androidsocket.socket.SocketClientForegroundService
import ir.example.androidsocket.socket.SocketConnectionListener
import ir.example.androidsocket.ui.base.BaseViewModel
import ir.example.androidsocket.utils.clientLog
import kotlinx.coroutines.flow.MutableStateFlow
import org.java_websocket.WebSocket
import javax.inject.Inject


@HiltViewModel
internal class ClientViewModel @Inject constructor() : BaseViewModel() {


    var clientMessage = MutableStateFlow("")
        private set

    var serverMessage = MutableStateFlow("")
        private set

    var waitingForServerConfirmation = MutableStateFlow<Boolean>(false)
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
            clientLog(
                "clientConnectionListener onConnected"
            )
            onEvent(ClientEvent.SetLoading(false))
            onEvent(ClientEvent.SetSocketConnectionStatus(Constants.SocketStatus.CONNECTED))
        }

        override fun onMessage(conn: WebSocket?, message: String?) {
            clientLog(
                "clientConnectionListener onMessage : $message"
            )
            onEvent(ClientEvent.SetLoading(false))
            onEvent(ClientEvent.SetServerMessage(message ?: ""))

        }

        override fun onDisconnected(code: Int, reason: String?) {
            clientLog(
                "clientConnectionListener onDisconnected : $code $reason"
            )
            emitMessageValue(R.string.disconnected_error_message, reason)
            onEvent(ClientEvent.SetSocketConnectionStatus(Constants.SocketStatus.DISCONNECTED))
            onEvent(ClientEvent.SetLoading(false))

        }

        override fun onError(exception: Exception?) {
            clientLog(
                "clientConnectionListener ClientActivity onError  ${exception?.message}"
            )
            emitMessageValue(R.string.error_message, exception?.message ?: "")
            onEvent(ClientEvent.SetLoading(false))
            onEvent(ClientEvent.SetSocketConnectionStatus(Constants.SocketStatus.DISCONNECTED))
        }

        override fun onException(exception: Exception?) {
            clientLog(
                "clientConnectionListener onException() ${exception?.message}"
            )
            onEvent(ClientEvent.SetLoading(false))
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
                if (!isServiceBound.value){
                    try {
                        serviceConnection?.let { connection ->
                            val serviceIntent = Intent(event.context, SocketClientForegroundService::class.java)
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                                event.context.startForegroundService(serviceIntent)
                                event.context.bindService(
                                    serviceIntent,
                                    connection,
                                    ComponentActivity.BIND_AUTO_CREATE
                                )
                                isServiceBound.value = true
                            } else {
                                event.context.startService(serviceIntent)
                                event.context.bindService(
                                    serviceIntent,
                                    connection,
                                    ComponentActivity.BIND_AUTO_CREATE
                                )
                            }
                        } ?: throw Exception()

                    } catch (e: Exception) {
                        clientLog("startSocketService  catch : ${e.message}")

                    }
                }

            }
            is ClientEvent.SetLoading -> {
                loading.value = event.value
            }
            is ClientEvent.SetServerIp -> {
                serverIp.value = event.ip
            }
            is ClientEvent.SetServerPort -> serverPort.value = event.port
            is ClientEvent.SetSocketConnectionStatus -> socketStatus.value = event.status
            is ClientEvent.SetClientMessage -> {
                clientMessage.value = event.message
            }
            is ClientEvent.SetServerMessage -> {
                serverMessage.value = event.message
                setWaitingForServer(false)
            }
            ClientEvent.OnConnectToServer -> {
                serverIpError.value = serverIp.value.isEmpty()
                serverPortError.value = serverPort.value.isEmpty()

                if (!serverIpError.value && !serverPortError.value && isServiceBound.value) {
                    onEvent(ClientEvent.SetLoading(true))
                    clientForegroundService?.let { service ->
                        service.setServerAddress(ip = serverIp.value, port = serverPort.value)
                        service.connectWebSocket()
                    }
                }
            }

            ClientEvent.OnDisconnectFromServer -> clientForegroundService?.disconnect()
            is ClientEvent.SendMessageToServer -> {
                if(event.message.isNotEmpty()){
                setWaitingForServer(true)}
                clientForegroundService?.sendMessageWithTimeout(message = event.message)}
        }
    }


    private fun setWaitingForServer(waiting : Boolean){
        waitingForServerConfirmation.value=waiting
    }

    fun performCleanup() {
        try {
            clientLog("performCleanup : try")
            // Unbind the service
            clientForegroundService?.let {
                serviceConnection?.let { serviceConnection ->
                    it.unbindService(serviceConnection)
                    isServiceBound.value = false
                }
            }
            // Perform additional cleanup or actions here

        } catch (e: Exception) {
            // Handle exceptions
            clientLog("performCleanup catch: ${e.message}")
        }

    }
}