package ir.example.androidsocket.utils

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.constraintlayout.compose.ChainStyle
import androidx.constraintlayout.compose.ConstrainScope
import androidx.constraintlayout.compose.ConstraintLayout
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import ir.example.androidsocket.ui.base.AppHeader
import ir.example.androidsocket.ui.theme.Indigo
import ir.example.androidsocket.ui.theme.spacing

@Composable
fun AppBaseScreen(
    headerTitle: Int,
    headerBackGround: Color,
    bodyContent: @Composable () -> Unit,
    footerContent: @Composable () -> Unit
) {

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
                .constrainAs(header) {
                    top.linkTo(parent.top)
                    start.linkTo(parent.start)
                    end.linkTo(parent.end)
                    bottom.linkTo(body.top)
                },
            headerTitle = headerTitle,
            headerBackground = headerBackGround
        )

        Box(modifier = Modifier
            .padding(MaterialTheme.spacing.extraMedium)
            .constrainAs(body) {
                top.linkTo(header.bottom)
                start.linkTo(parent.start)
                end.linkTo(parent.end)
                bottom.linkTo(footer.top)
            }) {
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