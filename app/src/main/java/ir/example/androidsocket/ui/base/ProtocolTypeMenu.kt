package ir.example.androidsocket.ui.base

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.padding
import androidx.compose.material.DropdownMenuItem
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Color.Companion.DarkGray
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import ir.example.androidsocket.Constants

@Composable
fun ProtocolTypeMenu(
    expanded: Boolean,
    protocols: List<String>,
    selectedProtocol: Constants.ProtocolType,
    onProtocolSelected: (String) -> Unit,
    onDismissClicked: () -> Unit,
) {

            DropdownMenu(
                modifier = Modifier.background(MaterialTheme.colorScheme.surface),
                offset = DpOffset((-90).dp,0.dp),
                expanded = expanded,
                onDismissRequest = { onDismissClicked() }) {
                protocols.forEachIndexed { index, s ->
                    DropdownMenuItem(
                        modifier = Modifier.background(if (selectedProtocol.title == s) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface),
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
