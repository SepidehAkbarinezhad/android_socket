package ir.example.androidsocket.utils

object BytesUtils {
    private const val GBK = "GBK"
    const val UTF_8 = "utf-8"
    private val ASCII = "0123456789ABCDEF".toCharArray()
    private val HEX_VOCABLE = charArrayOf('0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F')

    fun hexToString(hex: String): String {
        val output = StringBuilder()
        for (i in hex.indices step 2) {
            val str = hex.substring(i, i + 2)
            output.append(str.toInt(16).toChar())
        }
        return output.toString()
    }

    fun bytesToHex(bs: ByteArray): String {
        val sb = StringBuilder()

        for (b in bs) {
            val high = (b.toInt() shr 4) and 15
            val low = b.toInt() and 15
            sb.append(HEX_VOCABLE[high])
            sb.append(HEX_VOCABLE[low])
        }

        return sb.toString()
    }
}