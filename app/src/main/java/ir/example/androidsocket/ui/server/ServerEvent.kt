package ir.example.androidsocket.ui.server

import android.content.Context
import ir.example.androidsocket.Constants

sealed class ServerEvent {
    data class SetLoading(val value: Boolean) : ServerEvent()
    data class SetConnectionStatus(val status: Constants.ClientStatus) : ServerEvent()
    data class SetClientMessage(val message: String) : ServerEvent()
    data class GetWifiIpAddress(val context: Context) : ServerEvent()
    data class GetLanIpAddress(val context: Context) : ServerEvent()
}