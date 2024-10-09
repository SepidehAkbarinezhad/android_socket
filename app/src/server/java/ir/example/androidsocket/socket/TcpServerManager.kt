package ir.example.androidsocket.socket

import ir.example.androidsocket.SocketConnectionListener
import ir.example.androidsocket.utils.BytesUtils
import ir.example.androidsocket.utils.serverLog
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.net.ServerSocket
import java.net.Socket

class TcpServerManager(
    override var serverPort: Int,
    override val socketListener: List<SocketConnectionListener>,
) : SocketServer {

    private var serverSocket: ServerSocket? = null
    private val clientSockets = mutableListOf<Socket>()
    val BUFFER_SIZE = 1024
    private var outputStream: OutputStream? = null
    private var inputStream: InputStream? = null

    override fun startServer() {
        try {
            serverSocket = ServerSocket(serverPort)
            serverLog("TcpServerManager startServer: ${serverSocket?.isClosed}")
            socketListener.forEach { it.onStart() }
            while (!serverSocket!!.isClosed) {
                // is a blocking call. It waits until a client makes a connection request to the server.
                val clientSocket = serverSocket!!.accept()
                serverLog("TcpServerManager server connected: ${clientSocket.inetAddress.hostAddress}")
                clientSockets.add(clientSocket)

                // Handle each client in a separate coroutine
                CoroutineScope(Dispatchers.IO).launch {
                    handleClient(clientSocket)
                }
            }
        } catch (e: Exception) {
            serverLog("TcpServerManager startServer catch: ${e.message}")
        }
    }

    private suspend fun handleClient(clientSocket: Socket) {
        withContext(Dispatchers.IO) {
            val inputStream = clientSocket.getInputStream()
            val outputStream = clientSocket.getOutputStream()

            try {
                val buffer = ByteArray(BUFFER_SIZE)
                var bytesRead: Int = 0
                // Read data into the buffer and assign the number of bytes read
                while (clientSocket.isConnected && inputStream.read(buffer)
                        .also { bytesRead = it } != -1
                ) {
                    if (bytesRead > 0) {
                        val hexMessage = BytesUtils.bytesToHex(buffer.copyOf(bytesRead))
                        val stringMessage = BytesUtils.hexToString(hexMessage)
                        socketListener.forEach { it.onMessage(stringMessage) }
                    }
                }

            } catch (e: Exception) {
                serverLog("Error handling client: ${e.message}")
                socketListener.forEach { it.onError(e) }
            } finally {
                serverLog("Client disconnected: ${clientSocket.inetAddress.hostAddress}")
                clientSocket.close()
                clientSockets.remove(clientSocket)
            }
        }


    }

    override fun stopServer() {
        try {
            serverLog("stopServer()")
            inputStream?.close()
            outputStream?.close()
            serverSocket?.close()
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    override fun isPortAvailable(): Boolean {
        return try {
            ServerSocket(serverPort).close()
            true
        } catch (e: IOException) {
            false
        }
    }
}