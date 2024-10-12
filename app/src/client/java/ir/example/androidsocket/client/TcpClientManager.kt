package ir.example.androidsocket.client


import ir.example.androidsocket.SocketConnectionListener
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
                clientLog("TcpClientManager connectWithTimeout  $serverAddress $port")
                socket = Socket()
                socket?.let { socket->

                    socket.connect(
                        InetSocketAddress(serverAddress, port.toInt()),
                        timeoutMillis.toInt()
                    )

                    clientLog("TcpClientManager connectWithTimeout isConnected ${socket?.isConnected}")
                    if (socket.isConnected){
                        socketListener.forEach { it.onConnected() }
                        handleClient(socket)
                    }
                }


            } catch (e: IOException) {
                clientLog("IOException--> ${e.message}")
                socketListener.forEach { it.onException(e) }
            } catch (e: Exception) {
                clientLog("connectWithTimeout Exception-->  $serverAddress")
                socketListener.forEach { it.onException(e) }
            }
        }

    private suspend fun handleClient(clientSocket: Socket) {
        clientLog("handleClient")
        withContext(Dispatchers.IO) {
            inputStream = clientSocket.getInputStream()
            outputStream = clientSocket.getOutputStream()
            try {
                clientLog("handleClient try")
                val buffer = ByteArray(BUFFER_SIZE)
                var bytesRead: Int = 0
                // Read data into the buffer and assign the number of bytes read
                while (clientSocket.isConnected && inputStream?.read(buffer)
                        .also { bytesRead = it?:0 } != -1
                ) {
                    if (bytesRead > 0) {
                        clientLog("handleClient try bytesRead > 0")
                        val hexMessage = BytesUtils.bytesToHex(buffer.copyOf(bytesRead))
                        val stringMessage = BytesUtils.hexToString(hexMessage)
                        socketListener.forEach { it.onMessage(stringMessage) }
                    }
                }

            } catch (e: Exception) {
                clientLog("Error handling client: ${e.message}")
                socketListener.forEach { it.onError(e) }
            } finally {
                clientLog("Client disconnected: ${clientSocket.inetAddress.hostAddress}")
                clientSocket.close()
            }
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