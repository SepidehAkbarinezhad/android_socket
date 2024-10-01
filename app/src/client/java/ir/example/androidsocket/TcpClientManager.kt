package ir.example.androidsocket


import ir.example.androidsocket.utils.clientLog
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import java.io.IOException
import java.net.InetAddress
import java.net.InetSocketAddress
import java.net.Socket

class TcpClientManager(
    override var ip: String, override var port: String,
) : SocketClient {

    var socket: Socket? = null
    val serverAddress: InetAddress = InetAddress.getByName(ip)
    override suspend fun connectWithTimeout(timeoutMillis: Long): Unit =
        withContext(Dispatchers.IO) {
            //to show message properly
            delay(1000)
            try {
                clientLog("connectWithTimeout  $serverAddress  ${socket == null}")
                socket = Socket()
                socket?.connect(
                    InetSocketAddress(serverAddress, port.toInt()),
                    timeoutMillis.toInt()
                )
                clientLog("connectWithTimeout isConnected ${socket?.isConnected}")
                if (socket?.isConnected == true) {
                    clientLog("if(socket?.isConnected == true)")
                }

            } catch (e: IOException) {
                clientLog("IOException--> ${e.message}")
            } catch (e: Exception) {
                clientLog("connectWithTimeout Exception-->  $serverAddress")
                e.printStackTrace()
            }
        }

    override fun disconnect() {
        TODO("Not yet implemented")
    }

    override fun sendMessage(message: String ,timeoutMillis: Long) {
        TODO("Not yet implemented")
    }

    override fun receive(): String {
        TODO("Not yet implemented")
    }


}