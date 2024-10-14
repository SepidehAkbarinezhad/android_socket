package ir.example.androidsocket.client

import android.net.Uri
import ir.example.androidsocket.SocketConnectionListener
import ir.example.androidsocket.utils.clientLog
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout
import org.java_websocket.client.WebSocketClient
import org.java_websocket.handshake.ServerHandshake
import java.net.URI

class WebsocketClientManager(
    override var ip: String,
    override var port: String,
    override val socketListener: List<SocketConnectionListener>
) : SocketClient, WebSocketClient(URI("ws://$ip:$port")) {

    override suspend fun connectWithTimeout(timeoutMillis: Long) {
        withContext(Dispatchers.IO) {
            return@withContext try {
                withTimeout(timeoutMillis) {
                    socketListener.forEach { it.onStart() }
                    connect()
                }
            } catch (e: TimeoutCancellationException) {
                clientLog("connectWithTimeout TimeoutCancellationException: ${e.message}")
                socketListener.forEach { it.onException(e) }
            } catch (e: Exception) {
                clientLog("connectWithTimeout Exception: ${e.message}")
                socketListener.forEach { it.onException(e) }
            }
        }
    }

    override fun disconnect() {
        this.close()
    }

    override fun sendMessage(message: String, timeoutMillis: Long) {
        clientLog("sendMessageWithTimeout")
        CoroutineScope(Dispatchers.IO).launch {
            try {
                if (connection != null && connection!!.isOpen) {
                    connection!!.send(message)
                }
            } catch (e: TimeoutCancellationException) {
                clientLog("sendMessageWithTimeout TimeoutCancellationException : ${e.message}")
                socketListener.forEach { it.onException(e) }
            } catch (e: Exception) {
                clientLog("sendMessageWithTimeout Exception: ${e.message}")
                socketListener.forEach { it.onException(e) }
            }
        }

    }

    override fun attachFile(uri: Uri) {
        TODO("Not yet implemented")
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
        socketListener.forEach { it.onMessage(message = message) }
    }

    override fun onError(ex: Exception?) {
        clientLog("SocketClientManager onError  ${ex?.message}")
        socketListener.forEach { it.onError(ex) }
        ex?.printStackTrace()
    }


}