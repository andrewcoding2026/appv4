package com.nfcsecurity.ui.vpn

import android.app.Activity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.nfcsecurity.domain.model.VpnState
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.collectLatest

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VpnScreen(
    state: VpnUiState,
    events: SharedFlow<VpnEvent>,
    onConnect: () -> Unit,
    onDisconnect: () -> Unit,
    onPermissionGranted: () -> Unit,
    onBack: () -> Unit
) {
    val vpnPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            onPermissionGranted()
        }
    }

    LaunchedEffect(Unit) {
        events.collectLatest { event ->
            when (event) {
                is VpnEvent.RequestVpnPermission -> vpnPermissionLauncher.launch(event.intent)
                is VpnEvent.ShowError -> { /* handled by state */ }
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("VPN Protection") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            VpnStatusIndicator(state.vpnState)

            if (state.vpnState is VpnState.Connected) {
                VpnStatsCard(state.vpnState)
            }

            Spacer(modifier = Modifier.height(8.dp))

            when (state.vpnState) {
                is VpnState.Disconnected, is VpnState.Error -> {
                    Button(
                        onClick = onConnect,
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !state.isLoading
                    ) {
                        if (state.isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(18.dp).padding(end = 4.dp),
                                color = MaterialTheme.colorScheme.onPrimary,
                                strokeWidth = 2.dp
                            )
                        }
                        Text("Connect")
                    }
                }
                is VpnState.Connected -> {
                    OutlinedButton(
                        onClick = onDisconnect,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Disconnect")
                    }
                }
                is VpnState.Connecting -> {
                    OutlinedButton(
                        onClick = onDisconnect,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(18.dp).padding(end = 8.dp),
                            strokeWidth = 2.dp
                        )
                        Text("Connecting... (Cancel)")
                    }
                }
            }
        }
    }
}

@Composable
private fun VpnStatusIndicator(vpnState: VpnState) {
    val (statusText, color) = when (vpnState) {
        is VpnState.Connected -> "Protected" to Color(0xFF4CAF50)
        is VpnState.Connecting -> "Connecting..." to Color(0xFFFFC107)
        is VpnState.Disconnected -> "Unprotected" to MaterialTheme.colorScheme.error
        is VpnState.Error -> "Error: ${vpnState.message}" to MaterialTheme.colorScheme.error
    }
    Text(
        text = statusText,
        style = MaterialTheme.typography.headlineMedium,
        fontWeight = FontWeight.Bold,
        color = color
    )
}

@Composable
private fun VpnStatsCard(state: VpnState.Connected) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("Connection Details", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
            StatRow("Server", state.serverIp)
            StatRow("Bytes In", formatBytes(state.bytesIn))
            StatRow("Bytes Out", formatBytes(state.bytesOut))
        }
    }
}

@Composable
private fun StatRow(label: String, value: String) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(value, style = MaterialTheme.typography.bodySmall)
    }
}

private fun formatBytes(bytes: Long): String = when {
    bytes < 1024 -> "$bytes B"
    bytes < 1024 * 1024 -> "${bytes / 1024} KB"
    else -> "${bytes / (1024 * 1024)} MB"
}
