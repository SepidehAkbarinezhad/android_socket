package ir.example.androidsocket.ui.base

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp


@Composable
fun AppIcon(
    modifier: Modifier = Modifier,
    contentDescription: String,
    enable: Boolean,
    enableSource: Int,
    disableSource: Int,
) {
    Image(
        modifier = modifier.size(80.dp),
        painter = if (enable) painterResource(id = enableSource) else painterResource(
            id = disableSource
        ),
        colorFilter = ColorFilter.tint(if (enable) MaterialTheme.colorScheme.onSecondary else Color.LightGray),
        contentDescription = contentDescription
    )
}