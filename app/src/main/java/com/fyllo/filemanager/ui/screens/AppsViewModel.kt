package com.fyllo.filemanager.ui.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fyllo.filemanager.domain.model.AppItem
import com.fyllo.filemanager.domain.repository.FileRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class AppsViewModel(
    private val fileRepository: FileRepository
) : ViewModel() {

    private val _installedApps = MutableStateFlow<List<AppItem>>(emptyList())
    val installedApps: StateFlow<List<AppItem>> = _installedApps.asStateFlow()

    private val _apkFiles = MutableStateFlow<List<AppItem>>(emptyList())
    val apkFiles: StateFlow<List<AppItem>> = _apkFiles.asStateFlow()

    private val _apps = MutableStateFlow<List<AppItem>>(emptyList())
    val apps: StateFlow<List<AppItem>> = _apps.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    fun loadApps(filterGames: Boolean = false) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val installed = fileRepository.getInstalledApps(includeSystem = false)
                val apks = fileRepository.getApkFiles()
                
                _installedApps.value = if (filterGames) installed.filter { it.isGame } else installed
                _apkFiles.value = apks
                _apps.value = (_installedApps.value + _apkFiles.value).sortedBy { it.name }
            } catch (e: Exception) {
                _installedApps.value = emptyList()
                _apkFiles.value = emptyList()
                _apps.value = emptyList()
            } finally {
                _isLoading.value = false
            }
        }
    }
}
