package ir.example.androidsocket.ui.base

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import ir.example.androidsocket.ui.theme.Indigo
import ir.example.androidsocket.ui.theme.spacing

@Composable
fun AppHeader(
    modifier: Modifier = Modifier,
    headerTitle: Int,
    headerBackground : Color = Indigo
) {
    Box(
        modifier
            .fillMaxWidth()
            .background(headerBackground)
    ) {
        AppText(
            modifier = Modifier
                .align(Alignment.Center)
                .padding(MaterialTheme.spacing.medium),
            text = stringResource(id = headerTitle),
            textType = TextType.HEADER,
            fontWeight = FontWeight.Bold,
            textColor = Color.White
        )
    }
}