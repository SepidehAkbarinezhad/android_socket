package ir.example.androidsocket.ui

import android.app.Activity
import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Color.Companion.LightGray
import androidx.compose.ui.graphics.Color.Companion.White
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight.Companion.Bold
import androidx.compose.ui.text.font.FontWeight.Companion.Normal
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.androidSocket.R
import ir.example.androidsocket.Constants
import ir.example.androidsocket.ui.base.AppOutlinedTextField
import ir.example.androidsocket.ui.base.AppText
import ir.example.androidsocket.ui.base.BaseUiEvent
import ir.example.androidsocket.ui.base.ProtocolTypeMenu
import ir.example.androidsocket.ui.base.TextType
import ir.example.androidsocket.ui.theme.AndroidSocketTheme
import ir.example.androidsocket.ui.theme.Green400
import ir.example.androidsocket.ui.theme.Green900
import ir.example.androidsocket.ui.theme.Yellow600
import ir.example.androidsocket.ui.theme.spacing
import ir.example.androidsocket.utils.clientLog
import ir.example.androidsocket.utils.isIpValid
import ir.example.androidsocket.utils.isPortValid
import kotlinx.coroutines.launch


@Composable
internal fun ClientCompose(
    viewModel: ClientViewModel,
    onEvent: (ClientEvent) -> Unit
) {

    val keyboardController = LocalSoftwareKeyboardController.current
    val selectedProtocol by viewModel.selectedProtocol.collectAsState()
    val clientMessage by viewModel.clientMessage.collectAsStateWithLifecycle("")
    val fileUrl by viewModel.fileUrl.collectAsStateWithLifecycle()
    val fileProgress by viewModel.fileProgress.collectAsState()
    val serverMessage by viewModel.serverMessage.collectAsStateWithLifecycle("")
    val waitingForServerConfirmation by viewModel.waitingForServerConfirmation.collectAsStateWithLifecycle(
        null
    )
    val serverIp by viewModel.serverIp.collectAsState()
    val serverIpError by viewModel.serverIpError.collectAsState()
    val serverPort by viewModel.serverPort.collectAsState()
    val serverPortError by viewModel.serverPortError.collectAsState()
    val socketStatus by viewModel.socketStatus.collectAsState()
    val uiEvent by viewModel.uiEvent.collectAsStateWithLifecycle(initialValue = BaseUiEvent.None)

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            // Get the URI of the selected file (result.data?.data)
            val uri = result.data?.data
            if (uri != null) {
                onEvent(ClientEvent.SetClientMessage(uri.toString()))
                onEvent(ClientEvent.SetFileUrl(uri))
                println("File selected: $uri")
            }
        }
    }

    val onAttachFile = {
        val intent = Intent(Intent.ACTION_GET_CONTENT).apply {
            type = "*/*" // You can specify file types, e.g., "image/*", "application/pdf"
            addCategory(Intent.CATEGORY_OPENABLE)
        }
        launcher.launch(intent)
    }


    LaunchedEffect(key1 = socketStatus) {
        if (!socketStatus.isConnected) {
            onEvent(ClientEvent.SetClientMessage(""))
            onEvent(ClientEvent.SetServerMessage(""))
        }
    }

    AndroidSocketTheme(
        displayProgressBar = viewModel.loading.value,
        uiEvent = uiEvent,
        onResetScreenMessage = { viewModel.emitMessageValue(null) }
    ) {
        // A surface container using the 'background' color from the theme
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            ClientContent(
                onEvent = onEvent,
                selectedProtocol = selectedProtocol,
                serverIp = serverIp,
                serverIpError = serverIpError,
                serverPort = serverPort,
                serverPortError = serverPortError,
                clientMessage = clientMessage,
                fileUrl = fileUrl?.toString() ?: "",
                fileProgress = fileProgress,
                serverMessage = serverMessage,
                waitingForServer = waitingForServerConfirmation,
                socketStatus = socketStatus,
                onConnectToServer = {
                    clientLog("onConnectToServer")
                    onEvent(ClientEvent.OnConnectToServer)
                    keyboardController?.hide()
                },
                onDisconnectFromServer = {
                    onEvent(ClientEvent.OnDisconnectFromServer)
                    keyboardController?.hide()
                },
                onSendMessageEvent = { message ->
                    clientLog("onSendMessageEvent")
                    keyboardController?.hide()
                    onEvent(ClientEvent.SetServerMessage(""))
                    onEvent(ClientEvent.SetLoading(true))
                    onEvent(ClientEvent.SendMessageToServer(message))

                },
                onAttachFileEvent = { onAttachFile() }
            )
        }
    }
}

