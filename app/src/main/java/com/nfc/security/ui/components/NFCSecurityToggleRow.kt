package com.nfc.security.ui.components

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Icon
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.nfc.security.ui.theme.NFCSecurityAccent
import com.nfc.security.ui.theme.NFCSecuritySurface
import com.nfc.security.ui.theme.NFCSecurityText
import com.nfc.security.ui.theme.NFCSecurityTextDim
import com.nfc.security.ui.theme.NFCSecurityType

@Composable
fun NFCSecurityToggleRow(
    title: String,
    description: String? = null,
    icon: ImageVector? = null,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    NFCSecurityCard(modifier = modifier.fillMaxWidth()) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            if (icon != null) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = NFCSecurityAccent,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
            }
            androidx.compose.foundation.layout.Column(modifier = Modifier.weight(1f)) {
                Text(text = title, style = NFCSecurityType.titleMedium, color = NFCSecurityText)
                if (description != null) {
                    Text(text = description, style = NFCSecurityType.bodySmall, color = NFCSecurityTextDim)
                }
            }
            Spacer(modifier = Modifier.width(8.dp))
            Switch(
                checked = checked,
                onCheckedChange = onCheckedChange,
                colors = SwitchDefaults.colors(
                    checkedThumbColor = NFCSecuritySurface,
                    checkedTrackColor = NFCSecurityAccent,
                )
            )
        }
    }
}
