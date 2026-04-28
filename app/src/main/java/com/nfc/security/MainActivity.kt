package com.nfc.security

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.lifecycleScope
import androidx.navigation.compose.rememberNavController
import com.nfc.security.data.datastore.AegisPreferences
import com.nfc.security.domain.repository.FreemiumRepository
import com.nfc.security.service.NfcForegroundDispatchManager
import com.nfc.security.ui.navigation.AppNavGraph
import com.nfc.security.ui.navigation.NavRoutes
import com.nfc.security.ui.nfc.NfcMonitorViewModel
import com.nfc.security.ui.theme.AegisTheme
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    @Inject
    lateinit var nfcDispatchManager: NfcForegroundDispatchManager

    @Inject
    lateinit var freemiumRepository: FreemiumRepository

    @Inject
    lateinit var aegisPreferences: AegisPreferences

    private var nfcMonitorViewModel: NfcMonitorViewModel? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        lifecycleScope.launch { freemiumRepository.initTrialIfNeeded() }

        val onboardingComplete = runBlocking { aegisPreferences.onboardingComplete.first() }
        val startDestination = if (onboardingComplete) NavRoutes.DASHBOARD else NavRoutes.ONBOARDING

        enableEdgeToEdge()
        setContent {
            AegisTheme {
                val navController = rememberNavController()
                nfcMonitorViewModel = hiltViewModel()
                AppNavGraph(navController = navController, startDestination = startDestination)
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
