package com.fyllo.filemanager.ui.screens

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.fyllo.filemanager.data.repository.TrashManager
import com.fyllo.filemanager.domain.model.TrashItem
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class TrashViewModel(private val trashManager: TrashManager) : ViewModel() {

    val trashItems: StateFlow<List<TrashItem>> = trashManager.trashItems
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun restoreItems(items: List<TrashItem>) {
        viewModelScope.launch {
            items.forEach { item ->
                trashManager.restoreFromTrash(item.id)
            }
        }
    }

    fun deleteItemsPermanently(items: List<TrashItem>) {
        viewModelScope.launch {
            items.forEach { item ->
                trashManager.deletePermanently(item.id)
            }
        }
    }

    fun emptyTrash() {
        viewModelScope.launch {
            trashManager.emptyTrash()
        }
    }
}

class TrashViewModelFactory(private val context: Context) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(TrashViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return TrashViewModel(TrashManager(context)) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
