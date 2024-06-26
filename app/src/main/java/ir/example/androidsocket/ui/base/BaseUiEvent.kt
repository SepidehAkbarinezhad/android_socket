package ir.example.androidsocket.ui.base

import androidx.annotation.StringRes

interface BaseUiEvent {
    object None : BaseUiEvent
    data class ShowToast(@StringRes val messageId: Int?, val parameters: Array<out String?>) : BaseUiEvent
}