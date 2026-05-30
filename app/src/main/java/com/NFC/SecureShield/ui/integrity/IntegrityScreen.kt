package com.NFC.SecureShield.ui.integrity

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
import com.NFC.SecureShield.domain.model.SecurityCheckResult
import com.NFC.SecureShield.ui.components.NFCSecurityCard
import com.NFC.SecureShield.ui.components.NFCSecurityDot
import com.NFC.SecureShield.ui.components.NFCSecurityPill
import com.NFC.SecureShield.ui.components.NFCSecurityTopBar
import com.NFC.SecureShield.ui.components.PillTone
import com.NFC.SecureShield.ui.theme.NFCSecurityBg
import com.NFC.SecureShield.ui.theme.NFCSecurityAccent
import com.NFC.SecureShield.ui.theme.NFCSecurityCrit
import com.NFC.SecureShield.ui.theme.NFCSecuritySafe
import com.NFC.SecureShield.ui.theme.NFCSecurityText
import com.NFC.SecureShield.ui.theme.NFCSecurityTextDim
import com.NFC.SecureShield.ui.theme.NFCSecurityType
import com.NFC.SecureShield.ui.theme.NFCSecurityWarn
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
        "PASS" -> NFCSecuritySafe
        "WARN" -> NFCSecurityWarn
        "FAIL" -> NFCSecurityCrit
        else -> NFCSecurityAccent
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(NFCSecurityBg)
            .padding(horizontal = 16.dp)
    ) {
        NFCSecurityTopBar(title = "Integrity", subtitle = "Device security daemon", onBack = onBack)

        NFCSecurityCard(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text("Verdict", style = NFCSecurityType.labelSmall, color = NFCSecurityTextDim)
                    if (state.isLoading) {
                        CircularProgressIndicator(modifier = Modifier.size(24.dp), color = NFCSecurityAccent)
                    } else {
                        Text(verdict, style = NFCSecurityType.headlineLarge, color = verdictColor)
                    }
                    if (score != null) {
                        Text(
                            SimpleDateFormat("MMM d, HH:mm", Locale.getDefault()).format(Date(score.calculatedAt)),
                            style = NFCSecurityType.bodySmall,
                            color = NFCSecurityTextDim
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
                        style = NFCSecurityType.titleLarge,
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
            colors = ButtonDefaults.buttonColors(containerColor = NFCSecurityAccent),
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(4.dp))
            Text("Audit Now")
        }

        Spacer(modifier = Modifier.height(16.dp))
        Text("CHECKS", style = NFCSecurityType.labelSmall, color = NFCSecurityTextDim)
        Spacer(modifier = Modifier.height(8.dp))

        if (score != null) {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(score.checks) { check ->
                    CheckRow(check = check)
                }
            }
        } else if (!state.isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Tap 'Audit Now' to run checks", style = NFCSecurityType.bodyMedium, color = NFCSecurityTextDim)
            }
        }
    }
}

@Composable
private fun CheckRow(check: SecurityCheckResult) {
    val dotColor = when {
        check.passed -> NFCSecuritySafe
        check.severity.name == "CRITICAL" -> NFCSecurityCrit
        check.severity.name == "HIGH" -> NFCSecurityWarn
        else -> NFCSecurityTextDim
    }
    val tone = when {
        check.passed -> PillTone.SAFE
        check.severity.name == "CRITICAL" -> PillTone.CRIT
        check.severity.name == "HIGH" -> PillTone.WARN
        else -> PillTone.DEFAULT
    }
    NFCSecurityCard(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                NFCSecurityDot(color = dotColor)
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(check.checkName, style = NFCSecurityType.titleMedium, color = NFCSecurityText)
                    if (check.detail.isNotBlank()) {
                        Text(check.detail, style = NFCSecurityType.bodySmall, color = NFCSecurityTextDim)
                    }
                }
            }
            NFCSecurityPill(if (check.passed) "Pass" else "Fail", tone = tone)
        }
    }
}
