package com.fyllo.filemanager.domain.repository

import android.net.Uri
import com.fyllo.filemanager.domain.model.FileItem
import com.fyllo.filemanager.domain.model.FileOperationState
import kotlinx.coroutines.flow.Flow

interface FileRepository {
    suspend fun listFiles(parentUri: Uri?): List<FileItem>
    suspend fun searchFiles(query: String, filter: String? = null): List<FileItem>
    suspend fun getRecentFiles(limit: Int = 10): List<FileItem>
    suspend fun getAllMediaFiles(): List<FileItem>
    suspend fun getAllImageFiles(): List<FileItem>
    suspend fun getAllVideoFiles(): List<FileItem>
    suspend fun getAllAudioFiles(): List<FileItem>
    suspend fun getAllDocumentFiles(): List<FileItem>
    suspend fun getAllPdfFiles(): List<FileItem>
    suspend fun getAllSlideFiles(): List<FileItem>
    suspend fun getAllSpreadsheetFiles(): List<FileItem>
    suspend fun getAllArchiveFiles(): List<FileItem>
    suspend fun getAllCodeFiles(): List<FileItem>
    suspend fun getAllTextFiles(): List<FileItem>
    suspend fun getFavoriteFiles(): List<FileItem>
    suspend fun addFavoriteFile(uri: Uri)
    suspend fun removeFavoriteFile(uri: Uri)
    suspend fun getInstalledApps(includeSystem: Boolean = false): List<com.fyllo.filemanager.domain.model.AppItem>
    suspend fun getApkFiles(): List<com.fyllo.filemanager.domain.model.AppItem>
    suspend fun getFolderSize(folderName: String): Long
    suspend fun getApkFilesSize(): Long
    suspend fun getCategoryCounts(): Map<String, Int>
    suspend fun getDetailedCategoryStats(): List<com.fyllo.filemanager.ui.screens.StorageCategoryDetail>
    suspend fun getLargestFiles(limit: Int = 50): List<FileItem>
    suspend fun getOldFiles(daysOld: Long = 180, limit: Int = 50): List<FileItem>
    suspend fun getDuplicateFiles(): List<FileItem>
    suspend fun getFolderStorageSizes(): List<com.fyllo.filemanager.ui.screens.FolderSpaceItem>
    
    fun copyFiles(sourceUris: List<Uri>, destUri: Uri, conflictStrategy: com.fyllo.filemanager.domain.model.ConflictStrategy = com.fyllo.filemanager.domain.model.ConflictStrategy.KEEP_BOTH): Flow<FileOperationState>
    fun moveFiles(sourceUris: List<Uri>, destUri: Uri, conflictStrategy: com.fyllo.filemanager.domain.model.ConflictStrategy = com.fyllo.filemanager.domain.model.ConflictStrategy.KEEP_BOTH): Flow<FileOperationState>
    fun deleteFiles(uris: List<Uri>, permanent: Boolean): Flow<FileOperationState>
    fun restoreFiles(uris: List<Uri>): Flow<FileOperationState>
    
    suspend fun checkConflicts(sourceUris: List<Uri>, destUri: Uri): Boolean
    
    suspend fun renameFile(uri: Uri, newName: String): FileItem?
    suspend fun createFolder(parentUri: Uri, name: String): FileItem?
    suspend fun getFileDetails(uri: Uri): FileItem?
    suspend fun calculateSize(uri: Uri): Long
    
    fun compressFiles(sourceUris: List<Uri>, destUri: Uri): Flow<FileOperationState>
    fun extractArchive(sourceUri: Uri, destUri: Uri): Flow<FileOperationState>
}
