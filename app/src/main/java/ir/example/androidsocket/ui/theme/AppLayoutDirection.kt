package ir.example.androidsocket.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ProvidedValue
import androidx.compose.ui.unit.LayoutDirection

@Composable
fun AppLayoutDirection(direction: ProvidedValue<LayoutDirection>, content: @Composable () -> Unit) {
    CompositionLocalProvider(direction, content = content)
}