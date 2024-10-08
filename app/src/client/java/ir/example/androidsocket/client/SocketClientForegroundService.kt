package ir.example.androidsocket.client

import android.app.Service
import android.content.Intent
import android.os.Binder
import android.os.IBinder
import ir.example.androidsocket.Constants
import ir.example.androidsocket.SocketConnectionListener
import ir.example.androidsocket.utils.NotificationHandler
import ir.example.androidsocket.utils.clientLog
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class SocketClientForegroundService : Service() {

    companion object {
        private const val CHANNEL_ID = "SocketChannel"
        private const val NOTIFICATION_ID = 1
    }

    private lateinit var clientManager: SocketClient
    private val binder = LocalBinder()
    private val connectionListeners = mutableListOf<SocketConnectionListener>()
    private val notificationHandler =
        NotificationHandler(this, CHANNEL_ID)


    inner class LocalBinder : Binder() {
        fun getService(): SocketClientForegroundService = this@SocketClientForegroundService
    }

    private var serverAddress = ""


    override fun onCreate() {
        clientLog("SocketClientForegroundService onCreate")
        super.onCreate()
        notificationHandler.createNotificationChannel()
    }

    override fun onBind(intent: Intent?): IBinder? {
        return binder
    }

    override fun onUnbind(intent: Intent?): Boolean {
        clientLog("SocketClientForegroundService onUnbind")
        closeClientSocket()
        return super.onUnbind(intent)
    }

    override fun onDestroy() {
        clientLog("SocketClientForegroundService onDestroy")
        closeClientSocket()
        super.onDestroy()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        clientLog("SocketClientForegroundService onStartCommand")

        // Start the foreground service and display a notification when service is started
        startForeground(
            NOTIFICATION_ID,
            notificationHandler.createNotification(
                message = "socket client",
                onContentIntent = { null })
        )

        return START_STICKY
    }

    fun registerConnectionListener(listener: SocketConnectionListener) {
        connectionListeners.add(listener)
    }

    fun unregisterConnectionListener(listener: SocketConnectionListener) {
        connectionListeners.remove(listener)
    }


    fun connectWebSocket(protocolType: Constants.ProtocolType, ip: String, port: String) {
        clientManager = when (protocolType) {
            Constants.ProtocolType.WEBSOCKET -> WebsocketClientManager(ip,port,connectionListeners)
            Constants.ProtocolType.TCP -> TcpClientManager(ip, port , connectionListeners)
        }
        CoroutineScope(Dispatchers.Io).launch {
            clientManager.connectWithTimeout()
        }
    }

    fun closeClientSocket() {
        try {
            clientLog("SocketClientForegroundService closeClientSocket")
            clientManager.disconnect()
        } catch (e: Exception) {
            clientLog("closeClientSocket catch exception : ${e.message}")
        }
    }

    fun sendMessageWithTimeout(message: String) {
        CoroutineScope(Dispatchers.IO).launch {
            clientLog("SocketClientForegroundService sendMessageWithTimeout")
            clientManager.sendMessage(message = message, timeoutMillis = 50000)
        }
    }


}