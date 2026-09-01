package com.fyllo.filemanager.domain.model

import android.net.Uri

data class FileItem(
    val id: String,
    val name: String,
    val uri: Uri,
    val path: String,
    val isFolder: Boolean,
    val sizeBytes: Long,
    val lastModified: Long,
    val mimeType: String?,
    val extension: String,
    val itemCount: Int? = null, // For folders
    val durationMs: Long? = null, // For audio/video
    val dimensions: String? = null // e.g. "1920x1080"
)
