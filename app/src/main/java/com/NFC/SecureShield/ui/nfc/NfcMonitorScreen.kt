package com.NFC.SecureShield.ui.nfc

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.NFC.SecureShield.domain.model.NfcTagInfo
import java.text.SimpleDateFormat
import java.util.Date
import androidx.compose.ui.platform.LocalLocale


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NfcMonitorScreen(
    state: NfcMonitorUiState,
    onBack: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("NFC Monitor") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = if (state.isNfcEnabled)
                            Color(0xFF4CAF50).copy(alpha = 0.1f)
                        else
                            MaterialTheme.colorScheme.errorContainer
                    )
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = if (!state.isNfcSupported) "NFC Not Supported"
                            else if (state.isNfcEnabled) "NFC Active" else "NFC Disabled",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = if (state.isNfcEnabled) Color(0xFF2E7D32) else MaterialTheme.colorScheme.error
                        )
                        if (!state.isNfcEnabled && state.isNfcSupported) {
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Enable NFC in device settings to scan tags",
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                }
            }

            state.lastTag?.let { tag ->
                item {
                    Text("Last Discovered Tag", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                    Spacer(modifier = Modifier.height(8.dp))
                    TagCard(tag = tag)
                }
            }

            if (state.tagHistory.size > 1) {
                item {
                    Text(
                        "History (${state.tagHistory.size - 1} previous)",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold
                    )
                }
                items(state.tagHistory.drop(1)) { tag ->
                    TagCard(tag = tag)
                }
            }

            if (state.tagHistory.isEmpty()) {
                item {
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(24.dp)) {
                            Text("No tags scanned yet", style = MaterialTheme.typography.titleMedium)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                "Hold an NFC tag near the device to read it",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun TagCard(tag: NfcTagInfo) {
    val dateFormat = SimpleDateFormat("HH:mm:ss", LocalLocale.current.platformLocale)
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("ID: ${tag.id}", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(4.dp))
            Text("Type: ${tag.type::class.simpleName}", style = MaterialTheme.typography.bodySmall)
            Text("Discovered: ${dateFormat.format(Date(tag.discoveredAt))}", style = MaterialTheme.typography.bodySmall)
            if (tag.techList.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                FlowRow(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    tag.techList.forEach { tech ->
                        FilterChip(
                            selected = false,
                            onClick = {},
                            label = { Text(tech, style = MaterialTheme.typography.labelSmall) }
                        )
                    }
                }
            }
            tag.ndefRecords.firstOrNull()?.payloadText?.let { text ->
                Spacer(modifier = Modifier.height(8.dp))
                Text("Payload: $text", style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}
