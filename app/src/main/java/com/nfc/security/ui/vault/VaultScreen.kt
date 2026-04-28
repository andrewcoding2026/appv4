package com.nfc.security.ui.vault

import androidx.biometric.BiometricPrompt
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.Note
import androidx.compose.material.icons.filled.Password
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.nfc.security.data.db.VaultItemEntity
import com.nfc.security.ui.components.AegisCard
import com.nfc.security.ui.components.AegisPill
import com.nfc.security.ui.components.AegisTopBar
import com.nfc.security.ui.components.PillTone
import com.nfc.security.ui.theme.AegisBg
import com.nfc.security.ui.theme.AegisAccent
import com.nfc.security.ui.theme.AegisCrit
import com.nfc.security.ui.theme.AegisSafe
import com.nfc.security.ui.theme.AegisSurface
import com.nfc.security.ui.theme.AegisText
import com.nfc.security.ui.theme.AegisTextDim
import com.nfc.security.ui.theme.AegisType
import kotlin.OptIn

@Composable
fun VaultScreen(viewModel: VaultViewModel, onBack: () -> Unit) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    DisposableEffect(Unit) {
        onDispose { viewModel.lock() }
    }

    when (val s = state) {
        is VaultUiState.Locked, is VaultUiState.Unlocking -> {
            LockedView(
                isUnlocking = s is VaultUiState.Unlocking,
                onUnlock = {
                    val activity = context as? AppCompatActivity ?: return@LockedView
                    val executor = ContextCompat.getMainExecutor(context)
                    val biometricPrompt = BiometricPrompt(activity, executor,
                        object : BiometricPrompt.AuthenticationCallback() {
                            override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                                viewModel.onUnlocked()
                            }
                        }
                    )
                    val info = BiometricPrompt.PromptInfo.Builder()
                        .setTitle("Unlock Vault")
                        .setSubtitle("Authenticate to access your vault")
                        .setNegativeButtonText("Cancel")
                        .build()
                    biometricPrompt.authenticate(info)
                },
                onBack = onBack
            )
        }

        is VaultUiState.Unlocked -> {
            UnlockedView(
                items = s.items,
                onAdd = viewModel::addItem,
                onDelete = viewModel::deleteItem,
                onLock = { viewModel.lock(); onBack() },
                onBack = onBack
            )
        }

        is VaultUiState.Error -> {
            Column(
                modifier = Modifier.fillMaxSize().background(AegisBg).padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(s.message, color = AegisCrit, style = AegisType.bodyMedium)
            }
        }
    }
}

@Composable
private fun LockedView(isUnlocking: Boolean, onUnlock: () -> Unit, onBack: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AegisBg)
            .padding(horizontal = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        AegisTopBar(title = "Vault", onBack = onBack)
        Spacer(modifier = Modifier.weight(1f))
        Box(
            modifier = Modifier
                .size(96.dp)
                .background(AegisAccent.copy(alpha = 0.1f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Default.Lock, contentDescription = null, tint = AegisAccent, modifier = Modifier.size(48.dp))
        }
        Spacer(modifier = Modifier.height(24.dp))
        Text("Vault is Sealed", style = AegisType.titleLarge)
        Spacer(modifier = Modifier.height(8.dp))
        Text("Use biometrics to access your encrypted items.", style = AegisType.bodySmall, color = AegisTextDim)
        Spacer(modifier = Modifier.height(32.dp))
        Button(
            onClick = onUnlock,
            enabled = !isUnlocking,
            colors = ButtonDefaults.buttonColors(containerColor = AegisAccent)
        ) {
            Icon(Icons.Default.LockOpen, contentDescription = null, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text("Unlock with Biometric")
        }
        Spacer(modifier = Modifier.weight(1f))
    }
}

@Composable
private fun UnlockedView(
    items: List<VaultItemEntity>,
    onAdd: (String, String, String) -> Unit,
    onDelete: (Long) -> Unit,
    onLock: () -> Unit,
    onBack: () -> Unit,
) {
    var showAddDialog by remember { mutableStateOf(false) }

    Scaffold(
        containerColor = AegisBg,
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = AegisAccent,
                contentColor = AegisBg
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add item")
            }
        },
        topBar = {
            AegisTopBar(
                title = "Vault",
                subtitle = "${items.size} items",
                onBack = onBack,
                right = {
                    IconButton(onClick = onLock) {
                        Icon(Icons.Default.Lock, contentDescription = "Lock vault", tint = AegisTextDim)
                    }
                }
            )
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).padding(horizontal = 16.dp)) {
            AegisCard(modifier = Modifier.fillMaxWidth()) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.LockOpen, contentDescription = null, tint = AegisSafe, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Vault unlocked · AES-256-GCM", style = AegisType.bodySmall, color = AegisSafe)
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            if (items.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No items yet. Tap + to add.", style = AegisType.bodyMedium, color = AegisTextDim)
                }
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(items, key = { it.id }) { item ->
                        VaultItemRow(item = item, onDelete = { onDelete(item.id) })
                    }
                }
            }
        }
    }

    if (showAddDialog) {
        AddItemDialog(
            onConfirm = { label, type, secret ->
                onAdd(label, type, secret)
                showAddDialog = false
            },
            onDismiss = { showAddDialog = false }
        )
    }
}

@Composable
private fun VaultItemRow(item: VaultItemEntity, onDelete: () -> Unit) {
    AegisCard(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = when (item.type) {
                        "password" -> Icons.Default.Password
                        "note"     -> Icons.Default.Note
                        else       -> Icons.Default.Key
                    },
                    contentDescription = null,
                    tint = AegisAccent,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(item.label, style = AegisType.titleMedium, color = AegisText)
                    AegisPill(label = item.type, tone = PillTone.ACCENT)
                }
            }
            IconButton(onClick = onDelete) {
                Icon(Icons.Default.Delete, contentDescription = "Delete", tint = AegisCrit)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddItemDialog(onConfirm: (String, String, String) -> Unit, onDismiss: () -> Unit) {
    var label by remember { mutableStateOf("") }
    var secret by remember { mutableStateOf("") }
    var type by remember { mutableStateOf("password") }
    var expanded by remember { mutableStateOf(false) }
    val types = listOf("password", "note", "card", "totp")

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = AegisSurface,
        title = { Text("Add Vault Item", color = AegisText) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = label,
                    onValueChange = { label = it },
                    label = { Text("Label") },
                    modifier = Modifier.fillMaxWidth()
                )
                ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = it }) {
                    OutlinedTextField(
                        value = type,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Type") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) },
                        modifier = Modifier.fillMaxWidth().menuAnchor()
                    )
                    ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                        types.forEach {
                            DropdownMenuItem(text = { Text(it) }, onClick = { type = it; expanded = false })
                        }
                    }
                }
                OutlinedTextField(
                    value = secret,
                    onValueChange = { secret = it },
                    label = { Text("Secret") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { if (label.isNotBlank() && secret.isNotBlank()) onConfirm(label, type, secret) },
                colors = ButtonDefaults.buttonColors(containerColor = AegisAccent)
            ) { Text("Save") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel", color = AegisTextDim) }
        }
    )
}