@Composable
fun ClientContent(
    onEvent: (ClientEvent) -> Unit,
    selectedProtocol: Constants.ProtocolType,
    serverIp: String,
    serverIpError: Boolean,
    serverPort: String,
    serverPortError: Boolean,
    clientMessage: String,
    fileUrl: String,
    fileProgress: Int?,
    serverMessage: String,
    waitingForServer: Boolean?,
    socketStatus: Constants.SocketStatus,
    onConnectToServer: () -> Unit,
    onDisconnectFromServer: () -> Unit,
    onSendMessageEvent: (String) -> Unit,
    onAttachFileEvent: () -> Unit,
) {

    var expanded by remember { mutableStateOf(false) }
    val attachVisibility by remember(clientMessage, waitingForServer) {
        derivedStateOf { clientMessage.isEmpty() || (fileUrl.isNotEmpty() && waitingForServer != true) }
    }
    val animatedProgress by animateFloatAsState(
        targetValue = fileProgress?.toFloat() ?: 0f,
        animationSpec = tween(durationMillis = 500), label = ""
    )
    val configuration = LocalConfiguration.current
    Box(
        modifier = Modifier
            .fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.primary)
            ) {
                AppText(
                    modifier = Modifier
                        .padding(MaterialTheme.spacing.medium)
                        .align(Alignment.TopCenter),
                    text = stringResource(id = R.string.client_header, selectedProtocol.title),
                    textType = TextType.HEADER,
                    fontWeight = Bold,
                    textColor = White,
                    style = MaterialTheme.typography.headlineSmall
                )
                Icon(
                    modifier = Modifier
                        .align(Alignment.CenterEnd)
                        .padding(MaterialTheme.spacing.medium)
                        .clickable { expanded = !expanded },
                    imageVector = Icons.Default.MoreVert,
                    contentDescription = "menu",
                    tint = Color.White
                )
            }
            PowerButtonBody(
                socketStatus = socketStatus, onPowerButtonClicked = {
                    when (socketStatus.isConnected) {
                        true -> onConnectToServer()
                        false -> onDisconnectFromServer()
                    }
                }
            )
            Column(
                Modifier
                    .fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                AppOutlinedTextField(
                    modifier = Modifier
                        .padding(MaterialTheme.spacing.medium)
                        .fillMaxWidth(),
                    value = serverIp,
                    onValueChange = { ip ->
                        if (isIpValid(ip)) {
                            onEvent(ClientEvent.SetServerIp(ip))
                        }
                    },
                    label = stringResource(id = R.string.ip_label),
                    hasError = serverIpError,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),

                    )

                AppOutlinedTextField(
                    modifier = Modifier
                        .padding(MaterialTheme.spacing.medium)
                        .fillMaxWidth(),
                    value = serverPort,
                    onValueChange = { port ->
                        if (isPortValid(port))
                            onEvent(ClientEvent.SetServerPort(port))
                    },
                    label = stringResource(id = R.string.port_label),
                    hasError = serverPortError,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                )
            }

        }

        ProtocolTypeMenu(
            modifier = Modifier.align(Alignment.TopEnd),
            expanded = expanded,
            protocols = Constants.PROTOCOLS,
            selectedProtocol = Constants.ProtocolType.WEBSOCKET,
            onProtocolSelected = {
                onEvent(ClientEvent.SetProtocolType(it))
                expanded = false
            },
            onDismissClicked = { expanded = false })


    }


    /*   AppBaseScreen(
           headerTitle = stringResource(id = R.string.client_header, selectedProtocol.title),
           headerBackGround = Indigo,
           onProtocolSelected = { onEvent(ClientEvent.SetProtocolType(it)) },
           bodyContent = {
               Column(
                   Modifier.padding(MaterialTheme.spacing.small),
                   verticalArrangement = Arrangement.Center,
                   horizontalAlignment = Alignment.CenterHorizontally
               ) {
                   Row(
                       Modifier
                           .fillMaxWidth(),
                       verticalAlignment = Alignment.CenterVertically,
                   ) {
                       AppOutlinedTextField(
                           modifier = Modifier
                               .weight(1.5f)
                               .padding(MaterialTheme.spacing.small),
                           value = serverIp,
                           onValueChange = { ip ->
                               if (isIpValid(ip)) {
                                   onEvent(ClientEvent.SetServerIp(ip))
                               }
                           },
                           label = stringResource(id = R.string.ip_label),
                           hasError = serverIpError,
                           keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),

                           )

                       AppOutlinedTextField(
                           modifier = Modifier
                               .weight(1f)
                               .padding(MaterialTheme.spacing.small),
                           value = serverPort,
                           onValueChange = { port ->
                               if (isPortValid(port))
                                   onEvent(ClientEvent.SetServerPort(port))
                           },
                           label = stringResource(id = R.string.port_label),
                           hasError = serverPortError,
                           keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                       )
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

                   AppOutlinedTextField(
                       modifier = Modifier
                           .fillMaxWidth(),
                       value = clientMessage,
                       onValueChange = {
                           onEvent(ClientEvent.SetClientMessage(it))
                       },
                       enabled = socketStatus.connection,
                       singleLine = true,
                       label = stringResource(id = R.string.message),
                       colors = TextFieldDefaults.outlinedTextFieldColors(
                           textColor = Color.DarkGray,
                           focusedBorderColor = Indigo,
                           unfocusedBorderColor = Indigo400,
                           disabledBorderColor = Gray200,
                           disabledTextColor = Color.LightGray,
                           cursorColor = Indigo,
                           backgroundColor = Color.White,
                       ),
                       trailingIcon = {
                           if (!socketStatus.connection) {
                               IconButton(
                                   onClick = {
                                   }
                               ) {
                                   Icon(
                                       modifier = Modifier.size(28.dp),
                                       painter = painterResource(id = R.drawable.attach_file_icon),
                                       tint = Gray200,
                                       contentDescription = "Attach File"
                                   )
                               }
                           } else {
                               if (attachVisibility) {
                                   IconButton(
                                       onClick = {
                                           onAttachFileEvent()
                                       }
                                   ) {
                                       Icon(
                                           modifier = Modifier.size(28.dp),
                                           painter = painterResource(id = R.drawable.attach_file_icon),
                                           tint = Indigo,
                                           contentDescription = "Attach File"
                                       )
                                   }
                               } else if (waitingForServer != null) {
                                   if (fileProgress != null) {
                                       CircularProgressIndicator(modifier = Modifier.size(28.dp),progress = animatedProgress/100, color = Orange700 , backgroundColor = Color.LightGray)
                                   } else{
                                     IconButton(
                                           modifier = Modifier,
                                           onClick = {
                                               onAttachFileEvent()
                                           }
                                       ) {
                                           Icon(
                                               modifier = Modifier.size(28.dp),
                                               painter = painterResource(id = R.drawable.seen_icon),
                                               tint = if (waitingForServer == true) Gray200 else Color.Green,
                                               contentDescription = "send check"
                                           )
                                       }
                                   }

                               }
                           }

                       })

               }

           }) {
           AppButtonsRow(
               firstButtonTitle = if (!socketStatus.connection) stringResource(id = R.string.connect_to_server) else stringResource(
                   id = R.string.disconnect_from_server
               ),
               onFirstClicked = { if (!socketStatus.connection) onConnectToServer() else onDisconnectFromServer() },
               firstButtonColor = ButtonDefaults.buttonColors(
                   disabledBackgroundColor = Color.LightGray,
                   backgroundColor = Indigo,
               ),
               secondButtonTitle = if (fileUrl.isNotEmpty()) stringResource(id = R.string.send_file) else stringResource(
                   id = R.string.send_message
               ),
               secondEnable = socketStatus.connection && clientMessage.isNotEmpty() && !waitingForServer,
               secondButtonColor = ButtonDefaults.buttonColors(
                   disabledBackgroundColor = Color.LightGray,
                   backgroundColor = Indigo,
               )
           ) {
               onSendMessageEvent(clientMessage)
           }

       }*/


}

