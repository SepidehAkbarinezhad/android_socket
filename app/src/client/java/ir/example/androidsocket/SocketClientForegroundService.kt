package ir.example.androidsocket

import android.app.Service
import android.content.Intent
import android.os.Binder
import android.os.IBinder
import ir.example.androidsocket.utils.NotificationHandler
import ir.example.androidsocket.utils.clientLog
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.net.InetAddress
import java.net.URI

class SocketClientForegroundService : Service() {

    companion object {
        private const val CHANNEL_ID = "SocketChannel"
        private const val NOTIFICATION_ID = 1
    }

   // private lateinit var client: SocketClientManager
    private lateinit var client: TcpClientManager
    private val binder = LocalBinder()
    private val connectionListeners = mutableListOf<SocketConnectionListener>()
    private val notificationHandler =
        NotificationHandler(this, CHANNEL_ID)


    inner class LocalBinder : Binder() {
        fun getService(): SocketClientForegroundService = this@SocketClientForegroundService
    }

    private var serverAddress = ""
    private var serverIp = ""
    private var serverPort = ""


    override fun onCreate() {
        clientLog("SocketClientForegroundService onCreate")
        super.onCreate()
        notificationHandler.createNotificationChannel()
    }

    override fun onBind(intent: Intent?): IBinder? {
        return binder
    }

    override fun onUnbind(intent: Intent?): Boolean {
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

    fun setServerAddress(ip: String, port: String) {
        serverAddress = "ws://$ip:$port"
        serverIp = ip
        serverPort = port
    }


    fun connectWebSocket() {
        //client = SocketClientManager(URI(serverAddress), connectionListeners)
        client = TcpClientManager(InetAddress.getByName(serverIp),serverPort.toInt())
        CoroutineScope(Dispatchers.IO).launch {
            clientLog("InetAddress.getByName(serverIp)  ${InetAddress.getByName(serverIp)}  ip: ${serverIp}  port: ${serverPort.toInt()}")

            client.connectWithTimeout(80000)

        }
     /*   CoroutineScope(Dispatchers.Main).launch {
            val isConnected = client.connectWithTimeout()

            if (isConnected) {
                // Successfully connected
                clientLog("connectWebSocket isConnected")
            } else {
                // Connection failed or timed out
                clientLog("connectWebSocket is not Connected")
            }
        }*/
    }

    fun disconnect() {
        client.closeConnection()
    }

    fun sendMessageWithTimeout(message: String) {
        CoroutineScope(Dispatchers.IO).launch {
            clientLog("SocketClientForegroundService sendMessageWithTimeout")
            client.setAmount()
        }
    }

    private fun closeClientSocket() {
        try {
            clientLog("SocketClientForegroundService closeClientSocket")
           // client.close()
        } catch (e: Exception) {
            clientLog("closeClientSocket catch exception : ${e.message}")
        }
    }

}