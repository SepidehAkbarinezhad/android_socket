package ir.example.androidsocket

interface SocketConnectionListener {
    fun onStart()
    fun onConnected()
    fun onMessage( message: String?)
    fun onProgressUpdate(progress : Int)
    fun onDisconnected(code: Int?, reason: String?)
    fun onError(exception: Exception?)
    fun onException(exception: Exception?)
}