package com.fyllo.filemanager.ui.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fyllo.filemanager.domain.repository.SettingsRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.fyllo.filemanager.workers.AutoCleanWorker
import java.util.concurrent.TimeUnit

data class SettingsState(
    val themeMode: String = "System Default",
    val colorTheme: String = "Default",
    val amoledBlack: Boolean = false,
    val language: String = "System Default",
    val showHiddenFiles: Boolean = false,
    val recycleBinDuration: Int = 30,
    val autoCleanJunk: Boolean = false,
    val junkCleanInterval: String = "Weekly",
    val isOnboardingCompleted: Boolean = false,
    val eInkMode: Boolean = false,
    val enableScrollSound: Boolean = true
)

class SettingsViewModel(
    private val settingsRepository: SettingsRepository,
    private val workManager: WorkManager
) : ViewModel() {

    val settingsState: StateFlow<SettingsState> = combine(
        settingsRepository.themeMode,
        settingsRepository.colorTheme,
        settingsRepository.amoledBlack,
        settingsRepository.language,
        settingsRepository.showHiddenFiles,
        settingsRepository.recycleBinDuration,
        settingsRepository.autoCleanJunk,
        settingsRepository.junkCleanInterval,
        settingsRepository.isOnboardingCompleted,
        settingsRepository.eInkMode,
        settingsRepository.enableScrollSound
    ) { args: Array<Any?> ->
        SettingsState(
            themeMode = args[0] as String,
            colorTheme = args[1] as String,
            amoledBlack = args[2] as Boolean,
            language = args[3] as String,
            showHiddenFiles = args[4] as Boolean,
            recycleBinDuration = args[5] as Int,
            autoCleanJunk = args[6] as Boolean,
            junkCleanInterval = args[7] as String,
            isOnboardingCompleted = args[8] as Boolean,
            eInkMode = args[9] as Boolean,
            enableScrollSound = args[10] as Boolean
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = SettingsState()
    )

    fun setThemeMode(themeMode: String) {
        viewModelScope.launch {
            settingsRepository.setThemeMode(themeMode)
        }
    }

    fun setColorTheme(colorTheme: String) {
        viewModelScope.launch {
            settingsRepository.setColorTheme(colorTheme)
        }
    }

    fun setAmoledBlack(amoledBlack: Boolean) {
        viewModelScope.launch {
            settingsRepository.setAmoledBlack(amoledBlack)
        }
    }

    fun setEInkMode(enabled: Boolean) {
        viewModelScope.launch {
            settingsRepository.setEInkMode(enabled)
        }
    }

    fun setEnableScrollSound(enabled: Boolean) {
        viewModelScope.launch { settingsRepository.setEnableScrollSound(enabled) }
    }

    fun setLanguage(language: String) {
        val langCode = when (language) {
            "English" -> "en"
            "Arabic" -> "ar"
            "Spanish" -> "es"
            "French" -> "fr"
            "German" -> "de"
            "Italian" -> "it"
            "Portuguese" -> "pt"
            "Russian" -> "ru"
            "Japanese" -> "ja"
            "Korean" -> "ko"
            "Chinese" -> "zh"
            else -> ""
        }

        val currentLocales = androidx.appcompat.app.AppCompatDelegate.getApplicationLocales()
        val currentTag = if (!currentLocales.isEmpty) currentLocales.toLanguageTags() else ""

        viewModelScope.launch {
            settingsRepository.setLanguage(language)
            if (currentTag != langCode) {
                androidx.appcompat.app.AppCompatDelegate.setApplicationLocales(
                    androidx.core.os.LocaleListCompat.forLanguageTags(langCode)
                )
            }
        }
    }

    fun setShowHiddenFiles(show: Boolean) {
        viewModelScope.launch { settingsRepository.setShowHiddenFiles(show) }
    }

    fun setRecycleBinDuration(days: Int) {
        viewModelScope.launch { settingsRepository.setRecycleBinDuration(days) }
    }

    fun setAutoCleanJunk(autoClean: Boolean) {
        viewModelScope.launch { 
            settingsRepository.setAutoCleanJunk(autoClean)
            if (!autoClean) {
                workManager.cancelUniqueWork("AutoCleanJunkWork")
            } else {
                setJunkCleanInterval(settingsState.value.junkCleanInterval)
            }
        }
    }
    
    fun setJunkCleanInterval(interval: String) {
        viewModelScope.launch { 
            settingsRepository.setJunkCleanInterval(interval)
            
            // Cancel existing work first
            workManager.cancelUniqueWork("AutoCleanJunkWork")
            
            if (interval != "Off") {
                val repeatInterval = when (interval) {
                    "Daily" -> 1L to TimeUnit.DAYS
                    "Weekly" -> 7L to TimeUnit.DAYS
                    "Monthly" -> 30L to TimeUnit.DAYS
                    else -> return@launch
                }
                
                val workRequest = PeriodicWorkRequestBuilder<AutoCleanWorker>(
                    repeatInterval.first, repeatInterval.second
                ).build()
                
                workManager.enqueueUniquePeriodicWork(
                    "AutoCleanJunkWork",
                    ExistingPeriodicWorkPolicy.UPDATE,
                    workRequest
                )
            }
        }
    }

    fun setOnboardingCompleted(completed: Boolean) {
        viewModelScope.launch { settingsRepository.setOnboardingCompleted(completed) }
    }
}
