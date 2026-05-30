package com.NFC.SecureShield.free.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.NFC.SecureShield.free.ui.theme.NFCSecurityAccent
import com.NFC.SecureShield.free.ui.theme.NFCSecurityAccentSoft
import com.NFC.SecureShield.free.ui.theme.NFCSecurityCrit
import com.NFC.SecureShield.free.ui.theme.NFCSecuritySafe
import com.NFC.SecureShield.free.ui.theme.NFCSecuritySurfaceAlt
import com.NFC.SecureShield.free.ui.theme.NFCSecurityText
import com.NFC.SecureShield.free.ui.theme.NFCSecurityType
import com.NFC.SecureShield.free.ui.theme.NFCSecurityWarn

enum class PillTone { DEFAULT, ACCENT, SAFE, WARN, CRIT }

@Composable
fun NFCSecurityPill(label: String, tone: PillTone = PillTone.DEFAULT) {
    val (bg, fg) = when (tone) {
        PillTone.DEFAULT -> NFCSecuritySurfaceAlt to NFCSecurityText
        PillTone.ACCENT  -> NFCSecurityAccentSoft to NFCSecurityAccent
        PillTone.SAFE    -> Color(0x224ADE80) to NFCSecuritySafe
        PillTone.WARN    -> Color(0x22FFB454) to NFCSecurityWarn
        PillTone.CRIT    -> Color(0x22FF6B6B) to NFCSecurityCrit
    }
    Text(
        text = label.uppercase(),
        style = NFCSecurityType.labelSmall,
        color = fg,
        modifier = Modifier
            .clip(RoundedCornerShape(4.dp))
            .background(bg)
            .padding(horizontal = 8.dp, vertical = 4.dp)
    )
}
