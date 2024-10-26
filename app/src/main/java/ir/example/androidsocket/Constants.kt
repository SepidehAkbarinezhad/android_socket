package ir.example.androidsocket

object Constants {

    const val CLIENT_MESSAGE_NOTIFICATION_ID = 10

    enum class SocketStatus(
        val title: String,
        val isConnected: Boolean,
    ) {
        CONNECTED("connected", isConnected = true), DISCONNECTED("Disconnected", isConnected = false)
    }

    enum class ConnectionType(
        val title: String
    ) {
        NONE("none"), WIFI("wifi"), ETHERNET("ethernet")
    }

    enum class ActionCode(val title: String) {
        NotificationMessage(
            title = "client message"
        ),
    }

    enum class ProtocolType(val title: String) {
        WEBSOCKET("Websocket"), TCP("Tcp")
    }

    val PROTOCOLS = listOf(ProtocolType.WEBSOCKET.title, ProtocolType.TCP.title)

    object MessageConstantType {
        const val MESSAGE_TYPE_TEXT_CONTENT = 0x01
        const val MESSAGE_TYPE_FILE_CONTENT = 0x02
    }


}