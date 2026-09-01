package com.fyllo.filemanager.di

import android.content.Context
import com.fyllo.filemanager.data.repository.FileRepositoryImpl
import com.fyllo.filemanager.data.repository.StorageRepositoryImpl
import com.fyllo.filemanager.data.repository.TrashManager
import com.fyllo.filemanager.domain.repository.FileRepository
import com.fyllo.filemanager.domain.repository.StorageRepository
import com.fyllo.filemanager.domain.repository.SettingsRepository
import com.fyllo.filemanager.data.repository.SettingsRepositoryImpl
import com.fyllo.filemanager.domain.usecase.GetStorageStatsUseCase

class AppContainer(private val context: Context) {
    val applicationContext: Context = context.applicationContext
    val trashManager: TrashManager by lazy {
        TrashManager(context)
    }

    val storageRepository: StorageRepository by lazy {
        StorageRepositoryImpl(context)
    }

    val fileRepository: FileRepository by lazy {
        FileRepositoryImpl(context, trashManager)
    }

    val getStorageStatsUseCase: GetStorageStatsUseCase by lazy {
        GetStorageStatsUseCase(storageRepository)
    }

    val settingsRepository: SettingsRepository by lazy {
        SettingsRepositoryImpl(context)
    }
}
