package com.fyllo.filemanager.worker

import android.content.Context
import android.os.Environment
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

class JunkCleanWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        try {
            // Clean app & external cache
            val appCache = applicationContext.cacheDir
            deleteContents(appCache)
            
            val extCache = applicationContext.externalCacheDir
            if (extCache != null) {
                deleteContents(extCache)
            }

            val ext = Environment.getExternalStorageDirectory()

            // Clean Android/data/*/cache
            val androidData = File(ext, "Android/data")
            if (androidData.exists() && androidData.canRead()) {
                androidData.listFiles()?.forEach { appDir ->
                    val cacheDir = File(appDir, "cache")
                    if (cacheDir.exists() && cacheDir.canRead()) {
                        cacheDir.deleteRecursively()
                    }
                }
            }

            // Clean thumbnail caches & temp files
            listOf(
                "DCIM/.thumbnails",
                "Pictures/.thumbnails",
                "Download/.thumbnails",
                ".thumbnails",
                "WhatsApp/Media/.Statuses"
            ).forEach { path ->
                val dir = File(ext, path)
                if (dir.exists()) {
                    dir.deleteRecursively()
                }
            }

            // Clean .tmp and .log files at storage root
            ext.listFiles()?.filter { f ->
                f.isFile && (f.name.endsWith(".log") || f.name.endsWith(".tmp"))
            }?.forEach { it.delete() }

            Result.success()
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure()
        }
    }

    private fun deleteContents(dir: File) {
        if (dir.exists() && dir.isDirectory) {
            dir.listFiles()?.forEach { child ->
                if (child.isDirectory) child.deleteRecursively()
                else child.delete()
            }
        }
    }
}
