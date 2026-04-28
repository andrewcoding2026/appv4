package com.nfc.security.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.nfc.security.ui.theme.AegisAccent
import com.nfc.security.ui.theme.AegisBorder
import com.nfc.security.ui.theme.AegisBg
import com.nfc.security.ui.theme.AegisTextFaint
import com.nfc.security.ui.theme.AegisType

enum class AegisTab(val label: String, val icon: ImageVector, val route: String) {
    HOME("Home", Icons.Default.Home, "dashboard"),
    TUNNEL("Tunnel", Icons.Default.Wifi, "tunnel"),
    SCAN("Scan", Icons.Default.Shield, "scan"),
    VAULT("Vault", Icons.Default.Lock, "vault"),
    SYSTEM("System", Icons.Default.Security, "integrity"),
}

@Composable
fun AegisBottomTabs(activeRoute: String, onTabSelected: (String) -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(AegisBg)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(1.dp)
                .background(AegisBorder)
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            AegisTab.entries.forEach { tab ->
                val isActive = activeRoute == tab.route
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .clickable { onTabSelected(tab.route) }
                        .padding(horizontal = 12.dp, vertical = 4.dp)
                ) {
                    Icon(
                        imageVector = tab.icon,
                        contentDescription = tab.label,
                        tint = if (isActive) AegisAccent else AegisTextFaint,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = tab.label,
                        style = AegisType.labelSmall,
                        color = if (isActive) AegisAccent else AegisTextFaint
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    if (isActive) {
                        Box(
                            modifier = Modifier
                                .size(4.dp)
                                .background(AegisAccent, androidx.compose.foundation.shape.CircleShape)
                        )
                    } else {
                        Spacer(modifier = Modifier.size(4.dp))
                    }
                }
            }
        }
    }
}
