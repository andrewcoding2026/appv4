package com.nfc.security.ui.incident

import android.content.Intent
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.nfc.security.ui.components.AegisCard
import com.nfc.security.ui.components.AegisPill
import com.nfc.security.ui.components.AegisTopBar
import com.nfc.security.ui.components.PillTone
import com.nfc.security.ui.theme.AegisBg
import com.nfc.security.ui.theme.AegisCrit
import com.nfc.security.ui.theme.AegisSafe
import com.nfc.security.ui.theme.AegisText
import com.nfc.security.ui.theme.AegisTextDim
import com.nfc.security.ui.theme.AegisType
import com.nfc.security.ui.theme.AegisWarn
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun IncidentDetailScreen(
    eventId: Long,
    viewModel: IncidentDetailViewModel,
    onBack: () -> Unit,
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    LaunchedEffect(eventId) { viewModel.load(eventId) }
    LaunchedEffect(state.deleted) { if (state.deleted) onBack() }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AegisBg)
            .padding(horizontal = 16.dp)
    ) {
        val event = state.event
        AegisTopBar(
            title = event?.module?.uppercase() ?: "Incident",
            subtitle = "Incident Detail",
            onBack = onBack
        )

        if (state.isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = AegisCrit)
            }
            return@Column
        }

        if (event == null) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Event not found", style = AegisType.bodyMedium, color = AegisTextDim)
            }
            return@Column
        }

        val severityColor = when (event.severity) {
            "crit" -> AegisCrit
            "warn" -> AegisWarn
            "safe" -> AegisSafe
            else   -> AegisCrit
        }
        val tone = when (event.severity) {
            "crit" -> PillTone.CRIT
            "warn" -> PillTone.WARN
            "safe" -> PillTone.SAFE
            else   -> PillTone.DEFAULT
        }

        Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
            AegisCard(modifier = Modifier.fillMaxWidth()) {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        AegisPill(label = event.severity.uppercase(), tone = tone)
                        Text(
                            SimpleDateFormat("MMM d, HH:mm:ss", Locale.getDefault()).format(Date(event.createdAt)),
                            style = AegisType.labelSmall,
                            color = AegisTextDim
                        )
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(event.title, style = AegisType.titleLarge, color = severityColor)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(event.body, style = AegisType.bodyMedium, color = AegisText)
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            AegisCard(modifier = Modifier.fillMaxWidth()) {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    DetailRow("Module", event.module)
                    DetailRow("Severity", event.severity)
                    if (!event.payloadJson.isNullOrBlank()) {
                        DetailRow("Payload", event.payloadJson)
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(
                    onClick = {
                        val share = Intent(Intent.ACTION_SEND).apply {
                            type = "text/plain"
                            putExtra(Intent.EXTRA_TEXT, "Aegis Incident\n\n${event.title}\n${event.body}\n\nModule: ${event.module}\nSeverity: ${event.severity}")
                        }
                        context.startActivity(Intent.createChooser(share, "Export Evidence"))
                    },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = AegisCrit.copy(alpha = 0.15f), contentColor = AegisCrit)
                ) {
                    Icon(Icons.Default.Share, contentDescription = null)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Export")
                }
                OutlinedButton(
                    onClick = viewModel::deleteEvent,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.Delete, contentDescription = null)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("False Positive")
                }
            }
        }
    }
}

@Composable
private fun DetailRow(label: String, value: String) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, style = AegisType.labelSmall, color = AegisTextDim)
        Text(value, style = AegisType.bodySmall, color = AegisText, modifier = Modifier.padding(start = 8.dp))
    }
}
