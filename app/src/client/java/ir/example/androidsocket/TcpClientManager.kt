package ir.example.androidsocket

import ir.example.androidsocket.utils.clientLog
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.withContext
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.net.InetAddress
import java.net.InetSocketAddress
import java.net.Socket

class TcpClientManager(val serverAddress: InetAddress, val serverPort: Int) {

    private var socket: Socket? = null
    private var outputStream: OutputStream? = null
    private var inputStream: InputStream? = null

    fun pingServer(ip: String): Boolean {
        return try {
            val process = Runtime.getRuntime().exec("/system/bin/ping -c 1 $ip")
            val result = process.waitFor()
            result == 0
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    fun connectSimple(): Boolean {
        return try {
            val socket = Socket(serverAddress, serverPort)
            socket.close()
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
    suspend fun connect() {
        withContext(Dispatchers.IO) {
        socket = Socket()
        socket?.connect(
            InetSocketAddress(serverAddress, serverPort)
        )}
    }

    suspend fun connectWithTimeout(timeoutMillis: Long): Unit =
        withContext(Dispatchers.IO) {
             try {
                clientLog("connectWithTimeout  $serverAddress")
                socket = Socket()
                socket?.connect(
                    InetSocketAddress(serverAddress, serverPort),
                    200
                )
                outputStream = socket?.getOutputStream()
                inputStream = socket?.getInputStream()
                 clientLog("outputStream  $outputStream}")
                 clientLog("inputStream  $inputStream}")
                 clientLog("inputStream null? ${inputStream == null}")
                 clientLog("isConnected ${socket?.isConnected}")
                 clientLog("isClosed ${socket?.isClosed}")


                /* // Reading data from InputStream
                 val buffer = ByteArray(1024) // Buffer for reading data
                 clientLog(" 1")
                 val bytesRead = inputStream?.read(buffer) // Attempt to read from the InputStream
                 clientLog(" if (bytesRead != -1)  ${ bytesRead != -1}")
                 if (bytesRead != -1) {
                     clientLog("bytesRead != -1")

                     if(bytesRead!=null){
                         val receivedData = String(buffer, 0, bytesRead)
                         clientLog("Received data: $receivedData")
                     }

                 } else {
                     clientLog("End of stream reached.")
                 }*/
            }catch (e: IOException) {
                 clientLog("IOException--> ${e.message}")
             }
             catch (e: Exception) {
                 clientLog("Exception-->  $serverAddress")
                e.printStackTrace()

            }
        }

    suspend fun sendMessage(message: String): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                outputStream?.write(message.toByteArray())
                outputStream?.flush()
                true
            } catch (e: Exception) {
                e.printStackTrace()
                false
            }
        }
    }

    fun closeConnection() {
        try {
            inputStream?.close()
            outputStream?.close()
            socket?.close()
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }
}