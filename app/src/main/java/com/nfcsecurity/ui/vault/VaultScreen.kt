package com.nfcsecurity.ui.vault

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
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MenuAnchorType
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
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.nfcsecurity.data.db.VaultItemEntity
import com.nfcsecurity.ui.components.NFCSecurityCard
import com.nfcsecurity.ui.components.NFCSecurityPill
import com.nfcsecurity.ui.components.NFCSecurityTopBar
import com.nfcsecurity.ui.components.PillTone
import com.nfcsecurity.ui.theme.NFCSecurityBg
import com.nfcsecurity.ui.theme.NFCSecurityAccent
import com.nfcsecurity.ui.theme.NFCSecurityCrit
import com.nfcsecurity.ui.theme.NFCSecuritySafe
import com.nfcsecurity.ui.theme.NFCSecuritySurface
import com.nfcsecurity.ui.theme.NFCSecurityText
import com.nfcsecurity.ui.theme.NFCSecurityTextDim
import com.nfcsecurity.ui.theme.NFCSecurityType
import javax.crypto.Cipher

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
                prepareEncryptCipher = viewModel::prepareEncryptCipher,
                prepareDecryptCipher = viewModel::prepareDecryptCipher,
                onAddItem = viewModel::addItem,
                onDeleteItem = viewModel::deleteItem,
                onDecryptItem = viewModel::decryptItem,
                onLock = { viewModel.lock(); onBack() },
                onBack = onBack
            )
        }

        is VaultUiState.Error -> {
            Column(
                modifier = Modifier.fillMaxSize().background(NFCSecurityBg).padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(s.message, color = NFCSecurityCrit, style = NFCSecurityType.bodyMedium)
            }
        }
    }
}

