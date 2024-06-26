package ir.example.androidsocket.ui.base

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import ir.example.androidsocket.ui.theme.Gray200


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
        colorFilter = ColorFilter.tint(if (enable) Color.Green else Gray200),
        contentDescription = contentDescription
    )
}