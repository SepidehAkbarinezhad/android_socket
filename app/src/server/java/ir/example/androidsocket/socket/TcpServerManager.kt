package ir.example.androidsocket.socket

import ir.example.androidsocket.SocketConnectionListener
import ir.example.androidsocket.utils.BytesUtils
import ir.example.androidsocket.utils.clientLog
import ir.example.androidsocket.utils.serverLog
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
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

    // private val clientSockets = mutableListOf<Socket>()
    private var clientSocket: Socket? = null
    val BUFFER_SIZE = 1024
    private var inputStream: InputStream? = null
    private var outputStream: OutputStream? = null

    override fun startServer() {
        try {
            serverSocket = ServerSocket(serverPort)
            serverLog("startServer: ${serverSocket?.isClosed}")
            socketListener.forEach { it.onStart() }
            while (!serverSocket!!.isClosed) {
                /**
                 * accept is a blocking call. It waits until a client makes a connection request to the server.
                 **/
                clientSocket = serverSocket!!.accept()
                serverLog("server connected: ${clientSocket?.inetAddress?.hostAddress}")
                socketListener.forEach { it.onConnected() }
                //clientSockets.add(clientSocket)

                /*
                * Handle each client in a separate coroutine
                */
                CoroutineScope(Dispatchers.IO).launch {
                    clientSocket?.let {
                        inputStream = clientSocket?.getInputStream()
                        outputStream = clientSocket?.getOutputStream()
                        listenToClient(it)
                    }
                }
            }
        } catch (e: Exception) {
            serverLog("startServer catch: ${e.message}")
        }
    }

    private suspend fun listenToClient(clientSocket: Socket) {
        serverLog("handleClient")
        withContext(Dispatchers.IO) {
            try {
                serverLog("handleClient try")
                val buffer = ByteArray(BUFFER_SIZE)
                var bytesRead: Int = 0
                // Read data into the buffer and assign the number of bytes read
                while (clientSocket.isConnected && inputStream?.read(buffer)
                        .also { bytesRead = it?:0 } != -1
                ) {
                    if (bytesRead > 0) {
                        serverLog("handleClient try bytesRead > 0")
                        val hexMessage = BytesUtils.bytesToHex(buffer.copyOf(bytesRead))
                        val stringMessage = BytesUtils.hexToString(hexMessage)
                        socketListener.forEach { it.onMessage(stringMessage) }
                    }
                }

            } catch (e: Exception) {
                serverLog("Error handling client: ${e.message}")
                socketListener.forEach { it.onError(e) }
            } finally {
                /**
                 * this block is executed whenever the while become false or throw an exception
                 * **/
                serverLog("handleClient finally ${clientSocket.isConnected}")
                closeClient()

            }
        }
    }

    private suspend fun listenToClientFile(clientSocket: Socket) {
        serverLog("handleClient")
        withContext(Dispatchers.IO) {
            try {
                serverLog("handleClient try")

                // Prepare to receive a file (or message) from the client
                val buffer = ByteArray(BUFFER_SIZE)

                //indicates how many bytes were actually read from the input stream in the current read operation
                var bytesRead: Int = 0
                var totalBytesRead: Long = 0

                // File to save the incoming data, you may want to adjust the path
                val fileOutput = FileOutputStream(File("/path/to/save/received_file"))

                // Read data into the buffer and assign the number of bytes read
                while (clientSocket.isConnected && inputStream?.read(buffer)
                        .also { bytesRead = it ?: 0 } != -1
                ) {
                    if (bytesRead > 0) {
                        serverLog("handleClient bytesRead > 0")

                        // Write the received data to a file
                        fileOutput.write(buffer, 0, bytesRead)

                        // Update the total bytes read so far
                        totalBytesRead += bytesRead

                        // Calculate the progress percentage
                       /* val progress = (totalBytesRead.toDouble() / totalFileSize) * 100
                        serverLog("Progress: $progress%")

                        // Notify listeners with the progress if needed
                        socketListener.forEach { it.onProgress(progress.toInt()) }*/
                    }
                }

                // Close the file output stream after receiving all the data
                fileOutput.close()

            } catch (e: Exception) {
                serverLog("Error handling client: ${e.message}")
                socketListener.forEach { it.onError(e) }
            } finally {
                serverLog("handleClient finally ${clientSocket.isConnected}")
                closeClient()
            }
        }
    }

    /**
     * close the client whenever it become disconnected
     * **/
    private fun closeClient() {
        clientSocket?.let { client->
            socketListener.forEach {
                it.onDisconnected(
                    code = null,
                    reason = "client : ${client.inetAddress?.hostAddress} is disconnected"
                )
            }
            inputStream?.close()
            outputStream?.close()
            client.close()
        }

    }

    override fun stopServer() {
        try {
            serverLog("stopServer()")
            serverSocket?.close()
            closeClient()
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

    override suspend fun sendMessageWithTimeout(message: String, timeoutMillis: Long) {
        serverLog("sendMessageWithTimeout")
        CoroutineScope(Dispatchers.IO).launch {
            try {
                clientLog("send-->try")
                outputStream?.write(message.toByteArray())
                outputStream?.flush()
            } catch (e: Exception) {
                clientLog("send--> catch ${e.message}")
            }
        }
    }
}