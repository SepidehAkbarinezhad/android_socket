package ir.example.androidsocket.ui

import android.content.Context
import ir.example.androidsocket.Constants

sealed class ServerEvent {
    data class SetLoading(val value: Boolean) : ServerEvent()
    data class SetProtocolType(val type : String) : ServerEvent()
    data class SetSocketConnectionStatus(val status: Constants.SocketStatus) : ServerEvent()
    data class SetClientMessage(val message: String) : ServerEvent()
    data class GetWifiIpAddress(val context: Context) : ServerEvent()
    data class GetLanIpAddress(val context: Context) : ServerEvent()
}