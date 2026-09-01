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

class MediaViewModel(
    private val fileRepository: FileRepository
) : ViewModel() {

    private val _mediaFiles = MutableStateFlow<List<FileItem>>(emptyList())
    val mediaFiles: StateFlow<List<FileItem>> = _mediaFiles.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _operationState = MutableStateFlow<FileOperationState>(FileOperationState.Idle)
    val operationState: StateFlow<FileOperationState> = _operationState.asStateFlow()

    private var currentFilter: String = "all"

    fun loadMedia(filterType: String = "all") {
        currentFilter = filterType
        viewModelScope.launch {
            _isLoading.value = true
            try {
                _mediaFiles.value = when (filterType) {
                    "images" -> fileRepository.getAllImageFiles()
                    "videos" -> fileRepository.getAllVideoFiles()
                    "recent" -> fileRepository.getRecentFiles()
                    "favorites" -> fileRepository.getFavoriteFiles()
                    "audio" -> fileRepository.getAllAudioFiles()
                    "documents" -> fileRepository.getAllDocumentFiles()
                    "pdfs" -> fileRepository.getAllPdfFiles()
                    "slides" -> fileRepository.getAllSlideFiles()
                    "spreadsheets" -> fileRepository.getAllSpreadsheetFiles()
                    "archives" -> fileRepository.getAllArchiveFiles()
                    "code" -> fileRepository.getAllCodeFiles()
                    "text" -> fileRepository.getAllTextFiles()
                    else -> fileRepository.getAllMediaFiles()
                }
            } catch (e: Exception) {
                _mediaFiles.value = emptyList()
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun deleteMedia(uri: Uri, permanent: Boolean = false) {
        viewModelScope.launch {
            fileRepository.deleteFiles(listOf(uri), permanent = permanent).collect { state ->
                _operationState.value = state
                if (state == FileOperationState.Completed) {
                    loadMedia(currentFilter)
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
            loadMedia(currentFilter)
        }
    }

    fun deleteMultipleMedia(uris: List<Uri>, permanent: Boolean = false) {
        viewModelScope.launch {
            fileRepository.deleteFiles(uris, permanent = permanent).collect { state ->
                _operationState.value = state
                if (state == FileOperationState.Completed) {
                    loadMedia(currentFilter)
                    kotlinx.coroutines.delay(500)
                    _operationState.value = FileOperationState.Idle
                } else if (state is FileOperationState.Failed) {
                    kotlinx.coroutines.delay(1500)
                    _operationState.value = FileOperationState.Idle
                }
            }
        }
    }

    fun copyMultipleMedia(uris: List<Uri>, destUri: Uri) {
        viewModelScope.launch {
            fileRepository.copyFiles(uris, destUri).collect { state ->
                _operationState.value = state
                if (state == FileOperationState.Completed) {
                    loadMedia(currentFilter)
                    kotlinx.coroutines.delay(500)
                    _operationState.value = FileOperationState.Idle
                } else if (state is FileOperationState.Failed) {
                    kotlinx.coroutines.delay(1500)
                    _operationState.value = FileOperationState.Idle
                }
            }
        }
    }

    fun moveMultipleMedia(uris: List<Uri>, destUri: Uri) {
        viewModelScope.launch {
            fileRepository.moveFiles(uris, destUri).collect { state ->
                _operationState.value = state
                if (state == FileOperationState.Completed) {
                    loadMedia(currentFilter)
                    kotlinx.coroutines.delay(500)
                    _operationState.value = FileOperationState.Idle
                } else if (state is FileOperationState.Failed) {
                    kotlinx.coroutines.delay(1500)
                    _operationState.value = FileOperationState.Idle
                }
            }
        }
    }

    fun cancelOperation() {
        _operationState.value = FileOperationState.Idle
    }
}
