package ir.example.androidsocket.ui.client

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.MaterialTheme
import androidx.compose.material.TextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
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
    val isServiceBound by viewModel.isServiceBound.collectAsStateWithLifecycle(initialValue = false)
    val clientMessage by viewModel.clientMessage.collectAsStateWithLifecycle("")
    val serverMessage by viewModel.serverMessage.collectAsStateWithLifecycle("")
    val serverIp by viewModel.serverIp.collectAsState()
    val serverIpError by viewModel.serverIpError.collectAsState()
    val serverPort by viewModel.serverPort.collectAsState()
    val serverPortError by viewModel.serverPortError.collectAsState()
    val socketStatus by viewModel.socketStatus.collectAsState()
    val context = LocalContext.current
    val uiEvent by viewModel.uiEvent.collectAsStateWithLifecycle(initialValue = BaseUiEvent.None)


    LaunchedEffect(key1 = socketStatus) {
        if (!socketStatus.connection) {
            onEvent(ClientEvent.SetClientMessage(""))
            onEvent(ClientEvent.SetServerMessage(""))
        }
    }
    LaunchedEffect(key1 = isServiceBound) {
        clientLog("LaunchedEffect  $isServiceBound")
        if (!isServiceBound) {
            viewModel.startClientService(context)
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
                serverIp = serverIp,
                serverIpError = serverIpError,
                serverPort = serverPort,
                serverPortError = serverPortError,
                clientMessage = clientMessage,
                serverMessage = serverMessage,
                socketStatus = socketStatus,
                onConnectToServer = {
                    clientLog("onConnectToServer  $isServiceBound")
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
                    if (message.isEmpty())
                        viewModel.emitMessageValue(R.string.message_empty_error)
                    else {
                        onEvent(ClientEvent.SetLoading(true))
                        onEvent(ClientEvent.SendMessageToServer(message))
                    }

                }
            )
        }
    }
}

@Composable
fun ClientContent(
    onEvent: (ClientEvent) -> Unit,
    serverIp: String,
    serverIpError: Boolean,
    serverPort: String,
    serverPortError: Boolean,
    clientMessage: String,
    serverMessage: String,
    socketStatus: Constants.SocketStatus,
    onConnectToServer: () -> Unit,
    onDisconnectFromServer: () -> Unit,
    onSendMessageEvent: (String) -> Unit
) {

    var waitingForServer by remember {
        mutableStateOf(false)
    }
    AppBaseScreen(headerTitle = R.string.client_header, headerBackGround = Indigo, bodyContent = {

        Column(
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                Modifier
                    .fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceAround
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
                    hasError = serverIpError
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
                    hasError = serverPortError
                )
            }

            AppTitleValueText(
                modifier = Modifier.padding(MaterialTheme.spacing.small),
                title = stringResource(
                    id = R.string.connection_status_title
                ),
                value = socketStatus.title,
                valueColor = if (!socketStatus.connection) Color.Red else Green900,
                titleTextType = TextType.TEXT,
                valueTextType = TextType.TEXT
            )

            AppOutlinedTextField(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = MaterialTheme.spacing.small),
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
                    disabledBorderColor = Color.Gray,
                    disabledTextColor = Color.LightGray,
                    cursorColor = Indigo,
                    backgroundColor = Color.White,
                ),
            )

            if(socketStatus.connection && waitingForServer && serverMessage.isEmpty()){
                AppText(
                    modifier = Modifier.padding(MaterialTheme.spacing.small),
                    text = stringResource(R.string.server_confirmation_message),
                    textColor = Color.Red
                )
            }


            if (serverMessage.isNotEmpty()){
                waitingForServer=false
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
            secondButtonTitle = stringResource(id = R.string.send_message),
            secondEnable = socketStatus.connection,
            secondButtonColor = ButtonDefaults.buttonColors(
                disabledBackgroundColor = Color.LightGray,
                backgroundColor = Indigo,
            )
        ) {
            waitingForServer = true
            onSendMessageEvent(clientMessage)
        }

    }

}
