package com.nfc.security

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.rememberNavController
import com.nfc.security.domain.repository.FreemiumRepository
import com.nfc.security.service.NfcForegroundDispatchManager
import com.nfc.security.ui.navigation.AppNavGraph
import com.nfc.security.ui.nfc.NfcMonitorViewModel
import com.nfc.security.ui.theme.NFCSecurityTheme
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var nfcDispatchManager: NfcForegroundDispatchManager

    @Inject
    lateinit var freemiumRepository: FreemiumRepository

    private val activityScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    private var nfcMonitorViewModel: NfcMonitorViewModel? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        activityScope.launch { freemiumRepository.initTrialIfNeeded() }
        enableEdgeToEdge()
        setContent {
            NFCSecurityTheme {
                val navController = rememberNavController()
                nfcMonitorViewModel = hiltViewModel()
                AppNavGraph(navController = navController)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        nfcDispatchManager.enableForegroundDispatch(this)
    }

    override fun onPause() {
        super.onPause()
        nfcDispatchManager.disableForegroundDispatch(this)
    }

    override fun onNewIntent(intent: android.content.Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        val tagInfo = nfcDispatchManager.handleIntent(intent)
        tagInfo?.let { nfcMonitorViewModel?.onNewTagDiscovered(it) }
    }
}
