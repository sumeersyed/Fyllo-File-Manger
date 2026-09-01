package com.fyllo.filemanager.domain.model

data class FileError(
    val code: ErrorCode,
    val message: String,
    val operation: String,
    val recoverable: Boolean,
    val technicalCause: Throwable? = null
)

enum class ErrorCode {
    PERMISSION_DENIED,
    FILE_NOT_FOUND,
    STORAGE_FULL,
    IO_ERROR,
    INVALID_URI,
    READ_ONLY_STORAGE,
    UNKNOWN
}
