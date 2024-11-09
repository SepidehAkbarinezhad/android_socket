package ir.example.androidsocket.ui.theme

import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material.Scaffold
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.Typography
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.ProvidedValue
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import ir.example.androidsocket.ui.base.BaseUiEvent

private val DarkColorScheme = darkColorScheme(
    primary = AppColors.Dark.primary,
    onPrimary = AppColors.Dark.onPrimary,
    primaryContainer = AppColors.Dark.primaryContainer,
    onPrimaryContainer = AppColors.Dark.onPrimaryContainer,
    secondary = AppColors.Dark.secondary,
    onSecondary = AppColors.Dark.onSecondary,
    tertiary = AppColors.Dark.tertiary,
    onTertiary = AppColors.Dark.onTertiary,
    surface = AppColors.Dark.surface,
    onSurface = AppColors.Dark.onSurface,
    background = AppColors.Dark.background,
    error = AppColors.Dark.error,
    onError = AppColors.Dark.onError,

    )

private val LightColorScheme = lightColorScheme(
    primary = AppColors.Light.primary,
    onPrimary = AppColors.Light.onPrimary,
    primaryContainer = AppColors.Light.primaryContainer,
    onPrimaryContainer = AppColors.Light.onPrimaryContainer,
    secondary = AppColors.Light.secondary,
    onSecondary = AppColors.Light.onSecondary,
    tertiary = AppColors.Light.tertiary,
    onTertiary = AppColors.Light.onTertiary,
    surface = AppColors.Light.surface,
    onSurface = AppColors.Light.onSurface,
    background = AppColors.Light.background,
    error = AppColors.Light.error,
    onError = AppColors.Light.onError,
)

@Composable
fun AndroidSocketTheme(
    direction: ProvidedValue<LayoutDirection> = LocalLayoutDirection provides LayoutDirection.Ltr,
    darkTheme: Boolean = isSystemInDarkTheme(),
    scaffoldState: SnackbarHostState = SnackbarHostState(),
    uiEvent: BaseUiEvent,
    onResetScreenMessage: () -> Unit = {},
    displayProgressBar: Boolean? = null,
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    var isErrorToast by remember {
        mutableStateOf(false)
    }

    CompositionLocalProvider(LocalSpacing provides Spacing()) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = Typography(),
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
                            val result = scaffoldState.showSnackbar(
                                message = messageValue,
                                actionLabel = if (uiEvent.openActionIntent != null) "Open" else null
                            )
                            isErrorToast = uiEvent.openActionIntent == null
                            // Check if the "Open" action was clicked
                            if (result == SnackbarResult.ActionPerformed) {
                                uiEvent.openActionIntent?.let {
                                    context.startActivity(uiEvent.openActionIntent)
                                }
                            }
                            onResetScreenMessage()
                        }
                    }

                    else -> {}

                }
            }
            Scaffold(
                modifier = Modifier.fillMaxSize(),
                snackbarHost = {
                    SnackbarHost(
                        modifier = Modifier.padding(WindowInsets.navigationBars.asPaddingValues()),
                        hostState = scaffoldState
                    ) { data ->
                        CompositionLocalProvider(
                            direction
                        ) {
                            Snackbar(
                                snackbarData = data,
                                containerColor = if(isErrorToast)MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.onSecondary,
                                contentColor = Color.White,
                                actionColor = Color.White
                            )
                        }

                    }
                }
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(it)
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