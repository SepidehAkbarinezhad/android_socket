package ir.example.androidsocket.ui

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
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
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.androidSocket.R
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import ir.example.androidsocket.Constants
import ir.example.androidsocket.socket.SocketServerForegroundService.Companion.PORT
import ir.example.androidsocket.ui.base.AppIcon
import ir.example.androidsocket.ui.base.AppText
import ir.example.androidsocket.ui.base.BaseUiEvent
import ir.example.androidsocket.ui.base.ProtocolTypeMenu
import ir.example.androidsocket.ui.base.TextType
import ir.example.androidsocket.ui.theme.AndroidSocketTheme
import ir.example.androidsocket.ui.theme.Green400
import ir.example.androidsocket.ui.theme.Green900
import ir.example.androidsocket.ui.theme.spacing
import ir.example.androidsocket.utils.ConnectionTypeManager
import ir.example.androidsocket.utils.serverLog
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

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
    val fileIsSaved by viewModel.fileIsSaved.collectAsState()
    val isWifiConnected by connectionTypeManager.isWifiConnected.collectAsState()
    val isEthernetConnected by connectionTypeManager.isEthernetConnected.collectAsState()
    val selectedProtocol by viewModel.selectedProtocol.collectAsState()
    val isConnecting by viewModel.isConnecting.collectAsState()
    val wifiIpAddress by viewModel.wifiServerIp.collectAsState()
    val lanIpAddress by viewModel.ethernetServerIp.collectAsState()
    val socketStatus by viewModel.socketStatus.collectAsState()


    LaunchedEffect(key1 = socketStatus) {
        if (!socketStatus.isConnected) {
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
        val systemUiController = rememberSystemUiController()
        systemUiController.setNavigationBarColor(MaterialTheme.colorScheme.primary)
        Surface(
            modifier = Modifier.padding(WindowInsets.navigationBars.asPaddingValues()),
            color = MaterialTheme.colorScheme.primary,
        ) {
            ServerContent(
                onEvent = onEvent,
                connectionType = connectionType,
                selectedProtocol = selectedProtocol,
                isConnecting = isConnecting,
                wifiIpAddress = wifiIpAddress,
                lanIpAddress = lanIpAddress,
                socketStatus = socketStatus,
                clientMessage = clientMessage,
                fileProgress = fileProgress,
                fileIsSaved = fileIsSaved
            )
        }
    }
}

