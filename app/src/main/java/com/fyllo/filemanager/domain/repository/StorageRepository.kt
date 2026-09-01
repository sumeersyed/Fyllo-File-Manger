package com.fyllo.filemanager.domain.repository

import com.fyllo.filemanager.domain.model.StorageStats
import com.fyllo.filemanager.domain.model.StorageVolumeInfo
import kotlinx.coroutines.flow.Flow

interface StorageRepository {
    suspend fun getVolumes(): List<StorageVolumeInfo>
    fun observeVolumes(): Flow<List<StorageVolumeInfo>>
    
    suspend fun getStorageStats(volumeId: String): StorageStats
    fun observeStorageStats(volumeId: String): Flow<StorageStats>
}
