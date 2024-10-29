package ir.example.androidsocket.ui.base

import androidx.compose.material3.MaterialTheme
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.TextFieldColors
import androidx.compose.material.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import ir.example.androidsocket.ui.theme.spacing


@Composable
fun AppOutlinedTextField(
    modifier: Modifier = Modifier,
    value: String,
    onValueChange: (String) -> Unit,
    label: String = "",
    hint: String = "",
    singleLine: Boolean = true,
    enabled: Boolean = true,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    leadingIcon: @Composable (() -> Unit)? = {},
    trailingIcon : @Composable (() -> Unit)? = {},
    colors: TextFieldColors = TextFieldDefaults.outlinedTextFieldColors(
        textColor = MaterialTheme.colorScheme.onSurface,
        focusedBorderColor = MaterialTheme.colorScheme.primary,
        unfocusedBorderColor = MaterialTheme.colorScheme.primaryContainer,
        disabledBorderColor = MaterialTheme.colorScheme.primaryContainer,
        disabledTextColor = Color.LightGray,
        cursorColor = MaterialTheme.colorScheme.primary,
        backgroundColor = MaterialTheme.colorScheme.surface,
    ),
    keyboardActions: KeyboardActions = KeyboardActions.Default,
    textStyle: TextStyle = styleText(TextType.TEXT),
    isFocused: Boolean = false,
    hasError: Boolean = false,
    shape: RoundedCornerShape = RoundedCornerShape(MaterialTheme.spacing.extraMedium)
) {


    val focusRequester = remember { FocusRequester() }

    LaunchedEffect(key1 = isFocused) {
        if (isFocused)
            focusRequester.requestFocus()
    }

    OutlinedTextField(
        modifier = modifier,
        value = value,
        onValueChange = { onValueChange(it) },
        label = {
            AppText(
                text = if (hasError) {
                    "*$label"
                } else {
                    label
                } ?: "",
                textColor = if (hasError) MaterialTheme.colorScheme.error else if (enabled) MaterialTheme.colorScheme.onPrimaryContainer else Color.LightGray,
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.titleLarge
            )
        },
        singleLine = singleLine,
        enabled = enabled,
        textStyle = textStyle,
        colors = colors,
        placeholder = {
            if (hint.isNotEmpty())
                AppText(
                    text = hint,
                    textColor = Color.LightGray
                )
        },
        visualTransformation = visualTransformation,
        keyboardOptions = keyboardOptions,
        leadingIcon = leadingIcon,
        trailingIcon = trailingIcon,
        keyboardActions = keyboardActions,
        shape = shape
    )
}


@Preview(showBackground = true)
@Composable
fun AppOutlinedTextFieldPreview() {
    AppOutlinedTextField(
        modifier = Modifier
            .background(Color.White)
            .padding(24.dp),
        value = "موبایل",
        onValueChange = {},
        hint = "mobileHint",
        singleLine = false,
        enabled = true,
        label = "label"

    )
}