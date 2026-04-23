package com.nfc.security.ui.scan

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.BugReport
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.nfc.security.domain.model.MalwareHit

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CleanupScanScreen(
    state: ScanUiState,
    onStartScan: () -> Unit,
    onClearCache: () -> Unit,
    onDismissClearedNotice: () -> Unit,
    onBack: () -> Unit
) {
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(state.clearedBytes) {
        state.clearedBytes?.let { bytes ->
            val text = when {
                bytes < 1024 -> "$bytes B cleared"
                bytes < 1024 * 1024 -> "${bytes / 1024} KB cleared"
                else -> "${bytes / (1024 * 1024)} MB cleared"
            }
            snackbarHostState.showSnackbar(text)
            onDismissClearedNotice()
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("Cleanup & Scan") },
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
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = onStartScan,
                        modifier = Modifier.weight(1f),
                        enabled = !state.isScanning
                    ) {
                        if (state.isScanning) {
                            CircularProgressIndicator(
                                modifier = Modifier.padding(end = 8.dp),
                                color = MaterialTheme.colorScheme.onPrimary,
                                strokeWidth = 2.dp
                            )
                        }
                        Text(if (state.isScanning) "Scanning..." else "Start Scan")
                    }
                    OutlinedButton(
                        onClick = onClearCache,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Clear Cache")
                    }
                }
            }

            state.report?.let { report ->
                item {
                    ScanSummaryCard(report)
                }
                if (report.hits.isEmpty()) {
                    item {
                        Card(modifier = Modifier.fillMaxWidth()) {
                            Row(
                                modifier = Modifier.padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Icon(
                                    Icons.Default.CheckCircle,
                                    contentDescription = null,
                                    tint = Color(0xFF4CAF50)
                                )
                                Text("No threats detected", style = MaterialTheme.typography.bodyMedium)
                            }
                        }
                    }
                } else {
                    items(report.hits) { hit ->
                        MalwareHitCard(hit)
                    }
                }
            }

            state.error?.let { error ->
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = androidx.compose.material3.CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer
                        )
                    ) {
                        Text(
                            text = "Error: $error",
                            modifier = Modifier.padding(16.dp),
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ScanSummaryCard(report: com.nfc.security.domain.model.ScanReport) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text("Scan Results", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Text("Apps scanned: ${report.scannedApps}", style = MaterialTheme.typography.bodySmall)
            Text("Files scanned: ${report.scannedFiles}", style = MaterialTheme.typography.bodySmall)
            Text(
                "Risk score: ${report.riskScore}",
                style = MaterialTheme.typography.bodySmall,
                color = when {
                    report.riskScore == 0 -> Color(0xFF4CAF50)
                    report.riskScore < 30 -> Color(0xFFFFC107)
                    else -> MaterialTheme.colorScheme.error
                }
            )
        }
    }
}

@Composable
private fun MalwareHitCard(hit: MalwareHit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = androidx.compose.material3.CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.errorContainer
        )
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(
                Icons.Default.BugReport,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.error
            )
            Column {
                Text(
                    hit.packageNameOrPath.substringAfterLast('/'),
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onErrorContainer
                )
                Text(
                    hit.detail,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onErrorContainer.copy(alpha = 0.7f)
                )
            }
        }
    }
}
