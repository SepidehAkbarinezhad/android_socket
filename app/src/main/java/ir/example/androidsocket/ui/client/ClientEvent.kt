package ir.example.androidsocket.ui.client

import android.content.Context
import ir.example.androidsocket.Constants

sealed class ClientEvent {
    data class StartClientService(val context: Context) : ClientEvent()
    data class SetOpenPermissionDialog(val open : Boolean) : ClientEvent()
    data class SetLoading(val value: Boolean) : ClientEvent()
    data class SetServerIp(val ip: String) : ClientEvent()
    data class SetServerPort(val port: String) : ClientEvent()
    data class SetSocketConnectionStatus(val status: Constants.SocketStatus) : ClientEvent()
    data class SetClientMessage(val message: String) : ClientEvent()
    data class SetServerMessage(val message: String) : ClientEvent()
    data class SendMessageToServer(val message: String) : ClientEvent()
    data object OnConnectToServer : ClientEvent()
    data object OnDisconnectFromServer : ClientEvent()
}