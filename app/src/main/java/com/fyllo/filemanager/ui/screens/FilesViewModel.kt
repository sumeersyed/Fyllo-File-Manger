package com.fyllo.filemanager.ui.screens

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fyllo.filemanager.domain.model.FileItem
import com.fyllo.filemanager.domain.model.FileOperationState
import com.fyllo.filemanager.domain.repository.FileRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

import com.fyllo.filemanager.domain.repository.SettingsRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn

enum class SortOption {
    NAME_ASC, NAME_DESC, DATE_ASC, DATE_DESC, SIZE_ASC, SIZE_DESC
}

data class ClipboardState(
    val uris: List<Uri>,
    val isCut: Boolean
)

data class ConflictDialogState(
    val sourceUris: List<Uri>,
    val destUri: Uri,
    val isCut: Boolean
)

class FilesViewModel(
    private val fileRepository: FileRepository,
    private val settingsRepository: SettingsRepository
) : ViewModel() {

    val showHiddenFiles: StateFlow<Boolean> = settingsRepository.showHiddenFiles
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    private val _currentPath = MutableStateFlow<Uri?>(null)
    val currentPath: StateFlow<Uri?> = _currentPath.asStateFlow()

    private val _files = MutableStateFlow<List<FileItem>>(emptyList())
    val files: StateFlow<List<FileItem>> = _files.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _currentSortOption = MutableStateFlow(SortOption.NAME_ASC)
    val currentSortOption: StateFlow<SortOption> = _currentSortOption.asStateFlow()

    private val _isGridView = MutableStateFlow(false)
    val isGridView: StateFlow<Boolean> = _isGridView.asStateFlow()

    private val _clipboard = MutableStateFlow<ClipboardState?>(null)
    val clipboard: StateFlow<ClipboardState?> = _clipboard.asStateFlow()

    private val _operationState = MutableStateFlow<FileOperationState>(FileOperationState.Idle)
    val operationState: StateFlow<FileOperationState> = _operationState.asStateFlow()

    private val _conflictDialogState = MutableStateFlow<ConflictDialogState?>(null)
    val conflictDialogState: StateFlow<ConflictDialogState?> = _conflictDialogState.asStateFlow()

    private val pathStack = java.util.ArrayDeque<Uri>()

    init {
        viewModelScope.launch {
            settingsRepository.showHiddenFiles.collect {
                val current = _currentPath.value
                if (current != null) {
                    loadFiles(current)
                }
            }
        }
    }

    fun loadFiles(uri: Uri?) {
        viewModelScope.launch {
            _isLoading.value = true
            _currentPath.value = uri
            try {
                val fetchedFiles = fileRepository.listFiles(uri)
                val isShowHidden = showHiddenFiles.value
                val filtered = if (isShowHidden) {
                    fetchedFiles
                } else {
                    fetchedFiles.filter { !it.name.startsWith(".") }
                }
                _files.value = sortList(filtered, _currentSortOption.value)
            } catch (e: Exception) {
                _files.value = emptyList()
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Navigates into a subfolder while saving current location on the backstack.
     */
    fun navigateToFolder(targetUri: Uri) {
        val current = _currentPath.value
        if (current != null && current != targetUri) {
            pathStack.push(current)
        }
        loadFiles(targetUri)
    }

    /**
     * Navigates up to parent directory.
     * Returns true if moved to a parent directory, false if already at root.
     */
    fun navigateUp(): Boolean {
        if (!pathStack.isEmpty()) {
            val parentUri = pathStack.pop()
            loadFiles(parentUri)
            return true
        }
        val current = _currentPath.value ?: return false
        if (current.scheme == "file" && current.path != null) {
            val currentFile = java.io.File(current.path!!)
            val parentFile = currentFile.parentFile
            val rootPath = android.os.Environment.getExternalStorageDirectory().absolutePath
            if (parentFile != null && currentFile.absolutePath != rootPath && parentFile.absolutePath.startsWith(rootPath)) {
                loadFiles(Uri.fromFile(parentFile))
                return true
            }
        }
        return false
    }

    fun canNavigateUp(): Boolean {
        if (!pathStack.isEmpty()) return true
        val current = _currentPath.value ?: return false
        if (current.scheme == "file" && current.path != null) {
            val currentFile = java.io.File(current.path!!)
            val rootPath = android.os.Environment.getExternalStorageDirectory().absolutePath
            return currentFile.absolutePath != rootPath && currentFile.parentFile != null && currentFile.parentFile!!.absolutePath.startsWith(rootPath)
        }
        return false
    }

    fun sortFiles(option: SortOption) {
        _currentSortOption.value = option
        _files.value = sortList(_files.value, option)
    }

    private fun sortList(list: List<FileItem>, option: SortOption): List<FileItem> {
        val folders = list.filter { it.isFolder }
        val files = list.filter { !it.isFolder }

        val sortedFolders = when (option) {
            SortOption.NAME_ASC -> folders.sortedBy { it.name.lowercase() }
            SortOption.NAME_DESC -> folders.sortedByDescending { it.name.lowercase() }
            SortOption.DATE_ASC -> folders.sortedBy { it.lastModified }
            SortOption.DATE_DESC -> folders.sortedByDescending { it.lastModified }
            SortOption.SIZE_ASC -> folders.sortedBy { it.sizeBytes }
            SortOption.SIZE_DESC -> folders.sortedByDescending { it.sizeBytes }
        }

        val sortedFiles = when (option) {
            SortOption.NAME_ASC -> files.sortedBy { it.name.lowercase() }
            SortOption.NAME_DESC -> files.sortedByDescending { it.name.lowercase() }
            SortOption.DATE_ASC -> files.sortedBy { it.lastModified }
            SortOption.DATE_DESC -> files.sortedByDescending { it.lastModified }
            SortOption.SIZE_ASC -> files.sortedBy { it.sizeBytes }
            SortOption.SIZE_DESC -> files.sortedByDescending { it.sizeBytes }
        }

        return sortedFolders + sortedFiles
    }

    fun setGridView(isGrid: Boolean) {
        _isGridView.value = isGrid
    }

    fun createFolder(name: String) {
        viewModelScope.launch {
            val parentUri = _currentPath.value ?: Uri.fromFile(android.os.Environment.getExternalStorageDirectory())
            fileRepository.createFolder(parentUri, name)
            loadFiles(parentUri)
        }
    }

    fun deleteFile(uri: Uri, permanent: Boolean = false) {
        viewModelScope.launch {
            fileRepository.deleteFiles(listOf(uri), permanent = permanent).collect { state ->
                _operationState.value = state
                if (state == FileOperationState.Completed) {
                    loadFiles(_currentPath.value)
                    kotlinx.coroutines.delay(500)
                    _operationState.value = FileOperationState.Idle
                } else if (state is FileOperationState.Failed) {
                    kotlinx.coroutines.delay(1500)
                    _operationState.value = FileOperationState.Idle
                }
            }
        }
    }

    fun renameFile(uri: Uri, newName: String) {
        viewModelScope.launch {
            fileRepository.renameFile(uri, newName)
            loadFiles(_currentPath.value)
        }
    }

    private fun resolveDestinationDirectory(sourceUri: Uri? = null): Uri {
        val current = _currentPath.value
        if (current != null) {
            if (current.scheme == "file" && !current.path.isNullOrEmpty()) {
                val f = java.io.File(current.path!!)
                if (f.exists()) return current
            }
            val path = current.path
            if (path != null) {
                if (path.startsWith("/storage/") || path.startsWith("/sdcard/")) {
                    val f = java.io.File(path)
                    if (f.exists()) return Uri.fromFile(f)
                }
                if (path.contains("primary:")) {
                    val rel = path.substringAfter("primary:").trim('/')
                    val extDir = android.os.Environment.getExternalStorageDirectory().absolutePath
                    val f = java.io.File(extDir, rel)
                    if (f.exists()) return Uri.fromFile(f)
                }
            }
        }
        if (sourceUri != null) {
            val srcPath = sourceUri.path
            if (srcPath != null) {
                val f = java.io.File(srcPath)
                val parent = f.parentFile
                if (parent != null && parent.exists()) {
                    return Uri.fromFile(parent)
                }
            }
        }
        return Uri.fromFile(android.os.Environment.getExternalStorageDirectory())
    }

    private var currentOperationJob: kotlinx.coroutines.Job? = null

    fun cancelOperation() {
        currentOperationJob?.cancel()
        currentOperationJob = null
        _operationState.value = FileOperationState.Idle
    }

    fun extractFile(sourceUri: Uri) {
        val destUri = resolveDestinationDirectory(sourceUri)
        currentOperationJob?.cancel()
        currentOperationJob = viewModelScope.launch {
            fileRepository.extractArchive(sourceUri, destUri).collect { state ->
                _operationState.value = state
                if (state == FileOperationState.Completed) {
                    loadFiles(_currentPath.value)
                    kotlinx.coroutines.delay(500)
                    _operationState.value = FileOperationState.Idle
                    currentOperationJob = null
                } else if (state is FileOperationState.Failed) {
                    kotlinx.coroutines.delay(1500)
                    _operationState.value = FileOperationState.Idle
                    currentOperationJob = null
                }
            }
        }
    }

    fun copyFile(sourceUri: Uri, destUri: Uri) {
        currentOperationJob?.cancel()
        currentOperationJob = viewModelScope.launch {
            fileRepository.copyFiles(listOf(sourceUri), destUri).collect { state ->
                _operationState.value = state
                if (state == FileOperationState.Completed) {
                    loadFiles(_currentPath.value)
                    kotlinx.coroutines.delay(500)
                    _operationState.value = FileOperationState.Idle
                    currentOperationJob = null
                } else if (state is FileOperationState.Failed) {
                    kotlinx.coroutines.delay(1500)
                    _operationState.value = FileOperationState.Idle
                    currentOperationJob = null
                }
            }
        }
    }

    fun moveFile(sourceUri: Uri, destUri: Uri) {
        currentOperationJob?.cancel()
        currentOperationJob = viewModelScope.launch {
            fileRepository.moveFiles(listOf(sourceUri), destUri).collect { state ->
                _operationState.value = state
                if (state == FileOperationState.Completed) {
                    loadFiles(_currentPath.value)
                    kotlinx.coroutines.delay(500)
                    _operationState.value = FileOperationState.Idle
                    currentOperationJob = null
                } else if (state is FileOperationState.Failed) {
                    kotlinx.coroutines.delay(1500)
                    _operationState.value = FileOperationState.Idle
                    currentOperationJob = null
                }
            }
        }
    }

    private fun getUniqueZipFile(destDir: java.io.File, name: String): java.io.File {
        var newFile = java.io.File(destDir, name)
        var i = 1
        val nameWithoutExt = newFile.nameWithoutExtension
        val ext = newFile.extension.let { if (it.isNotEmpty()) ".$it" else "" }
        while (newFile.exists()) {
            newFile = java.io.File(destDir, "$nameWithoutExt ($i)$ext")
            i++
        }
        return newFile
    }

    fun compressFile(sourceUri: Uri, fileName: String) {
        val destUri = resolveDestinationDirectory(sourceUri)
        val destDir = java.io.File(destUri.path ?: android.os.Environment.getExternalStorageDirectory().absolutePath)
        val finalDestUri = Uri.fromFile(getUniqueZipFile(destDir, "$fileName.zip"))
        
        currentOperationJob?.cancel()
        currentOperationJob = viewModelScope.launch {
            fileRepository.compressFiles(listOf(sourceUri), finalDestUri).collect { state ->
                _operationState.value = state
                if (state == FileOperationState.Completed) {
                    loadFiles(_currentPath.value)
                    kotlinx.coroutines.delay(500)
                    _operationState.value = FileOperationState.Idle
                    currentOperationJob = null
                } else if (state is FileOperationState.Failed) {
                    kotlinx.coroutines.delay(1500)
                    _operationState.value = FileOperationState.Idle
                    currentOperationJob = null
                }
            }
        }
    }

    // --- Batch Operations ---

    fun deleteMultipleFiles(uris: List<Uri>, permanent: Boolean = false) {
        currentOperationJob?.cancel()
        currentOperationJob = viewModelScope.launch {
            fileRepository.deleteFiles(uris, permanent = permanent).collect { state ->
                _operationState.value = state
                if (state == FileOperationState.Completed) {
                    loadFiles(_currentPath.value)
                    kotlinx.coroutines.delay(500)
                    _operationState.value = FileOperationState.Idle
                    currentOperationJob = null
                } else if (state is FileOperationState.Failed) {
                    kotlinx.coroutines.delay(1500)
                    _operationState.value = FileOperationState.Idle
                    currentOperationJob = null
                }
            }
        }
    }

    fun compressMultipleFiles(uris: List<Uri>) {
        val firstUri = uris.firstOrNull()
        val destUri = resolveDestinationDirectory(firstUri)
        val destDir = java.io.File(destUri.path ?: android.os.Environment.getExternalStorageDirectory().absolutePath)
        val finalDestUri = Uri.fromFile(getUniqueZipFile(destDir, "archive.zip"))

        currentOperationJob?.cancel()
        currentOperationJob = viewModelScope.launch {
            fileRepository.compressFiles(uris, finalDestUri).collect { state ->
                _operationState.value = state
                if (state == FileOperationState.Completed) {
                    loadFiles(_currentPath.value)
                    kotlinx.coroutines.delay(500)
                    _operationState.value = FileOperationState.Idle
                    currentOperationJob = null
                } else if (state is FileOperationState.Failed) {
                    kotlinx.coroutines.delay(1500)
                    _operationState.value = FileOperationState.Idle
                    currentOperationJob = null
                }
            }
        }
    }

    // --- Clipboard Operations ---

    fun copyToClipboard(uris: List<Uri>, isCut: Boolean) {
        _clipboard.value = ClipboardState(uris, isCut)
    }

    fun clearClipboard() {
        _clipboard.value = null
    }

    fun pasteFromClipboard() {
        val clip = _clipboard.value ?: return
        val destUri = _currentPath.value ?: Uri.fromFile(android.os.Environment.getExternalStorageDirectory())
        
        viewModelScope.launch {
            val hasConflict = fileRepository.checkConflicts(clip.uris, destUri)
            if (hasConflict) {
                _conflictDialogState.value = ConflictDialogState(clip.uris, destUri, clip.isCut)
            } else {
                executePaste(clip.uris, destUri, clip.isCut, com.fyllo.filemanager.domain.model.ConflictStrategy.KEEP_BOTH)
            }
        }
    }

    fun resolveConflict(strategy: com.fyllo.filemanager.domain.model.ConflictStrategy) {
        val state = _conflictDialogState.value ?: return
        _conflictDialogState.value = null
        executePaste(state.sourceUris, state.destUri, state.isCut, strategy)
    }

    fun cancelConflict() {
        _conflictDialogState.value = null
    }

    private fun executePaste(uris: List<Uri>, destUri: Uri, isCut: Boolean, strategy: com.fyllo.filemanager.domain.model.ConflictStrategy) {
        currentOperationJob?.cancel()
        currentOperationJob = viewModelScope.launch {
            val flow = if (isCut) fileRepository.moveFiles(uris, destUri, strategy) else fileRepository.copyFiles(uris, destUri, strategy)
            flow.collect { state ->
                _operationState.value = state
                if (state == FileOperationState.Completed) {
                    loadFiles(_currentPath.value)
                    clearClipboard()
                    kotlinx.coroutines.delay(800)
                    _operationState.value = FileOperationState.Idle
                    currentOperationJob = null
                } else if (state is FileOperationState.Failed) {
                    kotlinx.coroutines.delay(1500)
                    _operationState.value = FileOperationState.Idle
                    currentOperationJob = null
                }
            }
        }
    }

    fun moveToSafeFolder(context: android.content.Context, uris: List<Uri>) {
        val safeFolder = java.io.File(context.filesDir, "SafeFolder").apply { if (!exists()) mkdirs() }
        currentOperationJob?.cancel()
        currentOperationJob = viewModelScope.launch {
            kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
                for (uri in uris) {
                    try {
                        val path = if (uri.scheme == "file" && uri.path != null) uri.path else {
                            val p = uri.path
                            if (p != null && (p.startsWith("/storage/") || p.startsWith("/sdcard/"))) p else null
                        }
                        if (path != null) {
                            val srcFile = java.io.File(path)
                            if (srcFile.exists()) {
                                if (srcFile.isDirectory) {
                                    val destDir = getUniqueZipFile(safeFolder, srcFile.name)
                                    srcFile.copyRecursively(destDir, overwrite = true)
                                    srcFile.deleteRecursively()
                                    purgePathFromMediaStore(context, srcFile.absolutePath, uri)
                                } else {
                                    val ext = srcFile.extension
                                    val finalExt = if (ext.isNotEmpty()) ".$ext" else ""
                                    val encodedPath = android.util.Base64.encodeToString(srcFile.absolutePath.toByteArray(), android.util.Base64.URL_SAFE or android.util.Base64.NO_WRAP)
                                    val vaultFileName = "vault_${encodedPath}_${System.currentTimeMillis()}$finalExt"
                                    val destFile = java.io.File(safeFolder, vaultFileName)
                                    try {
                                        com.fyllo.filemanager.core.security.EncryptionService().encryptFile(srcFile, destFile)
                                    } catch (e: Exception) {
                                        srcFile.copyTo(destFile, overwrite = true)
                                    }
                                    srcFile.delete()
                                    purgePathFromMediaStore(context, srcFile.absolutePath, uri)
                                }
                                continue
                            }
                        }

                        val resolver = context.contentResolver
                        var name = "Item"
                        if (uri.scheme == "content") {
                            resolver.query(uri, arrayOf(android.provider.OpenableColumns.DISPLAY_NAME), null, null, null)?.use { cursor ->
                                if (cursor.moveToFirst()) {
                                    val nameIdx = cursor.getColumnIndex(android.provider.OpenableColumns.DISPLAY_NAME)
                                    if (nameIdx != -1) name = cursor.getString(nameIdx)
                                }
                            }
                        } else {
                            name = uri.lastPathSegment ?: "Item"
                        }
                        val ext = name.substringAfterLast('.', "")
                        val finalExt = if (ext.isNotEmpty() && name.contains(".")) ".$ext" else ""
                        val encodedPath = android.util.Base64.encodeToString(uri.toString().toByteArray(), android.util.Base64.URL_SAFE or android.util.Base64.NO_WRAP)
                        val vaultFileName = "vault_${encodedPath}_${System.currentTimeMillis()}$finalExt"
                        val destFile = java.io.File(safeFolder, vaultFileName)
                        
                        resolver.openInputStream(uri)?.use { input ->
                            java.io.FileOutputStream(destFile).use { output ->
                                try {
                                    com.fyllo.filemanager.core.security.EncryptionService().encryptStream(input, output)
                                } catch (e: Exception) {
                                    input.copyTo(output)
                                }
                            }
                        }
                        if (uri.scheme == "content") {
                            try { resolver.delete(uri, null, null) } catch (e: Exception) { }
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }
            loadFiles(_currentPath.value)
            currentOperationJob = null
        }
    }

    private fun purgePathFromMediaStore(context: android.content.Context, path: String, uri: Uri? = null) {
        try {
            if (uri != null && uri.scheme == "content") {
                try { context.contentResolver.delete(uri, null, null) } catch (e: Exception) { }
            }
            if (path.isNotEmpty()) {
                val file = java.io.File(path)
                val pathsToPurge = mutableListOf<String>()
                pathsToPurge.add(path)
                if (file.isDirectory) {
                    try { file.walkTopDown().forEach { pathsToPurge.add(it.absolutePath) } } catch (e: Exception) { }
                }
                android.media.MediaScannerConnection.scanFile(context, pathsToPurge.toTypedArray(), null, null)
                val contentUri = android.provider.MediaStore.Files.getContentUri("external")
                val where = "${android.provider.MediaStore.Files.FileColumns.DATA} = ? OR ${android.provider.MediaStore.Files.FileColumns.DATA} LIKE ?"
                val args = arrayOf(path, "$path/%")
                context.contentResolver.delete(contentUri, where, args)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
