package com.fyllo.filemanager.ui.screens

import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import com.fyllo.filemanager.domain.model.FileItem
import com.fyllo.filemanager.ui.theme.NeonCyan
import java.io.File

@Composable
fun MediaViewerScreen(
    uriStr: String,
    onBackClick: () -> Unit,
    onNavigateToEdit: ((FileItem) -> Unit)? = null
) {
    val context = LocalContext.current
    var mediaList by remember { mutableStateOf<List<FileItem>>(emptyList()) }
    var initialIdx by remember { mutableStateOf(0) }
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(uriStr) {
        kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
            try {
                val uri = if (uriStr.startsWith("/")) Uri.fromFile(File(uriStr)) else Uri.parse(uriStr)
                val path = if (uri.scheme == "file") uri.path else if (uriStr.startsWith("/")) uriStr else null
                val fileName = uri.lastPathSegment ?: (if (path != null) File(path).name else "Media File")
                val ext = fileName.substringAfterLast('.', "").lowercase()

                val contentMime = try { context.contentResolver.getType(uri) } catch (e: Exception) { null }
                val isVideoExt = ext in listOf("mp4", "mkv", "avi", "mov", "3gp", "webm", "ts", "flv", "wmv", "m4v")
                val isVideo = contentMime?.startsWith("video/") == true || isVideoExt
                val mimeType = contentMime ?: if (isVideo) "video/mp4" else "image/*"

                val clickedItem = FileItem(
                    id = uri.toString(),
                    name = fileName,
                    uri = uri,
                    path = path ?: uri.toString(),
                    isFolder = false,
                    sizeBytes = if (path != null) File(path).length() else 0L,
                    lastModified = System.currentTimeMillis(),
                    mimeType = mimeType,
                    extension = ext
                )

                val siblings = mutableListOf<FileItem>()
                if (path != null) {
                    val currentFile = File(path)
                    val parent = currentFile.parentFile
                    if (parent != null && parent.exists() && parent.isDirectory) {
                        val mediaExtensions = setOf("jpg", "jpeg", "png", "gif", "webp", "heic", "bmp", "mp4", "mkv", "avi", "mov", "3gp", "webm", "ts", "flv", "wmv", "m4v")
                        parent.listFiles()?.filter { f ->
                            f.isFile && f.extension.lowercase() in mediaExtensions
                        }?.sortedBy { it.name.lowercase() }?.forEach { f ->
                            val fExt = f.extension.lowercase()
                            val fIsVid = fExt in listOf("mp4", "mkv", "avi", "mov", "3gp", "webm", "ts", "flv", "wmv", "m4v")
                            siblings.add(
                                FileItem(
                                    id = Uri.fromFile(f).toString(),
                                    name = f.name,
                                    uri = Uri.fromFile(f),
                                    path = f.absolutePath,
                                    isFolder = false,
                                    sizeBytes = f.length(),
                                    lastModified = f.lastModified(),
                                    mimeType = if (fIsVid) "video/mp4" else "image/*",
                                    extension = fExt
                                )
                            )
                        }
                    }
                }

                val finalItems = if (siblings.isNotEmpty()) siblings else listOf(clickedItem)
                val idx = finalItems.indexOfFirst { it.uri.toString() == uri.toString() || it.path == path || it.uri.toString() == uriStr }.coerceAtLeast(0)
                mediaList = finalItems
                initialIdx = idx
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                isLoading = false
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize().background(Color.Black)) {
        if (isLoading) {
            CircularProgressIndicator(
                color = NeonCyan,
                modifier = Modifier.align(Alignment.Center)
            )
        } else {
            if (mediaList.isNotEmpty()) {
                ImageViewerOverlay(
                    mediaFiles = mediaList,
                    initialIndex = initialIdx,
                    onDismiss = onBackClick,
                    onEdit = { file -> onNavigateToEdit?.invoke(file) },
                    onDeleteClick = { file ->
                        try {
                            if (file.uri.scheme == "file" && file.path.isNotEmpty()) {
                                File(file.path).delete()
                            }
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                        onBackClick()
                    },
                    onRenameClick = { _, _ -> },
                    onCopy = { },
                    onMove = { },
                    onCompress = { _, _ -> },
                    onSafeFolderClick = { }
                )
            }
        }
    }
}
