package com.fyllo.filemanager.domain.model

data class AppItem(
    val id: String,
    val name: String,
    val packageName: String,
    val sizeBytes: Long,
    val isGame: Boolean,
    val isSystemApp: Boolean,
    val isApk: Boolean, // True if this is an uninstalled APK file
    val apkPath: String? = null // Path to the APK file if isApk is true
)
