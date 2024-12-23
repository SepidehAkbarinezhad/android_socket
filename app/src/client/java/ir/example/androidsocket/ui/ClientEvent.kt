package ir.example.androidsocket.ui

import android.content.Context
import android.net.Uri
import ir.example.androidsocket.Constants

sealed class ClientEvent {
    data class StartClientService(val context: Context) : ClientEvent()
    data class SetLoading(val value: Boolean) : ClientEvent()
    data class SetInConnectionProcess(val value: Boolean) : ClientEvent()
    data class SetProtocolType(val type : String) : ClientEvent()
    data class SetServerIp(val ip: String) : ClientEvent()
    data class SetServerPort(val port: String) : ClientEvent()
    data class SetSocketConnectionStatus(val status: Constants.SocketStatus) : ClientEvent()
    data class SetClientMessage(val message: String) : ClientEvent()
    data class SetFileUrl(val uri: Uri?) : ClientEvent()
    data class SetServerMessage(val message: String) : ClientEvent()
    data class SendMessageToServer(val message: String) : ClientEvent()
    data object OnConnectionButtonClicked : ClientEvent()
    data object ResetClientMessage : ClientEvent()
}