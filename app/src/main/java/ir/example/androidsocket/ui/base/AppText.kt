package ir.example.androidsocket.ui.base

import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow

enum class TextType {
    HEADER,
    TEXT,
    TEXT2,
    TITLE,
    SUBTITLE,
    BUTTON,
}

@Composable
fun AppText(
    modifier: Modifier = Modifier,
    textType: TextType = TextType.TEXT,
    text: String,
    textColor: Color = Color.DarkGray,
    textAlign: TextAlign = TextAlign.Center,
    maxLine: Int = 1,
    fontWeight: FontWeight = FontWeight.Normal,
    textDecoration: TextDecoration? = null
) {
    Text(
        modifier = modifier,
        text = text,
        color = textColor,
        style = styleText(textType,textDecoration),
        textAlign = textAlign,
        maxLines = maxLine,
        overflow = TextOverflow.Ellipsis,
        fontWeight = fontWeight
    )
}

@Composable
fun styleText(textType: TextType, textDecoration: TextDecoration? = null): TextStyle {
    val baseStyle = when (textType) {
        TextType.HEADER -> MaterialTheme.typography.h5
        TextType.TITLE -> MaterialTheme.typography.h6
        TextType.SUBTITLE -> MaterialTheme.typography.subtitle1
        TextType.TEXT -> MaterialTheme.typography.body1
        TextType.TEXT2 -> MaterialTheme.typography.body2
        TextType.BUTTON -> MaterialTheme.typography.button
    }

    return baseStyle.copy(
        textDecoration = textDecoration // Add the text decoration to the style
    )
}

