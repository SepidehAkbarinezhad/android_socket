package ir.example.androidsocket.utils

import androidx.core.text.isDigitsOnly


fun isIpValid(ip: String): Boolean {
    for (i in ip) {
        if ((i != '.') && (!i.isDigit()))
            return false
    }
    return true
}

fun isPortValid(port: String): Boolean {
    return port.isDigitsOnly()
}
