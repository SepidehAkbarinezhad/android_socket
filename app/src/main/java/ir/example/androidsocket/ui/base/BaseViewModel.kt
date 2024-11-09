package ir.example.androidsocket.ui.base

import android.content.Intent
import android.os.Build
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import ir.example.androidsocket.Constants
import ir.example.androidsocket.utils.serverLog
import kotlinx.coroutines.flow.MutableStateFlow

internal abstract class BaseViewModel() : ViewModel() {

    var uiEvent = MutableStateFlow<BaseUiEvent>(BaseUiEvent.None)
        private set

    val loading = mutableStateOf(false)

    var openNotificationPermissionDialog = MutableStateFlow(false)

    var notificationPermissionGranted = MutableStateFlow(Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU)

    var selectedProtocol = MutableStateFlow(Constants.ProtocolType.WEBSOCKET)

    private fun sendUiEvent(event: BaseUiEvent) {
        uiEvent.value = event
    }

    fun emitMessageValue(messageId: Int?, vararg parameters: String? = emptyArray(), openActionIntent : Intent?=null) {
        serverLog("emitMessageValue : $messageId","progressCheck")
        sendUiEvent(BaseUiEvent.ShowToast(messageId, parameters,openActionIntent))
    }

    fun setOpenNotificationPermissionDialog(value : Boolean){
        openNotificationPermissionDialog.value= value
    }

    fun setNotificationGranted(value : Boolean){
        notificationPermissionGranted.value= value
    }

}