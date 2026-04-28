package com.nfc.security.ui.nfc

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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import com.nfc.security.domain.model.NfcTagInfo
import com.nfc.security.ui.components.AegisCard
import com.nfc.security.ui.components.AegisPill
import com.nfc.security.ui.components.AegisTopBar
import com.nfc.security.ui.components.PillTone
import com.nfc.security.ui.theme.AegisBg
import com.nfc.security.ui.theme.AegisAccent
import com.nfc.security.ui.theme.AegisSafe
import com.nfc.security.ui.theme.AegisText
import com.nfc.security.ui.theme.AegisTextDim
import com.nfc.security.ui.theme.AegisTextFaint
import com.nfc.security.ui.theme.AegisType
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun NfcSentinelScreen(state: NfcMonitorUiState, onBack: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AegisBg)
            .padding(horizontal = 16.dp)
    ) {
        AegisTopBar(title = "NFC Sentinel", subtitle = "Real-time monitoring", onBack = onBack)

        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
            Spacer(modifier = Modifier.height(16.dp))
            PulsingRings(active = state.isNfcEnabled)
            Spacer(modifier = Modifier.height(16.dp))
            AegisPill(
                label = when {
                    !state.isNfcEnabled -> "NFC Disabled"
                    state.lastTag != null -> "Tag Detected"
                    else -> "Scanning"
                },
                tone = when {
                    !state.isNfcEnabled -> PillTone.CRIT
                    state.lastTag != null -> PillTone.WARN
                    else -> PillTone.SAFE
                }
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = if (state.isNfcEnabled) "Monitoring NFC field" else "Enable NFC in device settings",
                style = AegisType.bodySmall,
                color = AegisTextDim
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        if (state.tagHistory.isNotEmpty()) {
            Text("RECENT TAGS", style = AegisType.labelSmall, color = AegisTextDim)
            Spacer(modifier = Modifier.height(8.dp))
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(state.tagHistory) { tag ->
                    TagRow(tag = tag)
                }
            }
        } else {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("No tags discovered yet", style = AegisType.bodyMedium, color = AegisTextFaint)
            }
        }
    }
}

@Composable
private fun PulsingRings(active: Boolean) {
    val transition = rememberInfiniteTransition(label = "pulse")
    val scale by transition.animateFloat(
        initialValue = 0.4f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1800, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "scale"
    )
    val alpha by transition.animateFloat(
        initialValue = 0.6f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1800, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "alpha"
    )

    Box(
        modifier = Modifier.size(160.dp),
        contentAlignment = Alignment.Center
    ) {
        val ringColor = if (active) AegisSafe else AegisTextFaint
        Canvas(modifier = Modifier.size(160.dp)) {
            for (i in 1..3) {
                val radius = (size.minDimension / 2) * scale * (i / 3f)
                drawCircle(
                    color = ringColor.copy(alpha = alpha / i),
                    radius = radius,
                    style = Stroke(width = 1.5.dp.toPx())
                )
            }
            drawCircle(color = if (active) AegisSafe else AegisTextFaint, radius = 24.dp.toPx())
        }
    }
}

@Composable
private fun TagRow(tag: NfcTagInfo) {
    AegisCard(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = tag.techList.firstOrNull() ?: "Unknown",
                    style = AegisType.titleMedium,
                    color = AegisText
                )
                Text(
                    text = tag.ndefRecords.firstOrNull()?.payloadText ?: "ID: ${tag.id}",
                    style = AegisType.bodySmall,
                    color = AegisTextDim,
                    maxLines = 1
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Column(horizontalAlignment = Alignment.End) {
                AegisPill(label = tag.type::class.simpleName ?: "Tag", tone = PillTone.ACCENT)
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date(tag.discoveredAt)),
                    style = AegisType.labelSmall,
                    color = AegisTextFaint
                )
            }
        }
    }
}
