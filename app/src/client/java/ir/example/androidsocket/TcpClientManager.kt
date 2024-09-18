package ir.example.androidsocket

import ir.example.androidsocket.utils.clientLog
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.net.InetAddress
import java.net.InetSocketAddress
import java.net.Socket
import java.util.Timer


class TcpClientManager(
    val serverAddress: InetAddress,
    val serverPort: Int,
) {

    enum class TransTypes(val value: Int) {
        TYPE_TRANSACTION(0),
        TYPE_SURVEY(1),
        TYPE_LASTTRANS(2)
    }

    private val ASC_ENQ: Byte = 0x05
    private val LIB_VER: Byte = 0x00
    private val ASC_STX: Byte = 0x02
    private val ASC_ETX: Byte = 0x03;
    private var sendBuf = ByteArray(1024)


    private var socket: Socket? = null
    private var outputStream: OutputStream? = null
    private var inputStream: InputStream? = null

    private var isEnqSent: Boolean = false
    private lateinit var timer: Timer
    private var mTransType: TransTypes = TransTypes.TYPE_TRANSACTION

    private var pSpentAmount = ByteArray(13)
    private var pInvoiceNumber = ByteArray(16)
    private var pBranchID = ByteArray(8)
    private var pDisCountAmount = ByteArray(8)
    private var pAgentCode = ByteArray(30)
    private var pAgentPass = ByteArray(30)
    private var pID1 = ByteArray(13)
    private val pIsMultiAccount: Byte = 0


    suspend fun connectWithTimeout(timeoutMillis: Long): Unit =
        withContext(Dispatchers.IO) {
            try {
                clientLog("connectWithTimeout  $serverAddress  ${socket == null}")
                socket = Socket()
                socket?.connect(
                    InetSocketAddress(serverAddress, serverPort),
                    timeoutMillis.toInt()
                )
                clientLog("connectWithTimeout isConnected ${socket?.isConnected}")
                if (socket?.isConnected == true) {
                    clientLog("if(socket?.isConnected == true)")
                    sendDataToDrawCard()
                }

            } catch (e: IOException) {
                clientLog("IOException--> ${e.message}")
            } catch (e: Exception) {
                clientLog("Exception-->  $serverAddress")
                e.printStackTrace()

            }
        }


    private suspend fun receiveData() {
        try {
            clientLog("receiveData try  ${socket == null}")

            withContext(Dispatchers.IO) {
                val inputStream = socket?.getInputStream()
                val buffer = ByteArray(1024)
                while (true) {
                    clientLog("receiveData inputStream   ${inputStream == null}")
                    // socket?.soTimeout = 8000
                    val bytesRead =
                        try {
                            clientLog("receiveData read try")
                            clientLog("isConnected ${socket?.isConnected}")
                            clientLog("isClosed ${socket?.isClosed}")
                            inputStream?.read(buffer)

                        } catch (e: Exception) {
                            clientLog("Exception during read: ${e.cause}   e is $e")
                            throw e // Rethrow to be caught in outer catch
                        }
                    clientLog("buffer is $buffer")


                    if (bytesRead != null && bytesRead > 0) {
                        parseReceivedData(buffer)
                        val hexData = BytesUtils.bytesToHex(buffer)


                        clientLog("buffer[0]: ${BytesUtils.byteToHex(buffer[0])}")
                        clientLog("buffer[1]: ${BytesUtils.byteToHex(buffer[1])}")
                        clientLog("buffer[2]: ${BytesUtils.byteToHex(buffer[2])}")
                        clientLog("buffer[3]: ${BytesUtils.byteToHex(buffer[3])}")
                        clientLog("buffer[4]: ${BytesUtils.byteToHex(buffer[4])}")
                        clientLog("buffer[5]: ${BytesUtils.byteToHex(buffer[5])}")
                        val stringData = BytesUtils.hexToString(hexData)
                        clientLog("hex is $hexData")
                        clientLog("string is $stringData")
                    }
                }
            }


        } catch (e: IOException) {
            withContext(Dispatchers.Main) {
                clientLog("IOException--> ${e.message}")
            }
        } catch (e: Exception) {
            withContext(Dispatchers.Main) {
                clientLog("Exception--> ${e.message}")
            }
        } finally {
            clientLog("Closing InputStream")
            try {
                inputStream?.close()
            } catch (e: IOException) {
                clientLog("IOException during InputStream close: ${e.message}")
            }
        }
    }


    private fun parseReceivedData(data : ByteArray){
        if (BytesUtils.byteToHex(data[4]) == "89") {
            //card is withdraw and pan is read
        }
    }
    private fun sendDataToDrawCard() {
        clientLog("sendDataToDrawCard()")
        var i = 0
        if (!isEnqSent) {
            i = sendWithdrawCardRequest()

            send(sendBuf, i)

            isEnqSent = true
            //  timer.interval = 1000L
            // timer.isEnabled = true
        }
    }

    private fun sendWithdrawCardRequest(): Int {
        clientLog("sendRequest()")
        val request = ByteArray(1024)
        var i = 0

        request[i++] = ASC_ENQ

       /* when (mTransType) {
            TransTypes.TYPE_TRANSACTION -> request[i++] = 0
            TransTypes.TYPE_SURVEY -> request[i++] = 1
            TransTypes.TYPE_LASTTRANS -> request[i++] = 2
        }*/

      /*  if (LIB_VER < 2) {
            if (pSpentAmount.isNotEmpty() && pInvoiceNumber.isNotEmpty()) {
                pSpentAmount.copyInto(request, i);
                i += 13;
                pInvoiceNumber.copyInto(request, i);
                i += 16;
            }
        } else {
            request[i++] = LIB_VER
            pSpentAmount = setLeftZero("1000", pSpentAmount.size)
            pSpentAmount.copyInto(request, destinationOffset = i)
            i += 13
            pInvoiceNumber = setLeftZero("2000", pInvoiceNumber.size)
            pInvoiceNumber.copyInto(request, destinationOffset = i)
            i += 40
        }*/

        sendBuf = request
        // Assuming you need to return the modified array
        return i
    }

    fun setAmount() {
        clientLog("setAmount()")
        sendBuf = sendAmount()
        send(sendBuf, 131)
        CoroutineScope(Dispatchers.IO).launch {
            receiveData()
        }
    }

    private fun send(sendBuf: ByteArray, bufLen: Int) {
        clientLog("send()")
        try {
            clientLog("send-->try")
            val outputStream = socket?.getOutputStream()
            outputStream?.write(sendBuf, 0, bufLen)
            outputStream?.flush()
        } catch (e: Exception) {
            clientLog("send--> catch ${e.message}")
        }
        CoroutineScope(Dispatchers.IO).launch {
            receiveData()
        }
    }

    private fun sendAmount(): ByteArray {
        clientLog("sendAmount()")

        val request = ByteArray(1024)
        var i = 0

        request[i++] = ASC_STX

        request[i++] = 0x01
        request[i++] = 0x26
        request[i++] = 0x91.toByte()
        request[i++] = 0x01


        pSpentAmount = setLeftZero("10000", pSpentAmount.size)
        pSpentAmount.copyInto(request, i)
        i += 13

        pInvoiceNumber = setLeftZero("2000", pInvoiceNumber.size)
        pInvoiceNumber.copyInto(request, i)
        i += 16

        pBranchID = setLeftZero("0", pBranchID.size)
        pBranchID.copyInto(request, i)
        i += 8

        pDisCountAmount = setLeftZero("", pDisCountAmount.size)
        pDisCountAmount.copyInto(request, i)
        i += 13

        pAgentCode = setLeftZero("5678", pAgentCode.size)
        pAgentCode.copyInto(request, i)
        i += 30


        pAgentPass = setLeftZero("9000", pAgentPass.size)
        pAgentPass.copyInto(request, i)
        i += 30


        request[i++] = pIsMultiAccount

        pID1 = setLeftZero("987654321", pID1.size)
        pID1.copyInto(request, i)
        i += 13

        request[i++] = ASC_ETX
        request[i] = lrc(request)

        return request
    }


    private fun lrc(buf: ByteArray): Byte {
        clientLog("lrc()")
        var result = 0
        val strBuffer = buf.sliceArray(1 until buf.size).toString(Charsets.US_ASCII)

        for (c in strBuffer) {
            result = result xor c.code
        }

        return result.toByte()
    }

    private fun setLeftZero(value: String, len: Int): ByteArray {
        clientLog("setLeftZero $value  $len")
        val sValue = "0".repeat(len) + value.trim()
        val substring = sValue.takeLast(len)

        val bytes = substring.toByteArray(Charsets.UTF_8)
        return bytes
    }

    fun closeConnection() {
        try {
            inputStream?.close()
            outputStream?.close()
            socket?.close()
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

}