package com.fyllo.filemanager.workers

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import java.io.File

class AutoCleanWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        try {
            // Clean App Cache
            val cacheDir = applicationContext.cacheDir
            deleteRecursive(cacheDir)
            
            // Clean External Cache
            val extCacheDir = applicationContext.externalCacheDir
            if (extCacheDir != null) {
                deleteRecursive(extCacheDir)
            }
            
            // Clean specific junk folders or temporary files
            // For now, cleaning the cache is the standard way to clear junk.
            
            return Result.success()
        } catch (e: Exception) {
            e.printStackTrace()
            return Result.failure()
        }
    }

    private fun deleteRecursive(fileOrDirectory: File) {
        if (fileOrDirectory.isDirectory) {
            fileOrDirectory.listFiles()?.forEach { child ->
                deleteRecursive(child)
            }
        }
        // Don't delete the root cache folder itself, just contents
        if (fileOrDirectory != applicationContext.cacheDir && fileOrDirectory != applicationContext.externalCacheDir) {
            fileOrDirectory.delete()
        }
    }
}
