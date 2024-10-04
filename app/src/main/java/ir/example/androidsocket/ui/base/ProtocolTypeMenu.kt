package ir.example.androidsocket.ui.base

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Card
import androidx.compose.material.DropdownMenuItem
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.DropdownMenu
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Color.Companion.DarkGray
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.androidSocket.R
import ir.example.androidsocket.Constants
import ir.example.androidsocket.ui.theme.Indigo
import ir.example.androidsocket.ui.theme.Indigo200
import ir.example.androidsocket.ui.theme.spacing
import ir.example.androidsocket.utils.serverLog

@Composable
fun ProtocolTypeMenu(
    modifier: Modifier = Modifier,
    protocols: List<String>,
    selectedProtocol: Constants.ProtocolType,
    onProtocolSelected: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
serverLog("ProtocolTypeMenu   ${selectedProtocol.title}")
    Row(modifier = modifier, verticalAlignment = Alignment.CenterVertically) {
        AppText(
            textType = TextType.SUBTITLE,
            text = stringResource(R.string.protocol_type),
            textColor = Indigo,
            fontWeight = FontWeight.Bold
        )
        Card(
            modifier = Modifier
                .width(150.dp)
                .padding(MaterialTheme.spacing.small),
            border = BorderStroke(1.dp, color = Indigo),
            shape = RoundedCornerShape(MaterialTheme.spacing.small),
            backgroundColor = Color.White
        ) {
            Row(
                modifier = Modifier
                    .padding(MaterialTheme.spacing.small)
                    .clickable { expanded = true }) {
                Icon(
                    imageVector = if (!expanded) Icons.Filled.KeyboardArrowDown else Icons.Filled.KeyboardArrowUp,
                    contentDescription = "protocol type"
                )
                Box(Modifier.fillMaxWidth()) {
                    AppText(
                        text = selectedProtocol.title
                    )
                    DropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }) {
                        protocols.forEachIndexed { index, s ->
                            DropdownMenuItem(
                                modifier = Modifier.background(if (selectedProtocol.title == s) Indigo200 else MaterialTheme.colors.surface),
                                onClick = {
                                    expanded = false
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
    }


}