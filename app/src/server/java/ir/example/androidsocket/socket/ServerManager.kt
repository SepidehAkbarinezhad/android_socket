package ir.example.androidsocket.socket

import ir.example.androidsocket.Constants
import ir.example.androidsocket.utils.serverLog
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class ServerManager(
    val socketProtocol: Constants.ProtocolType,
    val websocketServerManger: WebsocketServerManger,
    val tcpSocketManager: TcpServerManager,
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
                serverLog("SocketServerForegroundService startSocketServer exception: ${e.message}")
            }
        }
    }

    fun stopServer() {
        TODO("Not yet implemented")
    }


}