package com.nfcsecurity.ui.onboarding

import android.Manifest
import android.net.VpnService
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.NearMe
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.nfcsecurity.R
import com.nfcsecurity.ui.components.NFCSecurityCard
import com.nfcsecurity.ui.theme.NFCSecurityAccent
import com.nfcsecurity.ui.theme.NFCSecurityBg
import com.nfcsecurity.ui.theme.NFCSecurityBorder
import com.nfcsecurity.ui.theme.NFCSecurityText
import com.nfcsecurity.ui.theme.NFCSecurityTextDim
import com.nfcsecurity.ui.theme.NFCSecurityType
import kotlinx.coroutines.launch

@Composable
fun OnboardingScreen(viewModel: OnboardingViewModel, onFinish: () -> Unit) {
    val context = LocalContext.current
    val pagerState = rememberPagerState(pageCount = { viewModel.totalPages })
    val scope = rememberCoroutineScope()

    val notifLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { /* proceed regardless */ }

    val vpnLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { /* VPN consent result — proceed regardless */ }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(NFCSecurityBg)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(48.dp))

        HorizontalPager(
            state = pagerState,
            modifier = Modifier.weight(1f)
        ) { page ->
            when (page) {
                0 -> WelcomePage()
                1 -> HowItWorksPage()
                2 -> PermissionsPage(
                    onRequestVpn = {
                        val intent = VpnService.prepare(context)
                        if (intent != null) vpnLauncher.launch(intent)
                    },
                    onRequestNotifications = {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                            notifLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                        }
                    }
                )
            }
        }

        PageIndicator(current = pagerState.currentPage, total = viewModel.totalPages)
        Spacer(modifier = Modifier.height(24.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (pagerState.currentPage > 0) {
                OutlinedButton(
                    onClick = { scope.launch { pagerState.animateScrollToPage(pagerState.currentPage - 1) } },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = NFCSecurityTextDim)
                ) {
                    Text(stringResource(R.string.back))
                }
            }
            Button(
                onClick = {
                    if (pagerState.currentPage < viewModel.totalPages - 1) {
                        scope.launch { pagerState.animateScrollToPage(pagerState.currentPage + 1) }
                    } else {
                        viewModel.finish(onFinish)
                    }
                },
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(containerColor = NFCSecurityAccent)
            ) {
                Text(if (pagerState.currentPage < viewModel.totalPages - 1) stringResource(R.string.next) else stringResource(R.string.get_started))
            }
        }
        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
private fun WelcomePage() {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier
                .size(96.dp)
                .background(NFCSecurityAccent.copy(alpha = 0.1f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Shield,
                contentDescription = null,
                tint = NFCSecurityAccent,
                modifier = Modifier.size(48.dp)
            )
        }
        Spacer(modifier = Modifier.height(32.dp))
        Text(stringResource(R.string.welcome_title), style = NFCSecurityType.headlineLarge, textAlign = TextAlign.Center)
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            stringResource(R.string.welcome_desc),
            style = NFCSecurityType.bodyMedium,
            color = NFCSecurityTextDim,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun HowItWorksPage() {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(stringResource(R.string.how_it_works_title), style = NFCSecurityType.headlineLarge, textAlign = TextAlign.Center)
        Spacer(modifier = Modifier.height(32.dp))
        listOf(
            Triple(Icons.Default.NearMe, stringResource(R.string.module_nfc_title), stringResource(R.string.nfc_sentinel_desc)),
            Triple(Icons.Default.Shield, stringResource(R.string.module_vpn_title), stringResource(R.string.tunnel_desc)),
            Triple(Icons.Default.Lock, stringResource(R.string.module_vault_title), stringResource(R.string.vault_desc)),
            Triple(Icons.Default.Security, stringResource(R.string.integrity_desc), stringResource(R.string.integrity_desc)),
        ).forEach { (icon, title, desc) ->
            ModuleCard(icon = icon, title = title, desc = desc)
            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}

@Composable
private fun ModuleCard(icon: ImageVector, title: String, desc: String) {
    NFCSecurityCard(modifier = Modifier.fillMaxWidth()) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(imageVector = icon, contentDescription = null, tint = NFCSecurityAccent, modifier = Modifier.size(24.dp))
            Spacer(modifier = Modifier.size(12.dp))
            Column {
                Text(title, style = NFCSecurityType.titleMedium, color = NFCSecurityText)
                Text(desc, style = NFCSecurityType.bodySmall, color = NFCSecurityTextDim)
            }
        }
    }
}

@Composable
private fun PermissionsPage(onRequestVpn: () -> Unit, onRequestNotifications: () -> Unit) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(stringResource(R.string.permissions_title), style = NFCSecurityType.headlineLarge, textAlign = TextAlign.Center)
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            stringResource(R.string.permissions_desc),
            style = NFCSecurityType.bodyMedium, color = NFCSecurityTextDim, textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(24.dp))
        listOf(
            Triple(stringResource(R.string.perm_nfc_title), stringResource(R.string.perm_nfc_desc), null as (() -> Unit)?),
            Triple(stringResource(R.string.perm_vpn_title), stringResource(R.string.perm_vpn_desc), onRequestVpn),
        ).forEach { (title, desc, action) ->
            PermissionRow(title = title, desc = desc, onGrant = action)
            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}

@Composable
private fun PermissionRow(title: String, desc: String, onGrant: (() -> Unit)?) {
    NFCSecurityCard(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(title, style = NFCSecurityType.titleMedium, color = NFCSecurityText)
                Text(desc, style = NFCSecurityType.bodySmall, color = NFCSecurityTextDim)
            }
            if (onGrant != null) {
                Button(
                    onClick = onGrant,
                    colors = ButtonDefaults.buttonColors(containerColor = NFCSecurityAccent)
                ) { Text(stringResource(R.string.perm_allow)) }
            } else {
                Text(stringResource(R.string.perm_auto), style = NFCSecurityType.bodySmall, color = NFCSecurityTextDim)
            }
        }
    }
}

@Composable
private fun PageIndicator(current: Int, total: Int) {
    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        repeat(total) { index ->
            Box(
                modifier = Modifier
                    .size(if (index == current) 20.dp else 6.dp, 6.dp)
                    .background(
                        if (index == current) NFCSecurityAccent else NFCSecurityBorder,
                        CircleShape
                    )
            )
        }
    }
}
