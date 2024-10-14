package ir.example.androidsocket.client

import android.net.Uri
import ir.example.androidsocket.SocketConnectionListener

interface SocketClient {

    var ip : String
    var port : String
    val socketListener: List<SocketConnectionListener>

    suspend fun connectWithTimeout(timeoutMillis: Long = 35000)
    fun disconnect()
    fun sendMessage(message: String,timeoutMillis : Long)
    fun attachFile(uri: Uri)
    fun onMessage(message: String?)
}