package ir.example.androidsocket.ui

import android.app.Activity
import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.TextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.androidSocket.R
import ir.example.androidsocket.Constants
import ir.example.androidsocket.ui.base.AppButtonsRow
import ir.example.androidsocket.ui.base.AppOutlinedTextField
import ir.example.androidsocket.ui.base.AppText
import ir.example.androidsocket.ui.base.AppTitleValueText
import ir.example.androidsocket.ui.base.BaseUiEvent
import ir.example.androidsocket.ui.base.TextType
import ir.example.androidsocket.ui.theme.AndroidSocketTheme
import ir.example.androidsocket.ui.theme.Gray200
import ir.example.androidsocket.ui.theme.Green900
import ir.example.androidsocket.ui.theme.Indigo
import ir.example.androidsocket.ui.theme.Indigo400
import ir.example.androidsocket.ui.theme.Orange700
import ir.example.androidsocket.ui.theme.spacing
import ir.example.androidsocket.utils.AppBaseScreen
import ir.example.androidsocket.utils.clientLog
import ir.example.androidsocket.utils.isIpValid
import ir.example.androidsocket.utils.isPortValid

@Composable
internal fun ClientCompose(
    viewModel: ClientViewModel,
    onEvent: (ClientEvent) -> Unit
) {

    val keyboardController = LocalSoftwareKeyboardController.current
    val selectedProtocol by viewModel.selectedProtocol.collectAsState()
    val clientMessage by viewModel.clientMessage.collectAsStateWithLifecycle("")
    val fileUrl by viewModel.fileUrl.collectAsStateWithLifecycle()
    val serverMessage by viewModel.serverMessage.collectAsStateWithLifecycle("")
    val waitingForServerConfirmation by viewModel.waitingForServerConfirmation.collectAsStateWithLifecycle(
        false
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
        if (!socketStatus.connection) {
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
            color = MaterialTheme.colors.background
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
    serverMessage: String,
    waitingForServer: Boolean,
    socketStatus: Constants.SocketStatus,
    onConnectToServer: () -> Unit,
    onDisconnectFromServer: () -> Unit,
    onSendMessageEvent: (String) -> Unit,
    onAttachFileEvent: () -> Unit,

    ) {

    val attachVisibility by remember(clientMessage) {
        derivedStateOf { clientMessage.isEmpty() || fileUrl.isNotEmpty() }
    }

    AppBaseScreen(
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
                                    modifier = Modifier.size(24.dp),
                                    painter = painterResource(id = R.drawable.attach_file_icon),
                                    tint = Gray200,
                                    contentDescription = "Attach File"
                                )
                            }
                        } else {
                            IconButton(
                                modifier = Modifier.alpha(if (attachVisibility) 1f else 0f),
                                onClick = {
                                    onAttachFileEvent()
                                }
                            ) {
                                Icon(
                                    modifier = Modifier.size(24.dp),
                                    painter = painterResource(id = R.drawable.attach_file_icon),
                                    tint = Indigo,
                                    contentDescription = "Attach File"
                                )
                            }
                        }

                    })


                if (socketStatus.connection && waitingForServer && serverMessage.isEmpty()) {
                    AppText(
                        modifier = Modifier.padding(MaterialTheme.spacing.small),
                        text = stringResource(R.string.server_confirmation_message),
                        textColor = Color.Red
                    )
                }

                if (serverMessage.isNotEmpty()) {
                    AppText(
                        modifier = Modifier.padding(MaterialTheme.spacing.small),
                        text = serverMessage,
                        textColor = Orange700
                    )
                }
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
            secondEnable = socketStatus.connection && clientMessage.isNotEmpty(),
            secondButtonColor = ButtonDefaults.buttonColors(
                disabledBackgroundColor = Color.LightGray,
                backgroundColor = Indigo,
            )
        ) {
            onSendMessageEvent(clientMessage)
        }

    }

}
