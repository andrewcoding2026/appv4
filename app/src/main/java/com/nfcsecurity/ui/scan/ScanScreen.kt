package com.nfcsecurity.ui.scan

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.unit.dp
import com.nfcsecurity.domain.model.MalwareHit
import com.nfcsecurity.ui.components.NFCSecurityCard
import com.nfcsecurity.ui.components.NFCSecurityPill
import com.nfcsecurity.ui.components.NFCSecurityTopBar
import com.nfcsecurity.ui.components.PillTone
import com.nfcsecurity.ui.theme.NFCSecurityBg
import com.nfcsecurity.ui.theme.NFCSecurityAccent
import com.nfcsecurity.ui.theme.NFCSecurityCrit
import com.nfcsecurity.ui.theme.NFCSecuritySafe
import com.nfcsecurity.ui.theme.NFCSecurityText
import com.nfcsecurity.ui.theme.NFCSecurityTextDim
import com.nfcsecurity.ui.theme.NFCSecurityTextFaint
import com.nfcsecurity.ui.theme.NFCSecurityType
import com.nfcsecurity.ui.theme.NFCSecurityWarn

@Composable
fun ScanScreen(
    state: ScanUiState,
    onStartScan: () -> Unit,
    onClearCache: () -> Unit,
    onDismissClearedNotice: () -> Unit,
    onBack: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(NFCSecurityBg)
            .padding(horizontal = 16.dp)
    ) {
        NFCSecurityTopBar(title = "Antimalware", subtitle = "Package & file scan", onBack = onBack)

        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
        ) {
            if (state.isScanning) {
                ScanBeamAnimation()
                Spacer(modifier = Modifier.height(12.dp))
                NFCSecurityCard(modifier = Modifier.fillMaxWidth()) {
                    Text("Scanning installed packages and files...", style = NFCSecurityType.bodyMedium, color = NFCSecurityTextDim)
                }
            } else {
                val report = state.report
                if (report != null) {
                    NFCSecurityCard(modifier = Modifier.fillMaxWidth()) {
                        Row(horizontalArrangement = Arrangement.spacedBy(24.dp)) {
                            StatItem("Scanned Apps", "${report.scannedApps}")
                            StatItem("Scanned Files", "${report.scannedFiles}")
                            StatItem("Risk Score", "${report.riskScore}")
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    if (report.hits.isEmpty()) {
                        NFCSecurityCard(modifier = Modifier.fillMaxWidth()) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                NFCSecurityPill("Clean", PillTone.SAFE)
                                Spacer(modifier = Modifier.width(12.dp))
                                Text("No threats found", style = NFCSecurityType.bodyMedium, color = NFCSecuritySafe)
                            }
                        }
                    } else {
                        Text("FINDINGS", style = NFCSecurityType.labelSmall, color = NFCSecurityCrit)
                        Spacer(modifier = Modifier.height(8.dp))
                        report.hits.forEach { hit ->
                            HitRow(hit = hit)
                            Spacer(modifier = Modifier.height(8.dp))
                        }
                    }
                } else {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(160.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("Tap 'Start Scan' to begin", style = NFCSecurityType.bodyMedium, color = NFCSecurityTextFaint)
                    }
                }

                if (state.clearedBytes != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    NFCSecurityCard(modifier = Modifier.fillMaxWidth(), onClick = onDismissClearedNotice) {
                        Text(
                            "Cache cleared — ${state.clearedBytes / 1024}KB freed",
                            style = NFCSecurityType.bodySmall,
                            color = NFCSecuritySafe
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
            Button(
                onClick = onStartScan,
                enabled = !state.isScanning,
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(containerColor = NFCSecurityAccent)
            ) {
                Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Start Scan")
            }
            Button(
                onClick = onClearCache,
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(containerColor = NFCSecurityWarn.copy(alpha = 0.2f), contentColor = NFCSecurityWarn)
            ) {
                Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Clear Cache")
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
    }
}

@Composable
private fun ScanBeamAnimation() {
    val transition = rememberInfiniteTransition(label = "beam")
    val progress by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            tween(1200, easing = LinearEasing),
            RepeatMode.Restart
        ),
        label = "beam_pos"
    )
    Canvas(modifier = Modifier
        .fillMaxWidth()
        .height(80.dp)) {
        val y = progress * size.height
        drawLine(
            color = NFCSecurityAccent.copy(alpha = 0.8f),
            start = Offset(0f, y),
            end = Offset(size.width, y),
            strokeWidth = 2.dp.toPx()
        )
        drawLine(
            color = NFCSecurityAccent.copy(alpha = 0.2f),
            start = Offset(0f, (y - 20.dp.toPx()).coerceAtLeast(0f)),
            end = Offset(size.width, (y - 20.dp.toPx()).coerceAtLeast(0f)),
            strokeWidth = 8.dp.toPx()
        )
    }
}

@Composable
private fun StatItem(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, style = NFCSecurityType.titleLarge, color = NFCSecurityText)
        Text(label, style = NFCSecurityType.labelSmall, color = NFCSecurityTextDim)
    }
}

@Composable
private fun HitRow(hit: MalwareHit) {
    NFCSecurityCard(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(hit.packageNameOrPath, style = NFCSecurityType.titleMedium, color = NFCSecurityText)
                Text(hit.detail, style = NFCSecurityType.bodySmall, color = NFCSecurityTextDim)
            }
            Spacer(modifier = Modifier.width(8.dp))
            NFCSecurityPill(hit.hitType.name, PillTone.CRIT)
        }
    }
}
