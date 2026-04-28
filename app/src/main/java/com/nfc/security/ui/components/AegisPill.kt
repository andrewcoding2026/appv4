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
import com.nfc.security.ui.theme.AegisAccent
import com.nfc.security.ui.theme.AegisAccentSoft
import com.nfc.security.ui.theme.AegisCrit
import com.nfc.security.ui.theme.AegisSafe
import com.nfc.security.ui.theme.AegisSurfaceAlt
import com.nfc.security.ui.theme.AegisText
import com.nfc.security.ui.theme.AegisType
import com.nfc.security.ui.theme.AegisWarn

enum class PillTone { DEFAULT, ACCENT, SAFE, WARN, CRIT }

@Composable
fun AegisPill(label: String, tone: PillTone = PillTone.DEFAULT) {
    val (bg, fg) = when (tone) {
        PillTone.DEFAULT -> AegisSurfaceAlt to AegisText
        PillTone.ACCENT  -> AegisAccentSoft to AegisAccent
        PillTone.SAFE    -> Color(0x224ADE80) to AegisSafe
        PillTone.WARN    -> Color(0x22FFB454) to AegisWarn
        PillTone.CRIT    -> Color(0x22FF6B6B) to AegisCrit
    }
    Text(
        text = label.uppercase(),
        style = AegisType.labelSmall,
        color = fg,
        modifier = Modifier
            .clip(RoundedCornerShape(4.dp))
            .background(bg)
            .padding(horizontal = 8.dp, vertical = 4.dp)
    )
}
