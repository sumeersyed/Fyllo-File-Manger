package com.fyllo.filemanager.domain.repository

import kotlinx.coroutines.flow.Flow

interface SettingsRepository {
    val themeMode: Flow<String>
    val colorTheme: Flow<String>
    val amoledBlack: Flow<Boolean>
    val language: Flow<String>
    val showHiddenFiles: Flow<Boolean>
    val recycleBinDuration: Flow<Int>
    val autoCleanJunk: Flow<Boolean>
    val junkCleanInterval: Flow<String>
    val isOnboardingCompleted: Flow<Boolean>
    val eInkMode: Flow<Boolean>
    val enableScrollSound: Flow<Boolean>

    suspend fun setThemeMode(mode: String)
    suspend fun setColorTheme(color: String)
    suspend fun setAmoledBlack(enabled: Boolean)
    suspend fun setEInkMode(enabled: Boolean)
    suspend fun setEnableScrollSound(enabled: Boolean)
    suspend fun setLanguage(language: String)
    suspend fun setShowHiddenFiles(show: Boolean)
    suspend fun setRecycleBinDuration(days: Int)
    suspend fun setAutoCleanJunk(autoClean: Boolean)
    suspend fun setJunkCleanInterval(interval: String)
    suspend fun setOnboardingCompleted(completed: Boolean)
}
