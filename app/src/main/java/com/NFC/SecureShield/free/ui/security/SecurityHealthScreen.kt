package com.NFC.SecureShield.free.ui.security

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
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
import com.NFC.SecureShield.free.domain.model.SecurityCheckResult
import com.NFC.SecureShield.free.domain.model.SecurityCheckResult.Severity


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SecurityHealthScreen(
    state: SecurityHealthUiState,
    onRefresh: () -> Unit,
    onBack: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Security Health") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    TextButton(onClick = onRefresh, enabled = !state.isLoading) {
                        Text("Refresh")
                    }
                }
            )
        }
    ) { padding ->
        if (state.isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
            return@Scaffold
        }
        val score = state.score
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                ScoreHeader(score = score?.score)
            }
            if (score != null) {
                items(score.checks) { check ->
                    SecurityCheckRow(check = check)
                }
            }
        }
    }
}

@Composable
private fun ScoreHeader(score: Int?) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            val color = when {
                score == null -> MaterialTheme.colorScheme.onSurfaceVariant
                score >= 80 -> Color(0xFF4CAF50)
                score >= 60 -> Color(0xFFFFC107)
                else -> MaterialTheme.colorScheme.error
            }
            if (score != null) {
                Box(contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(
                        progress = { score / 100f },
                        modifier = Modifier.size(96.dp),
                        color = color,
                        trackColor = color.copy(alpha = 0.2f),
                        strokeWidth = 8.dp
                    )
                    Text(
                        text = "$score",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = color
                    )
                }
            } else {
                CircularProgressIndicator(modifier = Modifier.size(96.dp))
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = when {
                    score == null -> "Calculating..."
                    score >= 80 -> "Good"
                    score >= 60 -> "Needs Attention"
                    else -> "At Risk"
                },
                style = MaterialTheme.typography.titleMedium,
                color = color
            )
        }
    }
}

@Composable
private fun SecurityCheckRow(check: SecurityCheckResult) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(
                imageVector = if (check.passed) Icons.Default.CheckCircle else Icons.Default.Warning,
                contentDescription = null,
                tint = if (check.passed) Color(0xFF4CAF50) else when (check.severity) {
                    Severity.CRITICAL -> MaterialTheme.colorScheme.error
                    Severity.HIGH -> Color(0xFFFF5722)
                    Severity.MEDIUM -> Color(0xFFFFC107)
                    Severity.LOW -> Color(0xFF9E9E9E)
                },
                modifier = Modifier.size(24.dp)
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(check.checkName, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
                Text(
                    check.detail,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            AssistChip(
                onClick = {},
                label = { Text(check.severity.name, style = MaterialTheme.typography.labelSmall) },
                colors = AssistChipDefaults.assistChipColors(
                    containerColor = when (check.severity) {
                        Severity.CRITICAL -> MaterialTheme.colorScheme.errorContainer
                        Severity.HIGH -> Color(0xFFFF5722).copy(alpha = 0.15f)
                        Severity.MEDIUM -> Color(0xFFFFC107).copy(alpha = 0.15f)
                        Severity.LOW -> MaterialTheme.colorScheme.surfaceVariant
                    }
                )
            )
        }
    }
}
