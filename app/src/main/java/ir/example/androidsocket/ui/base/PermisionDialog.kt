package ir.example.androidsocket.ui.base

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import ir.example.androidsocket.ui.theme.spacing

@Composable
fun PermissionDialog(
    modifier: Modifier = Modifier,
    onDismissRequest: () -> Unit,
    onGrantClicked: () -> Unit,
) {
    Dialog(
        onDismissRequest = onDismissRequest,
        properties = DialogProperties(
            dismissOnBackPress = true, dismissOnClickOutside = true
        )
    ) {
        Column(
            modifier
                .fillMaxWidth()
                .background(Color.White)
                .padding(MaterialTheme.spacing.small),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            AppText(
                text = "permission is required",
                textColor = Color.Red,
                textType = TextType.TITLE
            )
            Box(modifier = Modifier.padding(vertical = MaterialTheme.spacing.small)
                .fillMaxWidth()
                .height(1.dp)
                .background(Color.Red))
            AppText(
                text = "notification permission is mandatory to inform the user about the service running and about the message received from client when app is in background.",
                maxLine = 5
            )
            AppButtonsRow(
                firstButtonTitle = "grant",
                onFirstClicked = { onGrantClicked() },
                secondButtonTitle = "cancel"
            ) {
                onDismissRequest()
            }
        }

    }

}