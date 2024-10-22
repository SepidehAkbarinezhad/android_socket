package ir.example.androidsocket.ui

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.icons.Icons
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.androidSocket.R
import ir.example.androidsocket.Constants
import ir.example.androidsocket.socket.SocketServerForegroundService
import ir.example.androidsocket.ui.base.AppIcon
import ir.example.androidsocket.ui.base.AppText
import ir.example.androidsocket.ui.base.AppTitleValueText
import ir.example.androidsocket.ui.base.BaseUiEvent
import ir.example.androidsocket.ui.base.TextType
import ir.example.androidsocket.ui.theme.AndroidSocketTheme
import ir.example.androidsocket.ui.theme.Green900
import ir.example.androidsocket.ui.theme.Indigo
import ir.example.androidsocket.ui.theme.spacing
import ir.example.androidsocket.utils.AppBaseScreen
import ir.example.androidsocket.utils.ConnectionTypeManager
import kotlinx.coroutines.delay

@Composable
internal fun ServerComposable(
    viewModel: ServerViewModel,
    connectionTypeManager: ConnectionTypeManager,
    onEvent: (ServerEvent) -> Unit
) {

    val context = LocalContext.current

    val uiEvent by viewModel.uiEvent.collectAsStateWithLifecycle(initialValue = BaseUiEvent.None)
    val loading by viewModel.loading
    val clientMessage by viewModel.clientMessage.collectAsState()
    val connectionType by connectionTypeManager.connectionType.collectAsState()
    val fileProgress by viewModel.fileProgress.collectAsState()
    val isWifiConnected by connectionTypeManager.isWifiConnected.collectAsState()
    val isEthernetConnected by connectionTypeManager.isEthernetConnected.collectAsState()
    val selectedProtocol by viewModel.selectedProtocol.collectAsState()
    val wifiIpAddress by viewModel.wifiServerIp.collectAsState()
    val lanIpAddress by viewModel.ethernetServerIp.collectAsState()
    val socketStatus by viewModel.socketStatus.collectAsState()


    LaunchedEffect(key1 = socketStatus) {
        if (!socketStatus.connection) {
            viewModel.onEvent(ServerEvent.SetClientMessage(""))
        }
    }
    LaunchedEffect(key1 = Unit) {
        viewModel.onEvent(ServerEvent.GetWifiIpAddress(context))
        viewModel.onEvent(ServerEvent.GetLanIpAddress(context))
    }
    LaunchedEffect(key1 = isWifiConnected, isEthernetConnected) {
        viewModel.onEvent(ServerEvent.GetWifiIpAddress(context))
        viewModel.onEvent(ServerEvent.GetLanIpAddress(context))
    }

    AndroidSocketTheme(uiEvent = uiEvent, displayProgressBar = loading) {
        Surface(
            modifier = Modifier,
            color = MaterialTheme.colors.primary,
        ) {
            ServerContent(
                onEvent = onEvent,
                connectionType = connectionType,
                selectedProtocol = selectedProtocol,
                wifiIpAddress = wifiIpAddress,
                lanIpAddress = lanIpAddress,
                socketStatus = socketStatus,
                clientMessage = clientMessage,
                fileProgress = fileProgress
            )
        }
    }
}

