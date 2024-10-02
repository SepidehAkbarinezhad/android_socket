package ir.example.androidsocket

object Constants {

    enum class SocketStatus(
        val title: String,
        val connection : Boolean
    ) {
        CONNECTED("connected",connection = true), DISCONNECTED("Disconnected",connection = false)
    }

    enum class ConnectionType(
        val title: String
    ) {
        NONE("none"), WIFI("wifi"), ETHERNET("ethernet")
    }

    enum class ActionCode(val title: String ) {
        NotificationMessage(
            title = "client message"
        ),
    }

    enum class ProtocolType(val title: String){
        WEBSOCKET("websocket"),TCP("tcp")
    }


}