package ir.example.androidsocket.client


import android.content.ContentResolver
import android.net.Uri
import android.os.ParcelFileDescriptor
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
import java.nio.ByteBuffer

class TcpClientManager(
    override var ip: String,
    override var port: String,
    override val socketListener: List<SocketConnectionListener>,
    private val contentResolver: ContentResolver
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
                clientLog("TcpClientManager connectWithTimeout  $timeoutMillis $serverAddress $port")
                socket = Socket()
                socket?.let { socket ->

                    socket.connect(
                        InetSocketAddress(serverAddress, port.toInt()),
                        timeoutMillis.toInt()
                    )

                    clientLog("TcpClientManager connectWithTimeout isConnected ${socket.isConnected}")
                    if (socket.isConnected) {
                        socketListener.forEach { it.onConnected() }
                        inputStream = socket.getInputStream()
                        outputStream = socket.getOutputStream()

                        listenToServer(socket)
                    }
                }


            } catch (e: IOException) {
                clientLog("connectWithTimeout IOException--> ${e.message}")
                socketListener.forEach { it.onException(e) }
            } catch (e: Exception) {
                clientLog("connectWithTimeout Exception-->  $serverAddress")
                socketListener.forEach { it.onException(e) }
            }
        }

    override fun sendMessage(message: String, timeoutMillis: Long) {
        clientLog("sendMessage()")
        CoroutineScope(Dispatchers.IO).launch {
            try {
                clientLog("send-->try")
                val outputStream = socket?.getOutputStream()

                // Convert message to byte array
                val messageBytes = message.toByteArray()

                // Convert message size to 4-byte array (integer)
                val messageSizeBytes = ByteBuffer.allocate(4).putInt(messageBytes.size).array()

                // Message type as a single byte (for example, 0x01 for text message)
                val messageType = 0x01.toByte()

                // Combine everything into one array
                val dataToSend = ByteArray(1 + 4 + messageBytes.size)
                dataToSend[0] = messageType // First byte is message type
                System.arraycopy(messageSizeBytes, 0, dataToSend, 1, 4) // Next 4 bytes are the message size
                System.arraycopy(messageBytes, 0, dataToSend, 5, messageBytes.size) // Rest is the message

                // Write the combined array to the output stream
                outputStream?.write(dataToSend)
                outputStream?.flush()

            } catch (e: Exception) {
                clientLog("send--> catch ${e.message}")
            }
        }
    }

    override fun sendFile(uri: Uri) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                clientLog("sendFile--> try $uri")
                val outputStream = socket?.getOutputStream()

                /**
                 * Get the file size using ContentResolver
                 * ParcelFileDescriptor : allows you to perform file operations like reading or writing on the file associated with the URI.
                 * **/
                val fileDescriptor: ParcelFileDescriptor? = contentResolver.openFileDescriptor(uri, "r")
                if (fileDescriptor != null) {
                    val fileSize : Long =
                        fileDescriptor.statSize // Get the file size directly from the file descriptor

                    val messageType = 0x02.toByte()

                    clientLog("sendFile--> write1")

                    // Send the file size to the server first
                    clientLog("sendFile--> Sending file size: $fileSize bytes")
                    val messageSizeBytes = ByteBuffer.allocate(4).putInt(fileSize.toInt()).array()


                    // Send message type and file size in a single array
                    val header = ByteArray(1 + 4) // 1 byte for message type + 4 bytes for file size
                    header[0] = messageType // Set the message type
                    System.arraycopy(messageSizeBytes, 0, header, 1, 4) // Copy the file size after the message type

                    // Write the header (message type + file size)
                    outputStream?.write(header)
                    outputStream?.flush()

                    // Prepare to read the file
                    val fileInputStream = contentResolver.openInputStream(uri)
                    if (fileInputStream == null) {
                        clientLog("sendFile--> InputStream is null for Uri: $uri")
                        return@launch
                    }

                    // Send the file in chunks
                    val buffer = ByteArray(BUFFER_SIZE)

                    var bytesRead: Int
                    var totalBytesRead = 0

                   while (fileInputStream.read(buffer).also { bytesRead = it } != -1) {
                       clientLog("sendFile-->sendFile while $bytesRead")
                       totalBytesRead += bytesRead
                       val progress = (totalBytesRead * 100) / fileSize.toInt()
                       delay(50)
                       socketListener.forEach { it.onProgressUpdate(progress) }
                        outputStream?.write(buffer, 0, bytesRead)
                        outputStream?.flush()
                    }
                    fileInputStream.close()
                    clientLog("sendFile--> File transfer complete")
                } else {
                    clientLog("sendFile--> Could not open file descriptor for Uri: $uri")
                }

            } catch (e: Exception) {
                clientLog("sendFile--> catch ${e.message}")
                socketListener.forEach { it.onError(e) }
            }
        }
    }

    /**
     *  listen to stream came from server
     *  **/
    private suspend fun listenToServer(clientSocket: Socket) {
        clientLog("listenToServer")

        withContext(Dispatchers.IO) {

            try {
                clientLog("listenToServer try")
                val buffer = ByteArray(BUFFER_SIZE)
                clientLog("listenToServer try ${clientSocket.isConnected}")
                var bytesRead: Int = 0
                // Read data into the buffer and assign the number of bytes read
                while (clientSocket.isConnected && inputStream?.read(buffer)
                        .also { bytesRead = it ?: 0 } != -1
                ) {
                    clientLog("listenToServer while: ${clientSocket.isConnected}")
                    if (bytesRead > 0) {
                        clientLog("handleClient try bytesRead > 0")
                        val hexMessage = BytesUtils.bytesToHex(buffer.copyOf(bytesRead))
                        val stringMessage = BytesUtils.hexToString(hexMessage)
                        socketListener.forEach { it.onMessage(stringMessage) }
                    }
                }

            } catch (e: Exception) {
                clientLog("listenToServer catch: ${e.message}")
                socketListener.forEach { it.onError(e) }
            } finally {
                /**
                 * this block is executed whenever the while become false (client becomes disconnected or inputStream?.read(buffer)==-1 which means server is disconnected) or
                 * the block throws an exception
                 * **/
                clientLog("listenToServer finally: ${clientSocket.isConnected}")
                closeClient()
            }
        }
    }

    /**
     * close the client whenever it become disconnected
     * **/
    private fun closeClient() {
        clientLog("closeClient")
        socket?.let { client ->
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

    override fun disconnect() {
        closeClient()
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