package ir.example.androidsocket.utils

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.constraintlayout.compose.ChainStyle
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.Dimension
import androidx.hilt.navigation.compose.hiltViewModel
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import ir.example.androidsocket.Constants
import ir.example.androidsocket.Constants.PROTOCOLS
import ir.example.androidsocket.ui.base.AppHeader
import ir.example.androidsocket.ui.base.BaseViewModel
import ir.example.androidsocket.ui.base.ProtocolTypeMenu
import ir.example.androidsocket.ui.theme.Indigo
import ir.example.androidsocket.ui.theme.spacing

@Composable
fun AppBaseScreen(
    headerTitle: String,
    headerBackGround: Color,
    onProtocolSelected : (String)->Unit,
    bodyContent: @Composable () -> Unit,
    footerContent: @Composable () -> Unit
) {

    var expanded by remember { mutableStateOf(false) }

    val systemUiController = rememberSystemUiController()

    SideEffect {
        systemUiController.setSystemBarsColor(
            color = Indigo,
        )
    }
    ConstraintLayout(
        Modifier
            .fillMaxSize()
            .background(Color.White),
    ) {

        val (header, body, footer) = createRefs()
        createVerticalChain(header, body, footer, chainStyle = ChainStyle.SpreadInside)

        AppHeader(
            modifier = Modifier
                .background(Color.Green)
                .constrainAs(header) {
                    top.linkTo(parent.top)
                    start.linkTo(parent.start)
                    end.linkTo(parent.end)
                    bottom.linkTo(body.top)
                },
            onMenuClick = {expanded = !expanded},
            headerTitle = headerTitle ,
            headerBackground = headerBackGround
        )

        Box(modifier = Modifier
            .constrainAs(body) {
                top.linkTo(header.bottom)
                start.linkTo(parent.start)
                end.linkTo(parent.end)
                bottom.linkTo(footer.top)
                width = Dimension.fillToConstraints
                height = Dimension.fillToConstraints
            },) {
            ProtocolTypeMenu(
                modifier = Modifier.align(Alignment.TopEnd),
                expanded = expanded,
                protocols = PROTOCOLS,
                selectedProtocol = Constants.ProtocolType.WEBSOCKET,
                onProtocolSelected = {
                    onProtocolSelected(it)
                    expanded = false
                },
                onDismissClicked = { expanded = false })
            bodyContent()
        }

        Box(
            modifier = Modifier.constrainAs(footer) {
                start.linkTo(parent.start)
                end.linkTo(parent.end)
                bottom.linkTo(parent.bottom)
            },
        ) {
            footerContent()
        }

    }
}