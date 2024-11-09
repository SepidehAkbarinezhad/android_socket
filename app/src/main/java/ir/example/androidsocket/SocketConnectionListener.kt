package ir.example.androidsocket

import android.net.Uri

interface SocketConnectionListener {
    fun onStart()
    fun onConnected()
    fun onMessage(messageContentType : Int?, message: String?,fileUri: Uri?=null)
    fun onProgressUpdate(progress : Int)
    fun onDisconnected(code: Int?, reason: String?)
    fun onError(exception: Exception?)
    fun onException(exception: Exception?)
}