package ir.example.androidsocket.client

import ir.example.androidsocket.SocketConnectionListener

interface SocketClient {

    var ip : String
    var port : String
    val socketListener: List<SocketConnectionListener>

    suspend fun connectWithTimeout(timeoutMillis: Long = 10000)
    fun disconnect()
    fun sendMessage(message: String,timeoutMillis : Long)
    fun onMessage(message: String?)
}