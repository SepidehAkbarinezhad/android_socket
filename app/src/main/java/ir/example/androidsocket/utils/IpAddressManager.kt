package ir.example.androidsocket.utils

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.wifi.WifiInfo
import android.net.wifi.WifiManager
import java.net.Inet4Address
import java.net.NetworkInterface


object IpAddressManager {

    fun getLocalIpAddress(context: Context): Pair<String?, String?> {
        var wifiIpAddress: String? = null
        var ethernetIpAddress: String? = null

        val connectivityManager =
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

        // WiFi
        val wifiNetwork =
            connectivityManager.getNetworkCapabilities(connectivityManager.activeNetwork)
        if (wifiNetwork?.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) == true) {
            wifiIpAddress = getWifiIpAddress(context)
        }

        // Ethernet
        val ethernetNetwork =
            connectivityManager.getNetworkCapabilities(connectivityManager.activeNetwork)
        if (ethernetNetwork?.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) == true) {
            ethernetIpAddress = getEthernetIpAddress()
        }

        return Pair(wifiIpAddress, ethernetIpAddress)
    }

    private fun getWifiIpAddress(context: Context): String? {
        try {
            val wifiManager =
                context.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
            val wifiInfo: WifiInfo = wifiManager.connectionInfo
            val ipAddress = wifiInfo.ipAddress

            return formatIpAddress(ipAddress)
        } catch (ex: Exception) {
        }
        return null
    }

    private fun getEthernetIpAddress(): String? {
        try {
            val networkInterfaces = NetworkInterface.getNetworkInterfaces()

            while (networkInterfaces.hasMoreElements()) {
                val networkInterface = networkInterfaces.nextElement()

                // Check if the interface is an Ethernet interface
                if (networkInterface.name.startsWith("eth") || networkInterface.name.startsWith("eth0")) {
                    val inetAddresses = networkInterface.inetAddresses

                    while (inetAddresses.hasMoreElements()) {
                        val inetAddress = inetAddresses.nextElement()

                        // Check if it's an IPv4 address
                        if (inetAddress is Inet4Address) {
                            return inetAddress.hostAddress
                        }
                    }
                }
            }
        } catch (ex: Exception) {
        }
        return null
    }

    private fun formatIpAddress(ipAddress: Int): String {
        return "${ipAddress and 0xFF}.${(ipAddress shr 8) and 0xFF}.${(ipAddress shr 16) and 0xFF}.${(ipAddress shr 24) and 0xFF}"
    }
}