@Composable
fun ServerContent(
    onEvent: (ServerEvent) -> Unit,
    selectedProtocol: Constants.ProtocolType,
    isConnecting: Boolean,
    connectionType: Constants.ConnectionType,
    wifiIpAddress: String,
    lanIpAddress: String,
    socketStatus: Constants.SocketStatus,
    clientMessage: String,
    fileProgress: Int?,
    fileIsSaved: Boolean
) {
    var isAnimating by remember { mutableStateOf(false) }

    var expanded by remember { mutableStateOf(false) }
    val isWifi = remember(connectionType) {
        connectionType == Constants.ConnectionType.WIFI
    }
    val isEthernet = remember(connectionType) {
        connectionType == Constants.ConnectionType.ETHERNET
    }
    val hasConnection = remember(isWifi, isEthernet) { isWifi || isEthernet }
    LaunchedEffect(isConnecting) {
        serverLog("LaunchedEffect(isConnecting)  $isConnecting")
        if (!isConnecting) {
            serverLog("...................")
            isAnimating = false
        }
    }

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
                modifier = Modifier.padding(WindowInsets.statusBars.asPaddingValues()),
                text = stringResource(id = R.string.server_header, selectedProtocol.title),
                textType = TextType.HEADER,
                fontWeight = FontWeight.Bold,
                textColor = MaterialTheme.colorScheme.primary,
                style = MaterialTheme.typography.headlineMedium
            )
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(.2f),
                contentAlignment = Alignment.Center
            ) {

                Column(
                    Modifier
                        .fillMaxWidth()
                        .padding(
                            horizontal = MaterialTheme.spacing.medium
                        )
                    //.alpha(if (clientMessage.isNotEmpty()) 1f else 0f)
                    ,
                    horizontalAlignment = Alignment.Start,
                    verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.small)
                ) {

                    AppText(
                        modifier = Modifier.padding(horizontal = MaterialTheme.spacing.small),
                        text = stringResource(id = R.string.message_from_client),
                        fontWeight = FontWeight.Bold,
                        textColor = if (clientMessage.isEmpty()) Color.Gray else MaterialTheme.colorScheme.primary,
                        style = MaterialTheme.typography.titleMedium,
                    )

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(MaterialTheme.spacing.extraLarge),
                        border = BorderStroke(
                            1.dp,
                            color = if (clientMessage.isEmpty()) Color.Gray else MaterialTheme.colorScheme.primary
                        ),
                        colors = CardDefaults.cardColors(containerColor = Color.White)
                    ) {
                        AppText(
                            modifier = Modifier.padding(MaterialTheme.spacing.small),
                            text = clientMessage
                        )
                    }
                }
            }

            ConnectionBody(
                modifier = Modifier.weight(.3f),
                socketStatus = socketStatus,
                isConnecting = true,
                wifiIpAddress = wifiIpAddress,
                lanIpAddress = lanIpAddress,
                connectionType = connectionType,
                isAnimating = isAnimating,
                onEvent = onEvent,
            ) {

            }

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
                    .clickable {
                        expanded = !expanded
                    },
                imageVector = Icons.Default.MoreVert,
                contentDescription = "menu",
                tint = MaterialTheme.colorScheme.primary
            )
            ProtocolTypeMenu(
                expanded = expanded,
                protocols = Constants.PROTOCOLS,
                selectedProtocol = selectedProtocol,
                onProtocolSelected = {
                    if(connectionType != Constants.ConnectionType.NONE){
                        isAnimating = true
                        onEvent(ServerEvent.SetIsConnecting(true))
                    }
                    onEvent(ServerEvent.SetProtocolType(it, connectionType = connectionType))
                    expanded = false
                },
                onDismissClicked = { expanded = false })
        }
    }

    /*  AppBaseScreen(
          headerTitle = stringResource(id = R.string.server_header, selectedProtocol.title),
          headerBackGround = Indigo,
          onProtocolSelected = {
              onEvent(ServerEvent.SetLoading(true))
              onEvent(ServerEvent.SetProtocolType(it))
          },
          bodyContent = {

              var expanded by remember { mutableStateOf(false) }
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
                          valueColor = if (!socketStatus.isConnected) Color.Red else Green900,
                          titleTextType = TextType.TEXT,
                          valueTextType = TextType.TEXT
                      )
                      fileProgress?.let { FileTransferAnimation() }
                      if (fileIsSaved) {
                          Dialog(onDismissRequest = { onEvent(ServerEvent.SetFileIsSaved(false)) }) {
                              Column(Modifier.background(Color.White).padding(MaterialTheme.spacing.extraMedium)) {
                                  AppText(
                                      Modifier,
                                      text = stringResource(id = R.string.file_message_saved),
                                      textType = TextType.TITLE
                                  )
                                  AppText(
                                      Modifier.padding(MaterialTheme.spacing.small).clickable {  onEvent(ServerEvent.SetFileIsSaved(false))  },
                                      text = "ok",
                                      textColor = Color.Red,
                                      textDecoration = TextDecoration.Underline,
                                      textType = TextType.TITLE

                                  )
                              }
                          }
                     }


                        Column(
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


                         }

                  }

              }


          }) {

      }
  */
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