@Composable
fun ServerContent(
    onEvent: (ServerEvent) -> Unit,
    selectedProtocol: Constants.ProtocolType,
    connectionType: Constants.ConnectionType,
    wifiIpAddress: String,
    lanIpAddress: String,
    socketStatus: Constants.SocketStatus,
    clientMessage: String,
    fileProgress : Int?
) {

    AppBaseScreen(
        headerTitle = stringResource(id = R.string.server_header, selectedProtocol.title),
        headerBackGround = Indigo,
        onProtocolSelected = {
            onEvent(ServerEvent.SetLoading(true))
            onEvent(ServerEvent.SetProtocolType(it))
        },
        bodyContent = {

            val isWifi = remember(connectionType) {
                connectionType == Constants.ConnectionType.WIFI
            }
            val isEthernet = remember(connectionType) {
                connectionType == Constants.ConnectionType.ETHERNET
            }
            val hasConnection = remember(isWifi, isEthernet) { isWifi || isEthernet }

            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(
                    modifier = Modifier.padding(MaterialTheme.spacing.small),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .padding(MaterialTheme.spacing.medium),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        AppIcon(
                            enable = isWifi,
                            enableSource = R.drawable.connected_wifi_icon,
                            disableSource = R.drawable.disconnected_wifi_icon,
                            contentDescription = stringResource(id = R.string.wifi_connection_description),
                        )
                        AppIcon(
                            enable = isEthernet,
                            enableSource = R.drawable.connected_ethernet_icon,
                            disableSource = R.drawable.disconnected_ethernet_icon,
                            contentDescription = stringResource(id = R.string.ethernet_connection_description),
                        )
                    }

                    if (hasConnection) {
                        Row(
                            Modifier
                                .fillMaxWidth()
                                .padding(MaterialTheme.spacing.small),

                            verticalAlignment = Alignment.Bottom,
                        ) {
                            Column(
                                modifier = Modifier
                                    .weight(1.5f)
                                    .padding(MaterialTheme.spacing.small),
                                horizontalAlignment = Alignment.Start,
                            ) {
                                AppText(
                                    modifier = Modifier.padding(MaterialTheme.spacing.small),
                                    text = stringResource(id = R.string.ip_label),
                                    textColor = Indigo,
                                    fontWeight = FontWeight.Bold
                                )
                                Card(
                                    Modifier.fillMaxWidth(),
                                    border = BorderStroke(1.dp, Indigo),
                                    colors = CardDefaults.cardColors(
                                        containerColor = Color.White,
                                    ),
                                ) {
                                    AppText(
                                        Modifier
                                            .fillMaxWidth()
                                            .padding(MaterialTheme.spacing.small),
                                        text = when (connectionType) {
                                            Constants.ConnectionType.NONE -> ""
                                            Constants.ConnectionType.WIFI -> wifiIpAddress
                                            Constants.ConnectionType.ETHERNET -> lanIpAddress
                                        },
                                        textColor = Indigo,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }

                            AppText(
                                modifier = Modifier
                                    .weight(.5f)
                                    .padding(MaterialTheme.spacing.small),
                                text = ":",
                                textColor = Indigo,
                                fontWeight = FontWeight.Bold
                            )

                            Column(
                                modifier = Modifier
                                    .weight(1f)
                                    .padding(MaterialTheme.spacing.small),
                                horizontalAlignment = Alignment.Start,
                            ) {
                                AppText(
                                    modifier = Modifier.padding(MaterialTheme.spacing.small),
                                    text = stringResource(id = R.string.port_label),
                                    textColor = Indigo,
                                    fontWeight = FontWeight.Bold
                                )
                                Card(
                                    Modifier.fillMaxWidth(),
                                    border = BorderStroke(1.dp, Indigo),
                                    colors = CardDefaults.cardColors(
                                        containerColor = Color.White,
                                    ),
                                ) {
                                    AppText(
                                        Modifier
                                            .fillMaxWidth()
                                            .padding(MaterialTheme.spacing.small),
                                        text = SocketServerForegroundService.PORT.toString(),
                                        textColor = Indigo,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }

                    }
                    AppTitleValueText(
                        modifier = Modifier.padding(MaterialTheme.spacing.small),
                        title = stringResource(
                            id = R.string.socket_status_title
                        ),
                        value = socketStatus.title,
                        valueColor = if (!socketStatus.connection) Color.Red else Green900,
                        titleTextType = TextType.TEXT,
                        valueTextType = TextType.TEXT
                    )
                    fileProgress?.let { FileTransferAnimation() }


                 /*   Column(
                        Modifier
                            .fillMaxWidth()
                            .padding(
                                horizontal = MaterialTheme.spacing.small,
                                vertical = MaterialTheme.spacing.extraMedium
                            )
                            .alpha(if (clientMessage.isNotEmpty()) 1f else 0f),
                        horizontalAlignment = Alignment.Start,
                        verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.small)
                    ) {

                        AppText(
                            text = stringResource(id = R.string.message_from_client),
                            fontWeight = FontWeight.Bold,
                            textType = TextType.TITLE,
                            textColor = Indigo
                        )

                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(MaterialTheme.spacing.extraLarge),
                            border = BorderStroke(1.dp, color = Indigo),
                            colors = CardDefaults.cardColors(containerColor = Color.White)
                        ) {
                            AppText(
                                modifier = Modifier.padding(MaterialTheme.spacing.small),
                                text = clientMessage
                            )
                        }


                    }*/

                }

            }


        }) {

    }

}

@Composable
fun FileTransferAnimation() {
    var arrowPosition by remember { mutableStateOf((-100).dp) } // Initial position above the screen
    var isAnimating by remember { mutableStateOf(true) } // State to control the animation
    var isArrowVisible by remember { mutableStateOf(true) } // Control visibility of the arrow

    // Animate the vertical position of the arrow
    val animatedArrowPosition by animateDpAsState(
        targetValue = arrowPosition, // Arrow moves down to the folder
        animationSpec = tween(durationMillis = 2000), label = ""
    )

    val folderScale by animateFloatAsState(
        targetValue = if (arrowPosition >= 300.dp) 1.2f else 1f,
        animationSpec = spring()
    )


    // Simulate continuous file transfer
    LaunchedEffect(Unit) {
        while (true) {
            isArrowVisible = true // Make arrow visible
            arrowPosition = (-100).dp // Reset arrow to top
            delay(1000) // Optional delay before starting the next cycle

            // Move the arrow down
            arrowPosition = 0.dp // Move the arrow down to the folder
            delay(1000) // Simulate file transfer duration

            // After reaching the folder, hide the arrow and reset position
            isArrowVisible = false // Hide the arrow
            arrowPosition = (-100).dp // Reset position to off-screen
            delay(1000) // Delay before starting the next cycle
        }
    }

    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier.size(250.dp)
    ) {
        // Arrow moving from top to the folder at the bottom
        if (isArrowVisible) { // Only show the arrow when it's visible
            Icon(
                modifier = Modifier
                    .size(70.dp)
                    .offset(y = animatedArrowPosition), // Apply animated position
                painter = painterResource(id = R.drawable.arrow_icon),
                contentDescription = "Arrow",
                tint = Color.Green
            )
        }

        // Folder icon at the bottom
        Image(
            modifier = Modifier
                .size(80.dp)
                .scale(folderScale)
                .align(Alignment.Center), // Position the folder at the bottom
            painter = painterResource(id = R.drawable.file_icon),
            contentDescription = "Folder",
        )
    }
}

