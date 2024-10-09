package ir.example.androidsocket.client

import ir.example.androidsocket.Constants
import ir.example.androidsocket.utils.clientLog
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.net.Socket

object TcpSocketClient {
    var socket : Socket ? = null
    const val BUFFER_SIZE = 1024

    private val _socketStatus = MutableStateFlow(Constants.SocketStatus.DISCONNECTED)
    val socketStatus: StateFlow<Constants.SocketStatus> get() = _socketStatus.asStateFlow()

    fun updateSocketStatus(connectionStatus: Constants.SocketStatus) {
        clientLog("updateSocketStatus $connectionStatus")
        _socketStatus.value = connectionStatus
    }


}