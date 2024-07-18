package ir.example.androidsocket.ui.theme

import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Scaffold
import androidx.compose.material.ScaffoldState
import androidx.compose.material.Snackbar
import androidx.compose.material.SnackbarHost
import androidx.compose.material.Typography
import androidx.compose.material.darkColors
import androidx.compose.material.lightColors
import androidx.compose.material.rememberScaffoldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.ProvidedValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import ir.example.androidsocket.ui.base.BaseUiEvent

private val DarkColorPalette = darkColors(
    primary = AppColors.Dark.primary,
    onPrimary = AppColors.Dark.onPrimary,
    surface = AppColors.Dark.surface,
    onSurface = AppColors.Dark.onSurface,
    secondary = AppColors.Dark.secondary,
    onSecondary = AppColors.Dark.onSecondary,
    background = AppColors.Dark.background,
    onBackground = AppColors.Dark.onBackground
)

private val LightColorPalette = lightColors(
    primary = AppColors.Light.primary,
    onPrimary = AppColors.Light.onPrimary,
    surface = AppColors.Light.surface,
    onSurface = AppColors.Light.onSurface,
    secondary = AppColors.Light.secondary,
    onSecondary = AppColors.Light.onSecondary,
    background = AppColors.Light.background,
    onBackground = AppColors.Light.onBackground
)

@Composable
fun AndroidSocketTheme(
    direction: ProvidedValue<LayoutDirection> = LocalLayoutDirection provides LayoutDirection.Ltr,
    darkTheme: Boolean = isSystemInDarkTheme(),
    scaffoldState: ScaffoldState = rememberScaffoldState(),
    uiEvent: BaseUiEvent,
    onResetScreenMessage: () -> Unit = {},
    displayProgressBar: Boolean? = null,
    content: @Composable () -> Unit
) {
    val colors = if (darkTheme) {
        DarkColorPalette
    } else {
        LightColorPalette
    }

    CompositionLocalProvider(LocalSpacing provides Spacing()) {
        androidx.compose.material.MaterialTheme(
            colors = colors,
            typography = Typography(),
            shapes = appShape,
        ) {
            val context = LocalContext.current

            LaunchedEffect(key1 = uiEvent) {
                when (uiEvent) {
                    is BaseUiEvent.ShowToast -> {
                        uiEvent.messageId?.let { messageId ->
                            val messageValue =
                                if (uiEvent.parameters.isNotEmpty() && uiEvent.parameters[0] != null) {
                                    context.getString(messageId, *uiEvent.parameters)
                                } else {
                                    context.getString(messageId)
                                }
                            scaffoldState.snackbarHostState.showSnackbar(message = messageValue)
                            onResetScreenMessage()
                        }
                    }
                }
            }
            Scaffold(
                scaffoldState = scaffoldState,
                snackbarHost = {
                    SnackbarHost(hostState = it) { data ->
                        CompositionLocalProvider(
                            direction
                        ) {
                            Snackbar(
                                snackbarData = data,
                                backgroundColor = androidx.compose.material.MaterialTheme.colors.secondary,
                                contentColor = Color.White
                            )
                        }

                    }
                }
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(it)
                        .background(color = androidx.compose.material.MaterialTheme.colors.background)
                ) {

                    AppLayoutDirection(direction, content)
                    if (displayProgressBar == true)
                        CircularProgressIndicator(
                            Modifier
                                .align(Alignment.Center)
                                .padding(24.dp),
                        )

                }
            }

        }
    }

}