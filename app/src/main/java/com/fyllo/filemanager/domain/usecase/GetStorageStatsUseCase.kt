package com.fyllo.filemanager.domain.usecase

import com.fyllo.filemanager.domain.model.StorageStats
import com.fyllo.filemanager.domain.repository.StorageRepository
import kotlinx.coroutines.flow.Flow

class GetStorageStatsUseCase(
    private val storageRepository: StorageRepository
) {
    operator fun invoke(volumeId: String): Flow<StorageStats> {
        return storageRepository.observeStorageStats(volumeId)
    }
}
