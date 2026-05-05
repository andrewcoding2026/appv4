package com.nfc.security.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.nfc.security.ui.theme.NFCSecurityBorder
import com.nfc.security.ui.theme.NFCSecuritySurface

val NFCSecurityCardShape = RoundedCornerShape(14.dp)

@Composable
fun NFCSecurityCard(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    Surface(
        modifier = modifier
            .clip(NFCSecurityCardShape)
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier),
        shape = NFCSecurityCardShape,
        color = NFCSecuritySurface,
        border = BorderStroke(1.dp, NFCSecurityBorder)
    ) {
        Column(modifier = Modifier.padding(16.dp), content = content)
    }
}
