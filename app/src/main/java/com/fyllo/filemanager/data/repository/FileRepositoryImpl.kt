package com.fyllo.filemanager.data.repository

import android.content.Context
import android.net.Uri
import android.provider.MediaStore
import androidx.documentfile.provider.DocumentFile
import com.fyllo.filemanager.domain.model.FileItem
import com.fyllo.filemanager.domain.model.FileOperationState
import com.fyllo.filemanager.domain.repository.FileRepository
import android.content.ContentUris
import com.fyllo.filemanager.domain.model.FileError
import com.fyllo.filemanager.domain.model.ErrorCode
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext
import java.io.File
import java.io.InputStream
import java.io.OutputStream
import java.util.UUID
import android.content.SharedPreferences
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.os.Environment
import com.fyllo.filemanager.domain.model.AppItem

class FileRepositoryImpl(
    private val context: Context,
    private val trashManager: TrashManager
) : FileRepository {

    private val prefs: SharedPreferences = context.getSharedPreferences("favorites_prefs", Context.MODE_PRIVATE)


    private fun getRealPathFromUri(uri: Uri): String? {
        if (uri.scheme == "file") return uri.path
        if (uri.scheme == "content") {
            try {
                val projection = arrayOf(MediaStore.MediaColumns.DATA)
                context.contentResolver.query(uri, projection, null, null, null)?.use { cursor ->
                    if (cursor.moveToFirst()) {
                        val col = cursor.getColumnIndex(MediaStore.MediaColumns.DATA)
                        if (col >= 0) {
                            val data = cursor.getString(col)
                            if (!data.isNullOrEmpty()) return data
                        }
                    }
                }
            } catch (e: Exception) { }
            try {
                val docId = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.KITKAT && android.provider.DocumentsContract.isDocumentUri(context, uri)) {
                    android.provider.DocumentsContract.getDocumentId(uri)
                } else {
                    uri.path
                }
                if (docId != null && docId.contains(":")) {
                    val split = docId.split(":")
                    val type = split[0]
                    val relativePath = if (split.size > 1) split[1] else ""
                    if ("primary".equals(type, ignoreCase = true)) {
                        val externalStorage = android.os.Environment.getExternalStorageDirectory().absolutePath
                        return "$externalStorage/$relativePath"
                    }
                }
            } catch (e: Exception) { }
        }
        return null
    }

    private fun resolveFileFromUri(uri: Uri): File? {
        val realPath = getRealPathFromUri(uri)
        if (realPath != null) {
            val f = File(realPath)
            if (f.exists()) return f
        }
        val path = uri.path
        if (path != null) {
            val f = File(path)
            if (f.exists()) return f
            try {
                val decodedPath = Uri.decode(path)
                val df = File(decodedPath)
                if (df.exists()) return df
            } catch (e: Exception) { }
        }
        return null
    }

    override suspend fun listFiles(parentUri: Uri?): List<FileItem> = withContext(Dispatchers.IO) {
        val result = mutableListOf<FileItem>()
        if (parentUri == null) return@withContext result
        
        // Handling both File scheme and DocumentFile/SAF scheme
        if (parentUri.scheme == "file") {
            val folder = File(parentUri.path ?: return@withContext result)
            folder.listFiles()?.forEach { file ->
                val isDir = file.isDirectory
                val count = if (isDir) {
                    try { file.list()?.size ?: 0 } catch (_: Exception) { 0 }
                } else null
                
                val size = if (file.isFile) {
                    file.length()
                } else {
                    // Quick shallow or folder estimate for responsiveness
                    try {
                        var dirSize = 0L
                        file.listFiles()?.forEach { child ->
                            if (child.isFile) dirSize += child.length()
                        }
                        dirSize
                    } catch (_: Exception) { 0L }
                }

                result.add(
                    FileItem(
                        id = UUID.randomUUID().toString(),
                        name = file.name,
                        uri = Uri.fromFile(file),
                        path = file.absolutePath,
                        isFolder = isDir,
                        sizeBytes = size,
                        lastModified = file.lastModified(),
                        mimeType = if (isDir) null else android.webkit.MimeTypeMap.getSingleton().getMimeTypeFromExtension(file.extension.lowercase()),
                        extension = file.extension,
                        itemCount = count
                    )
                )
            }
        } else {
            // DocumentFile via SAF
            val docFile = DocumentFile.fromTreeUri(context, parentUri) ?: return@withContext result
            docFile.listFiles().forEach { file ->
                val isDir = file.isDirectory
                result.add(
                    FileItem(
                        id = UUID.randomUUID().toString(),
                        name = file.name ?: "Unknown",
                        uri = file.uri,
                        path = file.uri.toString(),
                        isFolder = isDir,
                        sizeBytes = if (file.isFile) file.length() else 0L,
                        lastModified = file.lastModified(),
                        mimeType = file.type,
                        extension = "",
                        itemCount = null
                    )
                )
            }
        }
        result
    }

    override suspend fun searchFiles(query: String, filter: String?): List<FileItem> = withContext(Dispatchers.IO) {
        val result = mutableListOf<FileItem>()
        try {
            val projection = arrayOf(
                MediaStore.Files.FileColumns._ID,
                MediaStore.Files.FileColumns.DISPLAY_NAME,
                MediaStore.Files.FileColumns.DATA,
                MediaStore.Files.FileColumns.SIZE,
                MediaStore.Files.FileColumns.DATE_MODIFIED,
                MediaStore.Files.FileColumns.MIME_TYPE
            )
            val tokens = query.trim().split("\\s+".toRegex()).filter { it.isNotEmpty() }
            
            val selectionBuilder = StringBuilder()
            val selectionArgsList = mutableListOf<String>()

            // If there's a filter, we add it first
            when (filter) {
                "Images" -> {
                    selectionBuilder.append("${MediaStore.Files.FileColumns.MIME_TYPE} LIKE ?")
                    selectionArgsList.add("image/%")
                }
                "Videos" -> {
                    selectionBuilder.append("${MediaStore.Files.FileColumns.MIME_TYPE} LIKE ?")
                    selectionArgsList.add("video/%")
                }
                "Audio" -> {
                    selectionBuilder.append("${MediaStore.Files.FileColumns.MIME_TYPE} LIKE ?")
                    selectionArgsList.add("audio/%")
                }
                "Documents" -> {
                    selectionBuilder.append("(${MediaStore.Files.FileColumns.MIME_TYPE} LIKE ? OR ${MediaStore.Files.FileColumns.MIME_TYPE} LIKE ? OR ${MediaStore.Files.FileColumns.MIME_TYPE} LIKE ?)")
                    selectionArgsList.add("application/pdf")
                    selectionArgsList.add("application/msword")
                    selectionArgsList.add("text/plain")
                }
                "Archives" -> {
                    selectionBuilder.append("(${MediaStore.Files.FileColumns.MIME_TYPE} LIKE ? OR ${MediaStore.Files.FileColumns.MIME_TYPE} LIKE ?)")
                    selectionArgsList.add("application/zip")
                    selectionArgsList.add("application/x-rar-compressed")
                }
                "APKs" -> {
                    selectionBuilder.append("${MediaStore.Files.FileColumns.MIME_TYPE} LIKE ?")
                    selectionArgsList.add("application/vnd.android.package-archive")
                }
                else -> {
                    selectionBuilder.append("1=1") // Dummy condition
                }
            }

            if (tokens.isNotEmpty()) {
                selectionBuilder.append(" AND (")
                for (i in tokens.indices) {
                    if (i > 0) selectionBuilder.append(" AND ")
                    selectionBuilder.append("${MediaStore.Files.FileColumns.DISPLAY_NAME} LIKE ?")
                    selectionArgsList.add("%${tokens[i]}%")
                }
                selectionBuilder.append(")")
            }

            val selection = selectionBuilder.toString()
            val selectionArgs = selectionArgsList.toTypedArray()

            context.contentResolver.query(
                MediaStore.Files.getContentUri("external"),
                projection,
                selection,
                selectionArgs,
                null
            )?.use { cursor ->
                val idCol = cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns._ID)
                val nameCol = cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.DISPLAY_NAME)
                val dataCol = cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.DATA)
                val sizeCol = cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.SIZE)
                val dateCol = cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.DATE_MODIFIED)
                val mimeCol = cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.MIME_TYPE)

                while (cursor.moveToNext()) {
                    val path = cursor.getString(dataCol) ?: continue
                    val id = cursor.getLong(idCol)
                    val contentUri = ContentUris.withAppendedId(MediaStore.Files.getContentUri("external"), id)
                    result.add(
                        FileItem(
                            id = id.toString(),
                            name = cursor.getString(nameCol) ?: "Unknown",
                            uri = contentUri,
                            path = path,
                            isFolder = File(path).isDirectory,
                            sizeBytes = cursor.getLong(sizeCol),
                            lastModified = cursor.getLong(dateCol) * 1000,
                            mimeType = cursor.getString(mimeCol),
                            extension = File(path).extension,
                            itemCount = if (File(path).isDirectory) File(path).list()?.size else null
                        )
                    )
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        result
    }

    override suspend fun getRecentFiles(limit: Int): List<FileItem> = withContext(Dispatchers.IO) {
        val result = mutableListOf<FileItem>()
        val seenPaths = mutableSetOf<String>()
        try {
            val projection = arrayOf(
                MediaStore.Files.FileColumns._ID,
                MediaStore.Files.FileColumns.DISPLAY_NAME,
                MediaStore.Files.FileColumns.DATA,
                MediaStore.Files.FileColumns.SIZE,
                MediaStore.Files.FileColumns.DATE_MODIFIED,
                MediaStore.Files.FileColumns.MIME_TYPE,
                MediaStore.MediaColumns.WIDTH,
                MediaStore.MediaColumns.HEIGHT
            )
            
            val selection = "${MediaStore.Files.FileColumns.MIME_TYPE} IS NOT NULL AND " +
                    "${MediaStore.Files.FileColumns.MIME_TYPE} != '' AND " +
                    "${MediaStore.Files.FileColumns.SIZE} > 0"
            val sortOrder = "${MediaStore.Files.FileColumns.DATE_MODIFIED} DESC"

            context.contentResolver.query(
                MediaStore.Files.getContentUri("external"),
                projection,
                selection,
                null,
                sortOrder
            )?.use { cursor ->
                val idCol = cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns._ID)
                val nameCol = cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.DISPLAY_NAME)
                val dataCol = cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.DATA)
                val sizeCol = cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.SIZE)
                val dateCol = cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.DATE_MODIFIED)
                val mimeCol = cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.MIME_TYPE)

                val widthCol = cursor.getColumnIndex(MediaStore.MediaColumns.WIDTH)
                val heightCol = cursor.getColumnIndex(MediaStore.MediaColumns.HEIGHT)

                while (cursor.moveToNext() && result.size < limit) {
                    val path = cursor.getString(dataCol) ?: continue
                    val name = cursor.getString(nameCol) ?: File(path).name
                    
                    // Filter out hidden files, .Trash, .thumbnails, and non-existent files
                    if (name.startsWith(".") || path.contains("/.") || !File(path).exists()) continue
                    if (seenPaths.contains(path)) continue
                    seenPaths.add(path)

                    val id = cursor.getLong(idCol)
                    val contentUri = ContentUris.withAppendedId(MediaStore.Files.getContentUri("external"), id)
                    
                    var dims: String? = null
                    if (widthCol != -1 && heightCol != -1) {
                        val w = cursor.getInt(widthCol)
                        val h = cursor.getInt(heightCol)
                        if (w > 0 && h > 0) {
                            dims = "${w}x${h}"
                        }
                    }

                    result.add(
                        FileItem(
                            id = id.toString(),
                            name = name,
                            uri = contentUri,
                            path = path,
                            isFolder = false,
                            sizeBytes = cursor.getLong(sizeCol),
                            lastModified = cursor.getLong(dateCol) * 1000,
                            mimeType = cursor.getString(mimeCol),
                            extension = File(path).extension,
                            dimensions = dims
                        )
                    )
                }
            }

            // Fallback scan disk directories if MediaStore didn't return enough files
            if (result.size < limit) {
                val commonDirs = listOf(
                    android.os.Environment.getExternalStoragePublicDirectory(android.os.Environment.DIRECTORY_DOWNLOADS),
                    android.os.Environment.getExternalStoragePublicDirectory(android.os.Environment.DIRECTORY_DCIM),
                    android.os.Environment.getExternalStoragePublicDirectory(android.os.Environment.DIRECTORY_PICTURES),
                    android.os.Environment.getExternalStoragePublicDirectory(android.os.Environment.DIRECTORY_DOCUMENTS)
                )
                val diskFiles = mutableListOf<File>()
                for (dir in commonDirs) {
                    if (dir.exists() && dir.isDirectory) {
                        dir.walkTopDown().maxDepth(3).forEach { file ->
                            if (file.isFile && !file.name.startsWith(".") && !file.path.contains("/.") && file.length() > 0) {
                                diskFiles.add(file)
                            }
                        }
                    }
                }
                diskFiles.sortByDescending { it.lastModified() }
                for (file in diskFiles) {
                    if (result.size >= limit) break
                    if (seenPaths.contains(file.absolutePath)) continue
                    seenPaths.add(file.absolutePath)

                    val ext = file.extension.lowercase()
                    val mime = android.webkit.MimeTypeMap.getSingleton().getMimeTypeFromExtension(ext)

                    result.add(
                        FileItem(
                            id = UUID.randomUUID().toString(),
                            name = file.name,
                            uri = Uri.fromFile(file),
                            path = file.absolutePath,
                            isFolder = false,
                            sizeBytes = file.length(),
                            lastModified = file.lastModified(),
                            mimeType = mime,
                            extension = ext
                        )
                    )
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        result
    }

    override suspend fun getAllMediaFiles(): List<FileItem> = withContext(Dispatchers.IO) {
        val result = mutableListOf<FileItem>()
        try {
            val projection = arrayOf(
                MediaStore.Files.FileColumns._ID,
                MediaStore.Files.FileColumns.DISPLAY_NAME,
                MediaStore.Files.FileColumns.DATA,
                MediaStore.Files.FileColumns.SIZE,
                MediaStore.Files.FileColumns.DATE_MODIFIED,
                MediaStore.Files.FileColumns.MIME_TYPE,
                MediaStore.MediaColumns.WIDTH,
                MediaStore.MediaColumns.HEIGHT
            )
            
            val selection = "${MediaStore.Files.FileColumns.MEDIA_TYPE} = ? OR ${MediaStore.Files.FileColumns.MEDIA_TYPE} = ?"
            val selectionArgs = arrayOf(
                MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE.toString(),
                MediaStore.Files.FileColumns.MEDIA_TYPE_VIDEO.toString()
            )
            val sortOrder = "${MediaStore.Files.FileColumns.DATE_MODIFIED} DESC"

            context.contentResolver.query(
                MediaStore.Files.getContentUri("external"),
                projection,
                selection,
                selectionArgs,
                sortOrder
            )?.use { cursor ->
                val idCol = cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns._ID)
                val nameCol = cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.DISPLAY_NAME)
                val dataCol = cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.DATA)
                val sizeCol = cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.SIZE)
                val dateCol = cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.DATE_MODIFIED)
                val mimeCol = cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.MIME_TYPE)

                val widthCol = cursor.getColumnIndex(MediaStore.MediaColumns.WIDTH)
                val heightCol = cursor.getColumnIndex(MediaStore.MediaColumns.HEIGHT)

                while (cursor.moveToNext()) {
                    val path = cursor.getString(dataCol)
                    val id = cursor.getLong(idCol)
                    val contentUri = ContentUris.withAppendedId(MediaStore.Files.getContentUri("external"), id)
                    
                    var dims: String? = null
                    if (widthCol != -1 && heightCol != -1) {
                        val w = cursor.getInt(widthCol)
                        val h = cursor.getInt(heightCol)
                        if (w > 0 && h > 0) {
                            dims = "${w}x${h}"
                        }
                    }

                    result.add(
                        FileItem(
                            id = id.toString(),
                            name = cursor.getString(nameCol) ?: "Unknown",
                            uri = contentUri,
                            path = path,
                            isFolder = false,
                            sizeBytes = cursor.getLong(sizeCol),
                            lastModified = cursor.getLong(dateCol) * 1000,
                            mimeType = cursor.getString(mimeCol),
                            extension = File(path).extension,
                            dimensions = dims
                        )
                    )
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        result
    }

    override suspend fun getAllImageFiles(): List<FileItem> = withContext(Dispatchers.IO) {
        val result = mutableListOf<FileItem>()
        try {
            val projection = arrayOf(
                MediaStore.Files.FileColumns._ID,
                MediaStore.Files.FileColumns.DISPLAY_NAME,
                MediaStore.Files.FileColumns.DATA,
                MediaStore.Files.FileColumns.SIZE,
                MediaStore.Files.FileColumns.DATE_MODIFIED,
                MediaStore.Files.FileColumns.MIME_TYPE,
                MediaStore.MediaColumns.WIDTH,
                MediaStore.MediaColumns.HEIGHT
            )
            val selection = "${MediaStore.Files.FileColumns.MEDIA_TYPE} = ?"
            val selectionArgs = arrayOf(MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE.toString())
            val sortOrder = "${MediaStore.Files.FileColumns.DATE_MODIFIED} DESC"

            context.contentResolver.query(
                MediaStore.Files.getContentUri("external"),
                projection,
                selection,
                selectionArgs,
                sortOrder
            )?.use { cursor ->
                val idCol = cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns._ID)
                val nameCol = cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.DISPLAY_NAME)
                val dataCol = cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.DATA)
                val sizeCol = cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.SIZE)
                val dateCol = cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.DATE_MODIFIED)
                val mimeCol = cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.MIME_TYPE)
                val widthCol = cursor.getColumnIndex(MediaStore.MediaColumns.WIDTH)
                val heightCol = cursor.getColumnIndex(MediaStore.MediaColumns.HEIGHT)

                while (cursor.moveToNext()) {
                    val path = cursor.getString(dataCol) ?: continue
                    val id = cursor.getLong(idCol)
                    val contentUri = ContentUris.withAppendedId(MediaStore.Files.getContentUri("external"), id)
                    var dims: String? = null
                    if (widthCol != -1 && heightCol != -1) {
                        val w = cursor.getInt(widthCol)
                        val h = cursor.getInt(heightCol)
                        if (w > 0 && h > 0) dims = "${w}x${h}"
                    }
                    result.add(
                        FileItem(
                            id = id.toString(),
                            name = cursor.getString(nameCol) ?: "Unknown",
                            uri = contentUri,
                            path = path,
                            isFolder = false,
                            sizeBytes = cursor.getLong(sizeCol),
                            lastModified = cursor.getLong(dateCol) * 1000,
                            mimeType = cursor.getString(mimeCol),
                            extension = File(path).extension,
                            dimensions = dims
                        )
                    )
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        result
    }

    override suspend fun getAllVideoFiles(): List<FileItem> = withContext(Dispatchers.IO) {
        val result = mutableListOf<FileItem>()
        try {
            val projection = arrayOf(
                MediaStore.Files.FileColumns._ID,
                MediaStore.Files.FileColumns.DISPLAY_NAME,
                MediaStore.Files.FileColumns.DATA,
                MediaStore.Files.FileColumns.SIZE,
                MediaStore.Files.FileColumns.DATE_MODIFIED,
                MediaStore.Files.FileColumns.MIME_TYPE,
                MediaStore.MediaColumns.WIDTH,
                MediaStore.MediaColumns.HEIGHT
            )
            val selection = "${MediaStore.Files.FileColumns.MEDIA_TYPE} = ?"
            val selectionArgs = arrayOf(MediaStore.Files.FileColumns.MEDIA_TYPE_VIDEO.toString())
            val sortOrder = "${MediaStore.Files.FileColumns.DATE_MODIFIED} DESC"

            context.contentResolver.query(
                MediaStore.Files.getContentUri("external"),
                projection,
                selection,
                selectionArgs,
                sortOrder
            )?.use { cursor ->
                val idCol = cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns._ID)
                val nameCol = cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.DISPLAY_NAME)
                val dataCol = cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.DATA)
                val sizeCol = cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.SIZE)
                val dateCol = cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.DATE_MODIFIED)
                val mimeCol = cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.MIME_TYPE)
                val widthCol = cursor.getColumnIndex(MediaStore.MediaColumns.WIDTH)
                val heightCol = cursor.getColumnIndex(MediaStore.MediaColumns.HEIGHT)

                while (cursor.moveToNext()) {
                    val path = cursor.getString(dataCol) ?: continue
                    val id = cursor.getLong(idCol)
                    val contentUri = ContentUris.withAppendedId(MediaStore.Files.getContentUri("external"), id)
                    var dims: String? = null
                    if (widthCol != -1 && heightCol != -1) {
                        val w = cursor.getInt(widthCol)
                        val h = cursor.getInt(heightCol)
                        if (w > 0 && h > 0) dims = "${w}x${h}"
                    }
                    result.add(
                        FileItem(
                            id = id.toString(),
                            name = cursor.getString(nameCol) ?: "Unknown",
                            uri = contentUri,
                            path = path,
                            isFolder = false,
                            sizeBytes = cursor.getLong(sizeCol),
                            lastModified = cursor.getLong(dateCol) * 1000,
                            mimeType = cursor.getString(mimeCol),
                            extension = File(path).extension,
                            dimensions = dims
                        )
                    )
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        result
    }

    override suspend fun getAllAudioFiles(): List<FileItem> = withContext(Dispatchers.IO) {
        val result = mutableListOf<FileItem>()
        try {
            val projection = arrayOf(
                MediaStore.Files.FileColumns._ID,
                MediaStore.Files.FileColumns.DISPLAY_NAME,
                MediaStore.Files.FileColumns.DATA,
                MediaStore.Files.FileColumns.SIZE,
                MediaStore.Files.FileColumns.DATE_MODIFIED,
                MediaStore.Files.FileColumns.MIME_TYPE
            )
            
            val selection = "${MediaStore.Files.FileColumns.MEDIA_TYPE} = ?"
            val selectionArgs = arrayOf(MediaStore.Files.FileColumns.MEDIA_TYPE_AUDIO.toString())
            val sortOrder = "${MediaStore.Files.FileColumns.DISPLAY_NAME} ASC"

            context.contentResolver.query(
                MediaStore.Files.getContentUri("external"),
                projection,
                selection,
                selectionArgs,
                sortOrder
            )?.use { cursor ->
                val idCol = cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns._ID)
                val nameCol = cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.DISPLAY_NAME)
                val dataCol = cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.DATA)
                val sizeCol = cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.SIZE)
                val dateCol = cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.DATE_MODIFIED)
                val mimeCol = cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.MIME_TYPE)

                while (cursor.moveToNext()) {
                    val path = cursor.getString(dataCol)
                    val id = cursor.getLong(idCol)
                    val contentUri = ContentUris.withAppendedId(MediaStore.Files.getContentUri("external"), id)
                    result.add(
                        FileItem(
                            id = id.toString(),
                            name = cursor.getString(nameCol) ?: "Unknown",
                            uri = contentUri,
                            path = path,
                            isFolder = false,
                            sizeBytes = cursor.getLong(sizeCol),
                            lastModified = cursor.getLong(dateCol) * 1000,
                            mimeType = cursor.getString(mimeCol),
                            extension = File(path).extension
                        )
                    )
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        result
    }

    override suspend fun getAllDocumentFiles(): List<FileItem> = withContext(Dispatchers.IO) {
        val result = mutableListOf<FileItem>()
        try {
            val projection = arrayOf(
                MediaStore.Files.FileColumns._ID,
                MediaStore.Files.FileColumns.DISPLAY_NAME,
                MediaStore.Files.FileColumns.DATA,
                MediaStore.Files.FileColumns.SIZE,
                MediaStore.Files.FileColumns.DATE_MODIFIED,
                MediaStore.Files.FileColumns.MIME_TYPE
            )
            val docSelection = "${MediaStore.Files.FileColumns.MIME_TYPE} LIKE ? OR " +
                    "${MediaStore.Files.FileColumns.MIME_TYPE} LIKE ? OR " +
                    "${MediaStore.Files.FileColumns.MIME_TYPE} LIKE ? OR " +
                    "${MediaStore.Files.FileColumns.MIME_TYPE} LIKE ? OR " +
                    "${MediaStore.Files.FileColumns.MIME_TYPE} LIKE ? OR " +
                    "${MediaStore.Files.FileColumns.DATA} LIKE ? OR " +
                    "${MediaStore.Files.FileColumns.DATA} LIKE ? OR " +
                    "${MediaStore.Files.FileColumns.DATA} LIKE ? OR " +
                    "${MediaStore.Files.FileColumns.DATA} LIKE ? OR " +
                    "${MediaStore.Files.FileColumns.DATA} LIKE ? OR " +
                    "${MediaStore.Files.FileColumns.DATA} LIKE ? OR " +
                    "${MediaStore.Files.FileColumns.DATA} LIKE ? OR " +
                    "${MediaStore.Files.FileColumns.DATA} LIKE ? OR " +
                    "${MediaStore.Files.FileColumns.DATA} LIKE ?"
            val docArgs = arrayOf(
                "application/pdf", "application/msword", "text/%", "application/vnd.ms-%", "application/vnd.openxmlformats-%",
                "%.pdf", "%.doc%", "%.xls%", "%.ppt%", "%.txt", "%.csv", "%.json", "%.xml", "%.epub"
            )
            val sortOrder = "${MediaStore.Files.FileColumns.DATE_MODIFIED} DESC"

            context.contentResolver.query(
                MediaStore.Files.getContentUri("external"),
                projection,
                docSelection,
                docArgs,
                sortOrder
            )?.use { cursor ->
                val idCol = cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns._ID)
                val nameCol = cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.DISPLAY_NAME)
                val dataCol = cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.DATA)
                val sizeCol = cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.SIZE)
                val dateCol = cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.DATE_MODIFIED)
                val mimeCol = cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.MIME_TYPE)

                while (cursor.moveToNext()) {
                    val path = cursor.getString(dataCol) ?: continue
                    val id = cursor.getLong(idCol)
                    val contentUri = ContentUris.withAppendedId(MediaStore.Files.getContentUri("external"), id)
                    result.add(
                        FileItem(
                            id = id.toString(),
                            name = cursor.getString(nameCol) ?: File(path).name,
                            uri = contentUri,
                            path = path,
                            isFolder = false,
                            sizeBytes = cursor.getLong(sizeCol),
                            lastModified = cursor.getLong(dateCol) * 1000,
                            mimeType = cursor.getString(mimeCol),
                            extension = File(path).extension
                        )
                    )
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        result
    }

    private suspend fun queryFilesBySelection(selection: String, selectionArgs: Array<String>): List<FileItem> = withContext(Dispatchers.IO) {
        val result = mutableListOf<FileItem>()
        try {
            val projection = arrayOf(
                MediaStore.Files.FileColumns._ID,
                MediaStore.Files.FileColumns.DISPLAY_NAME,
                MediaStore.Files.FileColumns.DATA,
                MediaStore.Files.FileColumns.SIZE,
                MediaStore.Files.FileColumns.DATE_MODIFIED,
                MediaStore.Files.FileColumns.MIME_TYPE
            )
            val sortOrder = "${MediaStore.Files.FileColumns.DATE_MODIFIED} DESC"
            context.contentResolver.query(
                MediaStore.Files.getContentUri("external"),
                projection,
                selection,
                selectionArgs,
                sortOrder
            )?.use { cursor ->
                val idCol = cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns._ID)
                val nameCol = cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.DISPLAY_NAME)
                val dataCol = cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.DATA)
                val sizeCol = cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.SIZE)
                val dateCol = cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.DATE_MODIFIED)
                val mimeCol = cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.MIME_TYPE)

                while (cursor.moveToNext()) {
                    val path = cursor.getString(dataCol) ?: continue
                    val id = cursor.getLong(idCol)
                    val contentUri = ContentUris.withAppendedId(MediaStore.Files.getContentUri("external"), id)
                    result.add(
                        FileItem(
                            id = id.toString(),
                            name = cursor.getString(nameCol) ?: File(path).name,
                            uri = contentUri,
                            path = path,
                            isFolder = false,
                            sizeBytes = cursor.getLong(sizeCol),
                            lastModified = cursor.getLong(dateCol) * 1000,
                            mimeType = cursor.getString(mimeCol),
                            extension = File(path).extension
                        )
                    )
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        result
    }

    override suspend fun getAllPdfFiles(): List<FileItem> = withContext(Dispatchers.IO) {
        val selection = "${MediaStore.Files.FileColumns.MIME_TYPE} = ? OR ${MediaStore.Files.FileColumns.DATA} LIKE ?"
        val args = arrayOf("application/pdf", "%.pdf")
        queryFilesBySelection(selection, args)
    }

    override suspend fun getAllSlideFiles(): List<FileItem> = withContext(Dispatchers.IO) {
        val selection = "${MediaStore.Files.FileColumns.MIME_TYPE} LIKE ? OR ${MediaStore.Files.FileColumns.MIME_TYPE} LIKE ? OR ${MediaStore.Files.FileColumns.DATA} LIKE ? OR ${MediaStore.Files.FileColumns.DATA} LIKE ? OR ${MediaStore.Files.FileColumns.DATA} LIKE ?"
        val args = arrayOf("%powerpoint%", "%presentation%", "%.ppt%", "%.pptx%", "%.key")
        queryFilesBySelection(selection, args)
    }

    override suspend fun getAllSpreadsheetFiles(): List<FileItem> = withContext(Dispatchers.IO) {
        val selection = "${MediaStore.Files.FileColumns.MIME_TYPE} LIKE ? OR ${MediaStore.Files.FileColumns.MIME_TYPE} LIKE ? OR ${MediaStore.Files.FileColumns.DATA} LIKE ? OR ${MediaStore.Files.FileColumns.DATA} LIKE ? OR ${MediaStore.Files.FileColumns.DATA} LIKE ?"
        val args = arrayOf("%excel%", "%spreadsheet%", "%.xls%", "%.xlsx%", "%.csv")
        queryFilesBySelection(selection, args)
    }

    override suspend fun getAllArchiveFiles(): List<FileItem> = withContext(Dispatchers.IO) {
        val selection = "${MediaStore.Files.FileColumns.MIME_TYPE} LIKE ? OR ${MediaStore.Files.FileColumns.DATA} LIKE ? OR ${MediaStore.Files.FileColumns.DATA} LIKE ? OR ${MediaStore.Files.FileColumns.DATA} LIKE ? OR ${MediaStore.Files.FileColumns.DATA} LIKE ? OR ${MediaStore.Files.FileColumns.DATA} LIKE ?"
        val args = arrayOf("application/zip", "%.zip", "%.rar", "%.7z", "%.tar%", "%.gz")
        queryFilesBySelection(selection, args)
    }

    override suspend fun getAllCodeFiles(): List<FileItem> = withContext(Dispatchers.IO) {
        val selection = "${MediaStore.Files.FileColumns.DATA} LIKE ? OR ${MediaStore.Files.FileColumns.DATA} LIKE ? OR ${MediaStore.Files.FileColumns.DATA} LIKE ? OR ${MediaStore.Files.FileColumns.DATA} LIKE ? OR ${MediaStore.Files.FileColumns.DATA} LIKE ? OR ${MediaStore.Files.FileColumns.DATA} LIKE ? OR ${MediaStore.Files.FileColumns.DATA} LIKE ? OR ${MediaStore.Files.FileColumns.DATA} LIKE ? OR ${MediaStore.Files.FileColumns.DATA} LIKE ? OR ${MediaStore.Files.FileColumns.DATA} LIKE ?"
        val args = arrayOf("%.kt", "%.java", "%.py", "%.js", "%.ts", "%.json", "%.xml", "%.html", "%.css", "%.cpp")
        queryFilesBySelection(selection, args)
    }

    override suspend fun getAllTextFiles(): List<FileItem> = withContext(Dispatchers.IO) {
        val selection = "${MediaStore.Files.FileColumns.MIME_TYPE} = ? OR ${MediaStore.Files.FileColumns.DATA} LIKE ? OR ${MediaStore.Files.FileColumns.DATA} LIKE ?"
        val args = arrayOf("text/plain", "%.txt", "%.log")
        queryFilesBySelection(selection, args)
    }

    override suspend fun getFavoriteFiles(): List<FileItem> = withContext(Dispatchers.IO) {
        val result = mutableListOf<FileItem>()
        // IMPORTANT: ImageViewerOverlay saves favorites to "media_favorites" prefs
        // with the URI string as key and true/false as value.
        // We read from the same prefs to stay in sync.
        val mediaFavPrefs = context.getSharedPreferences("media_favorites", Context.MODE_PRIVATE)
        val favoriteUriStrings = mediaFavPrefs.all
            .filter { (_, value) -> value == true }
            .keys
            .toList()

        if (favoriteUriStrings.isEmpty()) return@withContext result

        try {
            for (uriString in favoriteUriStrings) {
                val uri = Uri.parse(uriString)
                val projection = arrayOf(
                    MediaStore.Files.FileColumns._ID,
                    MediaStore.Files.FileColumns.DISPLAY_NAME,
                    MediaStore.Files.FileColumns.DATA,
                    MediaStore.Files.FileColumns.SIZE,
                    MediaStore.Files.FileColumns.DATE_MODIFIED,
                    MediaStore.Files.FileColumns.MIME_TYPE,
                    MediaStore.MediaColumns.WIDTH,
                    MediaStore.MediaColumns.HEIGHT
                )
                context.contentResolver.query(uri, projection, null, null, null)?.use { cursor ->
                    if (cursor.moveToFirst()) {
                        val nameIdx = cursor.getColumnIndex(MediaStore.Files.FileColumns.DISPLAY_NAME)
                        val dataIdx = cursor.getColumnIndex(MediaStore.Files.FileColumns.DATA)
                        val sizeIdx = cursor.getColumnIndex(MediaStore.Files.FileColumns.SIZE)
                        val dateIdx = cursor.getColumnIndex(MediaStore.Files.FileColumns.DATE_MODIFIED)
                        val mimeIdx = cursor.getColumnIndex(MediaStore.Files.FileColumns.MIME_TYPE)
                        val widthIdx = cursor.getColumnIndex(MediaStore.MediaColumns.WIDTH)
                        val heightIdx = cursor.getColumnIndex(MediaStore.MediaColumns.HEIGHT)

                        val path = if (dataIdx >= 0) cursor.getString(dataIdx) else null
                        val name = if (nameIdx >= 0) cursor.getString(nameIdx) else uri.lastPathSegment ?: "Unknown"
                        
                        var dims: String? = null
                        if (widthIdx != -1 && heightIdx != -1) {
                            val w = cursor.getInt(widthIdx)
                            val h = cursor.getInt(heightIdx)
                            if (w > 0 && h > 0) {
                                dims = "${w}x${h}"
                            }
                        }

                        result.add(
                            FileItem(
                                id = uriString,
                                name = name ?: "Unknown",
                                uri = uri,
                                path = path ?: "",
                                isFolder = false,
                                sizeBytes = if (sizeIdx >= 0) cursor.getLong(sizeIdx) else 0L,
                                lastModified = if (dateIdx >= 0) cursor.getLong(dateIdx) * 1000 else 0L,
                                mimeType = if (mimeIdx >= 0) cursor.getString(mimeIdx) else null,
                                extension = File(path ?: "").extension,
                                dimensions = dims
                            )
                        )
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        result
    }

    override suspend fun addFavoriteFile(uri: Uri) = withContext(Dispatchers.IO) {
        val uris = prefs.getStringSet("favorites", emptySet())?.toMutableSet() ?: mutableSetOf()
        uris.add(uri.toString())
        prefs.edit().putStringSet("favorites", uris).apply()
    }

    override suspend fun removeFavoriteFile(uri: Uri) = withContext(Dispatchers.IO) {
        val uris = prefs.getStringSet("favorites", emptySet())?.toMutableSet() ?: mutableSetOf()
        uris.remove(uri.toString())
        prefs.edit().putStringSet("favorites", uris).apply()
    }

    override suspend fun getInstalledApps(includeSystem: Boolean): List<AppItem> = withContext(Dispatchers.IO) {
        val pm = context.packageManager
        val packages = pm.getInstalledApplications(PackageManager.GET_META_DATA)
        val result = mutableListOf<AppItem>()

        for (appInfo in packages) {
            val isSystem = (appInfo.flags and ApplicationInfo.FLAG_SYSTEM) != 0 || (appInfo.flags and ApplicationInfo.FLAG_UPDATED_SYSTEM_APP) != 0
            if (isSystem && !includeSystem) continue

            val name = pm.getApplicationLabel(appInfo).toString()
            val packageName = appInfo.packageName
            val sourceDir = appInfo.sourceDir
            val size = if (sourceDir != null) File(sourceDir).length() else 0L
            val isGame = appInfo.category == ApplicationInfo.CATEGORY_GAME || (appInfo.flags and ApplicationInfo.FLAG_IS_GAME) != 0

            result.add(
                AppItem(
                    id = packageName,
                    name = name,
                    packageName = packageName,
                    sizeBytes = size,
                    isGame = isGame,
                    isSystemApp = isSystem,
                    isApk = false,
                    apkPath = null
                )
            )
        }
        result.sortedBy { it.name.lowercase() }
    }

    override suspend fun getApkFiles(): List<AppItem> = withContext(Dispatchers.IO) {
        val result = mutableListOf<AppItem>()
        val pm = context.packageManager
        try {
            val projection = arrayOf(
                MediaStore.Files.FileColumns._ID,
                MediaStore.Files.FileColumns.DISPLAY_NAME,
                MediaStore.Files.FileColumns.DATA,
                MediaStore.Files.FileColumns.SIZE
            )
            
            val selection = "${MediaStore.Files.FileColumns.MIME_TYPE} = ? OR ${MediaStore.Files.FileColumns.DATA} LIKE ?"
            val selectionArgs = arrayOf("application/vnd.android.package-archive", "%.apk")
            
            context.contentResolver.query(
                MediaStore.Files.getContentUri("external"),
                projection,
                selection,
                selectionArgs,
                null
            )?.use { cursor ->
                val idCol = cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns._ID)
                val nameCol = cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.DISPLAY_NAME)
                val dataCol = cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.DATA)
                val sizeCol = cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.SIZE)

                while (cursor.moveToNext()) {
                    val path = cursor.getString(dataCol)
                    val id = cursor.getLong(idCol)
                    val size = cursor.getLong(sizeCol)
                    var name = cursor.getString(nameCol) ?: "Unknown"
                    var packageName = "unknown"
                    
                    // Extract info from APK
                    val packageInfo = pm.getPackageArchiveInfo(path, 0)
                    if (packageInfo != null) {
                        packageInfo.applicationInfo.sourceDir = path
                        packageInfo.applicationInfo.publicSourceDir = path
                        name = pm.getApplicationLabel(packageInfo.applicationInfo).toString()
                        packageName = packageInfo.packageName
                    }

                    result.add(
                        AppItem(
                            id = id.toString(),
                            name = name,
                            packageName = packageName,
                            sizeBytes = size,
                            isGame = false, // Hard to detect if APK is a game without deep inspection
                            isSystemApp = false,
                            isApk = true,
                            apkPath = path
                        )
                    )
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        // Deep scan filesystem directories for any APKs not yet indexed in MediaStore
        try {
            val seenPaths = result.mapNotNull { it.apkPath }.toMutableSet()
            val commonDirs = listOf(
                Environment.getExternalStorageDirectory(),
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS),
                File(Environment.getExternalStorageDirectory(), "Telegram"),
                File(Environment.getExternalStorageDirectory(), "WhatsApp"),
                File(Environment.getExternalStorageDirectory(), "Android/media")
            )
            for (dir in commonDirs) {
                if (dir.exists() && dir.isDirectory) {
                    val tree = dir.walkTopDown().maxDepth(5)
                    for (file in tree) {
                        if (file.isFile && file.extension.equals("apk", ignoreCase = true) && !file.name.startsWith(".")) {
                            if (!seenPaths.contains(file.absolutePath)) {
                                seenPaths.add(file.absolutePath)
                                var name = file.nameWithoutExtension
                                var pkg = "unknown"
                                try {
                                    val info = pm.getPackageArchiveInfo(file.absolutePath, 0)
                                    if (info != null) {
                                        info.applicationInfo.sourceDir = file.absolutePath
                                        info.applicationInfo.publicSourceDir = file.absolutePath
                                        name = pm.getApplicationLabel(info.applicationInfo).toString()
                                        pkg = info.packageName
                                    }
                                } catch (_: Exception) {}

                                result.add(
                                    AppItem(
                                        id = file.absolutePath,
                                        name = name,
                                        packageName = pkg,
                                        sizeBytes = file.length(),
                                        isGame = false,
                                        isSystemApp = false,
                                        isApk = true,
                                        apkPath = file.absolutePath
                                    )
                                )
                            }
                        }
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        result.distinctBy { it.apkPath ?: it.id }.sortedBy { it.name.lowercase() }
    }

    override suspend fun getFolderSize(folderName: String): Long = withContext(Dispatchers.IO) {
        var totalSize = 0L
        try {
            val projection = arrayOf(MediaStore.Files.FileColumns.SIZE)
            val selection = "${MediaStore.Files.FileColumns.DATA} LIKE ?"
            val selectionArgs = arrayOf("%/$folderName/%")

            context.contentResolver.query(
                MediaStore.Files.getContentUri("external"),
                projection,
                selection,
                selectionArgs,
                null
            )?.use { cursor ->
                val sizeCol = cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.SIZE)
                while (cursor.moveToNext()) {
                    totalSize += cursor.getLong(sizeCol)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        totalSize
    }

    override suspend fun getApkFilesSize(): Long = withContext(Dispatchers.IO) {
        var totalSize = 0L
        try {
            val projection = arrayOf(MediaStore.Files.FileColumns.SIZE)
            val selection = "${MediaStore.Files.FileColumns.MIME_TYPE} = ? OR ${MediaStore.Files.FileColumns.DATA} LIKE ?"
            val selectionArgs = arrayOf("application/vnd.android.package-archive", "%.apk")

            context.contentResolver.query(
                MediaStore.Files.getContentUri("external"),
                projection,
                selection,
                selectionArgs,
                null
            )?.use { cursor ->
                val sizeCol = cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.SIZE)
                while (cursor.moveToNext()) {
                    totalSize += cursor.getLong(sizeCol)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        totalSize
    }

    override suspend fun getCategoryCounts(): Map<String, Int> = withContext(Dispatchers.IO) {
        val counts = mutableMapOf<String, Int>()
        
        fun countUri(uri: Uri, selection: String? = null, selectionArgs: Array<String>? = null): Int {
            return try {
                context.contentResolver.query(
                    uri,
                    arrayOf(MediaStore.Files.FileColumns._ID),
                    selection,
                    selectionArgs,
                    null
                )?.use { it.count } ?: 0
            } catch (e: Exception) {
                0
            }
        }

        counts["Images"] = countUri(MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        counts["Videos"] = countUri(MediaStore.Video.Media.EXTERNAL_CONTENT_URI)
        counts["Audio"] = countUri(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI)
        
        val docSelection = "${MediaStore.Files.FileColumns.MIME_TYPE} LIKE ? OR " +
                "${MediaStore.Files.FileColumns.MIME_TYPE} LIKE ? OR " +
                "${MediaStore.Files.FileColumns.MIME_TYPE} LIKE ? OR " +
                "${MediaStore.Files.FileColumns.MIME_TYPE} LIKE ? OR " +
                "${MediaStore.Files.FileColumns.MIME_TYPE} LIKE ? OR " +
                "${MediaStore.Files.FileColumns.DATA} LIKE ? OR " +
                "${MediaStore.Files.FileColumns.DATA} LIKE ? OR " +
                "${MediaStore.Files.FileColumns.DATA} LIKE ? OR " +
                "${MediaStore.Files.FileColumns.DATA} LIKE ? OR " +
                "${MediaStore.Files.FileColumns.DATA} LIKE ? OR " +
                "${MediaStore.Files.FileColumns.DATA} LIKE ? OR " +
                "${MediaStore.Files.FileColumns.DATA} LIKE ? OR " +
                "${MediaStore.Files.FileColumns.DATA} LIKE ? OR " +
                "${MediaStore.Files.FileColumns.DATA} LIKE ?"
        val docArgs = arrayOf(
            "application/pdf", "application/msword", "text/%", "application/vnd.ms-%", "application/vnd.openxmlformats-%",
            "%.pdf", "%.doc%", "%.xls%", "%.ppt%", "%.txt", "%.csv", "%.json", "%.xml", "%.epub"
        )
        counts["Documents"] = countUri(MediaStore.Files.getContentUri("external"), docSelection, docArgs)

        val apkSelection = "${MediaStore.Files.FileColumns.MIME_TYPE} = ? OR ${MediaStore.Files.FileColumns.DATA} LIKE ?"
        val apkArgs = arrayOf("application/vnd.android.package-archive", "%.apk")
        counts["APKs"] = countUri(MediaStore.Files.getContentUri("external"), apkSelection, apkArgs)

        val pdfSelection = "${MediaStore.Files.FileColumns.MIME_TYPE} = ? OR ${MediaStore.Files.FileColumns.DATA} LIKE ?"
        val pdfArgs = arrayOf("application/pdf", "%.pdf")
        counts["PDFs"] = countUri(MediaStore.Files.getContentUri("external"), pdfSelection, pdfArgs)

        val slidesSelection = "${MediaStore.Files.FileColumns.MIME_TYPE} LIKE ? OR ${MediaStore.Files.FileColumns.DATA} LIKE ? OR ${MediaStore.Files.FileColumns.DATA} LIKE ?"
        val slidesArgs = arrayOf("%powerpoint%", "%.ppt%", "%.key")
        counts["Slides"] = countUri(MediaStore.Files.getContentUri("external"), slidesSelection, slidesArgs)

        val sheetsSelection = "${MediaStore.Files.FileColumns.MIME_TYPE} LIKE ? OR ${MediaStore.Files.FileColumns.DATA} LIKE ? OR ${MediaStore.Files.FileColumns.DATA} LIKE ?"
        val sheetsArgs = arrayOf("%excel%", "%.xls%", "%.csv")
        counts["Spreadsheets"] = countUri(MediaStore.Files.getContentUri("external"), sheetsSelection, sheetsArgs)

        val archivesSelection = "${MediaStore.Files.FileColumns.MIME_TYPE} LIKE ? OR ${MediaStore.Files.FileColumns.DATA} LIKE ? OR ${MediaStore.Files.FileColumns.DATA} LIKE ? OR ${MediaStore.Files.FileColumns.DATA} LIKE ?"
        val archivesArgs = arrayOf("application/zip", "%.zip", "%.rar", "%.7z")
        counts["Archives"] = countUri(MediaStore.Files.getContentUri("external"), archivesSelection, archivesArgs)

        val codeSelection = "${MediaStore.Files.FileColumns.DATA} LIKE ? OR ${MediaStore.Files.FileColumns.DATA} LIKE ? OR ${MediaStore.Files.FileColumns.DATA} LIKE ? OR ${MediaStore.Files.FileColumns.DATA} LIKE ? OR ${MediaStore.Files.FileColumns.DATA} LIKE ?"
        val codeArgs = arrayOf("%.kt", "%.java", "%.py", "%.js", "%.json")
        counts["Code"] = countUri(MediaStore.Files.getContentUri("external"), codeSelection, codeArgs)

        val textSelection = "${MediaStore.Files.FileColumns.MIME_TYPE} = ? OR ${MediaStore.Files.FileColumns.DATA} LIKE ?"
        val textArgs = arrayOf("text/plain", "%.txt")
        counts["Text files"] = countUri(MediaStore.Files.getContentUri("external"), textSelection, textArgs)

        counts
    }

    override suspend fun getDetailedCategoryStats(): List<com.fyllo.filemanager.ui.screens.StorageCategoryDetail> = withContext(Dispatchers.IO) {
        val list = mutableListOf<com.fyllo.filemanager.ui.screens.StorageCategoryDetail>()

        fun queryStats(name: String, uri: Uri, selection: String? = null, args: Array<String>? = null): Pair<Long, Int> {
            var totalBytes = 0L
            var count = 0
            try {
                context.contentResolver.query(
                    uri,
                    arrayOf(MediaStore.Files.FileColumns.SIZE),
                    selection,
                    args,
                    null
                )?.use { cursor ->
                    val sizeCol = cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.SIZE)
                    count = cursor.count
                    while (cursor.moveToNext()) {
                        totalBytes += cursor.getLong(sizeCol)
                    }
                }
            } catch (_: Exception) {}
            return Pair(totalBytes, count)
        }

        val images = queryStats("Images", MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        val audio = queryStats("Audio", MediaStore.Audio.Media.EXTERNAL_CONTENT_URI)
        val videos = queryStats("Videos", MediaStore.Video.Media.EXTERNAL_CONTENT_URI)

        val apkSelection = "${MediaStore.Files.FileColumns.MIME_TYPE} = ? OR ${MediaStore.Files.FileColumns.DATA} LIKE ?"
        val apks = queryStats("APKs", MediaStore.Files.getContentUri("external"), apkSelection, arrayOf("application/vnd.android.package-archive", "%.apk"))

        val archSelection = "${MediaStore.Files.FileColumns.MIME_TYPE} LIKE ? OR ${MediaStore.Files.FileColumns.DATA} LIKE ? OR ${MediaStore.Files.FileColumns.DATA} LIKE ? OR ${MediaStore.Files.FileColumns.DATA} LIKE ?"
        val archives = queryStats("Archives", MediaStore.Files.getContentUri("external"), archSelection, arrayOf("application/zip", "%.zip", "%.rar", "%.7z"))

        val pdfSelection = "${MediaStore.Files.FileColumns.MIME_TYPE} = ? OR ${MediaStore.Files.FileColumns.DATA} LIKE ?"
        val pdf = queryStats("PDF", MediaStore.Files.getContentUri("external"), pdfSelection, arrayOf("application/pdf", "%.pdf"))

        val codeSelection = "${MediaStore.Files.FileColumns.DATA} LIKE ? OR ${MediaStore.Files.FileColumns.DATA} LIKE ? OR ${MediaStore.Files.FileColumns.DATA} LIKE ? OR ${MediaStore.Files.FileColumns.DATA} LIKE ? OR ${MediaStore.Files.FileColumns.DATA} LIKE ?"
        val code = queryStats("Code", MediaStore.Files.getContentUri("external"), codeSelection, arrayOf("%.kt", "%.java", "%.py", "%.js", "%.json"))

        val textSelection = "${MediaStore.Files.FileColumns.MIME_TYPE} = ? OR ${MediaStore.Files.FileColumns.DATA} LIKE ?"
        val text = queryStats("Text", MediaStore.Files.getContentUri("external"), textSelection, arrayOf("text/plain", "%.txt"))

        val docSelection = "${MediaStore.Files.FileColumns.MIME_TYPE} LIKE ? OR ${MediaStore.Files.FileColumns.DATA} LIKE ? OR ${MediaStore.Files.FileColumns.DATA} LIKE ?"
        val docs = queryStats("Documents", MediaStore.Files.getContentUri("external"), docSelection, arrayOf("application/msword", "%.doc%", "%.xls%"))

        // Calculate Other / Miscellaneous
        var otherBytes = 0L
        var otherCount = 0
        try {
            val knownSum = images.first + audio.first + videos.first + apks.first + archives.first + pdf.first + code.first + text.first + docs.first
            val allStats = queryStats("All", MediaStore.Files.getContentUri("external"), "${MediaStore.Files.FileColumns.SIZE} > 0", null)
            otherBytes = (allStats.first - knownSum).coerceAtLeast(0L)
            otherCount = (allStats.second - (images.second + audio.second + videos.second + apks.second + archives.second + pdf.second + code.second + text.second + docs.second)).coerceAtLeast(0)
        } catch (_: Exception) {}

        list.add(com.fyllo.filemanager.ui.screens.StorageCategoryDetail("Images", images.first, images.second, androidx.compose.ui.graphics.Color(0xFF2E7D32)))
        list.add(com.fyllo.filemanager.ui.screens.StorageCategoryDetail("Audio", audio.first, audio.second, androidx.compose.ui.graphics.Color(0xFFE65100)))
        list.add(com.fyllo.filemanager.ui.screens.StorageCategoryDetail("Videos", videos.first, videos.second, androidx.compose.ui.graphics.Color(0xFFD32F2F)))
        list.add(com.fyllo.filemanager.ui.screens.StorageCategoryDetail("Other", otherBytes, otherCount, androidx.compose.ui.graphics.Color(0xFF455A64)))
        list.add(com.fyllo.filemanager.ui.screens.StorageCategoryDetail("APKs", apks.first, apks.second, androidx.compose.ui.graphics.Color(0xFF66BB6A)))
        list.add(com.fyllo.filemanager.ui.screens.StorageCategoryDetail("Archives", archives.first, archives.second, androidx.compose.ui.graphics.Color(0xFF8D6E63)))
        list.add(com.fyllo.filemanager.ui.screens.StorageCategoryDetail("PDF", pdf.first, pdf.second, androidx.compose.ui.graphics.Color(0xFFE53935)))
        list.add(com.fyllo.filemanager.ui.screens.StorageCategoryDetail("Code", code.first, code.second, androidx.compose.ui.graphics.Color(0xFF5C6BC0)))
        list.add(com.fyllo.filemanager.ui.screens.StorageCategoryDetail("Text", text.first, text.second, androidx.compose.ui.graphics.Color(0xFF78909C)))
        list.add(com.fyllo.filemanager.ui.screens.StorageCategoryDetail("Documents", docs.first, docs.second, androidx.compose.ui.graphics.Color(0xFF1E88E5)))

        list
    }

    override suspend fun getLargestFiles(limit: Int): List<FileItem> = withContext(Dispatchers.IO) {
        val result = mutableListOf<FileItem>()
        try {
            val projection = arrayOf(
                MediaStore.Files.FileColumns._ID,
                MediaStore.Files.FileColumns.DISPLAY_NAME,
                MediaStore.Files.FileColumns.DATA,
                MediaStore.Files.FileColumns.SIZE,
                MediaStore.Files.FileColumns.DATE_MODIFIED,
                MediaStore.Files.FileColumns.MIME_TYPE
            )
            val selection = "${MediaStore.Files.FileColumns.SIZE} > 0"
            val sortOrder = "${MediaStore.Files.FileColumns.SIZE} DESC"

            context.contentResolver.query(
                MediaStore.Files.getContentUri("external"),
                projection,
                selection,
                null,
                sortOrder
            )?.use { cursor ->
                val idCol = cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns._ID)
                val nameCol = cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.DISPLAY_NAME)
                val dataCol = cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.DATA)
                val sizeCol = cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.SIZE)
                val dateCol = cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.DATE_MODIFIED)
                val mimeCol = cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.MIME_TYPE)

                while (cursor.moveToNext() && result.size < limit) {
                    val path = cursor.getString(dataCol) ?: continue
                    val id = cursor.getLong(idCol)
                    result.add(
                        FileItem(
                            id = id.toString(),
                            name = cursor.getString(nameCol) ?: File(path).name,
                            uri = ContentUris.withAppendedId(MediaStore.Files.getContentUri("external"), id),
                            path = path,
                            isFolder = false,
                            sizeBytes = cursor.getLong(sizeCol),
                            lastModified = cursor.getLong(dateCol) * 1000,
                            mimeType = cursor.getString(mimeCol),
                            extension = File(path).extension
                        )
                    )
                }
            }
        } catch (_: Exception) {}
        result
    }

    override suspend fun getOldFiles(daysOld: Long, limit: Int): List<FileItem> = withContext(Dispatchers.IO) {
        val result = mutableListOf<FileItem>()
        try {
            val projection = arrayOf(
                MediaStore.Files.FileColumns._ID,
                MediaStore.Files.FileColumns.DISPLAY_NAME,
                MediaStore.Files.FileColumns.DATA,
                MediaStore.Files.FileColumns.SIZE,
                MediaStore.Files.FileColumns.DATE_MODIFIED,
                MediaStore.Files.FileColumns.MIME_TYPE
            )
            val cutoffSeconds = (System.currentTimeMillis() - (daysOld * 24 * 60 * 60 * 1000L)) / 1000
            val selection = "${MediaStore.Files.FileColumns.SIZE} > 0 AND ${MediaStore.Files.FileColumns.DATE_MODIFIED} < ?"
            val selectionArgs = arrayOf(cutoffSeconds.toString())
            val sortOrder = "${MediaStore.Files.FileColumns.DATE_MODIFIED} ASC"

            context.contentResolver.query(
                MediaStore.Files.getContentUri("external"),
                projection,
                selection,
                selectionArgs,
                sortOrder
            )?.use { cursor ->
                val idCol = cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns._ID)
                val nameCol = cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.DISPLAY_NAME)
                val dataCol = cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.DATA)
                val sizeCol = cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.SIZE)
                val dateCol = cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.DATE_MODIFIED)
                val mimeCol = cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.MIME_TYPE)

                while (cursor.moveToNext() && result.size < limit) {
                    val path = cursor.getString(dataCol) ?: continue
                    val id = cursor.getLong(idCol)
                    result.add(
                        FileItem(
                            id = id.toString(),
                            name = cursor.getString(nameCol) ?: File(path).name,
                            uri = ContentUris.withAppendedId(MediaStore.Files.getContentUri("external"), id),
                            path = path,
                            isFolder = false,
                            sizeBytes = cursor.getLong(sizeCol),
                            lastModified = cursor.getLong(dateCol) * 1000,
                            mimeType = cursor.getString(mimeCol),
                            extension = File(path).extension
                        )
                    )
                }
            }
        } catch (_: Exception) {}
        result
    }

    override suspend fun getDuplicateFiles(): List<FileItem> = withContext(Dispatchers.IO) {
        val result = mutableListOf<FileItem>()
        try {
            val projection = arrayOf(
                MediaStore.Files.FileColumns._ID,
                MediaStore.Files.FileColumns.DISPLAY_NAME,
                MediaStore.Files.FileColumns.DATA,
                MediaStore.Files.FileColumns.SIZE,
                MediaStore.Files.FileColumns.DATE_MODIFIED,
                MediaStore.Files.FileColumns.MIME_TYPE
            )
            val selection = "${MediaStore.Files.FileColumns.SIZE} > 1024" // larger than 1KB
            val sortOrder = "${MediaStore.Files.FileColumns.SIZE} DESC"

            val candidates = mutableListOf<FileItem>()
            context.contentResolver.query(
                MediaStore.Files.getContentUri("external"),
                projection,
                selection,
                null,
                sortOrder
            )?.use { cursor ->
                val idCol = cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns._ID)
                val nameCol = cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.DISPLAY_NAME)
                val dataCol = cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.DATA)
                val sizeCol = cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.SIZE)
                val dateCol = cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.DATE_MODIFIED)
                val mimeCol = cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.MIME_TYPE)

                while (cursor.moveToNext()) {
                    val path = cursor.getString(dataCol) ?: continue
                    val id = cursor.getLong(idCol)
                    candidates.add(
                        FileItem(
                            id = id.toString(),
                            name = cursor.getString(nameCol) ?: File(path).name,
                            uri = ContentUris.withAppendedId(MediaStore.Files.getContentUri("external"), id),
                            path = path,
                            isFolder = false,
                            sizeBytes = cursor.getLong(sizeCol),
                            lastModified = cursor.getLong(dateCol) * 1000,
                            mimeType = cursor.getString(mimeCol),
                            extension = File(path).extension
                        )
                    )
                }
            }

            // Group by size and name or extension
            val groupedBySize = candidates.groupBy { it.sizeBytes }
            for ((_, list) in groupedBySize) {
                if (list.size > 1) {
                    result.addAll(list)
                }
            }
        } catch (_: Exception) {}
        result.take(50)
    }

    override suspend fun getFolderStorageSizes(): List<com.fyllo.filemanager.ui.screens.FolderSpaceItem> = withContext(Dispatchers.IO) {
        val list = mutableListOf<com.fyllo.filemanager.ui.screens.FolderSpaceItem>()
        val root = android.os.Environment.getExternalStorageDirectory()
        try {
            val subDirs = root.listFiles()?.filter { file -> file.isDirectory && !file.name.startsWith(".") } ?: emptyList()
            for (dir in subDirs) {
                var size = 0L
                try {
                    dir.walkTopDown().maxDepth(4).filter { it.isFile }.forEach { f: File ->
                        size += f.length()
                    }
                } catch (_: Exception) {}
                if (size > 0L) {
                    list.add(com.fyllo.filemanager.ui.screens.FolderSpaceItem(name = dir.name, sizeBytes = size, path = dir.absolutePath))
                }
            }
            list.sortByDescending { it.sizeBytes }
        } catch (_: Exception) {}
        list.take(8)
    }

    private fun resolveTargetDirectory(destUri: Uri): File {
        val resolved = resolveFileFromUri(destUri)
        if (resolved != null) {
            if (resolved.isDirectory) return resolved
            val parent = resolved.parentFile
            if (parent != null && parent.isDirectory) return parent
        }
        val path = getRealPathFromUri(destUri) ?: destUri.path
        if (path != null) {
            val f = File(path)
            if (f.isDirectory) return f
            if (f.isFile && f.parentFile != null && f.parentFile!!.isDirectory) return f.parentFile!!
            if (!f.exists()) {
                if (path.lowercase().endsWith(".zip") || path.contains('.')) {
                    val p = f.parentFile
                    if (p != null) {
                        p.mkdirs()
                        return p
                    }
                }
                f.mkdirs()
                if (f.isDirectory) return f
            }
        }
        val fallback = android.os.Environment.getExternalStorageDirectory()
        if (!fallback.exists()) fallback.mkdirs()
        return fallback
    }

    private fun getUniqueDestFile(targetDir: File, name: String): File {
        var targetFile = File(targetDir, name)
        if (!targetFile.exists()) return targetFile

        val dotIdx = name.lastIndexOf('.')
        val baseName = if (dotIdx > 0) name.substring(0, dotIdx) else name
        val ext = if (dotIdx > 0) name.substring(dotIdx) else ""

        var count = 1
        while (targetFile.exists()) {
            targetFile = File(targetDir, "$baseName ($count)$ext")
            count++
        }
        return targetFile
    }

    private fun calculateTotalSizeForUris(uris: List<Uri>): Long {
        var total = 0L
        for (uri in uris) {
            val path = getRealPathFromUri(uri) ?: uri.path ?: continue
            val file = File(path)
            if (file.exists()) {
                if (file.isDirectory) {
                    file.walkTopDown().forEach { f -> if (f.isFile) total += f.length() }
                } else {
                    total += file.length()
                }
            }
        }
        return if (total > 0L) total else 1L
    }

    override suspend fun checkConflicts(sourceUris: List<Uri>, destUri: Uri): Boolean = withContext(Dispatchers.IO) {
        val destDir = resolveTargetDirectory(destUri)
        for (uri in sourceUris) {
            val path = getRealPathFromUri(uri) ?: uri.path ?: continue
            val srcFile = File(path)
            if (!srcFile.exists()) continue
            val targetFile = File(destDir, srcFile.name)
            if (targetFile.exists()) {
                return@withContext true
            }
        }
        return@withContext false
    }

    override fun copyFiles(sourceUris: List<Uri>, destUri: Uri, conflictStrategy: com.fyllo.filemanager.domain.model.ConflictStrategy): Flow<FileOperationState> = flow {
        emit(FileOperationState.Preparing)
        try {
            val destDir = resolveTargetDirectory(destUri)
            val totalBytes = calculateTotalSizeForUris(sourceUris)
            var processedBytes = 0L

            for (uri in sourceUris) {
                val path = getRealPathFromUri(uri) ?: uri.path ?: continue
                val srcFile = File(path)
                if (!srcFile.exists()) continue
                
                val directTarget = File(destDir, srcFile.name)
                if (directTarget.exists() && conflictStrategy == com.fyllo.filemanager.domain.model.ConflictStrategy.SKIP) {
                    continue
                }

                if (srcFile.isDirectory) {
                    val targetSubDir = if (directTarget.exists() && conflictStrategy == com.fyllo.filemanager.domain.model.ConflictStrategy.REPLACE) {
                        directTarget
                    } else {
                        getUniqueDestFile(destDir, srcFile.name)
                    }
                    targetSubDir.mkdirs()
                    val filesToCopy = srcFile.walkTopDown().toList()
                    for (file in filesToCopy) {
                        val relPath = file.relativeTo(srcFile).path
                        val target = File(targetSubDir, relPath)
                        
                        if (target.exists() && file.isFile) {
                            if (conflictStrategy == com.fyllo.filemanager.domain.model.ConflictStrategy.SKIP) continue
                        }
                        
                        if (file.isDirectory) {
                            target.mkdirs()
                        } else {
                            target.parentFile?.mkdirs()
                            file.inputStream().use { input ->
                                target.outputStream().use { output ->
                                    val buffer = ByteArray(64 * 1024)
                                    var read: Int
                                    while (input.read(buffer).also { read = it } != -1) {
                                        output.write(buffer, 0, read)
                                        processedBytes += read
                                        val progress = (processedBytes.toFloat() / totalBytes.toFloat()).coerceIn(0f, 1f)
                                        emit(FileOperationState.Running(progress, file.name, processedBytes, totalBytes))
                                    }
                                }
                            }
                        }
                    }
                } else {
                    val targetFile = if (directTarget.exists() && conflictStrategy == com.fyllo.filemanager.domain.model.ConflictStrategy.REPLACE) {
                        directTarget
                    } else {
                        getUniqueDestFile(destDir, srcFile.name)
                    }
                    srcFile.inputStream().use { input ->
                        targetFile.outputStream().use { output ->
                            val buffer = ByteArray(64 * 1024)
                            var read: Int
                            while (input.read(buffer).also { read = it } != -1) {
                                output.write(buffer, 0, read)
                                processedBytes += read
                                val progress = (processedBytes.toFloat() / totalBytes.toFloat()).coerceIn(0f, 1f)
                                emit(FileOperationState.Running(progress, srcFile.name, processedBytes, totalBytes))
                            }
                        }
                    }
                }
                val isSafeFolderTarget = destDir.absolutePath.contains("/files/SafeFolder") || destDir.absolutePath.contains("SafeFolder")
                if (!isSafeFolderTarget) {
                    scanPathToMediaStore(directTarget.absolutePath)
                }
            }
            emit(FileOperationState.Completed)
        } catch (e: Exception) {
            e.printStackTrace()
            emit(FileOperationState.Failed(FileError(ErrorCode.UNKNOWN, e.localizedMessage ?: "Copy failed", "Copy", false)))
        }
    }.flowOn(Dispatchers.IO)

    private fun purgePathFromMediaStore(path: String, uri: Uri? = null) {
        try {
            if (uri != null && uri.scheme == "content") {
                try { context.contentResolver.delete(uri, null, null) } catch (e: Exception) { }
            }
            val file = File(path)
            val pathsToPurge = mutableListOf<String>()
            pathsToPurge.add(path)
            if (file.isDirectory) {
                try {
                    file.walkTopDown().forEach { pathsToPurge.add(it.absolutePath) }
                } catch (e: Exception) { }
            }
            android.media.MediaScannerConnection.scanFile(context, pathsToPurge.toTypedArray(), null, null)
            
            val contentUri = MediaStore.Files.getContentUri("external")
            val where = "${MediaStore.Files.FileColumns.DATA} = ? OR ${MediaStore.Files.FileColumns.DATA} LIKE ?"
            val args = arrayOf(path, "$path/%")
            context.contentResolver.delete(contentUri, where, args)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun scanPathToMediaStore(path: String) {
        try {
            val file = File(path)
            if (file.isDirectory) {
                val children = file.walkTopDown().filter { it.isFile }.map { it.absolutePath }.toList()
                if (children.isNotEmpty()) {
                    android.media.MediaScannerConnection.scanFile(context, children.toTypedArray(), null, null)
                }
            } else {
                android.media.MediaScannerConnection.scanFile(context, arrayOf(path), null, null)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun moveFiles(sourceUris: List<Uri>, destUri: Uri, conflictStrategy: com.fyllo.filemanager.domain.model.ConflictStrategy): Flow<FileOperationState> = flow {
        emit(FileOperationState.Preparing)
        try {
            val destDir = resolveTargetDirectory(destUri)
            val isSafeFolderTarget = destDir.absolutePath.contains("/files/SafeFolder") || destDir.absolutePath.contains("SafeFolder")
            val totalBytes = calculateTotalSizeForUris(sourceUris)
            var processedBytes = 0L

            for (uri in sourceUris) {
                val path = getRealPathFromUri(uri) ?: uri.path ?: continue
                val srcFile = File(path)
                if (!srcFile.exists()) continue

                val directTarget = File(destDir, srcFile.name)
                if (directTarget.exists() && conflictStrategy == com.fyllo.filemanager.domain.model.ConflictStrategy.SKIP) {
                    continue
                }

                val targetFile = if (directTarget.exists() && conflictStrategy == com.fyllo.filemanager.domain.model.ConflictStrategy.REPLACE) {
                    directTarget
                } else {
                    getUniqueDestFile(destDir, srcFile.name)
                }
                
                var moved = false
                if (!targetFile.exists()) {
                    moved = srcFile.renameTo(targetFile)
                } else if (conflictStrategy == com.fyllo.filemanager.domain.model.ConflictStrategy.REPLACE && srcFile.isFile && targetFile.isFile) {
                    targetFile.delete()
                    moved = srcFile.renameTo(targetFile)
                }

                if (!moved) {
                    // Fallback to copy and delete if renameTo cross-filesystem fails
                    if (srcFile.isDirectory) {
                        targetFile.mkdirs()
                        val filesToCopy = srcFile.walkTopDown().toList()
                        for (file in filesToCopy) {
                            val relPath = file.relativeTo(srcFile).path
                            val target = File(targetFile, relPath)
                            if (target.exists() && file.isFile) {
                                if (conflictStrategy == com.fyllo.filemanager.domain.model.ConflictStrategy.SKIP) continue
                            }
                            if (file.isDirectory) {
                                target.mkdirs()
                            } else {
                                target.parentFile?.mkdirs()
                                file.inputStream().use { input ->
                                    target.outputStream().use { output ->
                                        val buffer = ByteArray(64 * 1024)
                                        var read: Int
                                        while (input.read(buffer).also { read = it } != -1) {
                                            output.write(buffer, 0, read)
                                            processedBytes += read
                                            val progress = (processedBytes.toFloat() / totalBytes.toFloat()).coerceIn(0f, 1f)
                                            emit(FileOperationState.Running(progress, file.name, processedBytes, totalBytes))
                                        }
                                    }
                                }
                            }
                        }
                        srcFile.deleteRecursively()
                        moved = true
                    } else {
                        srcFile.inputStream().use { input ->
                            targetFile.outputStream().use { output ->
                                val buffer = ByteArray(64 * 1024)
                                var read: Int
                                while (input.read(buffer).also { read = it } != -1) {
                                    output.write(buffer, 0, read)
                                    processedBytes += read
                                    val progress = (processedBytes.toFloat() / totalBytes.toFloat()).coerceIn(0f, 1f)
                                    emit(FileOperationState.Running(progress, srcFile.name, processedBytes, totalBytes))
                                }
                            }
                        }
                        srcFile.delete()
                        moved = true
                    }
                } else {
                    processedBytes += if (targetFile.isDirectory) getFolderSize(targetFile.name) else targetFile.length()
                    val progress = (processedBytes.toFloat() / totalBytes.toFloat()).coerceIn(0f, 1f)
                    emit(FileOperationState.Running(progress, srcFile.name, processedBytes, totalBytes))
                }

                // Clean source from MediaStore DB so ghost copies don't appear anywhere
                purgePathFromMediaStore(srcFile.absolutePath, uri)

                // Scan target into MediaStore unless moving into private SafeFolder
                if (!isSafeFolderTarget) {
                    scanPathToMediaStore(targetFile.absolutePath)
                }
            }
            emit(FileOperationState.Completed)
        } catch (e: Exception) {
            e.printStackTrace()
            emit(FileOperationState.Failed(FileError(ErrorCode.UNKNOWN, e.localizedMessage ?: "Move failed", "Move", false)))
        }
    }.flowOn(Dispatchers.IO)

    override fun deleteFiles(uris: List<Uri>, permanent: Boolean): Flow<FileOperationState> = flow {
        emit(FileOperationState.Preparing)
        var allSuccess = true
        val totalItems = uris.size
        var currentItem = 0

        // Calculate total size across all uris
        val totalBytes = calculateTotalSizeForUris(uris)
        var processedBytes = 0L

        for (uri in uris) {
            currentItem++
            val path = getRealPathFromUri(uri) ?: uri.path
            val fileName = path?.let { File(it).name } ?: uri.lastPathSegment ?: "Item"
            
            var itemSize = 0L
            if (path != null) {
                val f = File(path)
                if (f.exists()) {
                    itemSize = if (f.isDirectory) {
                        try { f.walkTopDown().filter { it.isFile }.sumOf { it.length() } } catch (_: Exception) { 0L }
                    } else {
                        f.length()
                    }
                }
            }

            val progress = (currentItem.toFloat() / totalItems.toFloat()).coerceIn(0f, 1f)
            emit(FileOperationState.Running(progress, fileName, processedBytes, totalBytes, "DELETE"))

            if (path != null) {
                val file = File(path)
                if (file.exists()) {
                    if (permanent) {
                        val deleted = if (file.isDirectory) file.deleteRecursively() else file.delete()
                        if (!deleted && file.exists()) allSuccess = false
                    } else {
                        val success = trashManager.moveToTrash(file)
                        if (!success) allSuccess = false
                    }
                    purgePathFromMediaStore(path, uri)
                } else {
                    purgePathFromMediaStore(path, uri)
                }
            } else if (uri.scheme == "content") {
                purgePathFromMediaStore("", uri)
            }

            processedBytes += itemSize
            val updatedProgress = (currentItem.toFloat() / totalItems.toFloat()).coerceIn(0f, 1f)
            emit(FileOperationState.Running(updatedProgress, fileName, processedBytes, totalBytes, "DELETE"))
        }
        if (allSuccess) emit(FileOperationState.Completed)
        else emit(FileOperationState.Failed(FileError(ErrorCode.UNKNOWN, "Failed to delete some files", "Delete", false)))
    }.flowOn(Dispatchers.IO)

    override fun restoreFiles(uris: List<Uri>): Flow<FileOperationState> = flow {
        emit(FileOperationState.Preparing)
        var allSuccess = true
        for (uri in uris) {
            val id = uri.toString()
            val success = trashManager.restoreFromTrash(id)
            if (!success) allSuccess = false
        }
        if (allSuccess) emit(FileOperationState.Completed)
        else emit(FileOperationState.Failed(FileError(ErrorCode.UNKNOWN, "Failed to restore some files", "Restore", false)))
    }.flowOn(Dispatchers.IO)

    override suspend fun renameFile(uri: Uri, newName: String): FileItem? = withContext(Dispatchers.IO) {
        try {
            val path = getRealPathFromUri(uri) ?: uri.path ?: return@withContext null
            val srcFile = File(path)
            if (!srcFile.exists()) return@withContext null

            val destFile = File(srcFile.parentFile, newName)
            var success = srcFile.renameTo(destFile)

            if (!success) {
                if (srcFile.isDirectory) {
                    success = srcFile.copyRecursively(destFile, overwrite = true)
                    if (success) srcFile.deleteRecursively()
                } else {
                    srcFile.copyTo(destFile, overwrite = true)
                    success = srcFile.delete()
                }
            }

            if (success) {
                return@withContext FileItem(
                    id = UUID.randomUUID().toString(),
                    name = destFile.name,
                    uri = Uri.fromFile(destFile),
                    path = destFile.absolutePath,
                    isFolder = destFile.isDirectory,
                    sizeBytes = if (destFile.isFile) destFile.length() else 0L,
                    lastModified = destFile.lastModified(),
                    mimeType = null,
                    extension = destFile.extension
                )
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        null
    }

    override suspend fun createFolder(parentUri: Uri, name: String): FileItem? = withContext(Dispatchers.IO) {
        try {
            val parentPath = getRealPathFromUri(parentUri) 
                ?: parentUri.path 
                ?: android.os.Environment.getExternalStorageDirectory().absolutePath
            val parentFile = File(parentPath)
            val newFolder = File(parentFile, name)
            if (!newFolder.exists()) {
                val created = newFolder.mkdirs()
                if (created || newFolder.exists()) {
                    return@withContext FileItem(
                        id = UUID.randomUUID().toString(),
                        name = newFolder.name,
                        uri = Uri.fromFile(newFolder),
                        path = newFolder.absolutePath,
                        isFolder = true,
                        sizeBytes = 0L,
                        lastModified = newFolder.lastModified(),
                        mimeType = null,
                        extension = ""
                    )
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        null
    }

    override suspend fun getFileDetails(uri: Uri): FileItem? = withContext(Dispatchers.IO) {
        try {
            val path = getRealPathFromUri(uri) ?: uri.path ?: return@withContext null
            val file = File(path)
            if (file.exists()) {
                val isFolder = file.isDirectory
                return@withContext FileItem(
                    id = UUID.randomUUID().toString(),
                    name = file.name,
                    uri = Uri.fromFile(file),
                    path = file.absolutePath,
                    isFolder = isFolder,
                    sizeBytes = if (isFolder) calculateSize(uri) else file.length(),
                    lastModified = file.lastModified(),
                    mimeType = null,
                    extension = file.extension,
                    itemCount = if (isFolder) file.list()?.size else null
                )
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        null
    }

    override suspend fun calculateSize(uri: Uri): Long = withContext(Dispatchers.IO) {
        try {
            val path = getRealPathFromUri(uri) ?: uri.path ?: return@withContext 0L
            val file = File(path)
            if (!file.exists()) return@withContext 0L
            if (file.isFile) return@withContext file.length()
            var total = 0L
            file.walkTopDown().forEach { f -> if (f.isFile) total += f.length() }
            return@withContext total
        } catch (e: Exception) {
            0L
        }
    }

    override fun compressFiles(sourceUris: List<Uri>, destUri: Uri): Flow<FileOperationState> = flow {
        emit(FileOperationState.Preparing)
        var zipFile: File? = null
        try {
            val destDir = resolveTargetDirectory(destUri)
            val createdZipFile = if (destUri.scheme == "file" && destUri.path != null && destUri.path!!.lowercase().endsWith(".zip")) {
                File(destUri.path!!)
            } else {
                val path = getRealPathFromUri(destUri) ?: destUri.path
                if (path != null && path.lowercase().endsWith(".zip")) {
                    File(path)
                } else {
                    getUniqueDestFile(destDir, "archive.zip")
                }
            }
            zipFile = createdZipFile
            zipFile.parentFile?.mkdirs()

            val filesToCompress = mutableListOf<Pair<File, String>>()
            var totalBytes = 0L

            for (uri in sourceUris) {
                val file = resolveFileFromUri(uri) ?: continue
                if (file.isDirectory) {
                    val rootParent = file.parentFile ?: file
                    file.walkTopDown().forEach { f ->
                        val relPath = f.relativeTo(rootParent).path.replace('\\', '/')
                        if (f.isDirectory) {
                            val dirEntryPath = if (relPath.endsWith("/")) relPath else "$relPath/"
                            filesToCompress.add(Pair(f, dirEntryPath))
                        } else {
                            filesToCompress.add(Pair(f, relPath))
                            totalBytes += f.length()
                        }
                    }
                } else {
                    filesToCompress.add(Pair(file, file.name))
                    totalBytes += file.length()
                }
            }

            if (totalBytes <= 0L) totalBytes = 1L
            var processedBytes = 0L
            var lastEmitTime = 0L
            val usedEntryNames = mutableSetOf<String>()

            java.util.zip.ZipOutputStream(java.io.BufferedOutputStream(java.io.FileOutputStream(zipFile), 1024 * 1024)).use { zos ->
                zos.setLevel(java.util.zip.Deflater.BEST_SPEED)
                for ((file, entryNameRaw) in filesToCompress) {
                    kotlinx.coroutines.currentCoroutineContext().ensureActive()
                    var entryName = entryNameRaw.replace('\\', '/')
                    
                    if (entryName in usedEntryNames) {
                        val isDir = entryName.endsWith("/")
                        val cleanName = if (isDir) entryName.dropLast(1) else entryName
                        val dotIdx = cleanName.lastIndexOf('.')
                        val base = if (dotIdx > 0 && !isDir) cleanName.substring(0, dotIdx) else cleanName
                        val ext = if (dotIdx > 0 && !isDir) cleanName.substring(dotIdx) else ""
                        var c = 1
                        var testName = "$base ($c)$ext${if (isDir) "/" else ""}"
                        while (testName in usedEntryNames) {
                            c++
                            testName = "$base ($c)$ext${if (isDir) "/" else ""}"
                        }
                        entryName = testName
                    }
                    usedEntryNames.add(entryName)

                    if (file.isDirectory) {
                        val entry = java.util.zip.ZipEntry(if (entryName.endsWith("/")) entryName else "$entryName/")
                        zos.putNextEntry(entry)
                        zos.closeEntry()
                    } else {
                        val entry = java.util.zip.ZipEntry(entryName)
                        zos.putNextEntry(entry)
                        java.io.BufferedInputStream(file.inputStream(), 1024 * 1024).use { input ->
                            val buffer = ByteArray(1024 * 1024)
                            var read: Int
                            while (input.read(buffer).also { read = it } != -1) {
                                kotlinx.coroutines.currentCoroutineContext().ensureActive()
                                zos.write(buffer, 0, read)
                                processedBytes += read
                                val currentTime = System.currentTimeMillis()
                                if (currentTime - lastEmitTime >= 100 || processedBytes >= totalBytes) {
                                    lastEmitTime = currentTime
                                    val progress = (processedBytes.toFloat() / totalBytes.toFloat()).coerceIn(0f, 1f)
                                    emit(FileOperationState.Running(progress, file.name, processedBytes, totalBytes, "ZIP"))
                                }
                            }
                        }
                        zos.closeEntry()
                    }
                }
            }

            try {
                android.media.MediaScannerConnection.scanFile(context, arrayOf(zipFile.absolutePath), null, null)
            } catch (e: Exception) { }

            emit(FileOperationState.Completed)
        } catch (e: Exception) {
            if (e is kotlinx.coroutines.CancellationException) {
                zipFile?.delete()
                throw e
            }
            e.printStackTrace()
            emit(FileOperationState.Failed(FileError(ErrorCode.UNKNOWN, e.localizedMessage ?: "Compress failed", "Compress", false)))
        }
    }.flowOn(Dispatchers.IO)

    override fun extractArchive(sourceUri: Uri, destUri: Uri): Flow<FileOperationState> = flow {
        emit(FileOperationState.Preparing)
        var createdTargetDir: File? = null
        try {
            val srcFile = resolveFileFromUri(sourceUri)
            val parentDestDir = resolveTargetDirectory(destUri)
            
            val sourceFileName = if (srcFile != null) srcFile.name else {
                var name: String? = null
                if (sourceUri.scheme == "content") {
                    try {
                        context.contentResolver.query(sourceUri, arrayOf(MediaStore.MediaColumns.DISPLAY_NAME), null, null, null)?.use { cursor ->
                            if (cursor.moveToFirst()) {
                                name = cursor.getString(0)
                            }
                        }
                    } catch (e: Exception) { }
                }
                name ?: sourceUri.lastPathSegment ?: "Extracted"
            }

            val archiveNameWithoutExt = File(sourceFileName).nameWithoutExtension.ifEmpty { "Extracted" }
            val ext = File(sourceFileName).extension.lowercase()

            var totalBytes = 0L
            var processedBytes = 0L
            var lastEmitTime = 0L

            if (ext == "gz" || ext == "tgz" || sourceFileName.lowercase().endsWith(".tar.gz")) {
                val extractTargetDir = getUniqueDestFile(parentDestDir, archiveNameWithoutExt)
                extractTargetDir.mkdirs()
                createdTargetDir = extractTargetDir

                val inputStream = if (srcFile != null) java.io.FileInputStream(srcFile) else context.contentResolver.openInputStream(sourceUri)
                if (inputStream == null) {
                    emit(FileOperationState.Failed(FileError(ErrorCode.UNKNOWN, "Cannot open stream for archive", "Extract", false)))
                    return@flow
                }
                
                java.util.zip.GZIPInputStream(java.io.BufferedInputStream(inputStream, 1024 * 1024)).use { gzos ->
                    val outputFile = File(extractTargetDir, archiveNameWithoutExt)
                    java.io.BufferedOutputStream(java.io.FileOutputStream(outputFile), 1024 * 1024).use { out ->
                        val buffer = ByteArray(1024 * 1024)
                        var read: Int
                        while (gzos.read(buffer).also { read = it } != -1) {
                            kotlinx.coroutines.currentCoroutineContext().ensureActive()
                            out.write(buffer, 0, read)
                            processedBytes += read
                            val currentTime = System.currentTimeMillis()
                            if (currentTime - lastEmitTime >= 100) {
                                lastEmitTime = currentTime
                                emit(FileOperationState.Running(0.5f, outputFile.name, processedBytes, processedBytes, "UNZIP"))
                            }
                        }
                    }
                }
                try {
                    android.media.MediaScannerConnection.scanFile(context, arrayOf(extractTargetDir.absolutePath), null, null)
                } catch (e: Exception) { }
            } else {
                if (srcFile != null && srcFile.exists()) {
                    val jZipFile = java.util.zip.ZipFile(srcFile)
                    val entriesList = mutableListOf<java.util.zip.ZipEntry>()
                    val entriesEnum = jZipFile.entries()
                    var firstRootDir: String? = null
                    var hasSingleRootDir = true

                    while (entriesEnum.hasMoreElements()) {
                        kotlinx.coroutines.currentCoroutineContext().ensureActive()
                        val entry = entriesEnum.nextElement()
                        entriesList.add(entry)
                        if (!entry.isDirectory) {
                            totalBytes += entry.size.coerceAtLeast(0L)
                        }
                        
                        val name = entry.name.replace('\\', '/')
                        val firstSlash = name.indexOf('/')
                        if (firstSlash > 0) {
                            val root = name.substring(0, firstSlash)
                            if (firstRootDir == null) {
                                firstRootDir = root
                            } else if (firstRootDir != root) {
                                hasSingleRootDir = false
                            }
                        } else if (name.isNotEmpty()) {
                            hasSingleRootDir = false
                        }
                    }

                    if (firstRootDir == null) hasSingleRootDir = false
                    if (totalBytes <= 0L) totalBytes = 1L

                    val extractTargetDir = if (hasSingleRootDir) {
                        parentDestDir
                    } else {
                        getUniqueDestFile(parentDestDir, archiveNameWithoutExt).also { it.mkdirs() }
                    }
                    if (!hasSingleRootDir) createdTargetDir = extractTargetDir

                    for (entry in entriesList) {
                        kotlinx.coroutines.currentCoroutineContext().ensureActive()
                        val normalizedName = entry.name.replace('\\', '/')
                        val newFile = File(extractTargetDir, normalizedName)

                        if (!newFile.canonicalPath.startsWith(extractTargetDir.canonicalPath)) {
                            continue
                        }

                        if (entry.isDirectory) {
                            newFile.mkdirs()
                        } else {
                            newFile.parentFile?.mkdirs()
                            jZipFile.getInputStream(entry).use { input ->
                                java.io.BufferedOutputStream(java.io.FileOutputStream(newFile), 1024 * 1024).use { output ->
                                    val buffer = ByteArray(1024 * 1024)
                                    var read: Int
                                    while (input.read(buffer).also { read = it } != -1) {
                                        kotlinx.coroutines.currentCoroutineContext().ensureActive()
                                        output.write(buffer, 0, read)
                                        processedBytes += read
                                        val currentTime = System.currentTimeMillis()
                                        if (currentTime - lastEmitTime >= 100 || processedBytes >= totalBytes) {
                                            lastEmitTime = currentTime
                                            val progress = (processedBytes.toFloat() / totalBytes.toFloat()).coerceIn(0f, 1f)
                                            emit(FileOperationState.Running(progress, newFile.name, processedBytes, totalBytes, "UNZIP"))
                                        }
                                    }
                                }
                            }
                        }
                    }
                    jZipFile.close()

                    try {
                        android.media.MediaScannerConnection.scanFile(context, arrayOf(extractTargetDir.absolutePath), null, null)
                    } catch (e: Exception) { }
                } else {
                    val inputStream = context.contentResolver.openInputStream(sourceUri)
                    if (inputStream == null) {
                        emit(FileOperationState.Failed(FileError(ErrorCode.UNKNOWN, "Cannot open stream for archive", "Extract", false)))
                        return@flow
                    }

                    val streamSize = try {
                        context.contentResolver.openAssetFileDescriptor(sourceUri, "r")?.use { it.length } ?: 1L
                    } catch (e: Exception) { 1L }
                    totalBytes = if (streamSize > 0L) streamSize else 1L

                    val extractTargetDir = getUniqueDestFile(parentDestDir, archiveNameWithoutExt)
                    extractTargetDir.mkdirs()
                    createdTargetDir = extractTargetDir

                    java.util.zip.ZipInputStream(java.io.BufferedInputStream(inputStream, 1024 * 1024)).use { zis ->
                        var entry = zis.nextEntry
                        while (entry != null) {
                            kotlinx.coroutines.currentCoroutineContext().ensureActive()
                            val normalizedName = entry.name.replace('\\', '/')
                            val newFile = File(extractTargetDir, normalizedName)
                            if (newFile.canonicalPath.startsWith(extractTargetDir.canonicalPath)) {
                                if (entry.isDirectory) {
                                    newFile.mkdirs()
                                } else {
                                    newFile.parentFile?.mkdirs()
                                    java.io.BufferedOutputStream(java.io.FileOutputStream(newFile), 1024 * 1024).use { output ->
                                        val buffer = ByteArray(1024 * 1024)
                                        var read: Int
                                        while (zis.read(buffer).also { read = it } != -1) {
                                            kotlinx.coroutines.currentCoroutineContext().ensureActive()
                                            output.write(buffer, 0, read)
                                            processedBytes += read
                                            val currentTime = System.currentTimeMillis()
                                            if (currentTime - lastEmitTime >= 100 || processedBytes >= totalBytes) {
                                                lastEmitTime = currentTime
                                                val progress = (processedBytes.toFloat() / totalBytes.toFloat()).coerceIn(0f, 1f)
                                                emit(FileOperationState.Running(progress, newFile.name, processedBytes, totalBytes, "UNZIP"))
                                            }
                                        }
                                    }
                                }
                            }
                            zis.closeEntry()
                            entry = zis.nextEntry
                        }
                    }

                    try {
                        android.media.MediaScannerConnection.scanFile(context, arrayOf(extractTargetDir.absolutePath), null, null)
                    } catch (e: Exception) { }
                }
            }
            emit(FileOperationState.Completed)
        } catch (e: Exception) {
            if (e is kotlinx.coroutines.CancellationException) {
                createdTargetDir?.deleteRecursively()
                throw e
            }
            e.printStackTrace()
            emit(FileOperationState.Failed(FileError(ErrorCode.UNKNOWN, e.localizedMessage ?: "Extract failed", "Extract", false)))
        }
    }.flowOn(Dispatchers.IO)
}
