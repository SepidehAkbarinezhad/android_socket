package ir.example.androidsocket.socket

import org.java_websocket.WebSocket

interface SocketConnectionListener {
    fun onStart()
    fun onConnected()
    fun onMessage(conn: WebSocket?, message: String?)
    fun onDisconnected(code: Int, reason: String?)
    fun onError(exception: Exception?)
    fun onException(exception: Exception?)
}