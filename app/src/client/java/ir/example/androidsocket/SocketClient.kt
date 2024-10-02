package ir.example.androidsocket

interface SocketClient {

    var ip : String
    var port : String
    val socketListener: List<SocketConnectionListener>

    suspend fun connectWithTimeout(timeoutMillis: Long = 5000)
    fun disconnect()
    fun sendMessage(message: String,timeoutMillis : Long)
    fun onMessage(message: String?)
}