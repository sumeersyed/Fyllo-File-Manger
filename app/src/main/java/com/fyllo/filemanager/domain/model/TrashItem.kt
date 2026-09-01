package com.fyllo.filemanager.domain.model

data class TrashItem(
    val id: String,
    val name: String,
    val originalPath: String,
    val trashPath: String,
    val sizeBytes: Long,
    val timestamp: Long,
    val isFolder: Boolean
)
