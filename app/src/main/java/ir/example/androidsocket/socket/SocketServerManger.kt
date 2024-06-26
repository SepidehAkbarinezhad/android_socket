package ir.example.androidsocket.socket

import ir.example.androidsocket.utils.serverLog
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout
import org.java_websocket.WebSocket
import org.java_websocket.handshake.ClientHandshake
import org.java_websocket.server.WebSocketServer
import java.io.IOException
import java.net.InetSocketAddress
import java.net.ServerSocket

class SocketServerManger(
    port: Int,
    val socketListener: List<SocketConnectionListener>,
) : WebSocketServer(InetSocketAddress(port)) {

    private var connection: WebSocket? = null

    private val _isConnectionOpen = MutableStateFlow(false)
    val isConnectionOpen: StateFlow<Boolean> = _isConnectionOpen


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
     * called when a client connection is closed
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
        socketListener.forEach { it.onMessage(conn, message) }
    }

    override fun onError(conn: WebSocket?, ex: Exception?) {
        serverLog("SocketServerManger onError : ${ex?.message}")
        socketListener.forEach { it.onError(ex) }
        ex?.printStackTrace()
    }

    suspend fun sendMessagesUntilSuccess(timeoutMillis: Long = 50000, message: String) {
        serverLog("SocketServerManger sendMessagesUntilSuccess")
        var success = false
        while (!success) {
            success = sendMessageWithTimeout(timeoutMillis, message)
            if (!success) {
                // Add a delay before retrying to avoid continuous retries and potential rate limiting
                delay(1000)
            }
        }

    }

    suspend fun sendMessageWithTimeout(timeoutMillis: Long = 5000, message: String): Boolean {
        serverLog("sendMessageWithTimeout 1", "timeOutTag")
        return withContext(Dispatchers.IO) {
            serverLog("sendMessageWithTimeout 2", "timeOutTag")
            return@withContext try {
                withTimeout(timeoutMillis) {
                    val result = CoroutineScope(Dispatchers.IO).async {
                        try {
                            serverLog("sendMessageWithTimeout try : $message", "timeOutTag")
                            if (connection != null && connection!!.isOpen) {
                                serverLog("sendMessageWithTimeout if", "timeOutTag")
                                delay(7000)
                                connection!!.send(message)
                                serverLog("sendMessageWithTimeout if 2", "timeOutTag")
                                true
                            } else {
                                serverLog(
                                    "sendMessageWithTimeout else  connection : ${connection == null}   isOpen:${connection?.isOpen}",
                                    "timeOutTag"
                                )
                                false
                            }
                        } catch (e: org.java_websocket.exceptions.WebsocketNotConnectedException) {
                            serverLog("sendMessageWithTimeout catch  ${e.message}", "timeOutTag")
                            socketListener.forEach { it.onException(e) }
                            false
                        }
                    }.await()

                    result
                }
            } catch (e: TimeoutCancellationException) {
                serverLog("send message timeout: ${e.message}", "timeOutTag")
                socketListener.forEach { it.onException(e) }
                false
            } catch (e: Exception) {
                serverLog("sendMessageWithTimeout e: ${e.message}", "timeOutTag")
                socketListener.forEach { it.onException(e) }
                false
            }
        }
    }


    fun isConnectionOpen() = connection?.isOpen ?: false

    fun isPortAvailable(port: Int): Boolean {
        return try {
            ServerSocket(port).close()
            true
        } catch (e: IOException) {
            false
        }
    }

}