@Composable
fun PowerButtonBody(socketStatus: Constants.SocketStatus, onPowerButtonClicked: () -> Unit) {
    val configuration = LocalConfiguration.current

    val screenWidth = configuration.screenWidthDp
    val screenHeight = configuration.screenHeightDp
    val headerHeight = screenHeight / 3
    val headerStartBrush = MaterialTheme.colorScheme.primary
    val headerEndBrush = MaterialTheme.colorScheme.onPrimaryContainer
    val shadowColor = MaterialTheme.colorScheme.onSurface
    var downHeaderRectangleTop = 0f

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(headerHeight.dp)
    ) {
        Canvas(
            modifier = Modifier
                .fillMaxSize()
        ) {
            drawRect(
                brush = Brush.linearGradient(
                    colors = listOf(headerStartBrush, headerEndBrush),
                    start = Offset(0f, 0f), // Top of the arc
                    end = Offset(
                        0f,
                        size.height - (size.height / 4) - 2
                    )                        // Bottom of the arc
                ),
                size = Size(size.width, size.height - (size.height / 4))
            )

            downHeaderRectangleTop = size.height - (size.height / 2)
            val downHeaderRectangle = Path().apply {
                arcTo(
                    rect = Rect(
                        left = 0 - 100f,
                        top = downHeaderRectangleTop - 2,
                        right = size.width + 100,
                        bottom = size.height
                    ),
                    startAngleDegrees = 0f,
                    sweepAngleDegrees = 180f,
                    forceMoveTo = false
                )
                close()
            }
            drawPath(
                path = downHeaderRectangle,
                color = headerEndBrush,
            )
            val shadowArcPath = Path().apply {
                arcTo(
                    rect = Rect(
                        left = 0 - 100f,
                        top = size.height - (size.height / 2),
                        right = size.width + 100,
                        bottom = size.height
                    ),
                    startAngleDegrees = 0f,
                    sweepAngleDegrees = 180f,
                    forceMoveTo = false
                )
            }
            drawPath(
                path = shadowArcPath,
                color = shadowColor,
                style = Stroke(width = 40f)
            )
        }

        val powerContainerCircleRadius = screenWidth / 2
        DrawPowerButton(
            headerHeight = headerHeight,
            powerContainerCircleRadius = powerContainerCircleRadius.toFloat(),
            socketStatus = socketStatus
        ) { onPowerButtonClicked() }

    }
}


