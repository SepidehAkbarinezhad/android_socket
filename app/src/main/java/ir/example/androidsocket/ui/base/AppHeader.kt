package ir.example.androidsocket.ui.base

import androidx.compose.material3.MaterialTheme
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import ir.example.androidsocket.ui.theme.Indigo
import ir.example.androidsocket.ui.theme.spacing

@Composable
fun AppHeader(
    modifier: Modifier = Modifier,
    onMenuClick: () -> Unit,
    headerTitle: String,
    headerBackground: Color = Indigo
) {
    Box(
        modifier
            .fillMaxWidth()
            .background(headerBackground),
    ) {

        AppText(
            modifier = Modifier
                .padding(MaterialTheme.spacing.medium)
                .align(Alignment.Center),
            text = headerTitle,
            textType = TextType.HEADER,
            fontWeight = FontWeight.Bold,
            textColor = Color.White
        )
        Icon(
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .padding(MaterialTheme.spacing.small)
                .clickable { onMenuClick() },
            imageVector = Icons.Default.MoreVert,
            contentDescription = "menu",
            tint = Color.White
        )
    }
}