package ir.example.androidsocket.socket

import ir.example.androidsocket.Constants
import ir.example.androidsocket.utils.serverLog
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import okio.Timeout

class ServerManager(
    private val socketProtocol: Constants.ProtocolType,
    private val websocketServerManger: WebsocketServerManger,
    private val tcpSocketManager: TcpServerManager,
) {

    private lateinit var serverManager: SocketServer

    fun startServer() {
        serverManager = when (socketProtocol) {
            Constants.ProtocolType.WEBSOCKET -> websocketServerManger
            Constants.ProtocolType.TCP -> tcpSocketManager
        }
        // check to prevent starting a server on a port that is already in use, which would cause a conflict and result in an error.
        if (!serverManager.isPortAvailable()) {
            stopServer()
        }

        CoroutineScope(Dispatchers.IO).launch {
            try {
                //some times it gets time to release the port
                delay(5000)
                serverManager.startServer()
            } catch (e: Exception) {
                serverLog("ServerManager startServer exception: ${e.message}")
            }
        }
    }

    /**
     * when client message is received by server , server send a confirmation message to client
     * **/
    suspend fun sendMessageWithTimeout(message: String, timeoutMillis: Long = 20000) {
        serverManager.sendMessageWithTimeout(message,timeoutMillis)
    }

    fun stopServer() {
        serverLog("ServerManager stopServer")
        serverManager.stopServer()
    }


}