package com.NFC.SecureShield.ui.dashboard

import androidx.compose.foundation.Canvas
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.NearMe
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.NFC.SecureShield.R
import com.NFC.SecureShield.domain.model.FreemiumState
import com.NFC.SecureShield.domain.model.VpnState
import com.NFC.SecureShield.ui.components.NFCSecurityCard
import com.NFC.SecureShield.ui.components.NFCSecurityPill
import com.NFC.SecureShield.ui.components.NFCSecurityTopBar
import com.NFC.SecureShield.ui.components.PillTone
import com.NFC.SecureShield.ui.navigation.NavRoutes
import com.NFC.SecureShield.ui.theme.NFCSecurityBg
import com.NFC.SecureShield.ui.theme.NFCSecurityAccent
import com.NFC.SecureShield.ui.theme.NFCSecurityCrit
import com.NFC.SecureShield.ui.theme.NFCSecuritySafe
import com.NFC.SecureShield.ui.theme.NFCSecurityText
import com.NFC.SecureShield.ui.theme.NFCSecurityTextDim
import com.NFC.SecureShield.ui.theme.NFCSecurityType
import com.NFC.SecureShield.ui.theme.NFCSecurityWarn
import java.util.concurrent.TimeUnit

@Composable
fun DashboardScreen(state: DashboardUiState, onNavigate: (String) -> Unit) {
    val overallScore = state.healthScore?.score
    val heroColor = when {
        overallScore == null -> NFCSecurityAccent
        overallScore >= 80 -> NFCSecuritySafe
        overallScore >= 60 -> NFCSecurityWarn
        else -> NFCSecurityCrit
    }
    val heroLabel = when {
        overallScore == null -> stringResource(R.string.status_scanning)
        overallScore >= 80 -> stringResource(R.string.status_secure)
        overallScore >= 60 -> stringResource(R.string.status_at_risk)
        else -> stringResource(R.string.status_critical)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(NFCSecurityBg)
            .padding(horizontal = 16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        NFCSecurityTopBar(
            title = stringResource(R.string.app_name),
            subtitle = stringResource(R.string.dashboard_title),
        )

        HeroStatusCard(
            score = overallScore,
            heroColor = heroColor,
            heroLabel = heroLabel,
        )

        Spacer(modifier = Modifier.height(12.dp))

        Text(stringResource(R.string.module_nfc_title).uppercase(), style = NFCSecurityType.labelSmall, color = NFCSecurityTextDim)
        Spacer(modifier = Modifier.height(8.dp))

        ModuleRow(
            icon = Icons.Default.NearMe,
            title = stringResource(R.string.module_nfc_title),
            statusLabel = if (state.nfcEnabled) stringResource(R.string.status_active) else stringResource(R.string.status_off),
            tone = if (state.nfcEnabled) PillTone.SAFE else PillTone.DEFAULT,
            onClick = { onNavigate(NavRoutes.NFC_SENTINEL) }
        )
        Spacer(modifier = Modifier.height(8.dp))
        ModuleRow(
            icon = Icons.Default.Wifi,
            title = stringResource(R.string.module_vpn_title),
            statusLabel = when (state.vpnState) {
                is VpnState.Connected -> stringResource(R.string.status_connected)
                is VpnState.Connecting -> stringResource(R.string.status_connecting)
                is VpnState.Disconnected -> stringResource(R.string.status_off)
                is VpnState.Error -> stringResource(R.string.status_error)
            },
            tone = when (state.vpnState) {
                is VpnState.Connected -> PillTone.SAFE
                is VpnState.Connecting -> PillTone.WARN
                else -> PillTone.DEFAULT
            },
            onClick = { onNavigate(NavRoutes.TUNNEL) }
        )
        Spacer(modifier = Modifier.height(8.dp))
        ModuleRow(
            icon = Icons.Default.Shield,
            title = stringResource(R.string.module_scan_title),
            statusLabel = stringResource(R.string.status_tap_to_scan),
            tone = PillTone.ACCENT,
            onClick = { onNavigate(NavRoutes.SCAN) }
        )
        Spacer(modifier = Modifier.height(8.dp))
        ModuleRow(
            icon = Icons.Default.Lock,
            title = stringResource(R.string.module_vault_title),
            statusLabel = stringResource(R.string.status_sealed),
            tone = PillTone.DEFAULT,
            onClick = { onNavigate(NavRoutes.VAULT) }
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(stringResource(R.string.quick_actions), style = NFCSecurityType.labelSmall, color = NFCSecurityTextDim)
        Spacer(modifier = Modifier.height(8.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            QuickChip(stringResource(R.string.action_run_scan), Modifier.weight(1f)) { onNavigate(NavRoutes.SCAN) }
            QuickChip(stringResource(R.string.action_tunnel), Modifier.weight(1f)) { onNavigate(NavRoutes.TUNNEL) }
            QuickChip(stringResource(R.string.action_vault), Modifier.weight(1f)) { onNavigate(NavRoutes.VAULT) }
        }

        if (state.sparklineData.isNotEmpty()) {
            Spacer(modifier = Modifier.height(16.dp))
            ThreatSparkline(data = state.sparklineData, color = heroColor)
        }

        if (state.freemiumState is FreemiumState.Trial) {
            Spacer(modifier = Modifier.height(12.dp))
            val days = TimeUnit.MILLISECONDS.toDays((state.freemiumState as FreemiumState.Trial).remainingMs)
            NFCSecurityCard(modifier = Modifier.fillMaxWidth()) {
                Text(
                    stringResource(R.string.trial_remaining, days.toInt()),
                    style = NFCSecurityType.bodySmall,
                    color = NFCSecurityWarn
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        IconButton(onClick = { onNavigate(NavRoutes.SETTINGS) }, modifier = Modifier.align(Alignment.End)) {
            Icon(Icons.Default.Security, contentDescription = "Settings", tint = NFCSecurityTextDim)
        }

        Spacer(modifier = Modifier.height(8.dp))
    }
}

@Composable
private fun HeroStatusCard(score: Int?, heroColor: Color, heroLabel: String) {
    NFCSecurityCard(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(stringResource(R.string.device_status), style = NFCSecurityType.labelSmall, color = NFCSecurityTextDim)
                Spacer(modifier = Modifier.height(4.dp))
                Text(heroLabel, style = NFCSecurityType.headlineLarge, color = heroColor)
                if (score != null) {
                    Text(stringResource(R.string.score_label, score), style = NFCSecurityType.bodySmall, color = NFCSecurityTextDim)
                }
            }
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .background(heroColor.copy(alpha = 0.1f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = if (score != null) "$score" else "--",
                    style = NFCSecurityType.titleLarge,
                    color = heroColor,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@Composable
private fun ModuleRow(
    icon: ImageVector,
    title: String,
    statusLabel: String,
    tone: PillTone,
    onClick: () -> Unit
) {
    NFCSecurityCard(modifier = Modifier.fillMaxWidth(), onClick = onClick) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(imageVector = icon, contentDescription = null, tint = NFCSecurityAccent, modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(12.dp))
                Text(title, style = NFCSecurityType.titleMedium, color = NFCSecurityText)
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                NFCSecurityPill(label = statusLabel, tone = tone)
                Spacer(modifier = Modifier.width(4.dp))
                Icon(Icons.Default.ChevronRight, contentDescription = null, tint = NFCSecurityTextDim, modifier = Modifier.size(16.dp))
            }
        }
    }
}

@Composable
private fun QuickChip(label: String, modifier: Modifier = Modifier, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        modifier = modifier,
        colors = ButtonDefaults.buttonColors(
            containerColor = NFCSecurityAccent.copy(alpha = 0.12f),
            contentColor = NFCSecurityAccent
        )
    ) {
        Text(label, style = NFCSecurityType.labelSmall)
    }
}

@Composable
private fun ThreatSparkline(data: List<Int>, color: Color) {
    Column {
        Text(stringResource(R.string.threat_activity_24h), style = NFCSecurityType.labelSmall, color = NFCSecurityTextDim)
        Spacer(modifier = Modifier.height(8.dp))
        NFCSecurityCard(modifier = Modifier.fillMaxWidth()) {
            Canvas(modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)) {
                if (data.isEmpty()) return@Canvas
                val maxVal = data.max().takeIf { it > 0 } ?: 1
                val stepX = size.width / (data.size - 1).coerceAtLeast(1)
                val path = Path()
                data.forEachIndexed { i, v ->
                    val x = i * stepX
                    val y = size.height - (v.toFloat() / maxVal) * size.height
                    if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
                }
                drawPath(path, color = color, style = Stroke(width = 2.dp.toPx()))
                data.forEachIndexed { i, v ->
                    val x = i * stepX
                    val y = size.height - (v.toFloat() / maxVal) * size.height
                    if (v > 0) drawCircle(color = color, radius = 3.dp.toPx(), center = Offset(x, y))
                }
            }
        }
    }
}
