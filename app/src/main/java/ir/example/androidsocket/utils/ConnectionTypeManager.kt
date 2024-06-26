package ir.example.androidsocket.utils

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import ir.example.androidsocket.Constants
import kotlinx.coroutines.flow.MutableStateFlow


class ConnectionTypeManager(context: Context) {

    private val connectivityManager =
        context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

    var connectionType: MutableStateFlow<Constants.ConnectionType> =
        MutableStateFlow(Constants.ConnectionType.NONE)
        private set

    var isEthernetConnected: MutableStateFlow<Boolean?> = MutableStateFlow(null)
        private set

    var isWifiConnected: MutableStateFlow<Boolean?> = MutableStateFlow(null)
        private set

    init {
        checkConnectionStatus()
    }

    fun checkConnectionStatus() {
        val networkCapability =
            connectivityManager.getNetworkCapabilities(connectivityManager.activeNetwork)
        if (networkCapability?.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) == true) {
            serverLog("checkEthernetStatus->ETHERNET")
            connectionType.value = Constants.ConnectionType.ETHERNET
            isEthernetConnected.value = true
        } else if (networkCapability?.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) == true) {
            serverLog("checkEthernetStatus->WIFI")
            connectionType.value = Constants.ConnectionType.WIFI
            isWifiConnected.value = true
        } else {
            serverLog("checkEthernetStatus->NONE")
            connectionType.value = Constants.ConnectionType.NONE
            isWifiConnected.value = false
            isEthernetConnected.value = false
        }
    }
}