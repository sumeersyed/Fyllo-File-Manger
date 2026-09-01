package com.fyllo.filemanager.domain.model

sealed class FileOperationState {
    object Idle : FileOperationState()
    object Preparing : FileOperationState()
    data class Running(val progress: Float, val currentFile: String, val processedBytes: Long, val totalBytes: Long, val operationType: String = "") : FileOperationState()
    object Paused : FileOperationState()
    object Completed : FileOperationState()
    object Cancelled : FileOperationState()
    data class Failed(val error: FileError) : FileOperationState()
}
