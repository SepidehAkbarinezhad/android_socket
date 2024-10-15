package ir.example.androidsocket.socket

import ir.example.androidsocket.SocketConnectionListener
import java.io.File

interface SocketServer {
    val serverPort: Int
    val path: File?
    val socketListener: List<SocketConnectionListener>

    fun startServer()
    fun stopServer()
    fun isPortAvailable(): Boolean
    suspend fun sendMessageWithTimeout(message : String,timeoutMillis : Long)
}