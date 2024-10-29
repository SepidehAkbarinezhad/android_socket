package ir.example.androidsocket.ui.base

import androidx.compose.material3.MaterialTheme
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Card
import androidx.compose.material.DropdownMenuItem
import androidx.compose.material3.DropdownMenu
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Color.Companion.DarkGray
import androidx.compose.ui.graphics.Color.Companion.Red
import androidx.compose.ui.unit.dp
import ir.example.androidsocket.Constants
import ir.example.androidsocket.ui.theme.spacing

@Composable
fun ProtocolTypeMenu(
    modifier: Modifier = Modifier,
    expanded: Boolean,
    protocols: List<String>,
    selectedProtocol: Constants.ProtocolType,
    onProtocolSelected: (String) -> Unit,
    onDismissClicked: () -> Unit,
) {


    Card(
        modifier = modifier
            .width(150.dp)
            .padding(MaterialTheme.spacing.small),
        border = BorderStroke(1.dp, color = MaterialTheme.colorScheme.primary),
        shape = RoundedCornerShape(MaterialTheme.spacing.small),
        backgroundColor = Color.White
    ) {

        Box(Modifier.fillMaxWidth()) {
            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { onDismissClicked() }) {
                protocols.forEachIndexed { index, s ->
                    DropdownMenuItem(
                        modifier = Modifier.background(if (selectedProtocol.title == s) Red else MaterialTheme.colorScheme.surface),
                        onClick = {
                            onProtocolSelected(s)
                        }) {
                        AppText(
                            text = s,
                            textColor = if (selectedProtocol.title == s) Color.White else DarkGray
                        )
                    }
                }
            }
        }
    }


}