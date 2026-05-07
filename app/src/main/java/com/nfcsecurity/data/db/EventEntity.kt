package com.nfcsecurity.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "events")
data class EventEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val module: String,
    val severity: String,
    val title: String,
    val body: String,
    val createdAt: Long,
    val read: Boolean = false,
    val payloadJson: String? = null,
)
