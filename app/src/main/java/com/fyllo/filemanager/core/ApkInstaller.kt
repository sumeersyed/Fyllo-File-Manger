package com.fyllo.filemanager.core

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.provider.Settings
import android.widget.Toast
import androidx.core.content.FileProvider
import com.fyllo.filemanager.domain.model.FileItem
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream

object ApkInstaller {

    /**
     * Installs an APK file from a [FileItem].
     */
    fun installApk(context: Context, fileItem: FileItem) {
        installApk(context, fileItem.uri, fileItem.path)
    }

    /**
     * Installs an APK file from a URI and optional file path.
     */
    fun installApk(context: Context, uri: Uri, path: String? = null) {
        try {
            // 1. Check Android 8.0+ (API 26+) "Install unknown apps" permission
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                if (!context.packageManager.canRequestPackageInstalls()) {
                    Toast.makeText(
                        context,
                        "Please allow 'Install unknown apps' permission to install APKs",
                        Toast.LENGTH_LONG
                    ).show()
                    val settingsIntent = Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES).apply {
                        data = Uri.parse("package:${context.packageName}")
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    }
                    context.startActivity(settingsIntent)
                    return
                }
            }

            // 2. Prepare destination in external cache directory owned by this app
            val cacheFolder = context.externalCacheDir
                ?: context.cacheDir
                ?: context.getExternalFilesDir(null)
                ?: File(context.filesDir, "apks")

            cacheFolder.mkdirs()

            // Clean old temp installation files
            try {
                cacheFolder.listFiles()?.filter { it.name.startsWith("install_temp_") }?.forEach { it.delete() }
            } catch (_: Exception) {}

            val tempApkFile = File(cacheFolder, "install_temp_${System.currentTimeMillis()}.apk")

            // 3. Stream copy source APK into cache folder to ensure FileProvider accessibility
            var copySuccess = false

            // Try reading from path/file URI first
            val directFile = when {
                !path.isNullOrEmpty() && File(path).exists() && File(path).canRead() -> File(path)
                uri.scheme == "file" && uri.path != null && File(uri.path!!).exists() -> File(uri.path!!)
                else -> getFileFromContentUri(context, uri)
            }

            if (directFile != null && directFile.exists() && directFile.length() > 0L) {
                try {
                    FileInputStream(directFile).use { input ->
                        FileOutputStream(tempApkFile).use { output ->
                            val buffer = ByteArray(65536)
                            var read: Int
                            while (input.read(buffer).also { read = it } != -1) {
                                output.write(buffer, 0, read)
                            }
                            output.flush()
                        }
                    }
                    copySuccess = tempApkFile.exists() && tempApkFile.length() > 0L
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }

            // Fallback: Copy via ContentResolver input stream
            if (!copySuccess) {
                try {
                    val isStream = context.contentResolver.openInputStream(uri)
                    if (isStream != null) {
                        isStream.use { input ->
                            FileOutputStream(tempApkFile).use { output ->
                                val buffer = ByteArray(65536)
                                var read: Int
                                while (input.read(buffer).also { read = it } != -1) {
                                    output.write(buffer, 0, read)
                                }
                                output.flush()
                            }
                        }
                        copySuccess = tempApkFile.exists() && tempApkFile.length() > 0L
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }

            if (!copySuccess || !tempApkFile.exists() || tempApkFile.length() == 0L) {
                Toast.makeText(context, "APK file is unreadable or empty", Toast.LENGTH_SHORT).show()
                return
            }

            // Ensure readable permissions
            try {
                tempApkFile.setReadable(true, false)
            } catch (_: Exception) {}

            // 4. Generate FileProvider URI
            val authority = "${context.packageName}.fileprovider"
            val fileProviderUri = FileProvider.getUriForFile(context, authority, tempApkFile)

            // 5. Create PackageInstaller Intent
            val installIntent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(fileProviderUri, "application/vnd.android.package-archive")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }

            // 6. Explicitly grant URI read permissions to system package installers
            val knownInstallers = listOf(
                "com.google.android.packageinstaller",
                "com.android.packageinstaller",
                "com.samsung.android.packageinstaller",
                "com.miui.packageinstaller",
                "com.oppo.packageinstaller"
            )

            for (pkg in knownInstallers) {
                try {
                    context.grantUriPermission(
                        pkg,
                        fileProviderUri,
                        Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION
                    )
                } catch (_: Exception) {}
            }

            val resolvedHandlers = context.packageManager.queryIntentActivities(installIntent, PackageManager.MATCH_DEFAULT_ONLY)
            for (handler in resolvedHandlers) {
                val pkgName = handler.activityInfo.packageName
                try {
                    context.grantUriPermission(
                        pkgName,
                        fileProviderUri,
                        Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION
                    )
                } catch (_: Exception) {}
            }

            context.startActivity(installIntent)

        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(context, "Cannot launch package installer: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
        }
    }

    private fun getFileFromContentUri(context: Context, contentUri: Uri): File? {
        try {
            val projection = arrayOf(MediaStore.MediaColumns.DATA)
            context.contentResolver.query(contentUri, projection, null, null, null)?.use { cursor ->
                if (cursor.moveToFirst()) {
                    val col = cursor.getColumnIndex(MediaStore.MediaColumns.DATA)
                    if (col >= 0) {
                        val p = cursor.getString(col)
                        if (!p.isNullOrEmpty()) {
                            val f = File(p)
                            if (f.exists()) return f
                        }
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return null
    }
}
