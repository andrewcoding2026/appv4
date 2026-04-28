package com.nfc.security.ui.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.NearMe
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.nfc.security.BuildConfig
import com.nfc.security.ui.components.AegisCard
import com.nfc.security.ui.components.AegisTopBar
import com.nfc.security.ui.components.AegisToggleRow
import com.nfc.security.ui.theme.AegisBg
import com.nfc.security.ui.theme.AegisAccent
import com.nfc.security.ui.theme.AegisCrit
import com.nfc.security.ui.theme.AegisText
import com.nfc.security.ui.theme.AegisTextDim
import com.nfc.security.ui.theme.AegisType

@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel,
    onBack: () -> Unit,
    onNavigateToIntegrity: () -> Unit,
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AegisBg)
            .padding(horizontal = 16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        AegisTopBar(title = "Settings", onBack = onBack)

        SettingsSection("MODULES")
        AegisToggleRow("NFC Sentinel", "Monitor NFC field for threats", Icons.Default.NearMe, state.moduleNfc, viewModel::setNfcEnabled)
        Spacer(modifier = Modifier.height(8.dp))
        AegisToggleRow("Tunnel", "Local DNS VPN protection", Icons.Default.Wifi, state.moduleVpn, viewModel::setVpnEnabled)
        Spacer(modifier = Modifier.height(8.dp))
        AegisToggleRow("Antimalware", "Package and file scanning", Icons.Default.Shield, state.moduleScan, viewModel::setScanEnabled)
        Spacer(modifier = Modifier.height(8.dp))
        AegisToggleRow("Vault", "Encrypted secret storage", Icons.Default.Lock, state.moduleVault, viewModel::setVaultEnabled)
        Spacer(modifier = Modifier.height(8.dp))
        AegisToggleRow("Integrity", "Continuous device health checks", Icons.Default.Security, state.moduleIntegrity, viewModel::setIntegrityEnabled)

        Spacer(modifier = Modifier.height(16.dp))
        SettingsSection("UPDATES")
        AegisCard(modifier = Modifier.fillMaxWidth()) {
            Text("Updates delivered via Google Play", style = AegisType.bodySmall, color = AegisTextDim)
        }

        Spacer(modifier = Modifier.height(16.dp))
        SettingsSection("ABOUT")
        AegisCard(modifier = Modifier.fillMaxWidth()) {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                DetailRow("Version", "${BuildConfig.VERSION_NAME} (${BuildConfig.VERSION_CODE})")
                DetailRow("Package", BuildConfig.APPLICATION_ID)
                DetailRow("Encryption", "AES-256-GCM · Android Keystore")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
        SettingsSection("ACTIONS")

        Button(
            onClick = onNavigateToIntegrity,
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = AegisAccent)
        ) {
            Text("Run Integrity Audit Now")
        }

        Spacer(modifier = Modifier.height(8.dp))

        Button(
            onClick = viewModel::clearAll,
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(
                containerColor = AegisCrit.copy(alpha = 0.15f),
                contentColor = AegisCrit
            )
        ) {
            Icon(Icons.Default.Delete, contentDescription = null)
            Spacer(modifier = Modifier.width(4.dp))
            Text("Delete All Data")
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
private fun SettingsSection(title: String) {
    Text(title, style = AegisType.labelSmall, color = AegisTextDim)
    Spacer(modifier = Modifier.height(8.dp))
}

@Composable
private fun DetailRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, style = AegisType.bodySmall, color = AegisTextDim)
        Text(value, style = AegisType.bodySmall, color = AegisText)
    }
}
