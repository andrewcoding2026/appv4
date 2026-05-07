package com.nfcsecurity.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "vault_items")
data class VaultItemEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val label: String,
    val type: String,
    val ciphertext: ByteArray,
    val iv: ByteArray,
    val createdAt: Long,
    val updatedAt: Long,
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is VaultItemEntity) return false
        return id == other.id
    }

    override fun hashCode(): Int = id.hashCode()
}
