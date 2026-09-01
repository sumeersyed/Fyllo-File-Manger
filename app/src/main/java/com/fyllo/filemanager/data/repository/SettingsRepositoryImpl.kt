package com.fyllo.filemanager.data.repository

import android.content.Context
import android.content.SharedPreferences
import com.fyllo.filemanager.domain.repository.SettingsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class SettingsRepositoryImpl(context: Context) : SettingsRepository {
    private val prefs: SharedPreferences = context.getSharedPreferences("app_settings", Context.MODE_PRIVATE)

    private val _themeMode = MutableStateFlow(prefs.getString("themeMode", "System Default") ?: "System Default")
    override val themeMode: Flow<String> = _themeMode

    private val _colorTheme = MutableStateFlow(prefs.getString("colorTheme", "Default") ?: "Default")
    override val colorTheme: Flow<String> = _colorTheme

    private val _amoledBlack = MutableStateFlow(prefs.getBoolean("amoledBlack", false))
    override val amoledBlack: Flow<Boolean> = _amoledBlack

    private val _language = MutableStateFlow(prefs.getString("language", "System Default") ?: "System Default")
    override val language: Flow<String> = _language

    private val _showHiddenFiles = MutableStateFlow(prefs.getBoolean("showHiddenFiles", false))
    override val showHiddenFiles: Flow<Boolean> = _showHiddenFiles

    private val _recycleBinDuration = MutableStateFlow(prefs.getInt("recycleBinDuration", 30))
    override val recycleBinDuration: Flow<Int> = _recycleBinDuration

    private val _autoCleanJunk = MutableStateFlow(prefs.getBoolean("autoCleanJunk", false))
    override val autoCleanJunk: Flow<Boolean> = _autoCleanJunk

    private val _junkCleanInterval = MutableStateFlow(prefs.getString("junkCleanInterval", "Weekly") ?: "Weekly")
    override val junkCleanInterval: Flow<String> = _junkCleanInterval

    private val _isOnboardingCompleted = MutableStateFlow(prefs.getBoolean("isOnboardingCompleted", false))
    override val isOnboardingCompleted: Flow<Boolean> = _isOnboardingCompleted
    
    private val _eInkMode = MutableStateFlow(prefs.getBoolean("eInkMode", false))
    override val eInkMode: Flow<Boolean> = _eInkMode

    private val _enableScrollSound = MutableStateFlow(prefs.getBoolean("enableScrollSound", true))
    override val enableScrollSound: Flow<Boolean> = _enableScrollSound

    override suspend fun setThemeMode(themeMode: String) {
        prefs.edit().putString("themeMode", themeMode).apply()
        _themeMode.value = themeMode
    }

    override suspend fun setColorTheme(colorTheme: String) {
        prefs.edit().putString("colorTheme", colorTheme).apply()
        _colorTheme.value = colorTheme
    }

    override suspend fun setAmoledBlack(amoledBlack: Boolean) {
        prefs.edit().putBoolean("amoledBlack", amoledBlack).apply()
        _amoledBlack.value = amoledBlack
    }

    override suspend fun setEInkMode(enabled: Boolean) {
        prefs.edit().putBoolean("eInkMode", enabled).apply()
        _eInkMode.value = enabled
    }

    override suspend fun setEnableScrollSound(enabled: Boolean) {
        prefs.edit().putBoolean("enableScrollSound", enabled).apply()
        _enableScrollSound.value = enabled
    }

    override suspend fun setLanguage(language: String) {
        prefs.edit().putString("language", language).apply()
        _language.value = language
    }

    override suspend fun setShowHiddenFiles(show: Boolean) {
        prefs.edit().putBoolean("showHiddenFiles", show).apply()
        _showHiddenFiles.value = show
    }

    override suspend fun setRecycleBinDuration(days: Int) {
        prefs.edit().putInt("recycleBinDuration", days).apply()
        _recycleBinDuration.value = days
    }

    override suspend fun setAutoCleanJunk(autoClean: Boolean) {
        prefs.edit().putBoolean("autoCleanJunk", autoClean).apply()
        _autoCleanJunk.value = autoClean
    }

    override suspend fun setJunkCleanInterval(interval: String) {
        prefs.edit().putString("junkCleanInterval", interval).apply()
        _junkCleanInterval.value = interval
    }

    override suspend fun setOnboardingCompleted(completed: Boolean) {
        prefs.edit().putBoolean("isOnboardingCompleted", completed).apply()
        _isOnboardingCompleted.value = completed
    }
}
