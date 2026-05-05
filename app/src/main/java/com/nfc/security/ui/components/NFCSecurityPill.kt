package com.nfc.security.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.nfc.security.ui.theme.NFCSecurityAccent
import com.nfc.security.ui.theme.NFCSecurityAccentSoft
import com.nfc.security.ui.theme.NFCSecurityCrit
import com.nfc.security.ui.theme.NFCSecuritySafe
import com.nfc.security.ui.theme.NFCSecuritySurfaceAlt
import com.nfc.security.ui.theme.NFCSecurityText
import com.nfc.security.ui.theme.NFCSecurityType
import com.nfc.security.ui.theme.NFCSecurityWarn

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
