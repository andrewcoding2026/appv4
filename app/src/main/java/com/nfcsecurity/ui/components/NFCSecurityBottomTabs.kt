package com.nfcsecurity.ui.components

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
import com.nfcsecurity.ui.theme.NFCSecurityAccent
import com.nfcsecurity.ui.theme.NFCSecurityBorder
import com.nfcsecurity.ui.theme.NFCSecurityBg
import com.nfcsecurity.ui.theme.NFCSecurityTextFaint
import com.nfcsecurity.ui.theme.NFCSecurityType

enum class NFCSecurityTab(val label: String, val icon: ImageVector, val route: String) {
    HOME("Home", Icons.Default.Home, "dashboard"),
    TUNNEL("Tunnel", Icons.Default.Wifi, "tunnel"),
    SCAN("Scan", Icons.Default.Shield, "scan"),
    VAULT("Vault", Icons.Default.Lock, "vault"),
    SYSTEM("System", Icons.Default.Security, "integrity"),
}

@Composable
fun NFCSecurityBottomTabs(activeRoute: String, onTabSelected: (String) -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(NFCSecurityBg)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(1.dp)
                .background(NFCSecurityBorder)
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            NFCSecurityTab.entries.forEach { tab ->
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
                        tint = if (isActive) NFCSecurityAccent else NFCSecurityTextFaint,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = tab.label,
                        style = NFCSecurityType.labelSmall,
                        color = if (isActive) NFCSecurityAccent else NFCSecurityTextFaint
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    if (isActive) {
                        Box(
                            modifier = Modifier
                                .size(4.dp)
                                .background(NFCSecurityAccent, androidx.compose.foundation.shape.CircleShape)
                        )
                    } else {
                        Spacer(modifier = Modifier.size(4.dp))
                    }
                }
            }
        }
    }
}
