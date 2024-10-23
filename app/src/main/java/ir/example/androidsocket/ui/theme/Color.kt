package ir.example.androidsocket.ui.theme

import androidx.compose.ui.graphics.Color

val Indigo = Color(0xFF3F51B5)
val Indigo400 = Color(0xFF5C6BC0)
val Indigo200 = Color(0xFF9FA8DA)
val Green900 = Color(0xFF1B5E20)
val Gray200 = Color(0xFFB0BEC5)
val Orange700 = Color(0xFFF57C00)

sealed class AppColors(
    val primary: Color,
    val primaryVariant: Color,
    val onPrimary: Color,
    val surface: Color,
    val onSurface: Color,
    val secondary: Color,
    val onSecondary: Color,
    val background: Color,
    val onBackground: Color
) {


    object Dark : AppColors(
        primary = Color(0xFF4a5c92),
        primaryVariant = Color(0xFFF44336),
        onPrimary = Color(0xFFFFFFFF),
        surface = Color(0xFFfaf8ff),
        onSurface = Color(0xFFdad9e0),
        secondary = Color(0xFF735471),
        onSecondary = Color(0xFFCDDC39),
        background = Color(0xFF0E181E),
        onBackground = Color(0xFFA8ADBD)
    )

    object Light : AppColors(
        primary = Color(0xFF3462CD),
        primaryVariant = Color(0xFFb3c5ff),
        onPrimary = Color(0xFFdbe1ff),
        surface = Color(0xFFF3F4F9),
        onSurface = Color(0xFFDfE2E5),
        secondary = Color(0xFF344563),
        onSecondary = Color(0xFFdbe1ff),
        background = Color(0xFFF7E8ED),
        onBackground = Color(0xFF74777F)
    )
}

