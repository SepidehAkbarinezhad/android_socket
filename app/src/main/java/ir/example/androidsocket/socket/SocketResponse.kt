package ir.example.androidsocket.socket

data class SocketResponse<out T> (
    val action : Boolean? = null,
    val data: T? = null,
    val message: String? = null,
    val code: String? = null,
)