@Composable
fun ConnectionBody(
    modifier: Modifier,
    socketStatus: Constants.SocketStatus,
    isConnecting: Boolean,
    wifiIpAddress: String,
    lanIpAddress: String,
    connectionType: Constants.ConnectionType,
    isAnimating: Boolean,
    onEvent: (ServerEvent) -> Unit,
    onPowerButtonClicked: () -> Unit
) {

    var powerContainerCircleRadius by remember { mutableIntStateOf(0) }
    val connectionStatusBrush = Brush.verticalGradient(
        colors = if (isAnimating) listOf(MaterialTheme.colorScheme.tertiary, Color.White)
        else
            when (socketStatus.isConnected) {
                true -> listOf(Green900, Green400)
                false -> listOf(Color.LightGray, Color.LightGray)
            }
    )
    val powerButtonTargetScale = remember { Animatable(1f) }
    val coroutineScope = rememberCoroutineScope()
    val currentScale = powerButtonTargetScale.value
    val alpha = (1 - currentScale / 1.5f).coerceIn(0f, 1f)

    fun animateCircle() {
        serverLog("animateCircle  $isAnimating")
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

    val headerStartBrush = MaterialTheme.colorScheme.primary
    val headerEndBrush = MaterialTheme.colorScheme.onPrimaryContainer
    val animateColor = MaterialTheme.colorScheme.tertiary
    val connectColor = MaterialTheme.colorScheme.onSecondary
    val disConnectColor = MaterialTheme.colorScheme.primaryContainer
    val borderColor by remember(socketStatus.isConnected, isAnimating) {
        derivedStateOf {
            when {
                isAnimating -> animateColor
                socketStatus.isConnected -> connectColor
                else -> disConnectColor
            }
        }
    }

    LaunchedEffect(isAnimating) {
        serverLog("LaunchedEffect(isAnimating)  $isAnimating")
        if (isAnimating){
            serverLog("LaunchedEffect(isAnimating)  ........")
            animateCircle()
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        //the parent box divided to 5 parts . 3 part for the top and 2 for bottom
        Box(
            Modifier
                .fillMaxWidth()
                .align(Alignment.TopCenter)
                .fillMaxHeight(.6f)
        ) {
            Canvas(
                modifier = Modifier
                    .fillMaxSize()
            ) {
                val powerContainerRectangle = Path().apply {
                    arcTo(
                        rect = Rect(
                            left = 0 - 30f,
                            top = (size.height / 4),
                            right = size.width + 30f,
                            bottom = size.height + 100
                        ),
                        startAngleDegrees = 0f,
                        sweepAngleDegrees = -180f,
                        forceMoveTo = false
                    )
                    close()
                }
                drawPath(
                    path = powerContainerRectangle,
                    color = headerEndBrush,
                )
                val shadowArcPath = Path().apply {
                    arcTo(
                        rect = Rect(
                            left = 0 - 30f,
                            top = (size.height / 4),
                            right = size.width + 30f,
                            bottom = size.height + 85
                        ),
                        startAngleDegrees = 0f,
                        sweepAngleDegrees = -180f,
                        forceMoveTo = false
                    )

                }
                drawPath(
                    path = shadowArcPath,
                    color = borderColor,
                    style = Stroke(24f)
                )

                powerContainerCircleRadius = (size.height.toInt()) / 4
                val powerContainerCircleOffset = Offset(size.width / 2f, size.height / 4)

                drawCircle(
                    color = Color.White,
                    radius = powerContainerCircleRadius.toFloat(),
                    center = powerContainerCircleOffset
                )
                drawCircle(
                    color = if (!isAnimating) borderColor else animateColor,
                    radius = powerContainerCircleRadius.toFloat(),
                    style = Stroke(width = 24f),
                    center = powerContainerCircleOffset
                )
                if (isAnimating)
                    drawCircle(
                        color = animateColor.copy(alpha = alpha),
                        radius = powerContainerCircleRadius * powerButtonTargetScale.value,
                        style = Stroke(width = 24f),
                        center = powerContainerCircleOffset
                    )
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(.5f),
                contentAlignment = Alignment.Center
            ) {
                if (socketStatus.isConnected) {
                    Image(
                        modifier = Modifier
                            .clickable {

                            }
                            .size((powerContainerCircleRadius / 3).dp),
                        painter = painterResource(id = R.drawable.power_icon),
                        contentDescription = socketStatus.title,
                    )
                } else {
                    Icon(
                        modifier = Modifier
                            .clickable {

                            }
                            .size((powerContainerCircleRadius / 3).dp),
                        painter = painterResource(id = R.drawable.power_icon),
                        tint = Color.LightGray,
                        contentDescription = socketStatus.title
                    )
                }
            }

        }

        ServerInfoContainer(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(.6f)
                .align(Alignment.BottomCenter)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(headerEndBrush, headerStartBrush)
                    )
                ),
            connectionType = connectionType,
            wifiIpAddress = wifiIpAddress,
            lanIpAddress = lanIpAddress,
            socketStatus = Constants.SocketStatus.CONNECTED
        )

    }

}

