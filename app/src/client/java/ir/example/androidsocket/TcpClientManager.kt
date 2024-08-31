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

class TcpClientManager(val serverAddress: InetAddress, val serverPort: Int) {

    enum class TransTypes(val value: Int) {
        TYPE_TRANSACTION(0),
        TYPE_SURVEY(1),
        TYPE_LASTTRANS(2)
    }

    private val ASC_ENQ: Byte = 0x05
    private val LIB_VER: Byte = 0x02
    private var sendBuf = ByteArray(1024)


    private var socket: Socket? = null
    private var outputStream: OutputStream? = null
    private var inputStream: InputStream? = null

    private var stateFlag: Int = 0
    private var isEnqSent: Boolean = false
    private lateinit var timer: Timer
    private var mTransType: TransTypes = TransTypes.TYPE_TRANSACTION


    fun pingServer(ip: String): Boolean {
        return try {
            val process = Runtime.getRuntime().exec("/system/bin/ping -c 1 $ip")
            val result = process.waitFor()
            result == 0
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    suspend fun connectWithTimeout(timeoutMillis: Long): Unit =
        withContext(Dispatchers.IO) {
            try {
                clientLog("connectWithTimeout  $serverAddress")
                socket = Socket()
                socket?.connect(
                    InetSocketAddress(serverAddress, serverPort),
                    timeoutMillis.toInt()
                )
                clientLog("connectWithTimeout isConnected ${socket?.isConnected}")
                if (socket?.isConnected == true) {
                    clientLog("if(socket?.isConnected == true)")
                  //  receive()
                     sendData()
                }

            } catch (e: IOException) {
                clientLog("IOException--> ${e.message}")
            } catch (e: Exception) {
                clientLog("Exception-->  $serverAddress")
                e.printStackTrace()

            }
        }

    private fun receive() {
        clientLog("receive()")

        try {
            clientLog("receive() try")
            // Create the state object
            val state = StateObject()
            state.workSocket = socket

            // Launch a coroutine to handle the asynchronous receive
            CoroutineScope(Dispatchers.IO).launch {
                receiveData(state)
            }
        } catch (e: Exception) {
            clientLog("receive e-->  ${e.message}")
        }
    }

    private suspend fun receiveData(state: StateObject) {
        clientLog("receiveData()")
        try {
            clientLog("receiveData try  ${socket == null}")

             withContext(Dispatchers.IO) {
                clientLog("inputStream")
                val inputStream =  socket?.getInputStream()
                 while (true) {
                     clientLog("receiveData inputStream is null  ${inputStream == null}")
                    // socket?.soTimeout = 8000
                     val bytesRead =
                         try {
                             clientLog("receiveData read try")
                             clientLog("isConnected ${socket?.isConnected}")
                             clientLog("isClosed ${socket?.isClosed}")
                             inputStream?.read(ByteArray(StateObject.BUFFER_SIZE))

                         } catch (e: Exception) {
                             clientLog("Exception during read: ${e.cause}   e is $e")
                             throw e // Rethrow to be caught in outer catch
                         }

                     if (bytesRead != null) {
                         clientLog("receiveData bytesRead  ${bytesRead > 0}")
                     }
                     if (bytesRead!=null && bytesRead > 0) {
                         clientLog("receiveData bytesRead if")
                         withContext(Dispatchers.Main) {
                             receiveCallback(state.buffer, bytesRead, state)
                         }
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

    private fun receiveCallback(buffer: ByteArray, bytesRead: Int, state: StateObject) {
        clientLog("receiveCallback")
        // Process the received data
        val receivedData = String(buffer, 0, bytesRead)
        clientLog("receiveCallback data $receivedData")
        state.stringBuilder.append(receivedData)
    }

    private fun sendData(): Boolean {
        var i = 0
        clientLog("sendData-->")
        try {
            when (stateFlag) {
                0 -> {
                    if (!isEnqSent) {
                        i = sendRequest()

                        send(sendBuf, i)

                        isEnqSent = true
                        //  timer.interval = 1000L
                        // timer.isEnabled = true
                    }
                }

                1 -> {

                }

                2 -> {

                }

                3 -> {

                }

                4 -> {
                    return true
                }
            }

        } catch (e: Exception) {
            /* val onErrorEventArgs = OnErrorEventArgs().apply {
                 code = SystemErrors.SYSTEM_SEND_ERROR.toInt()
                 subCode = 0
                 message = "خطا در ارسال اطلاعات"
             }

             onError(Any(), onErrorEventArgs)
             clearError()
             return false*/
        }
        return true
    }

    private fun sendRequest(): Int {
        clientLog("sendRequest())")
        val request = ByteArray(1024)
        var i = 0

        request[i++] = ASC_ENQ

        when (mTransType) {
            TransTypes.TYPE_TRANSACTION -> request[i++] = 0
            TransTypes.TYPE_SURVEY -> request[i++] = 1
            TransTypes.TYPE_LASTTRANS -> request[i++] = 2
        }

        request[i++] = LIB_VER

        /*  if (mTransType == TransTypes.TYPE_TRANSACTION && pSpentAmount.isNotEmpty() && pInvoiceNumber.isNotEmpty()) {
              System.arraycopy(pSpentAmount, 0, request, i, 13)
              i += 13
              System.arraycopy(pInvoiceNumber, 0, request, i, 40)
              i += 40
          }*/

        sendBuf = request
        // Assuming you need to return the modified array
        return i
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