@Composable
private fun LockedView(isUnlocking: Boolean, onUnlock: () -> Unit, onBack: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(NFCSecurityBg)
            .padding(horizontal = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        NFCSecurityTopBar(title = "Vault", onBack = onBack)
        Spacer(modifier = Modifier.weight(1f))
        Box(
            modifier = Modifier
                .size(96.dp)
                .background(NFCSecurityAccent.copy(alpha = 0.1f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Default.Lock, contentDescription = null, tint = NFCSecurityAccent, modifier = Modifier.size(48.dp))
        }
        Spacer(modifier = Modifier.height(24.dp))
        Text("Vault is Sealed", style = NFCSecurityType.titleLarge)
        Spacer(modifier = Modifier.height(8.dp))
        Text("Use biometrics to access your encrypted items.", style = NFCSecurityType.bodySmall, color = NFCSecurityTextDim)
        Spacer(modifier = Modifier.height(32.dp))
        Button(
            onClick = onUnlock,
            enabled = !isUnlocking,
            colors = ButtonDefaults.buttonColors(containerColor = NFCSecurityAccent)
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
    prepareEncryptCipher: () -> Cipher?,
    prepareDecryptCipher: (ByteArray) -> Cipher?,
    onAddItem: (String, String, String, Cipher) -> Unit,
    onDeleteItem: (Long) -> Unit,
    onDecryptItem: (Long, Cipher, (ByteArray?) -> Unit) -> Unit,
    onLock: () -> Unit,
    onBack: () -> Unit,
) {
    val context = LocalContext.current
    var showAddDialog by remember { mutableStateOf(false) }
    var revealedSecret by remember { mutableStateOf<String?>(null) }

    Scaffold(
        containerColor = NFCSecurityBg,
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = NFCSecurityAccent,
                contentColor = NFCSecurityBg
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add item")
            }
        },
        topBar = {
            NFCSecurityTopBar(
                title = "Vault",
                subtitle = "${items.size} items",
                onBack = onBack,
                right = {
                    IconButton(onClick = onLock) {
                        Icon(Icons.Default.Lock, contentDescription = "Lock vault", tint = NFCSecurityTextDim)
                    }
                }
            )
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).padding(horizontal = 16.dp)) {
            NFCSecurityCard(modifier = Modifier.fillMaxWidth()) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.LockOpen, contentDescription = null, tint = NFCSecuritySafe, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Vault unlocked · AES-256-GCM", style = NFCSecurityType.bodySmall, color = NFCSecuritySafe)
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            if (items.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No items yet. Tap + to add.", style = NFCSecurityType.bodyMedium, color = NFCSecurityTextDim)
                }
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(items, key = { it.id }) { item ->
                        VaultItemRow(
                            item = item,
                            onDelete = { onDeleteItem(item.id) },
                            onReveal = {
                                val activity = context as? AppCompatActivity ?: return@VaultItemRow
                                val cipher = prepareDecryptCipher(item.iv) ?: return@VaultItemRow
                                val executor = ContextCompat.getMainExecutor(context)
                                BiometricPrompt(activity, executor,
                                    object : BiometricPrompt.AuthenticationCallback() {
                                        override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                                            val authCipher = result.cryptoObject?.cipher ?: return
                                            onDecryptItem(item.id, authCipher) { bytes ->
                                                revealedSecret = bytes?.toString(Charsets.UTF_8)
                                            }
                                        }
                                    }
                                ).authenticate(
                                    BiometricPrompt.PromptInfo.Builder()
                                        .setTitle("Reveal Secret")
                                        .setSubtitle("Authenticate to decrypt \"${item.label}\"")
                                        .setNegativeButtonText("Cancel")
                                        .build(),
                                    BiometricPrompt.CryptoObject(cipher)
                                )
                            }
                        )
                    }
                }
            }
        }
    }

    if (showAddDialog) {
        AddItemDialog(
            onConfirm = { label, type, secret ->
                showAddDialog = false
                val activity = context as? AppCompatActivity ?: return@AddItemDialog
                val cipher = prepareEncryptCipher() ?: return@AddItemDialog
                val executor = ContextCompat.getMainExecutor(context)
                BiometricPrompt(activity, executor,
                    object : BiometricPrompt.AuthenticationCallback() {
                        override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                            val authCipher = result.cryptoObject?.cipher ?: return
                            onAddItem(label, type, secret, authCipher)
                        }
                    }
                ).authenticate(
                    BiometricPrompt.PromptInfo.Builder()
                        .setTitle("Confirm Save")
                        .setSubtitle("Authenticate to encrypt and save this vault item")
                        .setNegativeButtonText("Cancel")
                        .build(),
                    BiometricPrompt.CryptoObject(cipher)
                )
            },
            onDismiss = { showAddDialog = false }
        )
    }

    if (revealedSecret != null) {
        AlertDialog(
            onDismissRequest = { revealedSecret = null },
            containerColor = NFCSecuritySurface,
            title = { Text("Secret", color = NFCSecurityText) },
            text = { Text(revealedSecret ?: "", color = NFCSecurityText, style = NFCSecurityType.bodyMedium) },
            confirmButton = {
                TextButton(onClick = { revealedSecret = null }) {
                    Text("Close", color = NFCSecurityAccent)
                }
            }
        )
    }
}

@Composable
private fun VaultItemRow(item: VaultItemEntity, onDelete: () -> Unit, onReveal: () -> Unit) {
    NFCSecurityCard(modifier = Modifier.fillMaxWidth()) {
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
                    tint = NFCSecurityAccent,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(item.label, style = NFCSecurityType.titleMedium, color = NFCSecurityText)
                    NFCSecurityPill(label = item.type, tone = PillTone.ACCENT)
                }
            }
            Row {
                IconButton(onClick = onReveal) {
                    Icon(Icons.Default.Visibility, contentDescription = "Reveal secret", tint = NFCSecurityTextDim)
                }
                IconButton(onClick = onDelete) {
                    Icon(Icons.Default.Delete, contentDescription = "Delete", tint = NFCSecurityCrit)
                }
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
        containerColor = NFCSecuritySurface,
        title = { Text("Add Vault Item", color = NFCSecurityText) },
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
                        modifier = Modifier.fillMaxWidth().menuAnchor(MenuAnchorType.PrimaryNotEditable)
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
                colors = ButtonDefaults.buttonColors(containerColor = NFCSecurityAccent)
            ) { Text("Save") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel", color = NFCSecurityTextDim) }
        }
    )
}