package ir.example.androidsocket.ui.theme

import androidx.compose.ui.graphics.Color

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
        secondary = Color(0xFF33691E),
        onSecondary = Color(0xFF9CCC65),
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
        secondary = Color(0xFF33691E),
        onSecondary = Color(0xFF9CCC65),
        tertiary = Color(0xFFFDD835),
        onTertiary = Color(0xFFffffff),
        surface = Color(0xFFEBEBF0),
        onSurface = Color(0xFF44483d),
        background = Color(0xFFFFFFFF),
        error = Color(0xFFba1a1a),
        onError = Color(0xFFffffff)
    )
}

