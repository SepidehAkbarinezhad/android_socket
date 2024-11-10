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
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color.Companion.LightGray
import androidx.compose.ui.graphics.Color.Companion.White
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
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
    val onConnectedClicked by viewModel.onConnectedClicked.collectAsState()
    val socketIsConnected by remember(socketStatus) {
        mutableStateOf(socketStatus == Constants.SocketStatus.CONNECTED)
    }
    val inConnectionProcess by viewModel.inConnectionProcess.collectAsState()

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            // Get the URI of the selected file (result.data?.data)
            val uri = result.data?.data
            if (uri != null) {
                clientLog("File selected: $uri")
                onEvent(ClientEvent.SetClientMessage(uri.toString()))
                onEvent(ClientEvent.SetFileUrl(uri))
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
        if (!socketIsConnected) {
            onEvent(ClientEvent.SetClientMessage(""))
            onEvent(ClientEvent.SetServerMessage(""))
        }
    }

    LaunchedEffect(socketIsConnected, socketStatus) {
        clientLog(
            "LaunchedEffect---->ClientCompose-->  socketIsConnected:$socketIsConnected  title:${socketStatus.title}",
            "connection"
        )
    }

    ClientContent(
        onEvent = onEvent,
        selectedProtocol = selectedProtocol,
        serverIp = serverIp,
        serverIpError = serverIpError,
        serverPort = serverPort,
        serverPortError = serverPortError,
        clientMessage = clientMessage,
        fileProgress = fileProgress,
        waitingForServer = waitingForServerConfirmation,
        socketStatus = socketStatus,
        onConnectedClicked = onConnectedClicked,
        socketIsConnected = socketIsConnected,
        inConnectionProcess = inConnectionProcess,
        onConnectionButtonClicked = {
            clientLog(
                "if (!inConnectionProcess)  $socketIsConnected  ${socketStatus.title}",
                "connectionnnn"
            )
            onEvent(ClientEvent.OnConnectionButtonClicked)
            keyboardController?.hide()
        },
        onSendMessageEvent = { message ->
            clientLog("onSendMessageEvent")
            keyboardController?.hide()
            onEvent(ClientEvent.SetServerMessage(""))
            // onEvent(ClientEvent.SetLoading(true))
            onEvent(ClientEvent.SendMessageToServer(message))

        },
    )

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
    fileProgress: Int?,
    waitingForServer: Boolean?,
    socketStatus: Constants.SocketStatus,
    onConnectedClicked: Boolean,
    socketIsConnected: Boolean,
    inConnectionProcess: Boolean,
    onConnectionButtonClicked: () -> Unit,
    onSendMessageEvent: (String) -> Unit,
) {

    var expanded by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            AppText(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.primary)
                    .padding(WindowInsets.statusBars.asPaddingValues()),
                text = stringResource(id = R.string.client_header, selectedProtocol.title),
                textType = TextType.HEADER,
                fontWeight = Bold,
                textColor = White,
                style = MaterialTheme.typography.headlineMedium
            )
            PowerButtonBody(
                modifier = Modifier.weight(.3f),
                socketStatus = socketStatus,
                socketIsConnected = socketIsConnected,
                onConnectedClicked = onConnectedClicked,
                onPowerButtonClicked = onConnectionButtonClicked,
                inConnectionProcess = inConnectionProcess,
                serverAddress = if (serverIp.isNotEmpty() && serverPort.isNotEmpty() && socketIsConnected) "$serverIp : $serverPort" else "",
                formIsFilled = serverIp.isNotEmpty() && serverPort.isNotEmpty(),
                onEvent = onEvent
            )

            if (!socketIsConnected)
                ServerInfoForm(
                    modifier = Modifier
                        .weight(.3f)
                        .fillMaxWidth(),
                    serverIp = serverIp,
                    serverIpError = serverIpError,
                    serverPort = serverPort,
                    serverPortError = serverPortError,
                    onEvent = onEvent
                )

            if (socketIsConnected)
                MessageContainer(
                    modifier = Modifier
                        .weight(.3f)
                        .fillMaxWidth()
                        .padding(MaterialTheme.spacing.small),
                    socketStatus = socketStatus,
                    clientMessage = clientMessage,
                    fileProgress = fileProgress,
                    waitingForServer = waitingForServer,
                    onSendMessageEvent = { onSendMessageEvent(it) },
                    onEvent = onEvent
                )

        }
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentSize(Alignment.TopEnd)
                .padding(WindowInsets.statusBars.asPaddingValues()),
        ) {
            Icon(
                modifier = Modifier
                    .padding(MaterialTheme.spacing.small)
                    .clickable { expanded = !expanded },
                imageVector = Icons.Default.MoreVert,
                contentDescription = "menu",
                tint = White
            )
            ProtocolTypeMenu(
                expanded = expanded,
                protocols = Constants.PROTOCOLS,
                selectedProtocol = selectedProtocol,
                onProtocolSelected = {
                    onEvent(ClientEvent.SetProtocolType(it))
                    expanded = false
                },
                onDismissClicked = { expanded = false })
        }
    }

}

