package com.fyllo.filemanager.domain.usecase

import com.fyllo.filemanager.domain.model.FileItem
import com.fyllo.filemanager.domain.repository.FileRepository

class SearchFilesUseCase(
    private val fileRepository: FileRepository
) {
    suspend operator fun invoke(query: String, filter: String? = null): List<FileItem> {
        if (query.isBlank()) return emptyList()
        return fileRepository.searchFiles(query, filter)
    }
}
