package com.NFC.SecureShield

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.navigation.compose.rememberNavController
import com.NFC.SecureShield.data.datastore.NFCSecurityPreferences
import com.NFC.SecureShield.domain.repository.FreemiumRepository
import com.NFC.SecureShield.service.NfcForegroundDispatchManager
import com.NFC.SecureShield.ui.navigation.AppNavGraph
import com.NFC.SecureShield.ui.navigation.NavRoutes
import com.NFC.SecureShield.ui.nfc.NfcMonitorViewModel
import com.NFC.SecureShield.ui.theme.NFCSecurityTheme
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    @Inject
    lateinit var nfcDispatchManager: NfcForegroundDispatchManager

    @Inject
    lateinit var freemiumRepository: FreemiumRepository

    @Inject
    lateinit var securityPreferences: NFCSecurityPreferences

    private var nfcMonitorViewModel: NfcMonitorViewModel? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        lifecycleScope.launch { freemiumRepository.initTrialIfNeeded() }

        enableEdgeToEdge()
        setContent {
            NFCSecurityTheme {
                val navController = rememberNavController()
                nfcMonitorViewModel = hiltViewModel()
                val startDestination by securityPreferences.onboardingComplete
                    .map { if (it) NavRoutes.DASHBOARD else NavRoutes.ONBOARDING }
                    .collectAsStateWithLifecycle(initialValue = null)
                startDestination?.let {
                    AppNavGraph(navController = navController, startDestination = it)
                }
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
