package ir.example.androidsocket.ui.theme

import androidx.compose.ui.graphics.Color

val Indigo = Color(0xFF3F51B5)
val Indigo200 = Color(0xFF9FA8DA)
val Gray200 = Color(0xFFB0BEC5)
val Green900 = Color(0xFF33691E)
val Green400 = Color(0xFF9CCC65)

sealed class AppColors(
    val primary: Color,
    val onPrimary: Color,
    val primaryContainer: Color,
    val onPrimaryContainer: Color,
    val secondary: Color,
    val onSecondary: Color,
    val tertiary: Color,
    val onTertiary: Color,
    val surface: Color,
    val onSurface: Color,
    val background: Color,
    val error: Color,
    val onError: Color,

) {

    data object Dark : AppColors(
        primary = Color(0xFF3462CD),
        onPrimary = Color(0xFFFFFFFF),
        primaryContainer = Color(0xFFdbe1ff),
        onPrimaryContainer = Color(0xFF001849),
        secondary = Color(0xFF735471),
        onSecondary = Color(0xFFCDDC39),
        tertiary = Color(0xFFCDDC39),
        onTertiary = Color(0xFFCDDC39),
        surface = Color(0xFFfaf8ff),
        onSurface = Color(0xFFdad9e0),
        background = Color(0xFFdad9e0),
        error = Color(0xFFA8ADBD),
        onError = Color(0xFFA8ADBD)
    )

    data object Light : AppColors(
        primary = Color(0xFF3462CD),
        onPrimary = Color(0xFFffffff),
        primaryContainer = Color(0xFFd6e3ff),
        onPrimaryContainer = Color(0xFF001849),
        secondary = Color(0xFF565f71),
        onSecondary = Color(0xFFffffff),
        tertiary = Color(0xFFFDD835),
        onTertiary = Color(0xFFffffff),
        surface = Color(0xFFd9d9e0),
        onSurface = Color(0xFF44483d),
        background = Color(0xFFf9f9ff),
        error = Color(0xFFba1a1a),
        onError = Color(0xFFffffff)
    )
}

