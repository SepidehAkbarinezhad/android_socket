package ir.example.androidsocket.client

import android.net.Uri
import ir.example.androidsocket.SocketConnectionListener

interface SocketClient {

    var ip : String
    var port : String
    val socketListener: List<SocketConnectionListener>

    suspend fun connectWithTimeout(timeoutMillis: Long = 90000)
    fun sendMessage(message: String,timeoutMillis : Long)
    fun sendFile(uri: Uri)
    fun onMessage(message: String?)
    fun disconnect()
}