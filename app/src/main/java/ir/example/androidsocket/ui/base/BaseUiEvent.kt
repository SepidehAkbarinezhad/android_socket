package ir.example.androidsocket.ui.base

import android.content.Intent
import androidx.annotation.StringRes

sealed class BaseUiEvent {
    object None : BaseUiEvent()
    data class ShowToast(@StringRes val messageId: Int?, val parameters: Array<out String?> ,val openActionIntent : Intent?) : BaseUiEvent()
}