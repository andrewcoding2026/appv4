package com.NFC.SecureShield.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.NFC.SecureShield.ui.theme.NFCSecurityTextDim
import com.NFC.SecureShield.ui.theme.NFCSecurityType

@Composable
fun NFCSecurityTopBar(
    title: String,
    subtitle: String? = null,
    onBack: (() -> Unit)? = null,
    right: @Composable RowScope.() -> Unit = {}
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (onBack != null) {
                IconButton(onClick = onBack) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        modifier = Modifier.size(20.dp)
                    )
                }
            } else {
                Spacer(modifier = Modifier.size(48.dp))
            }
            Column {
                if (subtitle != null) {
                    Text(
                        text = subtitle.uppercase(),
                        style = NFCSecurityType.labelSmall,
                        color = NFCSecurityTextDim
                    )
                }
                Text(text = title, style = NFCSecurityType.titleLarge)
            }
        }
        Row(verticalAlignment = Alignment.CenterVertically, content = right)
    }
}
