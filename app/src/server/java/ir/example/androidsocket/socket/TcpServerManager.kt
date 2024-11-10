package ir.example.androidsocket.socket

import android.content.ContentResolver
import android.content.ContentValues
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import ir.example.androidsocket.Constants.MessageConstantType.MESSAGE_TYPE_FILE_CONTENT
import ir.example.androidsocket.Constants.MessageConstantType.MESSAGE_TYPE_TEXT_CONTENT
import ir.example.androidsocket.SocketConnectionListener
import ir.example.androidsocket.utils.clientLog
import ir.example.androidsocket.utils.serverLog
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.net.ServerSocket
import java.net.Socket
import java.nio.ByteBuffer
import java.nio.ByteOrder

class TcpServerManager(
    override var serverPort: Int,
    override var path: File?,
    override val socketListener: List<SocketConnectionListener>,
    private val contentResolver: ContentResolver
) : SocketServer {

    private var serverSocket: ServerSocket? = null

    // private val clientSockets = mutableListOf<Socket>()
    private var clientSocket: Socket? = null
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
                serverLog("startServer: while  ${serverSocket != null}")
                clientSocket = serverSocket!!.accept()
                serverLog("server connected: ${clientSocket?.inetAddress?.hostAddress}")
                socketListener.forEach { it.onConnected() }
                //clientSockets.add(clientSocket)

                /*
                * Handle each client in a separate coroutine
                */
                CoroutineScope(Dispatchers.IO).launch {
                    serverLog("Server try::")
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
                serverLog("listenToClient try ")
                while (clientSocket.isConnected && !clientSocket.isClosed) {

                    // Read the first byte to determine the message type
                    val messageType = inputStream?.read()?.toByte() ?: break
                    // serverLog("listenToClient messageType : $messageType")
                    when (messageType) {
                        MESSAGE_TYPE_TEXT_CONTENT.toByte() -> {
                            handleTextMessage()
                        }

                        MESSAGE_TYPE_FILE_CONTENT.toByte() -> {
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
        val totalMessageBytesRead = readFully(messageBuffer, messageSize)

        if (totalMessageBytesRead == messageSize) {
            val stringMessage = String(messageBuffer, 0, messageSize)
            serverLog("Received complete text message: $stringMessage")
            if(stringMessage=="GOODBYE"){
                closeClient()
            }else{
                socketListener.forEach {
                    it.onMessage(
                        MESSAGE_TYPE_TEXT_CONTENT,
                        message = stringMessage,
                        fileUri = null
                    )
                }
            }
        } else {
            serverLog("Failed to read the entire message. Only $totalMessageBytesRead bytes read.")
        }
    }

    private suspend fun handleFileMessage() {
        serverLog("handleFileMessage()")
        // Read file size (4 bytes)
        val fileSize = readMessageSizeFromStream() ?: return
        serverLog("handleFileMessage: Receiving file of size: $fileSize bytes")

        val fileTypeLength = readFileTypeLengthFromStream() ?: return
        serverLog("handleFileMessage: fileTypeLength: $fileTypeLength")
        val fileType = readFileType(fileTypeLength) ?: return
        serverLog("handleFileMessage: fileType: $fileType")

        val fileName = "received_file_${System.currentTimeMillis()}"
        val fileContent = receiveFileContent(fileSize)
        fileContent?.let {
            saveFileBasedOnVersion(fileName, fileContent, fileType)
        }
    }

    private fun receiveFileContent(fileSize: Int): ByteArray? {
        try {
            val fileContent = ByteArray(fileSize)
            var totalBytesRead = 0

            while (totalBytesRead < fileSize) {
                val bytesRead =
                    inputStream?.read(fileContent, totalBytesRead, fileSize - totalBytesRead)
                        ?: break
                if (bytesRead > 0) {
                    totalBytesRead += bytesRead
                    // Calculate the percentage of file read
                    val progress = (totalBytesRead * 100) / fileSize

                    // Call the onProgressUpdate callback with the progress percentage
                    socketListener.forEach { it.onProgressUpdate(progress) }

                    serverLog("Progress: $progress%")
                } else {
                    break
                }
            }
            return if (totalBytesRead == fileSize) {
                serverLog("Received full file content: $totalBytesRead bytes.")
                fileContent
            } else {
                serverLog("Failed to receive full file content. Only $totalBytesRead bytes read.")
                null
            }

        } catch (e: Exception) {
            serverLog("Error receiving file content: ${e.message}")
            return null
        }
    }

    private fun saveFileBasedOnVersion(fileName: String, fileContent: ByteArray, fileType: String) {
        try {
            val fileUri = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                // Use MediaStore for Android 10+ (Scoped Storage)
                saveFileToDownloads(fileName, fileContent, fileType)
            } else {
                // Use external storage for Android 9 and below
                val file = prepareFileForReceptionInDownloads(fileName)
                if (file != null) {
                    serverLog("File saved to Downloads: ${file.absolutePath}")
                    file.writeBytes(fileContent) // Write file content to the prepared file
                    Uri.fromFile(file) // Return the file URI
                } else {
                    serverLog("Failed to save the file on Android 9 and below.")
                    null
                }
            }
            socketListener.forEach {
                it.onMessage(
                    MESSAGE_TYPE_FILE_CONTENT,
                    "",
                    fileUri = fileUri
                )
            }
        } catch (e: Exception) {
            serverLog("Error saving received file message: ${e.message}")
            socketListener.forEach { it.onError(e) }
        }

    }

    private suspend fun readMessageSizeFromStream(): Int? {
        serverLog("readMessageSizeFromStream()")
        return withContext(Dispatchers.IO) {
            val sizeBuffer = ByteArray(4)
            val bytesRead = inputStream?.read(sizeBuffer) ?: return@withContext -1
            serverLog("readMessageSizeFromStream() bytesRead $bytesRead")
            if (bytesRead == 4) {
                ByteBuffer.wrap(sizeBuffer).order(ByteOrder.BIG_ENDIAN).int
            } else {
                null // Indicates failure to read size
            }
        }
    }

    private suspend fun readFileTypeLengthFromStream(): Int? {
        serverLog("readFileTypeLengthFromStream()")
        return withContext(Dispatchers.IO) {
            val sizeBuffer = ByteArray(1) // 1 byte for file type length
            val bytesRead = inputStream?.read(sizeBuffer) ?: return@withContext null
            serverLog("readFileTypeLengthFromStream() bytesRead $bytesRead")

            if (bytesRead == 1) {
                sizeBuffer[0].toInt() // Convert the single byte to an integer
            } else {
                serverLog("Failed to read file type length.")
                null // Indicates failure to read the byte
            }
        }
    }

    private suspend fun readFileType(fileLength: Int): String? {
        serverLog("readMessageSizeFromStream()")
        return withContext(Dispatchers.IO) {
            // Read the entire file type based on the length
            val messageBuffer = ByteArray(fileLength)
            val fileType = readFully(messageBuffer, fileLength)
            if (fileType == fileLength) {
                val stringMessage = String(messageBuffer, 0, fileLength)
                serverLog("Received file type: $stringMessage")
                stringMessage
            } else {
                serverLog("Failed to read the entire file type")
                null
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

    private fun saveFileToDownloads(
        fileName: String,
        fileContent: ByteArray,
        fileType: String
    ): Uri? {
        val fullFileName = "$fileName.$fileType"
        val contentValues = ContentValues().apply {
            put(MediaStore.Downloads.DISPLAY_NAME, fullFileName)
            put(MediaStore.Downloads.MIME_TYPE, "application/octet-stream")
            put(
                MediaStore.Downloads.RELATIVE_PATH,
                Environment.DIRECTORY_DOWNLOADS
            ) // Save in Downloads folder
        }

        val uri = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            contentResolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, contentValues)
        } else {
            TODO("VERSION.SDK_INT < Q")
        }

        uri?.let {
            contentResolver.openOutputStream(it)?.use { outputStream ->
                outputStream.write(fileContent)
                serverLog("File saved to Downloads: $fileName")
            }
        } ?: serverLog("Failed to save file.")
        return uri
    }

    private fun prepareFileForReceptionInDownloads(fileName: String): File? {
        val downloadsFolder =
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
        if (!downloadsFolder.exists()) {
            downloadsFolder.mkdirs() // Create the directory if it doesn't exist
        }

        val filePath = File(downloadsFolder, fileName)
        serverLog("File path prepared for Android 9 and below: ${filePath.absolutePath}")
        return filePath
    }


    /**
     * close the client whenever it become disconnected
     * **/
    private fun closeClient() {
        clientSocket?.let { client ->
            socketListener.forEach {
                it.onDisconnected(
                    code = null,
                    reason = " ${client.inetAddress?.hostAddress} is disconnected"
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