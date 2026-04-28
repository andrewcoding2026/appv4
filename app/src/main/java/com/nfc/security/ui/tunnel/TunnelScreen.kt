package com.nfc.security.ui.tunnel

import android.app.Activity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.Dns
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.nfc.security.domain.model.VpnState
import com.nfc.security.ui.components.AegisCard
import com.nfc.security.ui.components.AegisTopBar
import com.nfc.security.ui.components.AegisToggleRow
import com.nfc.security.ui.theme.AegisBg
import com.nfc.security.ui.theme.AegisAccent
import com.nfc.security.ui.theme.AegisCrit
import com.nfc.security.ui.theme.AegisSafe
import com.nfc.security.ui.theme.AegisText
import com.nfc.security.ui.theme.AegisTextDim
import com.nfc.security.ui.theme.AegisTextFaint
import com.nfc.security.ui.theme.AegisType
import com.nfc.security.ui.theme.AegisWarn
import com.nfc.security.ui.vpn.VpnEvent

@Composable
fun TunnelScreen(viewModel: TunnelViewModel, onBack: () -> Unit) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    val vpnLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) viewModel.onVpnPermissionGranted()
    }

    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            if (event is VpnEvent.RequestVpnPermission) {
                vpnLauncher.launch(event.intent)
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AegisBg)
            .padding(horizontal = 16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        AegisTopBar(title = "Tunnel", subtitle = "Local VPN", onBack = onBack)

        val statusColor = when (state.vpnState) {
            is VpnState.Connected -> AegisSafe
            is VpnState.Connecting -> AegisWarn
            else -> AegisCrit
        }
        val statusText = when (state.vpnState) {
            is VpnState.Connected -> "CONNECTED"
            is VpnState.Connecting -> "CONNECTING"
            is VpnState.Disconnected -> "OFFLINE"
            is VpnState.Error -> "ERROR"
        }

        AegisCard(modifier = Modifier.fillMaxWidth()) {
            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .background(statusColor.copy(alpha = 0.1f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    if (state.isLoading) {
                        CircularProgressIndicator(color = AegisAccent, modifier = Modifier.size(40.dp))
                    } else {
                        Text(statusText[0].toString(), style = AegisType.headlineLarge, color = statusColor)
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text(statusText, style = AegisType.titleLarge, color = statusColor)

                if (state.vpnState is VpnState.Connected) {
                    val conn = state.vpnState as VpnState.Connected
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(24.dp)) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("↓ ${formatBytes(conn.bytesIn)}", style = AegisType.labelSmall, color = AegisTextDim)
                            Text("IN", style = AegisType.labelSmall, color = AegisTextFaint)
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("↑ ${formatBytes(conn.bytesOut)}", style = AegisType.labelSmall, color = AegisTextDim)
                            Text("OUT", style = AegisType.labelSmall, color = AegisTextFaint)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = {
                        if (state.vpnState is VpnState.Connected) viewModel.onDisconnectClick()
                        else viewModel.onConnectClick()
                    },
                    enabled = !state.isLoading,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (state.vpnState is VpnState.Connected) AegisCrit else AegisAccent
                    )
                ) {
                    Text(if (state.vpnState is VpnState.Connected) "Disconnect" else "Connect")
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        AegisCard(modifier = Modifier.fillMaxWidth()) {
            Text(
                "Aegis runs a local VPN. Your IP address and country do not change.",
                style = AegisType.bodySmall,
                color = AegisTextDim,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        }

        Spacer(modifier = Modifier.height(16.dp))
        Text("FILTERS", style = AegisType.labelSmall, color = AegisTextDim)
        Spacer(modifier = Modifier.height(8.dp))

        AegisToggleRow(
            title = "DNS Filter",
            description = "Block malicious domains via local DNS",
            icon = Icons.Default.Dns,
            checked = state.dnsFilterEnabled,
            onCheckedChange = viewModel::setDnsFilter
        )
        Spacer(modifier = Modifier.height(8.dp))
        AegisToggleRow(
            title = "Tracker Block",
            description = "Block known tracking domains",
            icon = Icons.Default.Block,
            checked = state.trackerBlockEnabled,
            onCheckedChange = viewModel::setTrackerBlock
        )
        Spacer(modifier = Modifier.height(8.dp))
        AegisToggleRow(
            title = "Kill Switch",
            description = "Block traffic when VPN is off",
            icon = Icons.Default.Security,
            checked = state.killSwitchEnabled,
            onCheckedChange = viewModel::setKillSwitch
        )
        Spacer(modifier = Modifier.height(8.dp))
        AegisToggleRow(
            title = "Wi-Fi Guard",
            description = "Auto-connect on untrusted networks",
            icon = Icons.Default.Wifi,
            checked = state.wifiGuardEnabled,
            onCheckedChange = viewModel::setWifiGuard
        )

        if (state.blockedCount > 0) {
            Spacer(modifier = Modifier.height(16.dp))
            AegisCard(modifier = Modifier.fillMaxWidth()) {
                Text(
                    "${state.blockedCount} domains blocked",
                    style = AegisType.titleMedium,
                    color = AegisText
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}

private fun formatBytes(bytes: Long): String = when {
    bytes < 1024 -> "${bytes}B"
    bytes < 1024 * 1024 -> "${bytes / 1024}KB"
    else -> "${bytes / (1024 * 1024)}MB"
}
