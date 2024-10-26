package ir.example.androidsocket.ui.base

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import ir.example.androidsocket.ui.theme.spacing

@Composable
fun AppButton(
    modifier: Modifier = Modifier,
    border: BorderStroke? = null,
    text: String,
    enabled: Boolean = true,
    textColor: Color = Color.White,
    colors: ButtonColors = ButtonDefaults.buttonColors(
        disabledContainerColor = Color.LightGray,
        contentColor = MaterialTheme.colorScheme.primary,

        ),
    onClick: () -> Unit
) {
    Button(
        modifier = modifier, onClick = { onClick() },
        colors = colors,
        enabled = enabled,
        shape = RoundedCornerShape( MaterialTheme.spacing.medium),
        border = border
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelLarge,
            color = textColor,
            textAlign = TextAlign.Center
        )
    }
}


@Preview(showBackground = true)
@Composable
fun AppButtonPreview() {
    AppButton(
        modifier = Modifier,
        text = "click",
        enabled = true,
        onClick = {},
    )
}