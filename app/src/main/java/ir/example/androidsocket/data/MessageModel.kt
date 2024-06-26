package ir.example.androidsocket.data

data class MessageModel<T>(
    val actionCode: Int? = null,
    val data: T? = null,
    val message: String? = null
)

