package com.nfc.security.ui.integrity

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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.nfc.security.domain.model.SecurityCheckResult
import com.nfc.security.ui.components.AegisCard
import com.nfc.security.ui.components.AegisDot
import com.nfc.security.ui.components.AegisPill
import com.nfc.security.ui.components.AegisTopBar
import com.nfc.security.ui.components.PillTone
import com.nfc.security.ui.theme.AegisBg
import com.nfc.security.ui.theme.AegisAccent
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
fun IntegrityScreen(viewModel: IntegrityViewModel, onBack: () -> Unit) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val score = state.score
    val scoreValue = score?.score
    val verdict = when {
        scoreValue == null -> "CHECKING"
        scoreValue >= 80 -> "PASS"
        scoreValue >= 60 -> "WARN"
        else -> "FAIL"
    }
    val verdictColor = when (verdict) {
        "PASS" -> AegisSafe
        "WARN" -> AegisWarn
        "FAIL" -> AegisCrit
        else -> AegisAccent
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AegisBg)
            .padding(horizontal = 16.dp)
    ) {
        AegisTopBar(title = "Integrity", subtitle = "Device security daemon", onBack = onBack)

        AegisCard(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text("Verdict", style = AegisType.labelSmall, color = AegisTextDim)
                    if (state.isLoading) {
                        CircularProgressIndicator(modifier = Modifier.size(24.dp), color = AegisAccent)
                    } else {
                        Text(verdict, style = AegisType.headlineLarge, color = verdictColor)
                    }
                    if (score != null) {
                        Text(
                            SimpleDateFormat("MMM d, HH:mm", Locale.getDefault()).format(Date(score.calculatedAt)),
                            style = AegisType.bodySmall,
                            color = AegisTextDim
                        )
                    }
                }
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .background(verdictColor.copy(alpha = 0.1f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (scoreValue != null) "$scoreValue" else "--",
                        style = AegisType.titleLarge,
                        color = verdictColor,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        Button(
            onClick = viewModel::auditNow,
            enabled = !state.isLoading,
            colors = ButtonDefaults.buttonColors(containerColor = AegisAccent),
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(4.dp))
            Text("Audit Now")
        }

        Spacer(modifier = Modifier.height(16.dp))
        Text("CHECKS", style = AegisType.labelSmall, color = AegisTextDim)
        Spacer(modifier = Modifier.height(8.dp))

        if (score != null) {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(score.checks) { check ->
                    CheckRow(check = check)
                }
            }
        } else if (!state.isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Tap 'Audit Now' to run checks", style = AegisType.bodyMedium, color = AegisTextDim)
            }
        }
    }
}

@Composable
private fun CheckRow(check: SecurityCheckResult) {
    val dotColor = when {
        check.passed -> AegisSafe
        check.severity.name == "CRITICAL" -> AegisCrit
        check.severity.name == "HIGH" -> AegisWarn
        else -> AegisTextDim
    }
    val tone = when {
        check.passed -> PillTone.SAFE
        check.severity.name == "CRITICAL" -> PillTone.CRIT
        check.severity.name == "HIGH" -> PillTone.WARN
        else -> PillTone.DEFAULT
    }
    AegisCard(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                AegisDot(color = dotColor)
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(check.checkName, style = AegisType.titleMedium, color = AegisText)
                    if (check.detail.isNotBlank()) {
                        Text(check.detail, style = AegisType.bodySmall, color = AegisTextDim)
                    }
                }
            }
            AegisPill(if (check.passed) "Pass" else "Fail", tone = tone)
        }
    }
}
