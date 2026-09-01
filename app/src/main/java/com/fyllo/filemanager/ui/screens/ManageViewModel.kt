package com.fyllo.filemanager.ui.screens

import android.os.Environment
import android.os.storage.StorageManager
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fyllo.filemanager.domain.model.StorageStats
import com.fyllo.filemanager.domain.repository.FileRepository
import com.fyllo.filemanager.domain.usecase.GetStorageStatsUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

data class StorageCategoryDetail(
    val name: String,
    val sizeBytes: Long,
    val itemCount: Int,
    val color: androidx.compose.ui.graphics.Color
)

data class FolderSpaceItem(
    val name: String,
    val sizeBytes: Long,
    val path: String
)

class ManageViewModel(
    getStorageStatsUseCase: GetStorageStatsUseCase,
    private val fileRepository: FileRepository
) : ViewModel() {

    val storageStats: StateFlow<StorageStats?> = getStorageStatsUseCase(StorageManager.UUID_DEFAULT.toString())
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )

    private val _categoryDetails = MutableStateFlow<List<StorageCategoryDetail>>(emptyList())
    val categoryDetails: StateFlow<List<StorageCategoryDetail>> = _categoryDetails.asStateFlow()

    private val _largestFiles = MutableStateFlow<List<com.fyllo.filemanager.domain.model.FileItem>>(emptyList())
    val largestFiles: StateFlow<List<com.fyllo.filemanager.domain.model.FileItem>> = _largestFiles.asStateFlow()

    private val _folderSpaceList = MutableStateFlow<List<FolderSpaceItem>>(emptyList())
    val folderSpaceList: StateFlow<List<FolderSpaceItem>> = _folderSpaceList.asStateFlow()

    private val _junkSize = MutableStateFlow(0L)
    val junkSize: StateFlow<Long> = _junkSize.asStateFlow()

    private val _isScanning = MutableStateFlow(true)
    val isScanning: StateFlow<Boolean> = _isScanning.asStateFlow()

    private val _isCleaning = MutableStateFlow(false)
    val isCleaning: StateFlow<Boolean> = _isCleaning.asStateFlow()

    private val _junkFiles = MutableStateFlow<List<File>>(emptyList())

    init {
        loadAnalyzerData()
        scanJunk()
    }

    fun loadAnalyzerData() {
        viewModelScope.launch {
            _categoryDetails.value = fileRepository.getDetailedCategoryStats()
        }
        viewModelScope.launch {
            _largestFiles.value = fileRepository.getLargestFiles(20)
        }
        viewModelScope.launch {
            _folderSpaceList.value = fileRepository.getFolderStorageSizes()
        }
    }

    fun scanJunk() {
        viewModelScope.launch {
            _isScanning.value = true
            val found = withContext(Dispatchers.IO) { collectJunkFiles() }
            _junkFiles.value = found
            _junkSize.value = found.sumOf { sizeOf(it) }
            _isScanning.value = false
        }
    }

    fun cleanJunk() {
        viewModelScope.launch {
            _isCleaning.value = true
            withContext(Dispatchers.IO) {
                _junkFiles.value.forEach { file ->
                    try {
                        if (file.exists()) {
                            if (file.isDirectory) file.deleteRecursively()
                            else file.delete()
                        }
                    } catch (_: Exception) {}
                }
            }
            _junkFiles.value = emptyList()
            _junkSize.value = 0L
            _isCleaning.value = false
            // Re-scan after clean to confirm everything is gone
            scanJunk()
        }
    }

    private fun collectJunkFiles(): List<File> {
        val junk = mutableListOf<File>()
        val ext = Environment.getExternalStorageDirectory()

        // 1. Android/data/*/cache directories
        val androidData = File(ext, "Android/data")
        if (androidData.exists() && androidData.canRead()) {
            androidData.listFiles()?.forEach { appDir ->
                val cacheDir = File(appDir, "cache")
                if (cacheDir.exists() && cacheDir.canRead()) {
                    cacheDir.listFiles()?.forEach { junk.add(it) }
                }
            }
        }

        // 2. Thumbnail caches
        listOf(
            "DCIM/.thumbnails",
            "Pictures/.thumbnails",
            "Download/.thumbnails",
            ".thumbnails"
        ).forEach { path ->
            val dir = File(ext, path)
            if (dir.exists() && dir.isDirectory) junk.add(dir)
        }

        // 3. Temp / log files at root level
        ext.listFiles()?.filter { f ->
            f.isFile && (f.name.endsWith(".log") || f.name.endsWith(".tmp"))
        }?.let { junk.addAll(it) }

        // 4. Android/obb unused packages (empty dirs)
        val obbDir = File(ext, "Android/obb")
        if (obbDir.exists()) {
            obbDir.listFiles()?.filter { it.isDirectory && (it.listFiles()?.isEmpty() == true) }
                ?.let { junk.addAll(it) }
        }

        return junk
    }

    private fun sizeOf(file: File): Long = try {
        if (file.isDirectory) file.walkTopDown().filter { it.isFile }.sumOf { it.length() }
        else file.length()
    } catch (_: Exception) { 0L }
}