@Composable
fun ServerInfoContainer(
    modifier: Modifier = Modifier,
    connectionType: Constants.ConnectionType,
    wifiIpAddress: String,
    lanIpAddress: String,
    socketStatus: Constants.SocketStatus,
) {

    val ip by remember(connectionType, wifiIpAddress, lanIpAddress) {
        derivedStateOf {
            when (connectionType) {
                Constants.ConnectionType.NONE -> ""
                Constants.ConnectionType.WIFI -> wifiIpAddress
                Constants.ConnectionType.ETHERNET -> lanIpAddress
            }
        }
    }
    val isWifi = remember(connectionType) {
        connectionType == Constants.ConnectionType.WIFI
    }
    val isEthernet = remember(connectionType) {
        connectionType == Constants.ConnectionType.ETHERNET
    }

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        Row(
            Modifier
                .fillMaxWidth()
                .weight(.5f)
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
        if (connectionType != Constants.ConnectionType.NONE) {
            AppText(
                modifier = Modifier
                    .padding(MaterialTheme.spacing.small)
                    .weight(.5f),
                text = "$ip : $PORT",
                textColor = Color.White,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        }

//        Row(
//            Modifier
//                .fillMaxSize()
//                .padding(MaterialTheme.spacing.small),
//            horizontalArrangement = Arrangement.Center,
//            verticalAlignment = Alignment.CenterVertically
//        ) {
//
//            /*  Card(
//                  Modifier
//                      .weight(1.5f)
//                      .padding(MaterialTheme.spacing.small),
//                  border = BorderStroke(3.dp, MaterialTheme.colorScheme.primaryContainer),
//                  colors = CardDefaults.cardColors(containerColor = Color.Transparent)
//
//              ) {*/
//            Column(
//                modifier = Modifier
//                    .weight(1.5f)
//                    .padding(MaterialTheme.spacing.small),
//                horizontalAlignment = Alignment.Start,
//            ) {
//                AppText(
//                    modifier = Modifier.padding(MaterialTheme.spacing.small),
//                    text = stringResource(id = R.string.ip_label),
//                    fontWeight = FontWeight.Bold,
//                    style = MaterialTheme.typography.bodyLarge,
//                    textColor = Color.White
//
//                )
//                AppText(
//                    Modifier
//                        .fillMaxWidth()
//                        .padding(MaterialTheme.spacing.medium),
//                    text = when (connectionType) {
//                        Constants.ConnectionType.NONE -> ""
//                        Constants.ConnectionType.WIFI -> wifiIpAddress
//                        Constants.ConnectionType.ETHERNET -> lanIpAddress
//                    },
//                    fontWeight = FontWeight.Bold,
//                    style = MaterialTheme.typography.bodyLarge,
//                    textColor = Color.White
//
//                )
//            }
//            //  }
//
//
//            /*  Card(
//                  Modifier
//                      .weight(1f)
//                      .padding(MaterialTheme.spacing.small),
//                  border = BorderStroke(3.dp, MaterialTheme.colorScheme.primaryContainer),
//                  colors = CardDefaults.cardColors(containerColor = Color.Transparent),
//                  shape = RoundedCornerShape(MaterialTheme.spacing.medium)
//              ) {*/
//            Column(
//                modifier = Modifier
//                    .weight(1f)
//                    .padding(MaterialTheme.spacing.small),
//                horizontalAlignment = Alignment.Start,
//            ) {
//                AppText(
//                    modifier = Modifier.padding(MaterialTheme.spacing.small),
//                    text = stringResource(id = R.string.port_label),
//                    fontWeight = FontWeight.Bold,
//                    style = MaterialTheme.typography.bodyLarge,
//                    textColor = Color.White,
//                )
//                AppText(
//                    Modifier
//                        .fillMaxWidth()
//                        .padding(MaterialTheme.spacing.medium),
//                    text = SocketServerForegroundService.PORT.toString(),
//                    fontWeight = FontWeight.Bold,
//                    style = MaterialTheme.typography.bodyLarge,
//                    textColor = Color.White,
//                )
//            }
//            //  }
//        }
    }

}




