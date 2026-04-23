package com.nfc.security.ui.dashboard

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.nfc.security.domain.model.FreemiumState
import com.nfc.security.domain.model.VpnState
import com.nfc.security.ui.navigation.NavRoutes
import java.util.concurrent.TimeUnit

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    state: DashboardUiState,
    onNavigate: (String) -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("NFC Security") },
                navigationIcon = {
                    Icon(
                        imageVector = Icons.Default.Security,
                        contentDescription = null,
                        modifier = Modifier.padding(start = 12.dp)
                    )
                },
                actions = {
                    IconButton(onClick = { onNavigate(NavRoutes.SETTINGS) }) {
                        Icon(Icons.Default.Settings, contentDescription = "Settings")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            FreemiumBanner(state.freemiumState)

            StatusCard(
                title = "NFC Monitor",
                value = if (state.nfcEnabled) "Active" else "Inactive",
                valueColor = if (state.nfcEnabled) Color(0xFF4CAF50) else MaterialTheme.colorScheme.error,
                onClick = { onNavigate(NavRoutes.NFC_MONITOR) }
            )

            StatusCard(
                title = "VPN Protection",
                value = when (state.vpnState) {
                    is VpnState.Connected -> "Connected"
                    is VpnState.Connecting -> "Connecting..."
                    is VpnState.Disconnected -> "Disconnected"
                    is VpnState.Error -> "Error"
                },
                valueColor = when (state.vpnState) {
                    is VpnState.Connected -> Color(0xFF4CAF50)
                    is VpnState.Connecting -> Color(0xFFFFC107)
                    else -> MaterialTheme.colorScheme.error
                },
                onClick = { onNavigate(NavRoutes.VPN) }
            )

            SecurityScoreCard(state = state, onClick = { onNavigate(NavRoutes.SECURITY_HEALTH) })

            StatusCard(
                title = "Cleanup & Scan",
                value = "Tap to scan",
                valueColor = MaterialTheme.colorScheme.primary,
                onClick = { onNavigate(NavRoutes.CLEANUP_SCAN) }
            )
        }
    }
}

@Composable
private fun FreemiumBanner(freemiumState: FreemiumState) {
    if (freemiumState is FreemiumState.Trial) {
        val remaining = freemiumState.remainingMs
        val days = TimeUnit.MILLISECONDS.toDays(remaining)
        Card(modifier = Modifier.fillMaxWidth()) {
            Text(
                text = "Trial: $days day(s) remaining",
                modifier = Modifier.padding(12.dp),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun StatusCard(
    title: String,
    value: String,
    valueColor: Color,
    onClick: () -> Unit
) {
    Card(modifier = Modifier.fillMaxWidth(), onClick = onClick) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Medium)
            Text(text = value, style = MaterialTheme.typography.bodyMedium, color = valueColor)
        }
    }
}

@Composable
private fun SecurityScoreCard(state: DashboardUiState, onClick: () -> Unit) {
    Card(modifier = Modifier.fillMaxWidth(), onClick = onClick) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text("Security Health", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Medium)
                Spacer(modifier = Modifier.height(4.dp))
                val score = state.healthScore?.score
                Text(
                    text = if (score != null) "$score / 100" else "Loading...",
                    style = MaterialTheme.typography.bodyMedium,
                    color = when {
                        score == null -> MaterialTheme.colorScheme.onSurfaceVariant
                        score >= 80 -> Color(0xFF4CAF50)
                        score >= 60 -> Color(0xFFFFC107)
                        else -> MaterialTheme.colorScheme.error
                    }
                )
            }
            if (state.isLoading || state.healthScore == null) {
                CircularProgressIndicator(modifier = Modifier.size(32.dp))
            } else {
                CircularProgressIndicator(
                    progress = { state.healthScore.score / 100f },
                    modifier = Modifier.size(48.dp),
                    color = when {
                        state.healthScore.score >= 80 -> Color(0xFF4CAF50)
                        state.healthScore.score >= 60 -> Color(0xFFFFC107)
                        else -> MaterialTheme.colorScheme.error
                    }
                )
            }
        }
    }
}
