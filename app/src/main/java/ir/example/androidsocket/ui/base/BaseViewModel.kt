package ir.example.androidsocket.ui.base

import android.util.Log
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.launch

internal abstract class BaseViewModel() : ViewModel() {

    var uiEvent = MutableSharedFlow<BaseUiEvent>()
        private set

    val loading = mutableStateOf(false)

    private fun runOnCoroutineScope(
        coroutineScope: CoroutineScope = viewModelScope,
        catchFunction: (e: Exception) -> Unit = { e -> },
        block: suspend () -> Unit
    ) {
        coroutineScope.launch {
            try {
                block()
            } catch (e: Exception) {
                catchFunction(e)
            }
        }
    }

    private fun sendUiEvent(event: BaseUiEvent) {
        runOnCoroutineScope {
            uiEvent.emit(event)
        }
    }

    fun emitMessageValue(messageId: Int?, vararg parameters: String? = emptyArray()) {
        sendUiEvent(BaseUiEvent.ShowToast(messageId, parameters))
    }

}