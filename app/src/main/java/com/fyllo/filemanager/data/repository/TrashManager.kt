package com.fyllo.filemanager.data.repository

import android.content.Context
import android.os.Environment
import com.fyllo.filemanager.domain.model.TrashItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.util.UUID

class TrashManager(private val context: Context) {
    
    private val trashDir: File by lazy {
        // Use primary external storage root so moving files is instantaneous
        val dir = File(Environment.getExternalStorageDirectory(), ".Trash")
        if (!dir.exists()) {
            dir.mkdirs()
            // Create .nomedia so MediaStore ignores trashed files
            File(dir, ".nomedia").createNewFile()
        }
        dir
    }
    
    private val indexFile: File by lazy {
        File(context.filesDir, "trash_index.json").apply {
            if (!exists()) {
                writeText("[]")
            }
        }
    }
    
    private val _trashItems = MutableStateFlow<List<TrashItem>>(emptyList())
    val trashItems: StateFlow<List<TrashItem>> = _trashItems.asStateFlow()

    init {
        loadTrashIndex()
    }

    private fun loadTrashIndex() {
        try {
            val jsonString = indexFile.readText()
            val jsonArray = JSONArray(jsonString)
            val items = mutableListOf<TrashItem>()
            for (i in 0 until jsonArray.length()) {
                val obj = jsonArray.getJSONObject(i)
                items.add(
                    TrashItem(
                        id = obj.getString("id"),
                        name = obj.getString("name"),
                        originalPath = obj.getString("originalPath"),
                        trashPath = obj.getString("trashPath"),
                        sizeBytes = obj.getLong("sizeBytes"),
                        timestamp = obj.getLong("timestamp"),
                        isFolder = obj.getBoolean("isFolder")
                    )
                )
            }
            val now = System.currentTimeMillis()
            val thirtyDaysMs = 30L * 24 * 60 * 60 * 1000L

            // Delete files older than 30 days automatically
            val nonExpiredItems = mutableListOf<TrashItem>()
            for (item in items) {
                if (now - item.timestamp > thirtyDaysMs) {
                    try {
                        File(item.trashPath).deleteRecursively()
                    } catch (_: Exception) {}
                } else if (File(item.trashPath).exists()) {
                    nonExpiredItems.add(item)
                }
            }

            _trashItems.value = nonExpiredItems
            if (items.size != nonExpiredItems.size) {
                saveTrashIndex(nonExpiredItems)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun saveTrashIndex(items: List<TrashItem>) {
        try {
            val jsonArray = JSONArray()
            items.forEach { item ->
                val obj = JSONObject().apply {
                    put("id", item.id)
                    put("name", item.name)
                    put("originalPath", item.originalPath)
                    put("trashPath", item.trashPath)
                    put("sizeBytes", item.sizeBytes)
                    put("timestamp", item.timestamp)
                    put("isFolder", item.isFolder)
                }
                jsonArray.put(obj)
            }
            indexFile.writeText(jsonArray.toString())
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    suspend fun moveToTrash(sourceFile: File): Boolean = withContext(Dispatchers.IO) {
        if (!sourceFile.exists()) return@withContext false
        
        val uniqueId = UUID.randomUUID().toString()
        val destFile = File(trashDir, "${uniqueId}_${sourceFile.name}")
        
        var success = sourceFile.renameTo(destFile)
        if (!success) {
            try {
                if (sourceFile.isDirectory) {
                    success = sourceFile.copyRecursively(destFile, overwrite = true)
                    if (success) sourceFile.deleteRecursively()
                } else {
                    sourceFile.copyTo(destFile, overwrite = true)
                    success = sourceFile.delete()
                }
            } catch (e: Exception) {
                e.printStackTrace()
                success = false
            }
        }

        if (success) {
            val item = TrashItem(
                id = uniqueId,
                name = sourceFile.name,
                originalPath = sourceFile.absolutePath,
                trashPath = destFile.absolutePath,
                sizeBytes = if (destFile.isDirectory) getFolderTotalSize(destFile) else destFile.length(),
                timestamp = System.currentTimeMillis(),
                isFolder = destFile.isDirectory
            )
            val updated = _trashItems.value + item
            _trashItems.value = updated
            saveTrashIndex(updated)
            return@withContext true
        }
        return@withContext false
    }

    private fun getFolderTotalSize(file: File): Long {
        var size = 0L
        file.walkTopDown().forEach { f ->
            if (f.isFile) size += f.length()
        }
        return size
    }

    suspend fun restoreFromTrash(id: String): Boolean = withContext(Dispatchers.IO) {
        val item = _trashItems.value.find { it.id == id } ?: return@withContext false
        val trashedFile = File(item.trashPath)
        val originalFile = File(item.originalPath)
        
        // Ensure parent directory exists
        originalFile.parentFile?.mkdirs()
        
        var success = trashedFile.renameTo(originalFile)
        if (!success) {
            try {
                if (trashedFile.isDirectory) {
                    success = trashedFile.copyRecursively(originalFile, overwrite = true)
                    if (success) trashedFile.deleteRecursively()
                } else {
                    trashedFile.copyTo(originalFile, overwrite = true)
                    success = trashedFile.delete()
                }
            } catch (e: Exception) {
                e.printStackTrace()
                success = false
            }
        }

        if (success) {
            val updated = _trashItems.value.filter { it.id != id }
            _trashItems.value = updated
            saveTrashIndex(updated)
            return@withContext true
        }
        return@withContext false
    }

    suspend fun deletePermanently(id: String): Boolean = withContext(Dispatchers.IO) {
        val item = _trashItems.value.find { it.id == id } ?: return@withContext false
        val file = File(item.trashPath)
        val success = file.deleteRecursively()
        if (success || !file.exists()) {
            val updated = _trashItems.value.filter { it.id != id }
            _trashItems.value = updated
            saveTrashIndex(updated)
            return@withContext true
        }
        return@withContext false
    }
    
    suspend fun emptyTrash(): Boolean = withContext(Dispatchers.IO) {
        var allSuccess = true
        _trashItems.value.forEach { item ->
            val file = File(item.trashPath)
            if (!file.deleteRecursively() && file.exists()) {
                allSuccess = false
            }
        }
        _trashItems.value = emptyList()
        saveTrashIndex(emptyList())
        return@withContext allSuccess
    }
}
