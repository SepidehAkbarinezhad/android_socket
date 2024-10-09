package ir.example.androidsocket.socket

import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Binder
import android.os.IBinder
import ir.example.androidsocket.Constants
import ir.example.androidsocket.SocketConnectionListener
import ir.example.androidsocket.utils.NotificationHandler
import ir.example.androidsocket.utils.serverLog
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.random.Random

class SocketServerForegroundService() : Service() {

    companion object {
        val PORT = Random.nextInt(1024, 49151)
        private const val CHANNEL_ID = "SocketChannel"
        private const val NOTIFICATION_ID = 1
    }

    private lateinit var serverManager: ServerManager
    private val binder = LocalBinder()
    private val connectionListeners = mutableListOf<SocketConnectionListener>()
    private val notificationHandler = NotificationHandler(this, CHANNEL_ID)

    inner class LocalBinder : Binder() {
        fun getService(): SocketServerForegroundService = this@SocketServerForegroundService
    }


    /**
     * Called when the service is first created
     * is called only once during the lifetime of the service
     **/
    override fun onCreate() {
        serverLog("SocketServerForegroundService onCreate")
        super.onCreate()
        notificationHandler.createNotificationChannel()
    }


    /**
     * Called every time the service is started with startService()
     * handles the logic for what the service should do based on the provided Intent
     **/
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        serverLog("SocketServerForegroundService onStartCommand")

        // Start the foreground service and display a notification when service is started
        startForeground(
            NOTIFICATION_ID,
            notificationHandler.createNotification(
                message = "socket server",
                onContentIntent = { null })
        )

        startSocketServer()

        // Return START_STICKY to indicate that if the system kills the service after it's started, it should try to recreate the service
        // is useful for services that are performing background tasks, like maintaining a socket connection
        return START_STICKY
    }

    /**
     *  is invoked when a client calls bindService() to bind to the service
     * allows components (like activities) to interact with the service, send requests, and receive results
     **/
    override fun onBind(intent: Intent?): IBinder? {
        serverLog("SocketServerForegroundService onBind")
        return binder
    }

    override fun onUnbind(intent: Intent?): Boolean {
        serverLog("SocketServerForegroundService onUnbind")
        closeServerSocket()
        return super.onUnbind(intent)
    }

    override fun onDestroy() {
        serverLog("SocketServerForegroundService onDestroy")
        closeServerSocket()
        super.onDestroy()
    }

    override fun onRebind(intent: Intent?) {
        serverLog("SocketServerForegroundService onRebind")
        super.onRebind(intent)
    }


    fun registerConnectionListener(listener: SocketConnectionListener) {
        serverLog("SocketServerForegroundService registerConnectionListener")
        connectionListeners.add(listener)
    }


    fun startSocketServer(protocolType: Constants.ProtocolType = Constants.ProtocolType.WEBSOCKET) {
        serverLog("SocketServerForegroundService startSocketServer")
        serverManager = ServerManager(
            protocolType,
            WebsocketServerManger(PORT, connectionListeners),
            TcpServerManager(PORT, connectionListeners)
        )
        serverManager.startServer()

    }


    fun displayNotification(
        notificationId: Int,
        title: String,
        message: String,
        onContentIntent: (Context) -> PendingIntent?
    ) {
        notificationHandler.displayNotification(
            context = this,
            notificationId = notificationId,
            title = title,
            message = message,
            onContentIntent = onContentIntent
        )
    }

    fun sendMessageWithTimeout(message: String, timeoutMillis: Long = 20000) {
        serverLog("SocketForegroundService sendMessageWithTimeout")
        CoroutineScope(Dispatchers.IO).launch {
            // server.sendMessageWithTimeout(timeoutMillis = timeoutMillis, message = message)
        }
    }

    fun sendMessagesUntilSuccess(message: String) {
        serverLog("SocketForegroundService sendMessagesUntilSuccess")
        CoroutineScope(Dispatchers.IO).launch {
            //  server.sendMessagesUntilSuccess(message = message)
        }
    }

    private fun closeServerSocket() {
        try {
            serverLog("SocketServerForegroundService closeServerSocket")
              serverManager.stopServer()
        } catch (e: Exception) {
            serverLog("SocketServerForegroundService catch exception : ${e.message}")
        }
    }


}