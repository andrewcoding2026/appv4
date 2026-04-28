package com.nfc.security.ui.scan

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import com.nfc.security.domain.model.MalwareHit
import com.nfc.security.ui.components.AegisCard
import com.nfc.security.ui.components.AegisPill
import com.nfc.security.ui.components.AegisTopBar
import com.nfc.security.ui.components.PillTone
import com.nfc.security.ui.theme.AegisBg
import com.nfc.security.ui.theme.AegisAccent
import com.nfc.security.ui.theme.AegisCrit
import com.nfc.security.ui.theme.AegisSafe
import com.nfc.security.ui.theme.AegisText
import com.nfc.security.ui.theme.AegisTextDim
import com.nfc.security.ui.theme.AegisTextFaint
import com.nfc.security.ui.theme.AegisType
import com.nfc.security.ui.theme.AegisWarn

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
            .background(AegisBg)
            .padding(horizontal = 16.dp)
    ) {
        AegisTopBar(title = "Antimalware", subtitle = "Package & file scan", onBack = onBack)

        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
        ) {
            if (state.isScanning) {
                ScanBeamAnimation()
                Spacer(modifier = Modifier.height(12.dp))
                AegisCard(modifier = Modifier.fillMaxWidth()) {
                    Text("Scanning installed packages and files...", style = AegisType.bodyMedium, color = AegisTextDim)
                }
            } else {
                val report = state.report
                if (report != null) {
                    AegisCard(modifier = Modifier.fillMaxWidth()) {
                        Row(horizontalArrangement = Arrangement.spacedBy(24.dp)) {
                            StatItem("Scanned Apps", "${report.scannedApps}")
                            StatItem("Scanned Files", "${report.scannedFiles}")
                            StatItem("Risk Score", "${report.riskScore}")
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    if (report.hits.isEmpty()) {
                        AegisCard(modifier = Modifier.fillMaxWidth()) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                AegisPill("Clean", PillTone.SAFE)
                                Spacer(modifier = Modifier.width(12.dp))
                                Text("No threats found", style = AegisType.bodyMedium, color = AegisSafe)
                            }
                        }
                    } else {
                        Text("FINDINGS", style = AegisType.labelSmall, color = AegisCrit)
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
                        Text("Tap 'Start Scan' to begin", style = AegisType.bodyMedium, color = AegisTextFaint)
                    }
                }

                if (state.clearedBytes != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    AegisCard(modifier = Modifier.fillMaxWidth(), onClick = onDismissClearedNotice) {
                        Text(
                            "Cache cleared — ${state.clearedBytes / 1024}KB freed",
                            style = AegisType.bodySmall,
                            color = AegisSafe
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
                colors = ButtonDefaults.buttonColors(containerColor = AegisAccent)
            ) {
                Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Start Scan")
            }
            Button(
                onClick = onClearCache,
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(containerColor = AegisWarn.copy(alpha = 0.2f), contentColor = AegisWarn)
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
            color = AegisAccent.copy(alpha = 0.8f),
            start = Offset(0f, y),
            end = Offset(size.width, y),
            strokeWidth = 2.dp.toPx()
        )
        drawLine(
            color = AegisAccent.copy(alpha = 0.2f),
            start = Offset(0f, (y - 20.dp.toPx()).coerceAtLeast(0f)),
            end = Offset(size.width, (y - 20.dp.toPx()).coerceAtLeast(0f)),
            strokeWidth = 8.dp.toPx()
        )
    }
}

@Composable
private fun StatItem(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, style = AegisType.titleLarge, color = AegisText)
        Text(label, style = AegisType.labelSmall, color = AegisTextDim)
    }
}

@Composable
private fun HitRow(hit: MalwareHit) {
    AegisCard(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(hit.packageNameOrPath, style = AegisType.titleMedium, color = AegisText)
                Text(hit.detail, style = AegisType.bodySmall, color = AegisTextDim)
            }
            Spacer(modifier = Modifier.width(8.dp))
            AegisPill(hit.hitType.name, PillTone.CRIT)
        }
    }
}
