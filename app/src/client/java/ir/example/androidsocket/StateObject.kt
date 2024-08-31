package ir.example.androidsocket

import java.net.Socket

data class StateObject(
    var workSocket: Socket? = null,
    var buffer: ByteArray = ByteArray(BUFFER_SIZE),
    var stringBuilder: StringBuilder = StringBuilder()
) {
    companion object {
        const val BUFFER_SIZE = 1024
    }
}
