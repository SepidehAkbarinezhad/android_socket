package ir.example.androidsocket.socket

import ir.example.androidsocket.utils.clientLog
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.async
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout
import org.java_websocket.client.WebSocketClient
import org.java_websocket.handshake.ServerHandshake
import java.net.URI

class SocketClientManager(uri: URI, val socketListener: List<SocketConnectionListener>) :
    WebSocketClient(uri) {

    suspend fun connectWithTimeout(timeoutMillis: Long = 5000): Boolean =
        withContext(Dispatchers.IO) {
            return@withContext try {
                withTimeout(timeoutMillis) {
                    socketListener.forEach { it.onStart() }
                    connect()
                    connection.isOpen
                }
            } catch (e: TimeoutCancellationException) {
                clientLog("connectWithTimeout TimeoutCancellationException: ${e.message}")
                socketListener.forEach { it.onException(e) }
                false
            } catch (e: Exception) {
                clientLog("connectWithTimeout Exception: ${e.message}")
                socketListener.forEach { it.onException(e) }
                false
            }
        }

    suspend fun sendMessageWithTimeout(timeoutMillis: Long = 5000, message: String): Boolean {
        clientLog("sendMessageWithTimeout")
        return withContext(Dispatchers.IO) {
            return@withContext try {
                withTimeout(timeoutMillis) {
                    val result = CoroutineScope(Dispatchers.IO).async {
                        try {
                            if (connection != null && connection!!.isOpen) {
                                connection!!.send(message)
                                true
                            } else {
                                false
                            }
                        } catch (e: org.java_websocket.exceptions.WebsocketNotConnectedException) {
                            clientLog("sendMessageWithTimeout catch  ${e.message}")
                            socketListener.forEach { it.onException(e) }
                            false
                        }
                    }.await()
                    result
                }
            } catch (e: TimeoutCancellationException) {
                clientLog("sendMessageWithTimeout TimeoutCancellationException : ${e.message}")
                socketListener.forEach { it.onException(e) }
                false
            } catch (e: Exception) {
                clientLog("sendMessageWithTimeout Exception: ${e.message}")
                socketListener.forEach { it.onException(e) }
                false
            }
        }
    }

    override fun onOpen(handshakedata: ServerHandshake?) {
        clientLog("SocketClientManager  onOpen")
        socketListener.forEach { it.onConnected() }
    }

    override fun onClose(code: Int, reason: String?, remote: Boolean) {
        clientLog("SocketClientManager  onClose")
        socketListener.forEach { it.onDisconnected(code, reason) }
    }

    override fun onMessage(message: String?) {
        clientLog("SocketClientManager onMessage  $message")
        socketListener.forEach { it.onMessage(conn = null, message = message) }
    }

    override fun onError(ex: Exception?) {
        clientLog("SocketClientManager onError  ${ex?.message}")
        socketListener.forEach { it.onError(ex) }
        ex?.printStackTrace()
    }

    fun closeConnection() {
        this.close()
    }

}