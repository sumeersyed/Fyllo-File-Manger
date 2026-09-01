package com.fyllo.filemanager.di

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.fyllo.filemanager.ui.screens.FilesViewModel
import com.fyllo.filemanager.ui.screens.HomeViewModel
import com.fyllo.filemanager.ui.screens.ManageViewModel
import com.fyllo.filemanager.ui.screens.MediaViewModel
import com.fyllo.filemanager.ui.screens.TrashViewModel
import com.fyllo.filemanager.ui.screens.SettingsViewModel
import com.fyllo.filemanager.ui.screens.AppsViewModel
import com.fyllo.filemanager.ui.screens.SearchViewModel
class ViewModelFactory(private val appContainer: AppContainer) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(HomeViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return HomeViewModel(appContainer.getStorageStatsUseCase, appContainer.fileRepository) as T
        }
        if (modelClass.isAssignableFrom(FilesViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return FilesViewModel(appContainer.fileRepository, appContainer.settingsRepository) as T
        }
        if (modelClass.isAssignableFrom(ManageViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ManageViewModel(appContainer.getStorageStatsUseCase, appContainer.fileRepository) as T
        }
        if (modelClass.isAssignableFrom(MediaViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return MediaViewModel(appContainer.fileRepository) as T
        }
        if (modelClass.isAssignableFrom(TrashViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return TrashViewModel(appContainer.trashManager) as T
        }
        if (modelClass.isAssignableFrom(SettingsViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            val workManager = androidx.work.WorkManager.getInstance(appContainer.applicationContext)
            return SettingsViewModel(appContainer.settingsRepository, workManager) as T
        }
        if (modelClass.isAssignableFrom(AppsViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return AppsViewModel(appContainer.fileRepository) as T
        }
        if (modelClass.isAssignableFrom(SearchViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return SearchViewModel(appContainer.fileRepository) as T
        }
        if (modelClass.isAssignableFrom(com.fyllo.filemanager.ui.screens.StorageCleanupViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return com.fyllo.filemanager.ui.screens.StorageCleanupViewModel(appContainer.fileRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
