package ir.example.androidsocket.socket

import ir.example.androidsocket.SocketConnectionListener
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
import java.nio.ByteOrder

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
            socketListener.forEach { it.onException(e) }
        }
    }

    /**
     * inputStream?.read() : reads one byte from the stream and put it as message type
     * if it is 0x02 it is file type o inputStream.read(ByteArray(4)) read the next 4 bytes to determine the file size
     * **/
    private suspend fun listenToClient(clientSocket: Socket) {
        serverLog("listenToClient()")
        withContext(Dispatchers.IO) {
            try {
                serverLog("listenToClient try")

                while (clientSocket.isConnected) {
                    // Read the first byte to determine the message type
                    val messageType = inputStream?.read()?.toByte() ?: break
                    serverLog("listenToClient messageType : $messageType")
                    when (messageType) {
                        0x01.toByte() -> {
                            handleTextMessage()
                        }
                        0x02.toByte() -> {
                            handleFileMessage()
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

    private suspend fun handleTextMessage() {
        // Read message size (4 bytes for a 32-bit integer)
        val messageSize = readMessageSizeFromStream() ?: return
        serverLog("handleTextMessage messageSize: $messageSize ")

        // Read the entire message based on the size
        val messageBuffer = ByteArray(messageSize)
       /* val totalMessageBytesRead = readFully(messageBuffer, messageSize)

        if (totalMessageBytesRead == messageSize) {
            val stringMessage = String(messageBuffer, 0, messageSize)
            serverLog("Received complete text message: $stringMessage")
            socketListener.forEach { it.onMessage(stringMessage) }
        } else {
            serverLog("Failed to read the entire message. Only $totalMessageBytesRead bytes read.")
        }*/
    }

    private suspend fun handleFileMessage() {
        serverLog("handleFileMessage()")
        // Read file size (4 bytes)
        val fileSize = readMessageSizeFromStream() ?: return
        serverLog("handleFileMessage: Receiving file of size: $fileSize bytes")

        val filePath = prepareFileForReception("received_folder", "received_file")
        receiveFile(filePath, fileSize)
    }

    private fun receiveFile(filePath: File, fileSize: Int) {
        FileOutputStream(filePath).use { fileOutput ->
            var totalBytesRead = 0
            var bytesRead: Int
            val buffer = ByteArray(BUFFER_SIZE)

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
    }

    private fun prepareFileForReception(folderName: String, fileName: String): File {
        val directoryPath = File(path, folderName)

        // Create directory if it doesn't exist
        if (!directoryPath.exists()) {
            directoryPath.mkdirs()
            serverLog("Directory created: ${directoryPath.absolutePath}")
        }

        return File(directoryPath, fileName) // Return the file path
    }

    private suspend fun readMessageSizeFromStream(): Int? {
        serverLog("readMessageSizeFromStream()")
        return withContext(Dispatchers.IO){
            val sizeBuffer = ByteArray(4)
            val bytesRead = inputStream?.read(sizeBuffer) ?: return@withContext -1
            if (bytesRead == 4) {
                ByteBuffer.wrap(sizeBuffer).order(ByteOrder.BIG_ENDIAN).int
            } else {
                null // Indicates failure to read size
            }
        }
    }

    private fun readFully(buffer: ByteArray, expectedSize: Int): Int {
        var totalBytesRead = 0
        var bytesRead: Int

        while (totalBytesRead < expectedSize) {
            bytesRead =
                inputStream?.read(buffer, totalBytesRead, expectedSize - totalBytesRead) ?: break
            if (bytesRead > 0) {
                totalBytesRead += bytesRead
            } else {
                break
            }
        }
        return totalBytesRead
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