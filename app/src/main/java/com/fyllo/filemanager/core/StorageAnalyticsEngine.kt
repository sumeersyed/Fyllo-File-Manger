package com.fyllo.filemanager.core

import android.content.Context
import android.os.StatFs
import android.os.Environment
import android.provider.MediaStore
import com.fyllo.filemanager.domain.model.StorageStats
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn

data class StorageCategoryBreakdown(
    val images: Long = 0L,
    val videos: Long = 0L,
    val audio: Long = 0L,
    val documents: Long = 0L,
    val apks: Long = 0L,
    val other: Long = 0L,
    val totalUsed: Long = 0L,
    val totalSize: Long = 0L,
    val freeSize: Long = 0L
) {
    val usedPercent: Float get() = if (totalSize > 0) totalUsed.toFloat() / totalSize else 0f
}

/**
 * Emits real-time storage analytics every [refreshIntervalMs] milliseconds.
 * Uses StatFs for totals and ContentResolver for per-category sizes.
 */
class StorageAnalyticsEngine(private val context: Context) {

    private val refreshIntervalMs = 30_000L // 30 seconds

    val storageFlow: Flow<StorageCategoryBreakdown> = flow {
        while (true) {
            emit(computeSnapshot())
            delay(refreshIntervalMs)
        }
    }.flowOn(Dispatchers.IO)

    suspend fun computeSnapshot(): StorageCategoryBreakdown {
        val stat = StatFs(Environment.getExternalStorageDirectory().path)
        val totalBytes = stat.blockSizeLong * stat.blockCountLong
        val freeBytes = stat.blockSizeLong * stat.availableBlocksLong
        val usedBytes = totalBytes - freeBytes

        val images = queryCategorySize(MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        val videos = queryCategorySize(MediaStore.Video.Media.EXTERNAL_CONTENT_URI)
        val audio = queryCategorySize(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI)
        val docs = queryMimeGroupSize("application/pdf", "application/msword",
            "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
            "application/vnd.ms-excel", "application/vnd.ms-powerpoint",
            "text/plain", "text/csv")
        val apks = queryMimeGroupSize("application/vnd.android.package-archive")
        val other = (usedBytes - images - videos - audio - docs - apks).coerceAtLeast(0L)

        return StorageCategoryBreakdown(
            images = images,
            videos = videos,
            audio = audio,
            documents = docs,
            apks = apks,
            other = other,
            totalUsed = usedBytes,
            totalSize = totalBytes,
            freeSize = freeBytes
        )
    }

    private fun queryCategorySize(contentUri: android.net.Uri): Long {
        var total = 0L
        val projection = arrayOf(MediaStore.MediaColumns.SIZE)
        try {
            context.contentResolver.query(contentUri, projection, null, null, null)?.use { cursor ->
                val sizeCol = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.SIZE)
                while (cursor.moveToNext()) {
                    total += cursor.getLong(sizeCol)
                }
            }
        } catch (e: Exception) { e.printStackTrace() }
        return total
    }

    private fun queryMimeGroupSize(vararg mimeTypes: String): Long {
        var total = 0L
        val selection = mimeTypes.joinToString(" OR ") { "${MediaStore.Files.FileColumns.MIME_TYPE} = ?" }
        val selectionArgs = mimeTypes.toList().toTypedArray()
        val projection = arrayOf(MediaStore.Files.FileColumns.SIZE)
        try {
            context.contentResolver.query(
                MediaStore.Files.getContentUri("external"),
                projection, selection, selectionArgs, null
            )?.use { cursor ->
                val sizeCol = cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.SIZE)
                while (cursor.moveToNext()) total += cursor.getLong(sizeCol)
            }
        } catch (e: Exception) { e.printStackTrace() }
        return total
    }
}
