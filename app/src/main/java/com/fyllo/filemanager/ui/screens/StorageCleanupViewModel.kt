package com.fyllo.filemanager.ui.screens

import android.net.Uri
import android.os.Environment
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fyllo.filemanager.domain.model.FileItem
import com.fyllo.filemanager.domain.model.FileOperationState
import com.fyllo.filemanager.domain.repository.FileRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

enum class CleanupTab {
    LARGE, OLD, DUPLICATE, JUNK
}

class StorageCleanupViewModel(
    private val fileRepository: FileRepository
) : ViewModel() {

    private val _currentTab = MutableStateFlow(CleanupTab.LARGE)
    val currentTab: StateFlow<CleanupTab> = _currentTab.asStateFlow()

    private val _largeFiles = MutableStateFlow<List<FileItem>>(emptyList())
    val largeFiles: StateFlow<List<FileItem>> = _largeFiles.asStateFlow()

    private val _oldFiles = MutableStateFlow<List<FileItem>>(emptyList())
    val oldFiles: StateFlow<List<FileItem>> = _oldFiles.asStateFlow()

    private val _duplicateFiles = MutableStateFlow<List<FileItem>>(emptyList())
    val duplicateFiles: StateFlow<List<FileItem>> = _duplicateFiles.asStateFlow()

    private val _junkFiles = MutableStateFlow<List<File>>(emptyList())
    val junkFiles: StateFlow<List<File>> = _junkFiles.asStateFlow()

    private val _isScanning = MutableStateFlow(true)
    val isScanning: StateFlow<Boolean> = _isScanning.asStateFlow()

    private val _selectedUris = MutableStateFlow<Set<Uri>>(emptySet())
    val selectedUris: StateFlow<Set<Uri>> = _selectedUris.asStateFlow()

    private val _operationState = MutableStateFlow<FileOperationState>(FileOperationState.Idle)
    val operationState: StateFlow<FileOperationState> = _operationState.asStateFlow()

    init {
        performDeepScan()
    }

    fun selectTab(tab: CleanupTab) {
        _currentTab.value = tab
        _selectedUris.value = emptySet()
    }

    fun toggleSelection(uri: Uri) {
        val current = _selectedUris.value.toMutableSet()
        if (current.contains(uri)) current.remove(uri)
        else current.add(uri)
        _selectedUris.value = current
    }

    fun selectAll() {
        val files = when (_currentTab.value) {
            CleanupTab.LARGE -> _largeFiles.value
            CleanupTab.OLD -> _oldFiles.value
            CleanupTab.DUPLICATE -> _duplicateFiles.value
            CleanupTab.JUNK -> emptyList()
        }
        val allUris = files.map { it.uri }.toSet()
        if (_selectedUris.value.size == allUris.size) {
            _selectedUris.value = emptySet()
        } else {
            _selectedUris.value = allUris
        }
    }

    fun performDeepScan() {
        viewModelScope.launch {
            _isScanning.value = true
            _selectedUris.value = emptySet()

            launch {
                _largeFiles.value = fileRepository.getLargestFiles(50)
            }
            launch {
                _oldFiles.value = fileRepository.getOldFiles(daysOld = 180, limit = 50)
            }
            launch {
                _duplicateFiles.value = fileRepository.getDuplicateFiles()
            }
            launch {
                val junk = withContext(Dispatchers.IO) { collectJunkFiles() }
                _junkFiles.value = junk
            }

            _isScanning.value = false
        }
    }

    fun deleteSelectedFiles(permanent: Boolean = true) {
        val uris = _selectedUris.value.toList()
        if (uris.isEmpty()) return

        viewModelScope.launch {
            fileRepository.deleteFiles(uris, permanent = permanent).collect { state ->
                _operationState.value = state
                if (state == FileOperationState.Completed) {
                    performDeepScan()
                    _selectedUris.value = emptySet()
                    kotlinx.coroutines.delay(500)
                    _operationState.value = FileOperationState.Idle
                }
            }
        }
    }

    fun cleanJunk() {
        viewModelScope.launch {
            _isScanning.value = true
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
            _isScanning.value = false
            performDeepScan()
        }
    }

    private fun collectJunkFiles(): List<File> {
        val junk = mutableListOf<File>()
        val ext = Environment.getExternalStorageDirectory()

        val androidData = File(ext, "Android/data")
        if (androidData.exists() && androidData.canRead()) {
            androidData.listFiles()?.forEach { appDir ->
                val cacheDir = File(appDir, "cache")
                if (cacheDir.exists() && cacheDir.canRead()) {
                    cacheDir.listFiles()?.forEach { junk.add(it) }
                }
            }
        }

        listOf(
            "DCIM/.thumbnails",
            "Pictures/.thumbnails",
            "Download/.thumbnails",
            ".thumbnails"
        ).forEach { path ->
            val dir = File(ext, path)
            if (dir.exists() && dir.isDirectory) junk.add(dir)
        }

        ext.listFiles()?.filter { f ->
            f.isFile && (f.name.endsWith(".log") || f.name.endsWith(".tmp"))
        }?.let { junk.addAll(it) }

        return junk
    }
}
