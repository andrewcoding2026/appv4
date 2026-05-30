package com.NFC.SecureShield.ui.nfc

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
import com.NFC.SecureShield.domain.model.NfcTagInfo
import com.NFC.SecureShield.ui.components.NFCSecurityCard
import com.NFC.SecureShield.ui.components.NFCSecurityPill
import com.NFC.SecureShield.ui.components.NFCSecurityTopBar
import com.NFC.SecureShield.ui.components.PillTone
import com.NFC.SecureShield.ui.theme.NFCSecurityBg
import com.NFC.SecureShield.ui.theme.NFCSecuritySafe
import com.NFC.SecureShield.ui.theme.NFCSecurityText
import com.NFC.SecureShield.ui.theme.NFCSecurityTextDim
import com.NFC.SecureShield.ui.theme.NFCSecurityTextFaint
import com.NFC.SecureShield.ui.theme.NFCSecurityType
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun NfcSentinelScreen(state: NfcMonitorUiState, onBack: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(NFCSecurityBg)
            .padding(horizontal = 16.dp)
    ) {
        NFCSecurityTopBar(title = "NFC Sentinel", subtitle = "Real-time monitoring", onBack = onBack)

        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
            Spacer(modifier = Modifier.height(16.dp))
            PulsingRings(active = state.isNfcEnabled)
            Spacer(modifier = Modifier.height(16.dp))
            NFCSecurityPill(
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
                style = NFCSecurityType.bodySmall,
                color = NFCSecurityTextDim
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        if (state.tagHistory.isNotEmpty()) {
            Text("RECENT TAGS", style = NFCSecurityType.labelSmall, color = NFCSecurityTextDim)
            Spacer(modifier = Modifier.height(8.dp))
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(state.tagHistory) { tag ->
                    TagRow(tag = tag)
                }
            }
        } else {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("No tags discovered yet", style = NFCSecurityType.bodyMedium, color = NFCSecurityTextFaint)
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
        val ringColor = if (active) NFCSecuritySafe else NFCSecurityTextFaint
        Canvas(modifier = Modifier.size(160.dp)) {
            for (i in 1..3) {
                val radius = (size.minDimension / 2) * scale * (i / 3f)
                drawCircle(
                    color = ringColor.copy(alpha = alpha / i),
                    radius = radius,
                    style = Stroke(width = 1.5.dp.toPx())
                )
            }
            drawCircle(color = if (active) NFCSecuritySafe else NFCSecurityTextFaint, radius = 24.dp.toPx())
        }
    }
}

@Composable
private fun TagRow(tag: NfcTagInfo) {
    NFCSecurityCard(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = tag.techList.firstOrNull() ?: "Unknown",
                    style = NFCSecurityType.titleMedium,
                    color = NFCSecurityText
                )
                Text(
                    text = tag.ndefRecords.firstOrNull()?.payloadText ?: "ID: ${tag.id}",
                    style = NFCSecurityType.bodySmall,
                    color = NFCSecurityTextDim,
                    maxLines = 1
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Column(horizontalAlignment = Alignment.End) {
                NFCSecurityPill(label = tag.type::class.simpleName ?: "Tag", tone = PillTone.ACCENT)
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date(tag.discoveredAt)),
                    style = NFCSecurityType.labelSmall,
                    color = NFCSecurityTextFaint
                )
            }
        }
    }
}
