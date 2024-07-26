package ir.example.androidsocket.ui.base

import android.util.Log
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch

internal abstract class BaseViewModel() : ViewModel() {

    var uiEvent = MutableStateFlow<BaseUiEvent>(BaseUiEvent.None)
        private set

    val loading = mutableStateOf(false)

    var openPermissionDialog = MutableStateFlow(false)


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