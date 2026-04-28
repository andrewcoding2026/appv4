package com.nfc.security.data.db

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [EventEntity::class, VaultItemEntity::class],
    version = 1,
    exportSchema = false
)
abstract class AegisDatabase : RoomDatabase() {
    abstract fun eventDao(): EventDao
    abstract fun vaultItemDao(): VaultItemDao
}
