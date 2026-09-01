package com.fyllo.filemanager.domain.model

import android.net.Uri

data class StorageVolumeInfo(
    val id: String,
    val name: String,
    val path: String, // Or URI for SAF
    val totalBytes: Long,
    val usedBytes: Long,
    val freeBytes: Long,
    val type: StorageType,
    val removable: Boolean,
    val writable: Boolean,
    val filesystem: String?,
    val state: String?
)

enum class StorageType {
    INTERNAL, EXTERNAL_SD, USB_OTG, APP_SPECIFIC, SHARED, UNKNOWN
}

data class StorageStats(
    val totalBytes: Long,
    val usedBytes: Long,
    val freeBytes: Long,
    val categoryBytes: Map<StorageCategory, Long>
)

enum class StorageCategory {
    PHOTO_VIDEO, MUSIC, GAMES, OTHER_APPS, FILES, SYSTEM, FREE
}
