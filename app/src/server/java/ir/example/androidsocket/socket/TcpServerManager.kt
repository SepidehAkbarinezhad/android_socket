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
import java.nio.ByteBuffer

class TcpServerManager(
    override var serverPort: Int,
    override var path: File,
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

    /**
     * inputStream?.read() : reads one byte from the stream and put it as message type
     * if it is 0x02 it is file type o inputStream.read(ByteArray(4)) read the next 4 bytes to determine the file size
     * **/
    private suspend fun listenToClient(clientSocket: Socket) {
        serverLog("listenToClient")
        withContext(Dispatchers.IO) {
            try {
                serverLog("listenToClient try")
                val buffer = ByteArray(BUFFER_SIZE)
                var bytesRead: Int = 0

                while (clientSocket.isConnected) {
                    // Read the first byte to determine the message type
                    val messageType = inputStream?.read()?.toByte() ?: break

                    when (messageType) {
                        0x01.toByte() -> {
                            // Read text message
                            bytesRead = inputStream?.read(buffer) ?: break
                            if (bytesRead > 0) {
                                val stringMessage = String(buffer, 0, bytesRead)
                                serverLog("Received text message: $stringMessage")
                                socketListener.forEach { it.onMessage(stringMessage) }
                            }
                        }

                        0x02.toByte() -> {
                            // Read file size (4 bytes for a 32-bit integer)
                            val fileSizeBuffer = ByteArray(4)
                            bytesRead = inputStream?.read(fileSizeBuffer) ?: break
                            if (bytesRead == 4) {
                                val fileSize = ByteBuffer.wrap(fileSizeBuffer).int
                                serverLog("Receiving file of size: $fileSize bytes")

                                // Define folder and file paths
                                val folderName = "received_files"
                                val directoryPath = File(path, folderName) // Create a directory path in internal storage

                                // Create directory if it doesn't exist
                                if (!directoryPath.exists()) {
                                    directoryPath.mkdirs() // Create directory
                                    serverLog("Directory created: ${directoryPath.absolutePath}")
                                }

                                val filePath = File(directoryPath, "fileName") // Update with a unique name or timestamp if needed

                                // Prepare to receive the file data
                                FileOutputStream(filePath).use { fileOutput ->
                                    var totalBytesRead = 0

                                    // Read the file data in chunks
                                    while (totalBytesRead < fileSize) {
                                        bytesRead = inputStream?.read(buffer) ?: break
                                        if (bytesRead > 0) {
                                            fileOutput.write(buffer, 0, bytesRead)
                                            totalBytesRead += bytesRead
                                            serverLog("Received $totalBytesRead / $fileSize bytes")
                                        }
                                    }

                                    serverLog("File transfer complete: ${filePath.absolutePath}")
                                }
                            } else {
                                serverLog("Failed to read the file size correctly.")
                            }
                        }
                    }
                }

            } catch (e: Exception) {
                serverLog("Error handling listenToClient: ${e.message}")
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
        clientSocket?.let { client ->
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