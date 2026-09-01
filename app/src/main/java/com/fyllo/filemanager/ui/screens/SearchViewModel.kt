package com.fyllo.filemanager.ui.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fyllo.filemanager.domain.model.FileItem
import com.fyllo.filemanager.domain.repository.FileRepository
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch

@OptIn(FlowPreview::class)
class SearchViewModel(
    private val fileRepository: FileRepository
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _searchFilter = MutableStateFlow<String?>(null)
    val searchFilter: StateFlow<String?> = _searchFilter.asStateFlow()

    private val _searchResults = MutableStateFlow<List<FileItem>>(emptyList())
    val searchResults: StateFlow<List<FileItem>> = _searchResults.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    init {
        viewModelScope.launch {
            kotlinx.coroutines.flow.combine(_searchQuery, _searchFilter) { query, filter ->
                Pair(query, filter)
            }
                .debounce(300)
                .distinctUntilChanged()
                .collect { (query, filter) ->
                    if (query.isNotBlank() || filter != null) {
                        performSearch(query, filter)
                    } else {
                        _searchResults.value = emptyList()
                    }
                }
        }
    }

    fun onQueryChange(query: String) {
        _searchQuery.value = query
    }

    fun onFilterChange(filter: String?) {
        if (_searchFilter.value == filter) {
            _searchFilter.value = null // Toggle off
        } else {
            _searchFilter.value = filter
        }
    }

    private suspend fun performSearch(query: String, filter: String?) {
        _isLoading.value = true
        try {
            _searchResults.value = fileRepository.searchFiles(query, filter)
        } catch (e: Exception) {
            _searchResults.value = emptyList()
        } finally {
            _isLoading.value = false
        }
    }
}
