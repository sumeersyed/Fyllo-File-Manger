package com.fyllo.filemanager.ui.screens

import android.os.storage.StorageManager
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fyllo.filemanager.domain.model.FileItem
import com.fyllo.filemanager.domain.model.StorageStats
import com.fyllo.filemanager.domain.repository.FileRepository
import com.fyllo.filemanager.domain.usecase.GetStorageStatsUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class HomeViewModel(
    getStorageStatsUseCase: GetStorageStatsUseCase,
    private val fileRepository: FileRepository
) : ViewModel() {

    val storageStats: StateFlow<StorageStats?> = getStorageStatsUseCase(StorageManager.UUID_DEFAULT.toString())
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )

    private val _recentFiles = MutableStateFlow<List<FileItem>>(emptyList())
    val recentFiles: StateFlow<List<FileItem>> = _recentFiles.asStateFlow()

    private val _categoryCounts = MutableStateFlow<Map<String, Int>>(emptyMap())
    val categoryCounts: StateFlow<Map<String, Int>> = _categoryCounts.asStateFlow()

    init {
        refreshDashboardData()
    }

    fun refreshRecentFiles() {
        refreshDashboardData()
    }

    fun refreshDashboardData() {
        viewModelScope.launch {
            _recentFiles.value = fileRepository.getRecentFiles(10)
        }
        viewModelScope.launch {
            _categoryCounts.value = fileRepository.getCategoryCounts()
        }
    }
}
