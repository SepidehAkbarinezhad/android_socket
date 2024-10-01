package ir.example.androidsocket.ui.base

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import ir.example.androidsocket.Constants
import kotlinx.coroutines.flow.MutableStateFlow

internal abstract class BaseViewModel() : ViewModel() {

    var uiEvent = MutableStateFlow<BaseUiEvent>(BaseUiEvent.None)
        private set

    val loading = mutableStateOf(false)

    var openPermissionDialog = MutableStateFlow(false)

    val protocols = listOf(Constants.ProtocolType.WEBSOCKET.title, Constants.ProtocolType.TCP.title)

    private fun sendUiEvent(event: BaseUiEvent) {
        uiEvent.value = event
    }

    fun emitMessageValue(messageId: Int?, vararg parameters: String? = emptyArray()) {
        sendUiEvent(BaseUiEvent.ShowToast(messageId, parameters))
    }

    fun setOpenPermissionDialog(value : Boolean){
        openPermissionDialog.value= value
    }

}