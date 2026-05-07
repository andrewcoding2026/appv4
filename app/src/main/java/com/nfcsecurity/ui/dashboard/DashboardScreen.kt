package com.nfcsecurity.ui.dashboard

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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.nfcsecurity.domain.model.FreemiumState
import com.nfcsecurity.domain.model.VpnState
import com.nfcsecurity.ui.components.NFCSecurityCard
import com.nfcsecurity.ui.components.NFCSecurityPill
import com.nfcsecurity.ui.components.NFCSecurityTopBar
import com.nfcsecurity.ui.components.PillTone
import com.nfcsecurity.ui.navigation.NavRoutes
import com.nfcsecurity.ui.theme.NFCSecurityBg
import com.nfcsecurity.ui.theme.NFCSecurityAccent
import com.nfcsecurity.ui.theme.NFCSecurityCrit
import com.nfcsecurity.ui.theme.NFCSecuritySafe
import com.nfcsecurity.ui.theme.NFCSecurityText
import com.nfcsecurity.ui.theme.NFCSecurityTextDim
import com.nfcsecurity.ui.theme.NFCSecurityType
import com.nfcsecurity.ui.theme.NFCSecurityWarn
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
        overallScore == null -> "Scanning..."
        overallScore >= 80 -> "SECURE"
        overallScore >= 60 -> "AT RISK"
        else -> "CRITICAL"
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(NFCSecurityBg)
            .padding(horizontal = 16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        NFCSecurityTopBar(
            title = "NFC Security",
            subtitle = "Security Dashboard",
        )

        HeroStatusCard(
            score = overallScore,
            heroColor = heroColor,
            heroLabel = heroLabel,
        )

        Spacer(modifier = Modifier.height(12.dp))

        Text("MODULES", style = NFCSecurityType.labelSmall, color = NFCSecurityTextDim)
        Spacer(modifier = Modifier.height(8.dp))

        ModuleRow(
            icon = Icons.Default.NearMe,
            title = "NFC Sentinel",
            statusLabel = if (state.nfcEnabled) "Active" else "Off",
            tone = if (state.nfcEnabled) PillTone.SAFE else PillTone.DEFAULT,
            onClick = { onNavigate(NavRoutes.NFC_SENTINEL) }
        )
        Spacer(modifier = Modifier.height(8.dp))
        ModuleRow(
            icon = Icons.Default.Wifi,
            title = "Tunnel",
            statusLabel = when (state.vpnState) {
                is VpnState.Connected -> "Connected"
                is VpnState.Connecting -> "Connecting"
                is VpnState.Disconnected -> "Off"
                is VpnState.Error -> "Error"
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
            title = "Antimalware",
            statusLabel = "Tap to scan",
            tone = PillTone.ACCENT,
            onClick = { onNavigate(NavRoutes.SCAN) }
        )
        Spacer(modifier = Modifier.height(8.dp))
        ModuleRow(
            icon = Icons.Default.Lock,
            title = "Vault",
            statusLabel = "Sealed",
            tone = PillTone.DEFAULT,
            onClick = { onNavigate(NavRoutes.VAULT) }
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text("QUICK ACTIONS", style = NFCSecurityType.labelSmall, color = NFCSecurityTextDim)
        Spacer(modifier = Modifier.height(8.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            QuickChip("Run Scan", Modifier.weight(1f)) { onNavigate(NavRoutes.SCAN) }
            QuickChip("Tunnel", Modifier.weight(1f)) { onNavigate(NavRoutes.TUNNEL) }
            QuickChip("Vault", Modifier.weight(1f)) { onNavigate(NavRoutes.VAULT) }
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
                    "Trial · $days day(s) remaining",
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
                Text("Device Status", style = NFCSecurityType.labelSmall, color = NFCSecurityTextDim)
                Spacer(modifier = Modifier.height(4.dp))
                Text(heroLabel, style = NFCSecurityType.headlineLarge, color = heroColor)
                if (score != null) {
                    Text("Score $score / 100", style = NFCSecurityType.bodySmall, color = NFCSecurityTextDim)
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
        Text("THREAT ACTIVITY · 24H", style = NFCSecurityType.labelSmall, color = NFCSecurityTextDim)
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
