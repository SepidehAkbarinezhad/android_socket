package ir.example.androidsocket.ui.base

import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.input.key.onKeyEvent

fun Modifier.customKeyboard(
    onEnterPressed: () -> Unit,
    onDeletePressed: () -> Unit,
): Modifier = composed {

    this.onKeyEvent {

        when (it.nativeKeyEvent.keyCode) {
            KeyType.KEY_ENTER.value -> {

                onEnterPressed()
                true
            }
            KeyType.KEY_DELETE.value -> {
                onDeletePressed()
                true
            }
            else -> {
                false
            }
        }
    }

}

enum class KeyType(val value: Int) {
    KEY_ENTER(value = 66),
    KEY_DELETE(value = 67),
    KEY_BACK(value = 4),
    KEY_UP(value = 19),
    KEY_DOWN(value = 20),
    KEY_NUMBER_0(value = 7),
    KEY_NUMBER_1(value = 8),
    KEY_NUMBER_2(value = 9),
    KEY_NUMBER_3(value = 10),
    KEY_NUMBER_4(value = 11),
    KEY_NUMBER_5(value = 12),
    KEY_NUMBER_6(value = 13),
    KEY_NUMBER_7(value = 14),
    KEY_NUMBER_8(value = 15),
    KEY_NUMBER_9(value = 16);
}