@Composable
fun PowerButtonBody(
    modifier: Modifier,
    socketStatus: Constants.SocketStatus,
    onConnectedClicked: Boolean,
    socketIsConnected: Boolean,
    inConnectionProcess: Boolean,
    serverAddress: String,
    formIsFilled: Boolean,
    onEvent: (ClientEvent) -> Unit,
    onPowerButtonClicked: () -> Unit
) {
    clientLog(
        "PowerButtonBody  socketIsConnected:$socketIsConnected  ${socketStatus.title}",
        "connectionnnn"
    )
    var isAnimating by remember { mutableStateOf(false) }
    var circleCenter by remember { mutableStateOf(Offset(0f, 0f)) }
    var powerContainerCircleRadius by remember { mutableIntStateOf(0) }
    val connectionStatusBrush = Brush.verticalGradient(
        colors = if (isAnimating) listOf(MaterialTheme.colorScheme.tertiary, White)
        else
            when (socketIsConnected) {
                true -> listOf(Green900, Green400)
                false -> listOf(LightGray, LightGray)
            }
    )
    val powerButtonTargetScale = remember { Animatable(1f) }
    val coroutineScope = rememberCoroutineScope()
    val currentScale = powerButtonTargetScale.value
    val alpha = (1 - currentScale / 1.5f).coerceIn(0f, 1f)

    fun animateCircle() {
        isAnimating = true
        coroutineScope.launch {
            // Loop for continuous animation
            while (isAnimating) {
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
    LaunchedEffect(onConnectedClicked) {
        if (onConnectedClicked) {
            if (formIsFilled && !socketIsConnected) {
                animateCircle()
                onEvent(ClientEvent.SetInConnectionProcess(true))
            }
            onPowerButtonClicked()
        }

    }

    LaunchedEffect(inConnectionProcess) {
        if (!inConnectionProcess) {
            isAnimating = false
        }
    }

    val headerStartBrush = MaterialTheme.colorScheme.primary
    val headerEndBrush = MaterialTheme.colorScheme.onPrimaryContainer
    val animateColor = MaterialTheme.colorScheme.tertiary
    val connectColor = MaterialTheme.colorScheme.onSecondary
    val disConnectColor = MaterialTheme.colorScheme.primaryContainer
    val borderColor by remember(socketIsConnected, isAnimating) {
        derivedStateOf {
            when {
                isAnimating -> animateColor
                socketIsConnected -> connectColor
                else -> disConnectColor
            }
        }
    }
    Box(modifier.fillMaxWidth()) {
        Column(
            modifier = modifier
                .fillMaxWidth()
        ) {
            Box(
                Modifier
                    .fillMaxWidth()
                    .weight(.3f)
            ) {
                Canvas(
                    modifier = Modifier
                        .fillMaxSize()
                        .pointerInput(Unit) {
                            detectTapGestures { offset ->
                                // Check if the tap is within the circle's bounds
                                val distanceFromCenter = (offset - circleCenter).getDistance()
                                if (distanceFromCenter <= powerContainerCircleRadius) {
                                    if (!inConnectionProcess) {
                                        onEvent(ClientEvent.SetOnConnectedButtonClicked(true))
                                    }
                                }
                            }
                        }
                ) {
                    circleCenter = Offset(size.width / 2, size.height / 2)
                    powerContainerCircleRadius = size.height.toInt() / 3
                    drawRect(
                        brush = Brush.linearGradient(
                            colors = listOf(headerStartBrush, headerEndBrush),
                            start = Offset(0f, 0f), // Top of the arc
                            end = Offset(
                                0f,
                                size.height - (size.height / 4) - 2
                            )                        // Bottom of the arc
                        ),
                        size = Size(size.width, size.height)
                    )

                    drawCircle(color = White, radius = powerContainerCircleRadius.toFloat())
                    drawCircle(
                        color = if (!isAnimating) borderColor else animateColor,
                        radius = powerContainerCircleRadius.toFloat(),
                        style = Stroke(width = 24f)
                    )
                    if (isAnimating)
                        drawCircle(
                            color = animateColor.copy(alpha = alpha),
                            radius = powerContainerCircleRadius * powerButtonTargetScale.value,
                            style = Stroke(width = 24f)
                        )


                }
                if (socketIsConnected) {
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
                        tint = LightGray,
                        contentDescription = socketStatus.title
                    )
                }
            }
            Box(
                Modifier
                    .fillMaxWidth()
                    .weight(.2f)
            ) {
                Canvas(
                    modifier = Modifier
                        .fillMaxSize()
                ) {
                    drawRect(
                        color = headerEndBrush,
                        size = Size(size.width, size.height / 2)
                    )
                    val downHeaderRectangle = Path().apply {
                        arcTo(
                            rect = Rect(
                                left = 0 - 140f,
                                top = -10f,
                                right = size.width + 140,
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
                                top = 0f,
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
                        color = borderColor,
                        style = Stroke(width = 24f)
                    )
                }

                Column(
                    Modifier
                        .align(Alignment.Center),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    AppText(
                        Modifier.padding(MaterialTheme.spacing.small),
                        text = if (inConnectionProcess) stringResource(id = R.string.connecting_label) else socketStatus.title,
                        fontWeight = when (socketIsConnected) {
                            true -> Bold
                            false -> Normal
                        },
                        style = MaterialTheme.typography.titleLarge.copy(
                            brush = connectionStatusBrush
                        )
                    )
                    AppText(
                        Modifier
                            .padding(MaterialTheme.spacing.small)
                            .alpha(if (socketIsConnected) 1f else 0f),
                        text = serverAddress,
                        style = MaterialTheme.typography.titleMedium,
                        textColor = LightGray,
                        fontWeight = Bold
                    )
                }

            }

        }

    }
}


@Composable
fun ServerInfoForm(
    modifier: Modifier,
    serverIp: String,
    serverIpError: Boolean,
    serverPort: String,
    serverPortError: Boolean,
    onEvent: (ClientEvent) -> Unit
) {
    Column(
        modifier = modifier,
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
            textStyle = MaterialTheme.typography.bodySmall
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
            textStyle = MaterialTheme.typography.bodySmall
        )
    }

}


@Composable
fun MessageContainer(
    modifier: Modifier,
    socketStatus: Constants.SocketStatus,
    clientMessage: String,
    fileProgress: Int?,
    waitingForServer: Boolean?,
    onSendMessageEvent: (String) -> Unit,
    onEvent: (ClientEvent) -> Unit
) {

    val attachVisibility by remember(clientMessage, waitingForServer) {
        derivedStateOf { clientMessage.isEmpty() }
    }
    val animatedProgress by animateFloatAsState(
        targetValue = fileProgress?.toFloat() ?: 0f,
        animationSpec = tween(durationMillis = 500), label = ""
    )
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
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        AppOutlinedTextField(
            modifier = Modifier
                .fillMaxWidth(),
            value = clientMessage,
            onValueChange = {
                onEvent(ClientEvent.SetClientMessage(it))
            },
            singleLine = true,
            label = stringResource(id = R.string.message),
            textStyle = MaterialTheme.typography.bodySmall,
            trailingIcon = {

                if (attachVisibility) {
                    IconButton(
                        onClick = {
                            onAttachFile()
                        }
                    ) {
                        Icon(
                            modifier = Modifier.size(28.dp),
                            painter = painterResource(id = R.drawable.attach_file_icon),
                            tint = MaterialTheme.colorScheme.primary,
                            contentDescription = "Attach File"
                        )
                    }
                } else if (waitingForServer != null) {
                    if (fileProgress != null) {
                        //client is sending file
                        CircularProgressIndicator(
                            modifier = Modifier.size(28.dp),
                            progress = animatedProgress / 100,
                            color = MaterialTheme.colorScheme.onSecondary,
                            trackColor = LightGray
                        )
                    } else {
                        //client is sending text
                        IconButton(
                            modifier = Modifier,
                            onClick = {
                                onAttachFile()
                            }
                        ) {
                            Icon(
                                modifier = Modifier.size(28.dp),
                                painter = painterResource(id = R.drawable.seen_icon),
                                tint = if (waitingForServer == true) LightGray else MaterialTheme.colorScheme.primary,
                                contentDescription = "seen"
                            )
                        }
                    }

                } else {
                    if (clientMessage.isNotEmpty()) {
                        IconButton(
                            onClick = {
                                onSendMessageEvent(clientMessage)
                            }
                        ) {
                            Icon(
                                modifier = Modifier.size(28.dp),
                                painter = painterResource(id = R.drawable.send_icon),
                                tint = MaterialTheme.colorScheme.primary,
                                contentDescription = "send check"
                            )
                        }
                    }

                }

            },
            leadingIcon = {
                if (clientMessage.isNotEmpty() && waitingForServer == false) {
                    IconButton(
                        onClick = {
                            onEvent(ClientEvent.ResetClientMessage)
                        }
                    ) {
                        Icon(
                            modifier = Modifier.size(12.dp),
                            painter = painterResource(id = R.drawable.close_icon),
                            tint = MaterialTheme.colorScheme.primary,
                            contentDescription = "reset message"
                        )

                    }
                }
            })
    }

}