@Composable
fun DrawPowerButton(
    headerHeight: Int,
    powerContainerCircleRadius: Float,
    socketStatus: Constants.SocketStatus,
    onPowerButtonClicked: () -> Unit
) {
    var isAnimating by remember { mutableStateOf(false) }
    val connectionStatusBrush = Brush.verticalGradient(
        colors = if (isAnimating) listOf(Yellow600, White)
        else
            when (socketStatus.isConnected) {
                true -> listOf(Green900, Green400)
                false -> listOf(LightGray, LightGray)
            }
    )

    val powerButtonTargetScale = remember { Animatable(1f) }
    val coroutineScope = rememberCoroutineScope()
    // Calculate the alpha based on the current scale
    val currentScale = powerButtonTargetScale.value
    val alpha = (1 - currentScale / 1.5f).coerceIn(0f, 1f)
    fun animateCircle() {
        isAnimating = true

        coroutineScope.launch {
            // Loop for continuous animation
            while (isAnimating && !socketStatus.isConnected) {
                // Scale up
                powerButtonTargetScale.animateTo(
                    targetValue = 1.5f,
                    animationSpec = tween(durationMillis = 1000)
                )

                // Fade out the circle
                powerButtonTargetScale.animateTo(
                    targetValue = 1.5f,
                    animationSpec = tween(durationMillis = 1000) // Keep it at max scale for the fade
                )

                // Fade the circle out by resetting to original scale and alpha
                powerButtonTargetScale.snapTo(1f) // Reset scale instantly

                // Optional: add a delay before restarting the animation
                kotlinx.coroutines.delay(100)
            }
        }
    }
    LaunchedEffect(socketStatus.isConnected) {
        if (socketStatus.isConnected) {
            isAnimating = false
        }
    }

    Box(modifier = Modifier
        .fillMaxSize()
        .clickable {
            animateCircle()
            onPowerButtonClicked()
        }) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawCircle(color = White, radius = powerContainerCircleRadius)

            drawCircle(
                color = if (!isAnimating) LightGray else Yellow600,
                radius = powerContainerCircleRadius,
                style = Stroke(width = 14f)
            )

            if (isAnimating)
                drawCircle(
                    color = Yellow600.copy(alpha = alpha),
                    radius = powerContainerCircleRadius * powerButtonTargetScale.value,
                    style = Stroke(width = 24f)
                )

        }
        if (socketStatus.isConnected) {
            Image(
                modifier = Modifier
                    .align(Alignment.Center)
                    .size((powerContainerCircleRadius / 3).dp),
                painter = painterResource(id = R.drawable.power_icon),
                contentDescription = socketStatus.title,
            )
        } else {
            Icon(
                modifier = Modifier
                    .align(Alignment.Center)
                    .size((powerContainerCircleRadius / 3).dp),
                painter = painterResource(id = R.drawable.power_icon),
                tint = Color.LightGray,
                contentDescription = socketStatus.title
            )
        }
        AppText(
            Modifier
                .align(Alignment.BottomCenter)
                .offset(y = (-(headerHeight / 9)).dp),
            text = if (isAnimating) stringResource(id = R.string.connecting_label) else socketStatus.title,
            fontWeight = when (socketStatus.isConnected) {
                true -> Bold
                false -> Normal
            },
            style = MaterialTheme.typography.titleMedium.copy(
                brush = connectionStatusBrush
            )
        )
    }

}
