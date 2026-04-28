package com.nfc.security.ui.freemium

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.nfc.security.domain.model.FreemiumState
import com.nfc.security.ui.components.AegisCard
import com.nfc.security.ui.components.AegisPill
import com.nfc.security.ui.components.AegisTopBar
import com.nfc.security.ui.components.PillTone
import com.nfc.security.ui.theme.AegisBg
import com.nfc.security.ui.theme.AegisAccent
import com.nfc.security.ui.theme.AegisSafe
import com.nfc.security.ui.theme.AegisText
import com.nfc.security.ui.theme.AegisTextDim
import com.nfc.security.ui.theme.AegisType
import java.util.concurrent.TimeUnit

@Composable
fun PaywallScreen(state: PaywallUiState, onUnlockPremium: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AegisBg)
            .padding(horizontal = 16.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        AegisTopBar(title = "Aegis Pro", subtitle = "Unlock full protection")

        Icon(Icons.Default.Shield, contentDescription = null, tint = AegisAccent, modifier = Modifier.size(64.dp))
        Spacer(modifier = Modifier.height(16.dp))
        Text("Go Pro", style = AegisType.headlineLarge, textAlign = TextAlign.Center)
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            "Unlimited access to all Aegis security modules.",
            style = AegisType.bodyMedium,
            color = AegisTextDim,
            textAlign = TextAlign.Center
        )

        if (state.freemiumState is FreemiumState.Trial) {
            Spacer(modifier = Modifier.height(12.dp))
            val days = TimeUnit.MILLISECONDS.toDays((state.freemiumState as FreemiumState.Trial).remainingMs)
            AegisPill("$days days remaining", PillTone.WARN)
        }

        Spacer(modifier = Modifier.height(24.dp))

        listOf(
            "NFC Sentinel — unlimited scan history",
            "Vault — unlimited encrypted items",
            "Integrity — real-time Play Integrity checks",
            "Tunnel — DNS filter + tracker block"
        ).forEach { feature ->
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
            ) {
                Icon(Icons.Default.Check, contentDescription = null, tint = AegisSafe, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(feature, style = AegisType.bodyMedium, color = AegisText)
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        listOf(
            "Monthly" to "$2.99 / month",
            "Yearly" to "$19.99 / year  · Best value"
        ).forEach { (label, price) ->
            AegisCard(modifier = Modifier.fillMaxWidth(), onClick = onUnlockPremium) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(label, style = AegisType.titleMedium)
                        Text(price, style = AegisType.bodySmall, color = AegisTextDim)
                    }
                    AegisPill("Select", PillTone.ACCENT)
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (state.isUnlocking) {
            CircularProgressIndicator(color = AegisAccent)
        } else {
            Button(
                onClick = onUnlockPremium,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = AegisAccent)
            ) {
                Icon(Icons.Default.Lock, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Unlock Aegis Pro")
            }
        }

        Spacer(modifier = Modifier.height(8.dp))
        TextButton(onClick = { }) {
            Text("Restore Purchases", style = AegisType.bodySmall, color = AegisTextDim)
        }
        Spacer(modifier = Modifier.height(16.dp))
    }
}
