package ir.example.androidsocket

object Constants {

    const val CLIENT_MESSAGE_NOTIFICATION_ID = 10

    enum class ClientStatus(
        val title: String
    ) {
        CONNECTED("connected"), DISCONNECTED("Disconnected")
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


}