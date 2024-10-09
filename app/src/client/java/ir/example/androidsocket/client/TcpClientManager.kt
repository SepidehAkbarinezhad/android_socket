package ir.example.androidsocket.client


import ir.example.androidsocket.Constants
import ir.example.androidsocket.SocketConnectionListener
import ir.example.androidsocket.client.TcpSocketClient.socket
import ir.example.androidsocket.utils.BytesUtils
import ir.example.androidsocket.utils.clientLog
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.net.InetAddress
import java.net.InetSocketAddress
import java.net.Socket

class TcpClientManager(
    override var ip: String,
    override var port: String,
    override val socketListener: List<SocketConnectionListener>
) : SocketClient {

    var socket: Socket? = null
    val serverAddress: InetAddress = InetAddress.getByName(ip)
    private var outputStream: OutputStream? = null
    private var inputStream: InputStream? = null
    val BUFFER_SIZE = 1024

    override suspend fun connectWithTimeout(timeoutMillis: Long): Unit =
        withContext(Dispatchers.IO) {
            //to show message properly
            delay(1000)
            try {
                clientLog("TcpClientManager connectWithTimeout  $serverAddress  ${socket == null}")
                socket = Socket()
                socket?.connect(
                    InetSocketAddress(serverAddress, port.toInt()),
                    timeoutMillis.toInt()
                )
                clientLog("TcpClientManager connectWithTimeout isConnected ${socket?.isConnected}")
                if (socket?.isConnected == true){
                    socketListener.forEach { it.onConnected() }
                }

            } catch (e: IOException) {
                clientLog("TcpClientManager IOException--> ${e.message}")
            } catch (e: Exception) {
                clientLog("TcpClientManager connectWithTimeout Exception-->  $serverAddress")
            }
        }

    override fun disconnect() {
        inputStream?.close()
        outputStream?.close()
        socket?.close()
        socketListener.forEach { it.onDisconnected(code = null , reason = "socked is closed") }
    }

    override fun sendMessage(message: String, timeoutMillis: Long) {
        clientLog("sendMessage")
        CoroutineScope(Dispatchers.IO).launch {
            try {
                clientLog("send-->try")
                val outputStream = socket?.getOutputStream()
                outputStream?.write(message.toByteArray())
                outputStream?.flush()

            } catch (e: Exception) {
                clientLog("send--> catch ${e.message}")
            }
        }
    }

    override fun onMessage(message: String?) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                clientLog("send-->try")
                inputStream = socket?.getInputStream()
                val buffer = ByteArray(BUFFER_SIZE)
                val bytesRead = inputStream?.read(buffer)
                if (bytesRead != null && bytesRead > 0) {
                    val hexMessage = BytesUtils.bytesToHex(buffer)
                    val stringMessage = BytesUtils.hexToString(hexMessage)
                    socketListener.forEach { it.onMessage(message = stringMessage) }
                }
            } catch (e: Exception) {
                clientLog("send--> catch ${e.message}")
                socketListener.forEach { it.onError(e) }
            }
        }
    }


}