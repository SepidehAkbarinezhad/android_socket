package ir.example.androidsocket.socket

import ir.example.androidsocket.SocketConnectionListener
import ir.example.androidsocket.utils.serverLog
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout
import org.java_websocket.WebSocket
import org.java_websocket.handshake.ClientHandshake
import org.java_websocket.server.WebSocketServer
import java.io.File
import java.io.IOException
import java.net.InetSocketAddress
import java.net.ServerSocket

class WebsocketServerManger(
    override var serverPort: Int,
    override var path: File,
    override val socketListener: List<SocketConnectionListener>,
) : SocketServer,WebSocketServer(InetSocketAddress(serverPort)) {

    private var connection: WebSocket? = null


    /**
     * called when the WebSocket server has successfully started and is ready to accept client connections
     * */
    override fun onStart() {
        serverLog("SocketServerManger onStart")
    }

    /**
     * Called when a new client connection is opened
     * */
    override fun onOpen(conn: WebSocket?, handshake: ClientHandshake?) {
        serverLog("SocketServerManger onOpen ${conn?.remoteSocketAddress}")
        connection = conn
        socketListener.forEach { it.onConnected() }
    }

    /**
     * called when a individual client connection is closed , not when the server itself is stopped
     * This method is called when a client disconnects from the server or when the server forcefully closes a client connection
     * */
    override fun onClose(conn: WebSocket?, code: Int, reason: String?, remote: Boolean) {
        serverLog(
            "SocketServerManger onClose connection: ${conn?.remoteSocketAddress} - Code: $code, Reason: $reason"
        )
        socketListener.forEach { it.onDisconnected(code, reason) }
    }

    override fun onMessage(conn: WebSocket?, message: String?) {
        serverLog(
            "SocketServerManger onMessage $message"
        )
        socketListener.forEach { it.onMessage( message) }
    }

    override fun onError(conn: WebSocket?, ex: Exception?) {
        serverLog("SocketServerManger onError : ${ex?.message}")
        socketListener.forEach { it.onError(ex) }
        ex?.printStackTrace()
    }



    override suspend fun sendMessageWithTimeout(message: String,timeoutMillis: Long) {
        return withContext(Dispatchers.IO) {
            return@withContext try {
                withTimeout(timeoutMillis) {
                     CoroutineScope(Dispatchers.IO).async {
                        try {
                            if (connection != null && connection!!.isOpen) {
                                connection!!.send(message)

                            }
                        } catch (e: org.java_websocket.exceptions.WebsocketNotConnectedException) {
                            serverLog("sendMessageWithTimeout catch  ${e.message}", "timeOutTag")
                            socketListener.forEach { it.onException(e) }

                        }
                    }.await()

                }
            } catch (e: TimeoutCancellationException) {
                serverLog("sendMessageWithTimeout TimeoutCancellationException: ${e.message}", "timeOutTag")
                socketListener.forEach { it.onException(e) }
            } catch (e: Exception) {
                serverLog("sendMessageWithTimeout e: ${e.message}", "timeOutTag")
                socketListener.forEach { it.onException(e) }
            }
        }
    }


    override fun isPortAvailable(): Boolean {
        return try {
            ServerSocket(serverPort).close()
            true
        } catch (e: IOException) {
            false
        }
    }

    override fun startServer() {
        serverLog("WebsocketServerManger startServer")
        this.start()
    }

    override fun stopServer() {
        serverLog("WebsocketServerManger stopServer")
        this.stop()
    }

}
