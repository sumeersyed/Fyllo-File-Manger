package com.fyllo.filemanager.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.biometric.BiometricPrompt
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.grid.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.outlined.Backspace
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.material.icons.outlined.Image
import androidx.compose.material.icons.outlined.Movie
import androidx.compose.material.icons.outlined.AudioFile
import androidx.compose.material.icons.outlined.InsertDriveFile
import androidx.compose.material.icons.outlined.MoreVert
import androidx.compose.material.icons.outlined.Security
import androidx.compose.material.icons.outlined.Fingerprint
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import com.fyllo.filemanager.core.AppLockManager
import com.fyllo.filemanager.domain.model.FileItem
import java.io.File
import java.io.FileOutputStream
import java.util.UUID

import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.items as staggeredItems
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.outlined.Folder
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.DriveFileMove

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SafeFolderScreen(
    onBackClick: () -> Unit,
    onPinSuccess: () -> Unit = {}
) {
    val context = LocalContext.current
    val appLockManager = remember { AppLockManager(context) }
    var isUnlocked by remember { mutableStateOf(false) }

    val safeFolderDir = remember {
        File(context.filesDir, "SafeFolder").apply { if (!exists()) mkdirs() }
    }

    var vaultFiles by remember { mutableStateOf<List<FileItem>>(emptyList()) }
    var selectedCategory by remember { mutableStateOf("All") }
    var isGridView by remember { mutableStateOf(true) }

    fun getVaultDisplayName(vaultName: String): String {
        return try {
            val parts = vaultName.split("_")
            if (parts.size >= 3 && parts[0] == "vault") {
                val encodedName = parts[1]
                val originalPath = String(android.util.Base64.decode(encodedName, android.util.Base64.URL_SAFE or android.util.Base64.NO_WRAP))
                File(originalPath).name
            } else {
                vaultName
            }
        } catch (e: Exception) {
            vaultName
        }
    }

    fun refreshVaultFiles() {
        val list = mutableListOf<FileItem>()
        val files = safeFolderDir.listFiles() ?: emptyArray()
        for (f in files) {
            val cleanName = getVaultDisplayName(f.name)
            val ext = cleanName.substringAfterLast('.', "").lowercase()
            val mime = when (ext) {
                "jpg", "jpeg", "png", "webp", "gif", "heic", "bmp" -> "image/*"
                "mp4", "mkv", "avi", "mov", "3gp", "webm", "ts", "flv" -> "video/*"
                "mp3", "wav", "m4a", "ogg", "flac" -> "audio/*"
                "pdf" -> "application/pdf"
                "doc", "docx", "xls", "xlsx", "ppt", "pptx", "txt", "csv", "json", "xml" -> "application/msword"
                "apk" -> "application/vnd.android.package-archive"
                else -> if (f.isDirectory) "inode/directory" else "*/*"
            }
            list.add(
                FileItem(
                    id = UUID.randomUUID().toString(),
                    name = cleanName,
                    uri = Uri.fromFile(f),
                    path = f.absolutePath,
                    isFolder = f.isDirectory,
                    sizeBytes = if (f.isFile) f.length() else 0L,
                    lastModified = f.lastModified(),
                    mimeType = mime,
                    extension = ext
                )
            )
        }
        vaultFiles = list.sortedByDescending { it.lastModified }
    }

    LaunchedEffect(isUnlocked) {
        if (isUnlocked) {
            refreshVaultFiles()
        }
    }

    val filteredFiles = remember(vaultFiles, selectedCategory) {
        when (selectedCategory) {
            "Images" -> vaultFiles.filter { it.mimeType?.startsWith("image") == true || it.extension in listOf("jpg", "jpeg", "png", "webp", "gif", "heic", "bmp") }
            "Videos" -> vaultFiles.filter { it.mimeType?.startsWith("video") == true || it.extension in listOf("mp4", "mkv", "avi", "mov", "3gp", "webm", "ts", "flv") }
            "Audio" -> vaultFiles.filter { it.mimeType?.startsWith("audio") == true || it.extension in listOf("mp3", "wav", "m4a", "ogg", "flac") }
            "Documents" -> vaultFiles.filter { it.extension in listOf("pdf", "doc", "docx", "xls", "xlsx", "ppt", "pptx", "txt", "csv", "json", "xml", "epub") }
            "Folders" -> vaultFiles.filter { it.isFolder }
            else -> vaultFiles
        }
    }

    // File Picker to add files directly into Vault
    val addFilesLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetMultipleContents()
    ) { uris: List<Uri> ->
        if (uris.isNotEmpty()) {
            for (uri in uris) {
                try {
                    val inputStream = context.contentResolver.openInputStream(uri)
                    var originalName = "unknown"
                    context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
                        if (cursor.moveToFirst()) {
                            val nameIdx = cursor.getColumnIndex(android.provider.OpenableColumns.DISPLAY_NAME)
                            if (nameIdx != -1) originalName = cursor.getString(nameIdx)
                        }
                    }
                    val ext = originalName.substringAfterLast('.', "")
                    val finalExt = if (ext.isNotEmpty() && originalName.contains(".")) ".$ext" else ""
                    val encodedName = android.util.Base64.encodeToString(originalName.toByteArray(), android.util.Base64.URL_SAFE or android.util.Base64.NO_WRAP)
                    
                    val fileName = "vault_${encodedName}_${System.currentTimeMillis()}$finalExt"
                    val destFile = File(safeFolderDir, fileName)
                    if (inputStream != null) {
                        FileOutputStream(destFile).use { output ->
                            try {
                                com.fyllo.filemanager.core.security.EncryptionService().encryptStream(inputStream, output)
                            } catch (e: Exception) {
                                inputStream.copyTo(output)
                            }
                        }
                    }
                    // Delete original file/content and purge from MediaStore
                    if (uri.scheme == "content") {
                        try { context.contentResolver.delete(uri, null, null) } catch (e: Exception) { }
                    } else if (uri.scheme == "file" && uri.path != null) {
                        val src = File(uri.path!!)
                        src.delete()
                        android.media.MediaScannerConnection.scanFile(context, arrayOf(src.absolutePath), null, null)
                        val contentUri = android.provider.MediaStore.Files.getContentUri("external")
                        val where = "${android.provider.MediaStore.Files.FileColumns.DATA} = ?"
                        context.contentResolver.delete(contentUri, where, arrayOf(src.absolutePath))
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
            refreshVaultFiles()
            android.widget.Toast.makeText(context, "Moved to Vault", android.widget.Toast.LENGTH_SHORT).show()
        }
    }

    if (isUnlocked) {
        // Vault Explorer View
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Outlined.Security, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(24.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Vault", fontWeight = FontWeight.Bold)
                        }
                    },
                    navigationIcon = {
                        IconButton(onClick = onBackClick) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                        }
                    },
                    actions = {
                        IconButton(onClick = { isGridView = !isGridView }) {
                            Icon(if (isGridView) Icons.AutoMirrored.Filled.List else Icons.Default.GridView, contentDescription = "Toggle View")
                        }
                        IconButton(onClick = { isUnlocked = false }) {
                            Icon(Icons.Default.Lock, contentDescription = "Lock Vault", tint = MaterialTheme.colorScheme.primary)
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
                )
            },
            floatingActionButton = {
                FloatingActionButton(
                    onClick = { addFilesLauncher.launch("*/*") },
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                    shape = CircleShape
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Add Files")
                }
            },
            containerColor = MaterialTheme.colorScheme.background
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                // Category Filter Pills
                val categories = listOf("All", "Images", "Videos", "Audio", "Documents", "Folders")
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState())
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    for (cat in categories) {
                        val isSelected = cat == selectedCategory
                        FilterChip(
                            selected = isSelected,
                            onClick = { selectedCategory = cat },
                            label = { Text(cat, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f),
                                selectedLabelColor = MaterialTheme.colorScheme.primary
                            ),
                            shape = RoundedCornerShape(20.dp)
                        )
                    }
                }

                Box(modifier = Modifier.fillMaxSize().weight(1f)) {
                    if (filteredFiles.isEmpty()) {
                        Column(
                            modifier = Modifier.align(Alignment.Center),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(Icons.Outlined.Security, contentDescription = null, tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f), modifier = Modifier.size(64.dp))
                            Spacer(modifier = Modifier.height(16.dp))
                            Text("No ${if (selectedCategory == "All") "files" else selectedCategory.lowercase()} in Vault", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("Tap + to add private files & media", color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f), fontSize = 13.sp)
                        }
                    } else {
                        var selectedFileForOptions by remember { mutableStateOf<FileItem?>(null) }
                        
                        val isImageOrVideoCategory = selectedCategory == "Images" || selectedCategory == "Videos"

                        if (isGridView || isImageOrVideoCategory) {
                            // Masonry Staggered Grid for Images and Videos
                            LazyVerticalStaggeredGrid(
                                columns = StaggeredGridCells.Fixed(if (isImageOrVideoCategory) 2 else 3),
                                modifier = Modifier.fillMaxSize(),
                                contentPadding = PaddingValues(start = 8.dp, end = 8.dp, top = 4.dp, bottom = 80.dp),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalItemSpacing = 8.dp
                            ) {
                                staggeredItems(filteredFiles, key = { it.path }) { file ->
                                    VaultFileGridItem(
                                        file = file,
                                        isMasonry = isImageOrVideoCategory,
                                        onClick = { selectedFileForOptions = file },
                                        onOptionsClick = { selectedFileForOptions = file }
                                    )
                                }
                            }
                        } else {
                            LazyColumn(
                                modifier = Modifier.fillMaxSize(),
                                contentPadding = PaddingValues(bottom = 80.dp)
                            ) {
                                items(filteredFiles, key = { it.path }) { file ->
                                    VaultFileListItem(
                                        file = file,
                                        onClick = { selectedFileForOptions = file },
                                        onOptionsClick = { selectedFileForOptions = file }
                                    )
                                }
                            }
                        }

                        selectedFileForOptions?.let { file ->
                            ModalBottomSheet(
                                onDismissRequest = { selectedFileForOptions = null },
                                containerColor = MaterialTheme.colorScheme.surface
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Text(file.name, fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 16.dp))
                                    ListItem(
                                        headlineContent = { Text("Restore / Move Out of Vault") },
                                        supportingContent = { Text("Restores file to public storage") },
                                        leadingContent = { Icon(Icons.Default.DriveFileMove, contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
                                        modifier = Modifier.clickable {
                                            selectedFileForOptions = null
                                            restoreVaultFile(context, file)
                                            refreshVaultFiles()
                                        }
                                    )
                                    ListItem(
                                        headlineContent = { Text("Delete Permanently") },
                                        supportingContent = { Text("Deletes file permanently from Vault") },
                                        leadingContent = { Icon(Icons.Default.Delete, contentDescription = null, tint = MaterialTheme.colorScheme.error) },
                                        colors = ListItemDefaults.colors(headlineColor = MaterialTheme.colorScheme.error),
                                        modifier = Modifier.clickable {
                                            selectedFileForOptions = null
                                            File(file.path).delete()
                                            refreshVaultFiles()
                                            android.widget.Toast.makeText(context, "Deleted from Vault", android.widget.Toast.LENGTH_SHORT).show()
                                        }
                                    )
                                    Spacer(modifier = Modifier.height(32.dp))
                                }
                            }
                        }
                    }
                }
            }
        }
    } else {
        // Vault Lock Screen (PIN / Fingerprint)
        val isSetupMode = remember { !appLockManager.hasSafeFolderPin() }

        var pin by remember { mutableStateOf("") }
        var confirmPin by remember { mutableStateOf("") }
        var isConfirmStage by remember { mutableStateOf(false) }
        var errorMessage by remember { mutableStateOf<String?>(null) }
        val maxPinLength = 4

        fun triggerBiometric() {
            val activity = context as? FragmentActivity ?: return
            val executor = ContextCompat.getMainExecutor(context)
            val prompt = BiometricPrompt(
                activity,
                executor,
                object : BiometricPrompt.AuthenticationCallback() {
                    override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                        isUnlocked = true
                        onPinSuccess()
                    }
                    override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {}
                }
            )
            val promptInfo = BiometricPrompt.PromptInfo.Builder()
                .setTitle("Unlock Vault")
                .setSubtitle("Use your fingerprint to proceed")
                .setNegativeButtonText("Use PIN")
                .build()

            prompt.authenticate(promptInfo)
        }

        LaunchedEffect(Unit) {
            if (!isSetupMode && appLockManager.isBiometricEnabled) {
                triggerBiometric()
            }
        }

        val onNumberClick: (String) -> Unit = { number ->
            errorMessage = null
            if (!isSetupMode) {
                if (pin.length < maxPinLength) {
                    pin += number
                    if (pin.length == maxPinLength) {
                        if (appLockManager.verifySafeFolderPin(pin)) {
                            isUnlocked = true
                            onPinSuccess()
                            pin = ""
                        } else {
                            errorMessage = "Incorrect PIN"
                            pin = ""
                        }
                    }
                }
            } else {
                if (!isConfirmStage) {
                    if (pin.length < maxPinLength) {
                        pin += number
                        if (pin.length == maxPinLength) {
                            isConfirmStage = true
                        }
                    }
                } else {
                    if (confirmPin.length < maxPinLength) {
                        confirmPin += number
                        if (confirmPin.length == maxPinLength) {
                            if (confirmPin == pin) {
                                appLockManager.setSafeFolderPin(pin)
                                isUnlocked = true
                                onPinSuccess()
                            } else {
                                errorMessage = "PINs do not match. Try again."
                                pin = ""
                                confirmPin = ""
                                isConfirmStage = false
                            }
                        }
                    }
                }
            }
        }

        val onDeleteClick = {
            if (!isSetupMode || !isConfirmStage) {
                if (pin.isNotEmpty()) pin = pin.dropLast(1)
            } else {
                if (confirmPin.isNotEmpty()) confirmPin = confirmPin.dropLast(1)
            }
        }

        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Vault", color = MaterialTheme.colorScheme.onBackground) },
                    navigationIcon = {
                        IconButton(onClick = onBackClick) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = MaterialTheme.colorScheme.onBackground)
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
                )
            },
            containerColor = MaterialTheme.colorScheme.background
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(36.dp))

                Box(
                    modifier = Modifier
                        .size(72.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFFFB300).copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Security,
                        contentDescription = "Vault Lock",
                        tint = Color(0xFFFFB300),
                        modifier = Modifier.size(36.dp)
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                val currentPinDisplay = if (isConfirmStage) confirmPin else pin
                val promptText = when {
                    errorMessage != null -> errorMessage!!
                    isSetupMode && !isConfirmStage -> "Set a 4-digit PIN for Vault"
                    isSetupMode && isConfirmStage -> "Confirm your 4-digit PIN"
                    else -> "Enter PIN to unlock Vault"
                }

                Text(
                    text = promptText,
                    color = if (errorMessage != null) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onBackground,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold
                )

                Spacer(modifier = Modifier.height(36.dp))

                // PIN Dots
                Row(
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    for (i in 0 until maxPinLength) {
                        val isActive = i < currentPinDisplay.length
                        Box(
                            modifier = Modifier
                                .size(16.dp)
                                .clip(CircleShape)
                                .background(if (isActive) Color(0xFFFFB300) else MaterialTheme.colorScheme.surfaceVariant)
                        )
                    }
                }

                Spacer(modifier = Modifier.weight(1f))

                // Keypad
                Column(
                    modifier = Modifier.padding(horizontal = 48.dp, vertical = 32.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    val rows = listOf(
                        listOf("1", "2", "3"),
                        listOf("4", "5", "6"),
                        listOf("7", "8", "9")
                    )

                    for (row in rows) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            for (number in row) {
                                SafeKeypadButton(text = number, onClick = { onNumberClick(number) })
                            }
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        if (!isSetupMode && appLockManager.isBiometricEnabled) {
                            Box(
                                modifier = Modifier
                                    .size(64.dp)
                                    .clip(CircleShape)
                                    .clickable { triggerBiometric() },
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.Fingerprint,
                                    contentDescription = "Fingerprint",
                                    tint = Color(0xFFFFB300),
                                    modifier = Modifier.size(32.dp)
                                )
                            }
                        } else {
                            Spacer(modifier = Modifier.size(64.dp))
                        }

                        SafeKeypadButton(text = "0", onClick = { onNumberClick("0") })

                        Box(
                            modifier = Modifier
                                .size(64.dp)
                                .clip(CircleShape)
                                .clickable(onClick = onDeleteClick),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Outlined.Backspace,
                                contentDescription = "Delete",
                                tint = MaterialTheme.colorScheme.onBackground
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

@Composable
private fun SafeKeypadButton(text: String, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .size(64.dp)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

fun restoreVaultFile(context: android.content.Context, file: FileItem) {
    try {
        val parts = file.name.split("_")
        if (parts.size >= 3 && parts[0] == "vault") {
            val encodedName = parts[1]
            val originalNameOrPath = String(android.util.Base64.decode(encodedName, android.util.Base64.URL_SAFE or android.util.Base64.NO_WRAP))
            var destFile = File(originalNameOrPath)
            if (!destFile.isAbsolute) {
                val downloads = android.os.Environment.getExternalStoragePublicDirectory(android.os.Environment.DIRECTORY_DOWNLOADS)
                destFile = File(downloads, originalNameOrPath)
            } else if (!destFile.parentFile?.exists()!!) {
                 destFile.parentFile?.mkdirs()
            }
            
            val source = File(file.path)
            try {
                com.fyllo.filemanager.core.security.EncryptionService().decryptFile(source, destFile)
            } catch (e: Exception) {
                source.copyTo(destFile, overwrite = true)
            }
            source.delete()
            android.media.MediaScannerConnection.scanFile(context, arrayOf(destFile.absolutePath), null, null)
            android.widget.Toast.makeText(context, "Restored to ${destFile.name}", android.widget.Toast.LENGTH_LONG).show()
        } else {
            val downloads = android.os.Environment.getExternalStoragePublicDirectory(android.os.Environment.DIRECTORY_DOWNLOADS)
            val destFile = File(downloads, file.name)
            val source = File(file.path)
            try {
                com.fyllo.filemanager.core.security.EncryptionService().decryptFile(source, destFile)
            } catch (e: Exception) {
                source.copyTo(destFile, overwrite = true)
            }
            source.delete()
            android.media.MediaScannerConnection.scanFile(context, arrayOf(destFile.absolutePath), null, null)
            android.widget.Toast.makeText(context, "Restored to Downloads", android.widget.Toast.LENGTH_SHORT).show()
        }
    } catch (e: Exception) {
        android.widget.Toast.makeText(context, "Failed to restore", android.widget.Toast.LENGTH_SHORT).show()
    }
}

@Composable
private fun VaultFileGridItem(
    file: FileItem,
    isMasonry: Boolean = false,
    onClick: () -> Unit,
    onOptionsClick: () -> Unit
) {
    val context = LocalContext.current
    var bitmap by remember(file.path) { mutableStateOf<androidx.compose.ui.graphics.ImageBitmap?>(null) }
    var imageAspectRatio by remember(file.path) { mutableStateOf<Float?>(null) }

    LaunchedEffect(file.path) {
        kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
            try {
                val f = File(file.path)
                if (f.exists()) {
                    val outStream = java.io.ByteArrayOutputStream()
                    f.inputStream().use { input ->
                        com.fyllo.filemanager.core.security.EncryptionService().decryptStream(input, outStream)
                    }
                    val bytes = outStream.toByteArray()
                    if (file.mimeType?.startsWith("image") == true || file.extension in listOf("jpg", "jpeg", "png", "webp", "gif", "heic", "bmp")) {
                        val opts = android.graphics.BitmapFactory.Options().apply { inSampleSize = 2 }
                        val bmp = android.graphics.BitmapFactory.decodeByteArray(bytes, 0, bytes.size, opts)
                        if (bmp != null) {
                            bitmap = bmp.asImageBitmap()
                            if (bmp.width > 0 && bmp.height > 0) {
                                imageAspectRatio = (bmp.width.toFloat() / bmp.height.toFloat()).coerceIn(0.55f, 1.8f)
                            }
                        }
                    } else if (file.mimeType?.startsWith("video") == true || file.extension in listOf("mp4", "mkv", "avi", "mov")) {
                        val temp = File.createTempFile("v_thumb", ".mp4", context.cacheDir)
                        temp.writeBytes(bytes)
                        val retriever = android.media.MediaMetadataRetriever()
                        try {
                            retriever.setDataSource(temp.absolutePath)
                            val bmp = retriever.frameAtTime
                            if (bmp != null) {
                                bitmap = bmp.asImageBitmap()
                                if (bmp.width > 0 && bmp.height > 0) {
                                    imageAspectRatio = (bmp.width.toFloat() / bmp.height.toFloat()).coerceIn(0.55f, 1.8f)
                                }
                            }
                        } catch (e: Exception) {}
                        retriever.release()
                        temp.delete()
                    }
                }
            } catch (e: Exception) {}
        }
    }

    val cardModifier = if (isMasonry) {
        val aspect = imageAspectRatio ?: if (file.id.hashCode() % 2 == 0) 0.75f else 1.1f
        Modifier
            .fillMaxWidth()
            .aspectRatio(aspect)
            .clip(RoundedCornerShape(16.dp))
            .clickable { onClick() }
    } else {
        Modifier
            .fillMaxWidth()
            .aspectRatio(1f)
            .clip(RoundedCornerShape(16.dp))
            .clickable { onClick() }
    }

    Card(
        modifier = cardModifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            if (bitmap != null) {
                androidx.compose.foundation.Image(
                    bitmap = bitmap!!,
                    contentDescription = file.name,
                    contentScale = androidx.compose.ui.layout.ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
                if (file.mimeType?.startsWith("video") == true || file.extension in listOf("mp4", "mkv", "avi", "mov")) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.Center)
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(Color.Black.copy(alpha = 0.5f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.PlayArrow,
                            contentDescription = "Video",
                            tint = Color.White,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }
            } else {
                Column(
                    modifier = Modifier.align(Alignment.Center),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    val icon = when {
                        file.isFolder -> Icons.Outlined.Folder
                        file.mimeType?.startsWith("image") == true -> Icons.Outlined.Image
                        file.mimeType?.startsWith("video") == true -> Icons.Outlined.Movie
                        file.mimeType?.startsWith("audio") == true -> Icons.Outlined.AudioFile
                        else -> Icons.Outlined.Description
                    }
                    Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(36.dp))
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = file.name,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        maxLines = 1,
                        overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
                        modifier = Modifier.padding(horizontal = 8.dp)
                    )
                }
            }
            IconButton(
                onClick = onOptionsClick,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(4.dp)
                    .size(28.dp)
                    .background(Color.Black.copy(alpha = 0.35f), CircleShape)
            ) {
                Icon(Icons.Outlined.MoreVert, contentDescription = "Options", tint = Color.White, modifier = Modifier.size(18.dp))
            }
        }
    }
}

@Composable
private fun VaultFileListItem(
    file: FileItem,
    onClick: () -> Unit,
    onOptionsClick: () -> Unit
) {
    val context = LocalContext.current
    var bitmap by remember(file.path) { mutableStateOf<androidx.compose.ui.graphics.ImageBitmap?>(null) }

    LaunchedEffect(file.path) {
        kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
            try {
                val f = File(file.path)
                if (f.exists()) {
                    val outStream = java.io.ByteArrayOutputStream()
                    f.inputStream().use { input ->
                        com.fyllo.filemanager.core.security.EncryptionService().decryptStream(input, outStream)
                    }
                    val bytes = outStream.toByteArray()
                    if (file.mimeType?.startsWith("image") == true || file.extension in listOf("jpg", "jpeg", "png", "webp", "gif")) {
                        val opts = android.graphics.BitmapFactory.Options().apply { inSampleSize = 4 }
                        val bmp = android.graphics.BitmapFactory.decodeByteArray(bytes, 0, bytes.size, opts)
                        bitmap = bmp?.asImageBitmap()
                    } else if (file.mimeType?.startsWith("video") == true || file.extension in listOf("mp4", "mkv", "avi", "mov")) {
                        val temp = File.createTempFile("v_thumb", ".mp4", context.cacheDir)
                        temp.writeBytes(bytes)
                        val retriever = android.media.MediaMetadataRetriever()
                        try {
                            retriever.setDataSource(temp.absolutePath)
                            val bmp = retriever.frameAtTime
                            bitmap = bmp?.asImageBitmap()
                        } catch (e: Exception) {}
                        retriever.release()
                        temp.delete()
                    }
                }
            } catch (e: Exception) {}
        }
    }

    ListItem(
        headlineContent = { Text(file.name, fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis) },
        supportingContent = { Text(android.text.format.Formatter.formatFileSize(context, file.sizeBytes), fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant) },
        leadingContent = {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant),
                contentAlignment = Alignment.Center
            ) {
                if (bitmap != null) {
                    androidx.compose.foundation.Image(
                        bitmap = bitmap!!,
                        contentDescription = file.name,
                        contentScale = androidx.compose.ui.layout.ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    val icon = when {
                        file.isFolder -> Icons.Outlined.Folder
                        file.mimeType?.startsWith("image") == true -> Icons.Outlined.Image
                        file.mimeType?.startsWith("video") == true -> Icons.Outlined.Movie
                        file.mimeType?.startsWith("audio") == true -> Icons.Outlined.AudioFile
                        else -> Icons.Outlined.Description
                    }
                    Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(24.dp))
                }
            }
        },
        trailingContent = {
            IconButton(onClick = onOptionsClick) {
                Icon(Icons.Outlined.MoreVert, contentDescription = "Options")
            }
        },
        modifier = Modifier.clickable { onClick() }
    )
}
