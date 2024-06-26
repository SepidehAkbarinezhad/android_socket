package ir.example.androidsocket.ui.base

import android.graphics.Bitmap
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.view.drawToBitmap

@Composable
fun captureBitmap(
    content: @Composable () -> Unit,
): () -> Bitmap {

    val context = LocalContext.current

    /**
     * ComposeView that would take composable as its content
     * Kept in remember so recomposition doesn't re-initialize it
     **/
    val composeView = remember { ComposeView(context) }

    /**
     * Callback function which could get latest image bitmap
     **/
    fun captureBitmap(): Bitmap {
        return composeView.drawToBitmap()
    }


    AndroidView(
        factory = {
            composeView.apply {
                setContent {
                    content.invoke()
                }
            }
        }
    )


    return ::captureBitmap

}


/*
@Composable
fun captureBitmap(
    content: @Composable () -> Unit,
    onBitmapCaptured: (Bitmap) -> Unit
) {
    val context = LocalContext.current
    val composeView = remember { ComposeView(context) }

    var isViewPositioned by remember { mutableStateOf(false) }

    DisposableEffect(composeView) {
        val viewTreeObserver = composeView.viewTreeObserver

        val listener = ViewTreeObserver.OnPreDrawListener {
            if (!isViewPositioned) {
                isViewPositioned = true
                serverLog(
                    "listener",
                    "printTag"
                )
                onBitmapCaptured(composeView.drawToBitmap())
            }
            true
        }

        viewTreeObserver.addOnPreDrawListener(listener)

        onDispose {
            // Remove the listener when the ComposeView is disposed
            viewTreeObserver.removeOnPreDrawListener(listener)
        }
    }

    AndroidView(factory = {
        composeView.apply {
            setContent {
                content.invoke()
            }
        }
    })
}
*/


/*
@Composable
fun captureBitmap(
    content: @Composable () -> Unit,
    onBitmapCaptured: (Bitmap) -> Unit
) {
    val context = LocalContext.current
    val composeView = remember { ComposeView(context) }

    var isViewPositioned by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .size(1.dp, 1.dp)
            .onGloballyPositioned {
                serverLog("onGloballyPositioned", "printTag")
                if (!isViewPositioned) {
                    isViewPositioned = true
                    onBitmapCaptured(composeView.drawToBitmap())
                }
            }
    ) {
        // Embed the ComposeView inside the Box
        AndroidView(factory = {
            composeView.apply {
                setContent {
                    content.invoke()
                }
            }
        })
    }
}*/