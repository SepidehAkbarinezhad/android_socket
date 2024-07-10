package ir.example.androidsocket.ui.client

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.lifecycle.lifecycleScope
import dagger.hilt.android.AndroidEntryPoint
import ir.example.androidsocket.utils.clientLog
import kotlinx.coroutines.launch


@AndroidEntryPoint
class ClientActivity : ComponentActivity() {

    private val viewModel: ClientViewModel by viewModels()

    @SuppressLint("SuspiciousIndentation")
    override fun onCreate(savedInstanceState: Bundle?) {
        clientLog("ClientActivity onCreate()")
        super.onCreate(savedInstanceState)
        setContent {
            ClientCompose(viewModel = viewModel){event->viewModel.onEvent(event)}
        }
    }

    override fun onStart() {
        clientLog("ClientActivity onStart()")
        super.onStart()
    }

    override fun onStop() {
        clientLog("ClientActivity onStop()")
        super.onStop()
    }


    override fun onDestroy() {
        clientLog("mainActivity onDestroy ")
        lifecycleScope.launch {
            viewModel.performCleanup()
        }
        super.onDestroy()

    }


}

