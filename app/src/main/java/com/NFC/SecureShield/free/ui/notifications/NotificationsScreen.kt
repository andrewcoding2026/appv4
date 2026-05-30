package com.NFC.SecureShield.free.ui.notifications

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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DoneAll
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.NFC.SecureShield.free.data.db.EventEntity
import com.NFC.SecureShield.free.ui.components.NFCSecurityCard
import com.NFC.SecureShield.free.ui.components.NFCSecurityDot
import com.NFC.SecureShield.free.ui.components.NFCSecurityTopBar
import com.NFC.SecureShield.free.ui.theme.NFCSecurityBg
import com.NFC.SecureShield.free.ui.theme.NFCSecurityAccent
import com.NFC.SecureShield.free.ui.theme.NFCSecurityCrit
import com.NFC.SecureShield.free.ui.theme.NFCSecuritySafe
import com.NFC.SecureShield.free.ui.theme.NFCSecurityText
import com.NFC.SecureShield.free.ui.theme.NFCSecurityTextDim
import com.NFC.SecureShield.free.ui.theme.NFCSecurityTextFaint
import com.NFC.SecureShield.free.ui.theme.NFCSecurityType
import com.NFC.SecureShield.free.ui.theme.NFCSecurityWarn
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun NotificationsScreen(
    viewModel: NotificationsViewModel,
    onBack: () -> Unit,
    onEventClick: (Long) -> Unit,
) {
    val events by viewModel.events.collectAsStateWithLifecycle()
    val filter by viewModel.filter.collectAsStateWithLifecycle()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(NFCSecurityBg)
            .padding(horizontal = 16.dp)
    ) {
        NFCSecurityTopBar(
            title = "Notifications",
            onBack = onBack,
            right = {
                IconButton(onClick = viewModel::markAllRead) {
                    Icon(Icons.Default.DoneAll, contentDescription = "Mark all read", tint = NFCSecurityTextDim)
                }
            }
        )

        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            items(NotifFilter.entries) { f ->
                FilterChip(
                    selected = filter == f,
                    onClick = { viewModel.setFilter(f) },
                    label = { Text(f.name, style = NFCSecurityType.labelSmall) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = NFCSecurityAccent.copy(alpha = 0.15f),
                        selectedLabelColor = NFCSecurityAccent,
                        labelColor = NFCSecurityTextDim
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        if (events.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("No notifications", style = NFCSecurityType.bodyMedium, color = NFCSecurityTextFaint)
            }
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(events, key = { it.id }) { event ->
                    EventRow(event = event, onClick = { onEventClick(event.id) })
                }
            }
        }
    }
}

@Composable
private fun EventRow(event: EventEntity, onClick: () -> Unit) {
    val dotColor = when (event.severity) {
        "crit"  -> NFCSecurityCrit
        "warn"  -> NFCSecurityWarn
        "safe"  -> NFCSecuritySafe
        else    -> NFCSecurityAccent
    }
    NFCSecurityCard(modifier = Modifier.fillMaxWidth(), onClick = onClick) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            NFCSecurityDot(color = dotColor)
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        event.module.uppercase(),
                        style = NFCSecurityType.labelSmall,
                        color = NFCSecurityTextDim
                    )
                    Text(
                        formatTime(event.createdAt),
                        style = NFCSecurityType.labelSmall,
                        color = NFCSecurityTextFaint
                    )
                }
                Spacer(modifier = Modifier.height(2.dp))
                Text(event.title, style = NFCSecurityType.titleMedium, color = NFCSecurityText)
                if (event.body.isNotBlank()) {
                    Text(
                        event.body,
                        style = NFCSecurityType.bodySmall,
                        color = NFCSecurityTextDim,
                        maxLines = 2
                    )
                }
            }
        }
    }
}

private fun formatTime(ms: Long): String =
    SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(ms))
