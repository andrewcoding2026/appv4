package com.NFC.SecureShield.free.ui.freemium

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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.NFC.SecureShield.free.R
import com.NFC.SecureShield.free.domain.model.FreemiumState
import com.NFC.SecureShield.free.ui.components.NFCSecurityCard
import com.NFC.SecureShield.free.ui.components.NFCSecurityPill
import com.NFC.SecureShield.free.ui.components.NFCSecurityTopBar
import com.NFC.SecureShield.free.ui.components.PillTone
import com.NFC.SecureShield.free.ui.theme.NFCSecurityBg
import com.NFC.SecureShield.free.ui.theme.NFCSecurityAccent
import com.NFC.SecureShield.free.ui.theme.NFCSecuritySafe
import com.NFC.SecureShield.free.ui.theme.NFCSecurityText
import com.NFC.SecureShield.free.ui.theme.NFCSecurityTextDim
import com.NFC.SecureShield.free.ui.theme.NFCSecurityType
import java.util.concurrent.TimeUnit

@Composable
fun PaywallScreen(state: PaywallUiState, onUnlockPremium: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(NFCSecurityBg)
            .padding(horizontal = 16.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        NFCSecurityTopBar(title = stringResource(R.string.unlock_pro), subtitle = stringResource(R.string.pro_subtitle))

        Icon(Icons.Default.Shield, contentDescription = null, tint = NFCSecurityAccent, modifier = Modifier.size(64.dp))
        Spacer(modifier = Modifier.height(16.dp))
        Text(stringResource(R.string.go_pro), style = NFCSecurityType.headlineLarge, textAlign = TextAlign.Center)
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            stringResource(R.string.pro_desc),
            style = NFCSecurityType.bodyMedium,
            color = NFCSecurityTextDim,
            textAlign = TextAlign.Center
        )

        if (state.freemiumState is FreemiumState.Trial) {
            Spacer(modifier = Modifier.height(12.dp))
            val days = TimeUnit.MILLISECONDS.toDays((state.freemiumState as FreemiumState.Trial).remainingMs)
            NFCSecurityPill(stringResource(R.string.trial_remaining, days.toInt()), PillTone.WARN)
        }

        Spacer(modifier = Modifier.height(24.dp))

        listOf(
            stringResource(R.string.feature_nfc),
            stringResource(R.string.feature_vault),
            stringResource(R.string.feature_integrity),
            stringResource(R.string.feature_tunnel)
        ).forEach { feature ->
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
            ) {
                Icon(Icons.Default.Check, contentDescription = null, tint = NFCSecuritySafe, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(feature, style = NFCSecurityType.bodyMedium, color = NFCSecurityText)
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        listOf(
            stringResource(R.string.monthly) to stringResource(R.string.price_monthly),
            stringResource(R.string.yearly) to stringResource(R.string.price_yearly)
        ).forEach { (label, price) ->
            NFCSecurityCard(modifier = Modifier.fillMaxWidth(), onClick = onUnlockPremium) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(label, style = NFCSecurityType.titleMedium)
                        Text(price, style = NFCSecurityType.bodySmall, color = NFCSecurityTextDim)
                    }
                    NFCSecurityPill(stringResource(R.string.select), PillTone.ACCENT)
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (state.isUnlocking) {
            CircularProgressIndicator(color = NFCSecurityAccent)
        } else {
            Button(
                onClick = onUnlockPremium,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = NFCSecurityAccent)
            ) {
                Icon(Icons.Default.Lock, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(stringResource(R.string.unlock_pro))
            }
        }

        Spacer(modifier = Modifier.height(8.dp))
        TextButton(onClick = { }) {
            Text(stringResource(R.string.restore_purchases), style = NFCSecurityType.bodySmall, color = NFCSecurityTextDim)
        }
        Spacer(modifier = Modifier.height(16.dp))
    }
}
