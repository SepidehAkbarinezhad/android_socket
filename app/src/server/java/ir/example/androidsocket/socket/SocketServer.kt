package ir.example.androidsocket.socket

import ir.example.androidsocket.SocketConnectionListener

interface SocketServer {
    val serverPort: Int
    val socketListener: List<SocketConnectionListener>

    fun startServer()
    fun stopServer()
    fun isPortAvailable(): Boolean
    suspend fun sendMessageWithTimeout(message : String,timeoutMillis : Long)
}