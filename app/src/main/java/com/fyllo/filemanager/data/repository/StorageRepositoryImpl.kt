package com.fyllo.filemanager.data.repository

import android.app.usage.StorageStatsManager
import android.content.Context
import android.os.Environment
import android.os.storage.StorageManager
import com.fyllo.filemanager.domain.model.StorageCategory
import com.fyllo.filemanager.domain.model.StorageStats
import com.fyllo.filemanager.domain.model.StorageType
import com.fyllo.filemanager.domain.model.StorageVolumeInfo
import com.fyllo.filemanager.domain.repository.StorageRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext
import java.io.File
import java.util.UUID

class StorageRepositoryImpl(
    private val context: Context
) : StorageRepository {

    private val storageManager = context.getSystemService(Context.STORAGE_SERVICE) as StorageManager
    private val storageStatsManager = context.getSystemService(Context.STORAGE_STATS_SERVICE) as StorageStatsManager

    override suspend fun getVolumes(): List<StorageVolumeInfo> = withContext(Dispatchers.IO) {
        val volumes = mutableListOf<StorageVolumeInfo>()
        
        // Use StorageManager to get real volumes
        val storageVolumes = storageManager.storageVolumes
        for (volume in storageVolumes) {
            val uuidStr = volume.uuid ?: StorageManager.UUID_DEFAULT.toString()
            val uuid = try {
                if (volume.uuid == null) StorageManager.UUID_DEFAULT else UUID.fromString(uuidStr)
            } catch (e: Exception) {
                StorageManager.UUID_DEFAULT
            }
            
            val totalBytes = storageStatsManager.getTotalBytes(uuid)
            val freeBytes = storageStatsManager.getFreeBytes(uuid)
            val usedBytes = totalBytes - freeBytes

            val state = volume.state
            
            val type = if (volume.isPrimary) StorageType.INTERNAL
                       else if (volume.isRemovable) StorageType.EXTERNAL_SD
                       else StorageType.UNKNOWN

            val path = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.R) {
                volume.directory?.absolutePath ?: ""
            } else {
                // Fallback for API 29
                ""
            }

            volumes.add(
                StorageVolumeInfo(
                    id = uuidStr,
                    name = volume.getDescription(context),
                    path = path,
                    totalBytes = totalBytes,
                    usedBytes = usedBytes,
                    freeBytes = freeBytes,
                    type = type,
                    removable = volume.isRemovable,
                    writable = state == Environment.MEDIA_MOUNTED,
                    filesystem = null, // Requires deeper native calls to get FS type
                    state = state
                )
            )
        }
        volumes
    }

    override fun observeVolumes(): Flow<List<StorageVolumeInfo>> = flow {
        // Emit initial
        emit(getVolumes())
        // A real implementation would register a BroadcastReceiver for Intent.ACTION_MEDIA_MOUNTED etc.
        // and yield new values here.
    }.flowOn(Dispatchers.IO)

    override suspend fun getStorageStats(volumeId: String): StorageStats = withContext(Dispatchers.IO) {
        val uuid = if (volumeId == StorageManager.UUID_DEFAULT.toString()) {
            StorageManager.UUID_DEFAULT
        } else {
            UUID.fromString(volumeId)
        }

        val totalBytes = storageStatsManager.getTotalBytes(uuid)
        val freeBytes = storageStatsManager.getFreeBytes(uuid)
        val usedBytes = totalBytes - freeBytes

        // Get actual sizes using MediaStore and PackageManager
        val photoVideoSize = getMediaCategorySize(android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI) +
                             getMediaCategorySize(android.provider.MediaStore.Video.Media.EXTERNAL_CONTENT_URI)
                             
        val musicSize = getMediaCategorySize(android.provider.MediaStore.Audio.Media.EXTERNAL_CONTENT_URI)
        
        val appsSize = getAppsSize()
        
        // Sum known categories
        val knownCategoriesSize = photoVideoSize + musicSize + appsSize
        
        // The rest of the used space goes into Documents/Files/Other/System
        val otherFilesSize = maxOf(0L, usedBytes - knownCategoriesSize)

        val categoryBytes = mapOf(
            StorageCategory.PHOTO_VIDEO to photoVideoSize,
            StorageCategory.MUSIC to musicSize,
            StorageCategory.OTHER_APPS to appsSize,
            StorageCategory.GAMES to 0L, // Included in OTHER_APPS
            StorageCategory.FILES to otherFilesSize,
            StorageCategory.FREE to freeBytes
        )

        StorageStats(
            totalBytes = totalBytes,
            usedBytes = usedBytes,
            freeBytes = freeBytes,
            categoryBytes = categoryBytes
        )
    }
    
    private fun getMediaCategorySize(uri: android.net.Uri): Long {
        var size = 0L
        val projection = arrayOf(android.provider.MediaStore.MediaColumns.SIZE)
        try {
            context.contentResolver.query(uri, projection, null, null, null)?.use { cursor ->
                val sizeIndex = cursor.getColumnIndexOrThrow(android.provider.MediaStore.MediaColumns.SIZE)
                while (cursor.moveToNext()) {
                    size += cursor.getLong(sizeIndex)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return size
    }

    private fun getAppsSize(): Long {
        var size = 0L
        try {
            val pm = context.packageManager
            val apps = pm.getInstalledApplications(0)
            for (app in apps) {
                if ((app.flags and android.content.pm.ApplicationInfo.FLAG_SYSTEM) == 0) {
                    val file = File(app.sourceDir)
                    if (file.exists()) {
                        size += file.length()
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return size
    }

    override fun observeStorageStats(volumeId: String): Flow<StorageStats> = flow {
        emit(getStorageStats(volumeId))
    }.flowOn(Dispatchers.IO)
}
