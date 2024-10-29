package ir.example.androidsocket.ui.base

import androidx.compose.material3.MaterialTheme
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.ButtonDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import ir.example.androidsocket.ui.theme.spacing

@Composable
fun AppButtonsRow(
    modifier: Modifier = Modifier,
    firstButtonTitle: String,
    firstTitleButtonColor: Color = Color.White,
    firstButtonColor: ButtonColors = ButtonDefaults.buttonColors(),
    onFirstClicked: () -> Unit,
    firstButtonBorder: BorderStroke? = null,
    firstEnable: Boolean = true,
    secondEnable: Boolean = true,
    secondButtonTitle: String,
    secondTitleButtonColor: Color = Color.White,
    secondButtonColor: ButtonColors = ButtonDefaults.buttonColors(),
    secondButtonBorder: BorderStroke? = null,
    onSecondClick: () -> Unit
) {

    Row(
        modifier.height(IntrinsicSize.Max),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        AppButton(
            modifier = Modifier.fillMaxHeight()
                .weight(1f).padding(MaterialTheme.spacing.small),
            text = firstButtonTitle,
            textColor = firstTitleButtonColor,
            border = firstButtonBorder,
            colors = firstButtonColor,
            enabled = firstEnable,
        ) {
            onFirstClicked()
        }
        AppButton(
            modifier = Modifier.fillMaxHeight().weight(1f).padding(MaterialTheme.spacing.small)
                .weight(1f),
            border = secondButtonBorder,
            text = secondButtonTitle,
            textColor = secondTitleButtonColor,
            colors = secondButtonColor,
            enabled = secondEnable
        ) {
            onSecondClick()
        }
    }
}

@Preview(showBackground = true)
@Composable
fun AppButtonRowPreview() {

    AppButtonsRow(
        modifier = Modifier,
        firstButtonTitle = "confirm",
        onFirstClicked = { },
        firstButtonColor = ButtonDefaults.buttonColors(
            disabledContainerColor = Color.LightGray,
            contentColor = MaterialTheme.colorScheme.primary,
        ),
        secondButtonTitle = "cancel",
        secondEnable = false,
        secondButtonColor = ButtonDefaults.buttonColors(
            disabledContainerColor = Color.LightGray,
            contentColor = MaterialTheme.colorScheme.primary,
        )
    ) {
    }
}