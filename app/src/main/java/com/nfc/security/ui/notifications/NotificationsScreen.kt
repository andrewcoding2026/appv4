package com.nfc.security.ui.notifications

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
import com.nfc.security.data.db.EventEntity
import com.nfc.security.ui.components.AegisCard
import com.nfc.security.ui.components.AegisDot
import com.nfc.security.ui.components.AegisTopBar
import com.nfc.security.ui.theme.AegisBg
import com.nfc.security.ui.theme.AegisAccent
import com.nfc.security.ui.theme.AegisCrit
import com.nfc.security.ui.theme.AegisSafe
import com.nfc.security.ui.theme.AegisText
import com.nfc.security.ui.theme.AegisTextDim
import com.nfc.security.ui.theme.AegisTextFaint
import com.nfc.security.ui.theme.AegisType
import com.nfc.security.ui.theme.AegisWarn
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
            .background(AegisBg)
            .padding(horizontal = 16.dp)
    ) {
        AegisTopBar(
            title = "Notifications",
            onBack = onBack,
            right = {
                IconButton(onClick = viewModel::markAllRead) {
                    Icon(Icons.Default.DoneAll, contentDescription = "Mark all read", tint = AegisTextDim)
                }
            }
        )

        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            items(NotifFilter.entries) { f ->
                FilterChip(
                    selected = filter == f,
                    onClick = { viewModel.setFilter(f) },
                    label = { Text(f.name, style = AegisType.labelSmall) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = AegisAccent.copy(alpha = 0.15f),
                        selectedLabelColor = AegisAccent,
                        labelColor = AegisTextDim
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        if (events.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("No notifications", style = AegisType.bodyMedium, color = AegisTextFaint)
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
        "crit"  -> AegisCrit
        "warn"  -> AegisWarn
        "safe"  -> AegisSafe
        else    -> AegisAccent
    }
    AegisCard(modifier = Modifier.fillMaxWidth(), onClick = onClick) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            AegisDot(color = dotColor)
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        event.module.uppercase(),
                        style = AegisType.labelSmall,
                        color = AegisTextDim
                    )
                    Text(
                        formatTime(event.createdAt),
                        style = AegisType.labelSmall,
                        color = AegisTextFaint
                    )
                }
                Spacer(modifier = Modifier.height(2.dp))
                Text(event.title, style = AegisType.titleMedium, color = AegisText)
                if (event.body.isNotBlank()) {
                    Text(
                        event.body,
                        style = AegisType.bodySmall,
                        color = AegisTextDim,
                        maxLines = 2
                    )
                }
            }
        }
    }
}

private fun formatTime(ms: Long): String =
    SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(ms))
