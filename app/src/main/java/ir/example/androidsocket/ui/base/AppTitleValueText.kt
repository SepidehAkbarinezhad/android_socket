package ir.example.androidsocket.ui.base

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import ir.example.androidsocket.ui.theme.Indigo
import ir.example.androidsocket.ui.theme.spacing


@Composable
fun AppTitleValueText(
    modifier: Modifier = Modifier,
    horizontalArrangement: Arrangement.Horizontal = Arrangement.Start,
    title: String,
    value: String,
    titleColor: Color = Indigo,
    valueColor: Color = Color.DarkGray,
    titleTextType: TextType= TextType.TITLE,
    valueTextType: TextType= TextType.SUBTITLE,
) {
    Row(
        modifier.padding(MaterialTheme.spacing.small),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = horizontalArrangement
    ) {
        AppText(
            modifier = Modifier.padding(start = MaterialTheme.spacing.small),
            text = title,
            textType = titleTextType,
            textColor = titleColor,
        )
        AppText(
            text = " $value",
            textType = valueTextType,
            textColor = valueColor
        )
    }
}