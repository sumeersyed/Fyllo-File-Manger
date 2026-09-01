package com.fyllo.filemanager.ui.screens

import android.text.format.Formatter
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.items as staggeredItems
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.waitForUpOrCancellation
import kotlinx.coroutines.delay
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.VideogameAsset
import androidx.compose.foundation.border
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.ContentCut
import androidx.compose.material.icons.filled.ContentPaste
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FolderZip
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.fyllo.filemanager.domain.model.FileItem
import com.fyllo.filemanager.ui.theme.NeonCyan
import com.fyllo.filemanager.ui.theme.NeonGreen
import com.fyllo.filemanager.ui.theme.NeonPink
import androidx.compose.material3.MaterialTheme
import com.fyllo.filemanager.ui.theme.LocalEInkMode
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

import androidx.compose.material.icons.filled.ViewList
import androidx.compose.material.icons.filled.ViewModule
import androidx.compose.material.icons.filled.FormatListBulleted
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.outlined.Smartphone
import androidx.compose.material.icons.outlined.Folder
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.Spring
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import com.fyllo.filemanager.ui.components.FloatingBottomDock
import com.fyllo.filemanager.ui.components.DashboardNavTab

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FilesScreen(
    files: List<FileItem>,
    isLoading: Boolean,
    currentPath: String,
    onBackClick: () -> Unit,
    onFileClick: (FileItem) -> Unit,
    onRenameClick: (FileItem, String) -> Unit,
    onMoveClick: (FileItem, android.net.Uri) -> Unit,
    onCopyClick: (FileItem, android.net.Uri) -> Unit,
    onCompressClick: (FileItem) -> Unit,
    onExtractClick: (FileItem) -> Unit = {},
    onDeleteClick: (FileItem) -> Unit,
    onDeleteMultiple: (List<android.net.Uri>) -> Unit = {},
    onDeleteWithOptions: (List<android.net.Uri>, Boolean) -> Unit = { _, _ -> },
    onCopyMultiple: (List<android.net.Uri>, Boolean) -> Unit = { _, _ -> },
    onCompressMultiple: (List<android.net.Uri>) -> Unit = { _ -> },
    clipboardState: com.fyllo.filemanager.ui.screens.ClipboardState? = null,
    operationState: com.fyllo.filemanager.domain.model.FileOperationState = com.fyllo.filemanager.domain.model.FileOperationState.Idle,
    conflictDialogState: com.fyllo.filemanager.ui.screens.ConflictDialogState? = null,
    onResolveConflict: (com.fyllo.filemanager.domain.model.ConflictStrategy) -> Unit = {},
    onCancelConflict: () -> Unit = {},
    onCancelOperation: () -> Unit = {},
    onPasteClick: () -> Unit = {},
    onClearClipboardClick: () -> Unit = {},
    isGridView: Boolean = false,
    currentSortOption: SortOption = SortOption.NAME_ASC,
    onSetGridView: (Boolean) -> Unit = {},
    onSortFiles: (SortOption) -> Unit = {},
    onCreateFolder: (String) -> Unit = {},
    onNavigateToSearch: () -> Unit = {},
    onNavigateToHome: () -> Unit = {},
    onNavigateToApps: () -> Unit = {},
    onNavigateToFolderByPath: (String) -> Unit = {},
    onNavigateUp: () -> Boolean = { false }
) {
    var selectedFileForOptions by remember { mutableStateOf<FileItem?>(null) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var showMoreMenu by remember { mutableStateOf(false) }
    var showSortMenu by remember { mutableStateOf(false) }
    var showNewFolderDialog by remember { mutableStateOf(false) }
    var newFolderName by remember { mutableStateOf("") }
    var isSelectionMode by remember { mutableStateOf(false) }
    var selectedFiles by remember { mutableStateOf(setOf<android.net.Uri>()) }

    var showRenameDialog by remember { mutableStateOf(false) }
    var renameValue by remember { mutableStateOf("") }
    var showDetailsDialog by remember { mutableStateOf<FileItem?>(null) }

    val context = LocalContext.current
    val rootPath = android.os.Environment.getExternalStorageDirectory().absolutePath
    val folderName = when {
        currentPath.isEmpty() || currentPath == rootPath || currentPath == "/storage/emulated/0" || currentPath == "/sdcard" -> "Internal storage"
        else -> currentPath.substringAfterLast("/").takeIf { it.isNotEmpty() && it != "0" } ?: "Internal storage"
    }

    val copyLauncher = androidx.activity.compose.rememberLauncherForActivityResult(
        androidx.activity.result.contract.ActivityResultContracts.OpenDocumentTree()
    ) { uri ->
        if (uri != null && selectedFileForOptions != null) {
            onCopyClick(selectedFileForOptions!!, uri)
            selectedFileForOptions = null
        }
    }

    val moveLauncher = androidx.activity.compose.rememberLauncherForActivityResult(
        androidx.activity.result.contract.ActivityResultContracts.OpenDocumentTree()
    ) { uri ->
        if (uri != null && selectedFileForOptions != null) {
            onMoveClick(selectedFileForOptions!!, uri)
            selectedFileForOptions = null
        }
    }



    // Operation Progress Overlay
    if (operationState is com.fyllo.filemanager.domain.model.FileOperationState.Running) {
        if (operationState.operationType == "ZIP" || operationState.operationType == "UNZIP") {
            com.fyllo.filemanager.ui.components.ZipOperationDialog(
                state = operationState,
                onCancel = onCancelOperation
            )
        } else {
            com.fyllo.filemanager.ui.components.WaveOperationProgressDialog(
                operationTitle = "Processing",
                state = operationState,
                onCancel = onCancelOperation
            )
        }
    }

    if (conflictDialogState != null) {
        AlertDialog(
            onDismissRequest = onCancelConflict,
            title = { Text("File Conflict Detected") },
            text = { Text("One or more files/folders with the same name already exist in the destination. How would you like to proceed?") },
            confirmButton = {},
            dismissButton = {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Button(
                        onClick = { onResolveConflict(com.fyllo.filemanager.domain.model.ConflictStrategy.REPLACE) },
                        modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
                    ) {
                        Text("Replace / Merge")
                    }
                    Button(
                        onClick = { onResolveConflict(com.fyllo.filemanager.domain.model.ConflictStrategy.KEEP_BOTH) },
                        modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
                    ) {
                        Text("Keep Both")
                    }
                    Button(
                        onClick = { onResolveConflict(com.fyllo.filemanager.domain.model.ConflictStrategy.SKIP) },
                        modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
                    ) {
                        Text("Skip")
                    }
                    Button(
                        onClick = onCancelConflict,
                        colors = ButtonDefaults.textButtonColors(),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Cancel")
                    }
                }
            }
        )
    }

    if (showNewFolderDialog) {
        AlertDialog(
            onDismissRequest = { showNewFolderDialog = false },
            title = { Text("Add New Folder") },
            text = {
                OutlinedTextField(
                    value = newFolderName,
                    onValueChange = { newFolderName = it },
                    label = { Text("Folder Name") },
                    colors = if (LocalEInkMode.current) OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.onBackground,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline
                    ) else OutlinedTextFieldDefaults.colors()
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (newFolderName.isNotBlank()) {
                            onCreateFolder(newFolderName)
                            newFolderName = ""
                        }
                        showNewFolderDialog = false
                    },
                    colors = if (LocalEInkMode.current) ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.surface,
                        contentColor = MaterialTheme.colorScheme.onSurface
                    ) else ButtonDefaults.buttonColors()
                ) {
                    Text("Create")
                }
            },
            dismissButton = {
                Button(
                    onClick = { showNewFolderDialog = false },
                    colors = ButtonDefaults.textButtonColors()
                ) {
                    Text("Cancel")
                }
            },
            containerColor = MaterialTheme.colorScheme.background
        )
    }

    if (selectedFileForOptions != null) {
        com.fyllo.filemanager.ui.components.FileOptionsBottomSheet(
            fileName = selectedFileForOptions!!.name,
            onDismiss = { selectedFileForOptions = null },
            onDeleteClick = { showDeleteDialog = true },
            onRenameClick = { 
                renameValue = selectedFileForOptions!!.name
                showRenameDialog = true
            },
            onMoveClick = { moveLauncher.launch(android.net.Uri.parse("content://")) },
            onCopyClick = { copyLauncher.launch(android.net.Uri.parse("content://")) },
            onCopyClipboardClick = {
                onCopyMultiple(listOf(selectedFileForOptions!!.uri), false)
                selectedFileForOptions = null
            },
            onCutClipboardClick = {
                onCopyMultiple(listOf(selectedFileForOptions!!.uri), true)
                selectedFileForOptions = null
            },
            onCompressClick = {
                onCompressClick(selectedFileForOptions!!)
                selectedFileForOptions = null
            },
            onExtractClick = {
                onExtractClick(selectedFileForOptions!!)
                selectedFileForOptions = null
            },
            isArchive = selectedFileForOptions!!.extension.lowercase() in listOf("zip", "rar", "7z", "tar", "gz", "tgz", "bz2"),
            onShareClick = {
                val shareIntent = android.content.Intent(android.content.Intent.ACTION_SEND).apply {
                    type = selectedFileForOptions!!.mimeType ?: "*/*"
                    putExtra(android.content.Intent.EXTRA_STREAM, selectedFileForOptions!!.uri)
                    addFlags(android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION)
                }
                context.startActivity(android.content.Intent.createChooser(shareIntent, "Share File"))
                selectedFileForOptions = null
            },
            onDetailsClick = { 
                showDetailsDialog = selectedFileForOptions
                selectedFileForOptions = null
            },
            onFavoriteClick = {
                val sharedPrefs = context.getSharedPreferences("media_favorites", android.content.Context.MODE_PRIVATE)
                sharedPrefs.edit().putBoolean(selectedFileForOptions!!.uri.toString(), true).apply()
                android.widget.Toast.makeText(context, "Added to Favorites", android.widget.Toast.LENGTH_SHORT).show()
                selectedFileForOptions = null
            },
            onSafeFolderClick = {
                onMoveClick(selectedFileForOptions!!, android.net.Uri.parse("file://${context.filesDir.absolutePath}/SafeFolder"))
                selectedFileForOptions = null
            }
        )
    }

    if (showRenameDialog && selectedFileForOptions != null) {
        AlertDialog(
            onDismissRequest = { showRenameDialog = false },
            title = { Text("Rename File") },
            text = {
                OutlinedTextField(
                    value = renameValue,
                    onValueChange = { renameValue = it },
                    label = { Text("New Name") }
                )
            },
            confirmButton = {
                Button(onClick = {
                    if (renameValue.isNotBlank()) {
                        onRenameClick(selectedFileForOptions!!, renameValue)
                    }
                    showRenameDialog = false
                    selectedFileForOptions = null
                }) { Text("Rename") }
            },
            dismissButton = {
                Button(onClick = { showRenameDialog = false }) { Text("Cancel") }
            }
        )
    }

    showDetailsDialog?.let { file ->
        com.fyllo.filemanager.ui.components.DetailsDialog(
            file = file,
            onDismiss = { showDetailsDialog = null }
        )
    }

    if (showDeleteDialog && selectedFileForOptions != null) {
        com.fyllo.filemanager.ui.components.DeleteConfirmationDialog(
            fileName = selectedFileForOptions!!.name,
            onConfirm = {
                onDeleteClick(selectedFileForOptions!!)
                showDeleteDialog = false
                selectedFileForOptions = null
            },
            onConfirmWithOptions = { permanent ->
                onDeleteWithOptions(listOf(selectedFileForOptions!!.uri), permanent)
                showDeleteDialog = false
                selectedFileForOptions = null
            },
            onDismiss = { showDeleteDialog = false }
        )
    }

    androidx.activity.compose.BackHandler(enabled = true) {
        if (!onNavigateUp()) {
            onBackClick()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(folderName, maxLines = 1) },
                navigationIcon = {
                    IconButton(onClick = { if (!onNavigateUp()) onBackClick() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = MaterialTheme.colorScheme.onBackground)
                    }
                },
                actions = {
                    IconButton(onClick = onNavigateToSearch) {
                        Icon(Icons.Default.Search, contentDescription = "Search", tint = MaterialTheme.colorScheme.onBackground)
                    }
                    IconButton(onClick = { showMoreMenu = true }) {
                        Icon(Icons.Default.MoreVert, contentDescription = "More", tint = MaterialTheme.colorScheme.onBackground)
                    }
                    DropdownMenu(
                        expanded = showMoreMenu,
                        onDismissRequest = { showMoreMenu = false },
                        modifier = if (LocalEInkMode.current) Modifier.background(MaterialTheme.colorScheme.background).border(1.dp, MaterialTheme.colorScheme.outline) else Modifier
                    ) {
                        DropdownMenuItem(
                            text = { Text("Select All") },
                            onClick = {
                                isSelectionMode = true
                                selectedFiles = files.map { it.uri }.toSet()
                                showMoreMenu = false
                            }
                        )
                        DropdownMenuItem(
                            text = { Text(if (isSelectionMode) "Cancel Selection" else "Select") },
                            onClick = {
                                isSelectionMode = !isSelectionMode
                                if (!isSelectionMode) selectedFiles = emptySet()
                                showMoreMenu = false
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Sort by") },
                            onClick = {
                                showSortMenu = true
                                showMoreMenu = false
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Add new folder") },
                            onClick = {
                                showNewFolderDialog = true
                                showMoreMenu = false
                            }
                        )
                        DropdownMenuItem(
                            text = { Text(if (isGridView) "View Details" else "View Grid") },
                            onClick = {
                                onSetGridView(!isGridView)
                                showMoreMenu = false
                            }
                        )
                    }
                    DropdownMenu(
                        expanded = showSortMenu,
                        onDismissRequest = { showSortMenu = false },
                        modifier = if (LocalEInkMode.current) Modifier.background(MaterialTheme.colorScheme.background).border(1.dp, MaterialTheme.colorScheme.outline) else Modifier
                    ) {
                        SortOption.values().forEach { option ->
                            val label = when (option) {
                                SortOption.NAME_ASC -> "Name (A-Z)"
                                SortOption.NAME_DESC -> "Name (Z-A)"
                                SortOption.DATE_ASC -> "Date (Oldest)"
                                SortOption.DATE_DESC -> "Date (Newest)"
                                SortOption.SIZE_ASC -> "Size (Smallest)"
                                SortOption.SIZE_DESC -> "Size (Largest)"
                            }
                            val isSelected = currentSortOption == option
                            DropdownMenuItem(
                                text = { 
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = label, 
                                            color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onBackground,
                                            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal
                                        )
                                        if (isSelected) {
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Icon(Icons.Default.Check, contentDescription = "Selected", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp))
                                        }
                                    }
                                },
                                onClick = {
                                    onSortFiles(option)
                                    showSortMenu = false
                                }
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        bottomBar = {
            if (operationState !is com.fyllo.filemanager.domain.model.FileOperationState.Running) {
                if (isSelectionMode) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 24.dp, vertical = 12.dp)
                            .navigationBarsPadding(),
                        contentAlignment = Alignment.Center
                    ) {
                        Surface(
                            shape = RoundedCornerShape(32.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.95f),
                            shadowElevation = 8.dp,
                            border = androidx.compose.foundation.BorderStroke(
                                1.dp,
                                MaterialTheme.colorScheme.primary.copy(alpha = 0.25f)
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(64.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(horizontal = 8.dp),
                                horizontalArrangement = Arrangement.SpaceEvenly,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // Copy Action
                                Column(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(20.dp))
                                        .clickable {
                                            onCopyMultiple(selectedFiles.toList(), false)
                                            isSelectionMode = false
                                            selectedFiles = emptySet()
                                        }
                                        .padding(vertical = 6.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.Center
                                ) {
                                    Icon(Icons.Default.ContentCopy, contentDescription = "Copy", tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(20.dp))
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text("Copy", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }

                                // Move Action
                                Column(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(20.dp))
                                        .clickable {
                                            onCopyMultiple(selectedFiles.toList(), true)
                                            isSelectionMode = false
                                            selectedFiles = emptySet()
                                        }
                                        .padding(vertical = 6.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.Center
                                ) {
                                    Icon(Icons.Default.ContentCut, contentDescription = "Move", tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(20.dp))
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text("Move", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }

                                // Zip Action
                                Column(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(20.dp))
                                        .clickable {
                                            onCompressMultiple(selectedFiles.toList())
                                            isSelectionMode = false
                                            selectedFiles = emptySet()
                                        }
                                        .padding(vertical = 6.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.Center
                                ) {
                                    Icon(Icons.Default.FolderZip, contentDescription = "Zip", tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(20.dp))
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text("Zip", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }

                                // Delete Action
                                Column(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(20.dp))
                                        .clickable {
                                            onDeleteMultiple(selectedFiles.toList())
                                            isSelectionMode = false
                                            selectedFiles = emptySet()
                                        }
                                        .padding(vertical = 6.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.Center
                                ) {
                                    Icon(Icons.Default.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(20.dp))
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text("Delete", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.error)
                                }
                            }
                        }
                    }
                } else if (clipboardState != null) {
                com.fyllo.filemanager.ui.components.WaveClipboardBar(
                    clipboardState = clipboardState,
                    targetFolderName = folderName,
                    onPasteClick = onPasteClick,
                    onClearClipboardClick = onClearClipboardClick
                )
            }
            }
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
            ) {
                // ─── 1. Breadcrumb Pill Bar ──────────────────────────────────────────
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState())
                        .padding(horizontal = 16.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val rootPath = android.os.Environment.getExternalStorageDirectory().absolutePath
                    val relativeParts = if (currentPath.isNotEmpty() && currentPath.startsWith(rootPath)) {
                        currentPath.removePrefix(rootPath).trim('/').split("/").filter { it.isNotEmpty() }
                    } else if (currentPath.isNotEmpty() && currentPath != "/") {
                        currentPath.trim('/').split("/").filter { it.isNotEmpty() }
                    } else {
                        emptyList()
                    }

                    // Root Pill: [ 📱 Internal storage ]
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = if (relativeParts.isEmpty()) MaterialTheme.colorScheme.primary.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .clickable {
                                onNavigateToFolderByPath(rootPath)
                            }
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Smartphone,
                                contentDescription = null,
                                tint = if (relativeParts.isEmpty()) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Internal storage",
                                fontSize = 13.sp,
                                fontWeight = if (relativeParts.isEmpty()) FontWeight.SemiBold else FontWeight.Medium,
                                color = if (relativeParts.isEmpty()) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    var cumulativePath = rootPath
                    for ((index, part) in relativeParts.withIndex()) {
                        cumulativePath = "$cumulativePath/$part"
                        val targetPath = cumulativePath
                        val isLast = index == relativeParts.size - 1

                        Icon(
                            imageVector = Icons.Default.ChevronRight,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                            modifier = Modifier
                                .padding(horizontal = 4.dp)
                                .size(16.dp)
                        )

                        Surface(
                            shape = RoundedCornerShape(20.dp),
                            color = if (isLast) MaterialTheme.colorScheme.primary.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                            modifier = Modifier
                                .clip(RoundedCornerShape(20.dp))
                                .clickable {
                                    if (!isLast) {
                                        onNavigateToFolderByPath(targetPath)
                                    }
                                }
                        ) {
                            Text(
                                text = part,
                                fontSize = 13.sp,
                                fontWeight = if (isLast) FontWeight.SemiBold else FontWeight.Medium,
                                color = if (isLast) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                            )
                        }
                    }
                }

                // ─── 2. View Mode & Sort Toolbar ──────────────────────────────────────
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Left Group: View Mode Toggle (Grid / List)
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        modifier = Modifier.clip(RoundedCornerShape(20.dp))
                    ) {
                        Row(
                            modifier = Modifier.padding(2.dp),
                            horizontalArrangement = Arrangement.spacedBy(2.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Grid Mode Toggle
                            Box(
                                modifier = Modifier
                                    .size(34.dp)
                                    .clip(CircleShape)
                                    .background(
                                        if (isGridView) MaterialTheme.colorScheme.primary
                                        else Color.Transparent
                                    )
                                    .clickable { onSetGridView(true) },
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.GridView,
                                    contentDescription = "Grid View",
                                    tint = if (isGridView) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(18.dp)
                                )
                            }

                            // List Mode Toggle
                            Box(
                                modifier = Modifier
                                    .size(34.dp)
                                    .clip(CircleShape)
                                    .background(
                                        if (!isGridView) MaterialTheme.colorScheme.primary
                                        else Color.Transparent
                                    )
                                    .clickable { onSetGridView(false) },
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ViewList,
                                    contentDescription = "List View",
                                    tint = if (!isGridView) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }

                    // Right Group: Sort Chip Pill [ 🔽 Name ▼ ]
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .clickable { showSortMenu = true }
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            val sortLabel = when (currentSortOption) {
                                SortOption.NAME_ASC -> "Name"
                                SortOption.NAME_DESC -> "Name (Z-A)"
                                SortOption.DATE_ASC -> "Oldest"
                                SortOption.DATE_DESC -> "Newest"
                                SortOption.SIZE_ASC -> "Smallest"
                                SortOption.SIZE_DESC -> "Size"
                            }
                            Text(
                                text = sortLabel,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(
                                imageVector = Icons.Default.KeyboardArrowDown,
                                contentDescription = "Sort Menu",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                // ─── 4. File / Folder Content List with Smooth Animation ─────────────
                AnimatedVisibility(
                    visible = isLoading,
                    enter = fadeIn(),
                    exit = fadeOut()
                ) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                    }
                }

                if (!isLoading) {
                    if (files.isEmpty()) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(
                                    imageVector = Icons.Outlined.Folder,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                                    modifier = Modifier.size(56.dp)
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text("Folder is empty", color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f))
                            }
                        }
                    } else {
                        if (isGridView) {
                            LazyVerticalStaggeredGrid(
                                columns = StaggeredGridCells.Fixed(2),
                                modifier = Modifier.fillMaxSize().animateContentSize(spring(stiffness = Spring.StiffnessLow)),
                                contentPadding = PaddingValues(start = 12.dp, end = 12.dp, top = 8.dp, bottom = 96.dp),
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                                verticalItemSpacing = 12.dp
                            ) {
                                staggeredItems(files, key = { it.id }) { file ->
                                    FileGridItem(
                                        file = file,
                                        isSelectionMode = isSelectionMode,
                                        isSelected = selectedFiles.contains(file.uri),
                                        onHold2s = {
                                            if (!isSelectionMode) {
                                                isSelectionMode = true
                                                selectedFiles = selectedFiles + file.uri
                                            }
                                        },
                                        onClick = {
                                            if (isSelectionMode) {
                                                selectedFiles = if (selectedFiles.contains(file.uri)) {
                                                    selectedFiles - file.uri
                                                } else {
                                                    selectedFiles + file.uri
                                                }
                                                if (selectedFiles.isEmpty()) isSelectionMode = false
                                            } else {
                                                onFileClick(file)
                                            }
                                        },
                                        onOptionsClick = { selectedFileForOptions = file }
                                    )
                                }
                            }
                        } else {
                            LazyColumn(
                                modifier = Modifier.fillMaxSize().animateContentSize(spring(stiffness = Spring.StiffnessLow)),
                                contentPadding = PaddingValues(bottom = 96.dp)
                            ) {
                                items(files, key = { it.id }) { file ->
                                    FileListItem(
                                        file = file,
                                        isSelectionMode = isSelectionMode,
                                        isSelected = selectedFiles.contains(file.uri),
                                        onHold2s = {
                                            if (!isSelectionMode) {
                                                isSelectionMode = true
                                                selectedFiles = selectedFiles + file.uri
                                            }
                                        },
                                        onClick = {
                                            if (isSelectionMode) {
                                                selectedFiles = if (selectedFiles.contains(file.uri)) {
                                                    selectedFiles - file.uri
                                                } else {
                                                    selectedFiles + file.uri
                                                }
                                                if (selectedFiles.isEmpty()) isSelectionMode = false
                                            } else {
                                                onFileClick(file)
                                            }
                                        },
                                        onOptionsClick = { selectedFileForOptions = file }
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // ─── 5. Floating Bottom Dock Navigation & FAB ────────────────────────────
            if (!isSelectionMode && clipboardState == null) {
                FloatingBottomDock(
                    selectedTab = DashboardNavTab.FILES,
                    onTabSelected = { tab ->
                        when (tab) {
                            DashboardNavTab.HOME -> onNavigateToHome()
                            DashboardNavTab.FILES -> { /* Already in files */ }
                            DashboardNavTab.SEARCH -> onNavigateToSearch()
                            DashboardNavTab.APPS -> onNavigateToApps()
                        }
                    },
                    onFabClick = { showNewFolderDialog = true },
                    modifier = Modifier.align(Alignment.BottomCenter)
                )
            }
        }
    }
}

@Composable
fun FileListItem(
    file: FileItem,
    isSelectionMode: Boolean = false,
    isSelected: Boolean = false,
    onHold2s: () -> Unit,
    onClick: () -> Unit,
    onOptionsClick: () -> Unit
) {
    val context = LocalContext.current
    var isPressed by remember { mutableStateOf(false) }

    LaunchedEffect(isPressed) {
        if (isPressed) {
            delay(2000)
            onHold2s()
        }
    }
    
    val date = SimpleDateFormat("MM/dd/yy", Locale.getDefault()).format(Date(file.lastModified))
    
    val details = if (file.isFolder) {
        val count = file.itemCount ?: 0
        if (file.sizeBytes > 0) {
            val formattedSize = Formatter.formatFileSize(context, file.sizeBytes)
            "$count items • $formattedSize"
        } else {
            "Folder • $count items"
        }
    } else {
        Formatter.formatFileSize(context, file.sizeBytes)
    }

    val backgroundColor = when {
        isSelected && !LocalEInkMode.current -> MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
        isSelected && LocalEInkMode.current -> MaterialTheme.colorScheme.surfaceVariant
        else -> Color.Transparent
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(backgroundColor)
            .pointerInput(isSelectionMode) {
                awaitEachGesture {
                    awaitFirstDown()
                    isPressed = true
                    val up = waitForUpOrCancellation()
                    isPressed = false
                    if (up != null) {
                        onClick()
                    }
                }
            }
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (isSelectionMode) {
            Checkbox(
                checked = isSelected,
                onCheckedChange = null,
                colors = if (LocalEInkMode.current) CheckboxDefaults.colors(
                    checkedColor = MaterialTheme.colorScheme.onBackground,
                    checkmarkColor = MaterialTheme.colorScheme.background
                ) else CheckboxDefaults.colors()
            )
            Spacer(modifier = Modifier.width(8.dp))
        }

        // Icon Box
        if (file.isFolder) {
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Folder,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(30.dp)
                )
            }
        } else {
            val ext = file.extension.lowercase()
            val isImage = file.mimeType?.startsWith("image/") == true || ext in listOf("jpg", "jpeg", "png", "webp", "heic", "heif", "gif", "bmp")
            val isVideo = file.mimeType?.startsWith("video/") == true || ext in listOf("mp4", "mkv", "avi", "mov", "3gp", "webm", "ts", "flv", "wmv", "m4v")
            val isImageOrVideo = isImage || isVideo
            
            if (isImageOrVideo) {
                Box(contentAlignment = Alignment.Center) {
                    coil.compose.AsyncImage(
                        model = coil.request.ImageRequest.Builder(context)
                            .data(file.uri)
                            .size(160)
                            .crossfade(false)
                            .build(),
                        contentDescription = null,
                        contentScale = androidx.compose.ui.layout.ContentScale.Crop,
                        modifier = Modifier
                            .size(44.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color.Black)
                    )
                    if (isVideo) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color.Black.copy(alpha = 0.35f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.PlayArrow,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            } else {
                val isMusicByExt = ext in listOf("wav", "mp3", "m4a", "flac", "ogg")
                val isMusicByMime = file.mimeType?.startsWith("audio/") == true

                val fillColor: Color
                val borderColor: Color
                val labelChar: String?
                val iconVec: ImageVector?

                when {
                    ext == "pptx" || ext == "ppt" -> { fillColor = Color(0xFF6B2519); borderColor = Color(0xFFD04423); labelChar = "P"; iconVec = null }
                    ext == "xlsx" || ext == "xls" || ext == "csv" -> { fillColor = Color(0xFF135A37); borderColor = Color(0xFF21A366); labelChar = "X"; iconVec = null }
                    ext == "docx" || ext == "doc" -> { fillColor = Color(0xFF103A7A); borderColor = Color(0xFF185ABD); labelChar = "W"; iconVec = null }
                    isMusicByExt || isMusicByMime -> { fillColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f); borderColor = MaterialTheme.colorScheme.primary; labelChar = null; iconVec = Icons.Default.MusicNote }
                    else -> { fillColor = Color(0xFF2D2D2D); borderColor = Color.Gray; labelChar = if (file.extension.isNotEmpty()) file.extension.take(3).uppercase() else "?"; iconVec = null }
                }

                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(fillColor)
                        .border(1.dp, borderColor, RoundedCornerShape(8.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    if (iconVec != null) {
                        Icon(imageVector = iconVec, contentDescription = null, tint = borderColor, modifier = Modifier.size(24.dp))
                    } else if (labelChar != null) {
                        Text(text = labelChar, color = borderColor, fontWeight = FontWeight.Bold, fontSize = if (labelChar.length > 1) 12.sp else 20.sp)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.width(16.dp))

        // Details
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = file.name,
                color = MaterialTheme.colorScheme.onBackground,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                maxLines = 1,
                overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = details,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha=0.6f),
                fontSize = 12.sp
            )
        }

        Text(
            text = date,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha=0.6f),
            fontSize = 12.sp
        )
        
        Spacer(modifier = Modifier.width(8.dp))

        IconButton(onClick = onOptionsClick, modifier = Modifier.size(24.dp)) {
            Icon(Icons.Default.MoreVert, contentDescription = "Options", tint = MaterialTheme.colorScheme.onBackground.copy(alpha=0.6f))
        }
    }
}

@Composable
fun FileGridItem(
    file: FileItem,
    isSelectionMode: Boolean = false,
    isSelected: Boolean = false,
    onHold2s: () -> Unit,
    onClick: () -> Unit,
    onOptionsClick: () -> Unit
) {
    val context = LocalContext.current
    var isPressed by remember { mutableStateOf(false) }

    LaunchedEffect(isPressed) {
        if (isPressed) {
            delay(2000)
            onHold2s()
        }
    }

    val ext = file.extension.lowercase()
    val isImage = file.mimeType?.startsWith("image/") == true || ext in listOf("jpg", "jpeg", "png", "webp", "heic", "heif", "gif", "bmp")
    val isVideo = file.mimeType?.startsWith("video/") == true || ext in listOf("mp4", "mkv", "avi", "mov", "3gp", "webm", "ts", "flv", "wmv", "m4v")
    val isImageOrVideo = !file.isFolder && (isImage || isVideo)

    // Varied height for 2-column Masonry layout
    val cardHeight = remember(file.uri) {
        val hash = file.uri.toString().hashCode()
        when (Math.abs(hash) % 3) {
            0 -> 180.dp
            1 -> 220.dp
            else -> 260.dp
        }
    }

    if (isImageOrVideo) {
        // Masonry Card for Media items
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(cardHeight)
                .clip(RoundedCornerShape(16.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant)
                .border(
                    width = if (isSelected) 2.5.dp else 0.dp,
                    color = if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent,
                    shape = RoundedCornerShape(16.dp)
                )
                .pointerInput(isSelectionMode) {
                    awaitEachGesture {
                        awaitFirstDown()
                        isPressed = true
                        val up = waitForUpOrCancellation()
                        isPressed = false
                        if (up != null) {
                            onClick()
                        }
                    }
                }
        ) {
            coil.compose.AsyncImage(
                model = coil.request.ImageRequest.Builder(context)
                    .data(file.uri)
                    .crossfade(false)
                    .build(),
                contentDescription = null,
                contentScale = androidx.compose.ui.layout.ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )

            // Video Play Badge
            if (isVideo) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(Color.Black.copy(alpha = 0.55f))
                        .align(Alignment.Center),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = "Play Video",
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

            // Bottom Gradient Scrim with title & details
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter)
                    .background(
                        androidx.compose.ui.graphics.Brush.verticalGradient(
                            colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.85f))
                        )
                    )
                    .padding(horizontal = 10.dp, vertical = 8.dp)
            ) {
                Column {
                    Text(
                        text = file.name,
                        color = Color.White,
                        fontSize = 11.5.sp,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                        overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                    )
                    if (file.sizeBytes > 0) {
                        Text(
                            text = Formatter.formatShortFileSize(context, file.sizeBytes),
                            color = Color.White.copy(alpha = 0.75f),
                            fontSize = 10.sp
                        )
                    }
                }
            }

            // Top Row (Checkbox or Options)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(4.dp)
                    .align(Alignment.TopStart),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (isSelectionMode) {
                    Checkbox(
                        checked = isSelected,
                        onCheckedChange = null,
                        modifier = Modifier.size(24.dp),
                        colors = CheckboxDefaults.colors(
                            checkedColor = MaterialTheme.colorScheme.primary,
                            checkmarkColor = Color.White
                        )
                    )
                } else {
                    Spacer(modifier = Modifier.size(24.dp))
                }

                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .clip(CircleShape)
                        .background(Color.Black.copy(alpha = 0.4f))
                        .clickable { onOptionsClick() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.MoreVert,
                        contentDescription = "Options",
                        tint = Color.White,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    } else {
        // Standard Grid Card for Folders and Documents
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(
                    if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                    else MaterialTheme.colorScheme.surfaceVariant
                )
                .border(
                    width = if (isSelected) 2.dp else if (LocalEInkMode.current) 1.dp else 0.dp,
                    color = if (isSelected) MaterialTheme.colorScheme.primary else if (LocalEInkMode.current) MaterialTheme.colorScheme.outline else Color.Transparent,
                    shape = RoundedCornerShape(16.dp)
                )
                .pointerInput(isSelectionMode) {
                    awaitEachGesture {
                        awaitFirstDown()
                        isPressed = true
                        val up = waitForUpOrCancellation()
                        isPressed = false
                        if (up != null) {
                            onClick()
                        }
                    }
                }
                .padding(12.dp)
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (isSelectionMode) {
                        Checkbox(
                            checked = isSelected,
                            onCheckedChange = null,
                            modifier = Modifier.size(24.dp)
                        )
                    } else {
                        Spacer(modifier = Modifier.size(24.dp))
                    }
                    IconButton(onClick = onOptionsClick, modifier = Modifier.size(24.dp)) {
                        Icon(Icons.Default.MoreVert, contentDescription = "Options", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                if (file.isFolder) {
                    Box(
                        modifier = Modifier
                            .size(52.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Folder,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(34.dp)
                        )
                    }
                } else {
                    val isMusicByExt = ext in com.fyllo.filemanager.ui.screens.AUDIO_EXTENSIONS
                    val isMusicByMime = file.mimeType?.startsWith("audio/") == true
                    val fillColor: Color
                    val borderColor: Color
                    val labelChar: String?
                    val iconVec: androidx.compose.ui.graphics.vector.ImageVector?
                    when {
                        ext == "pptx" || ext == "ppt" -> { fillColor = Color(0xFF6B2519); borderColor = Color(0xFFD04423); labelChar = "P"; iconVec = null }
                        ext == "xlsx" || ext == "xls" || ext == "csv" -> { fillColor = Color(0xFF135A37); borderColor = Color(0xFF21A366); labelChar = "X"; iconVec = null }
                        ext == "docx" || ext == "doc" -> { fillColor = Color(0xFF103A7A); borderColor = Color(0xFF185ABD); labelChar = "W"; iconVec = null }
                        isMusicByExt || isMusicByMime -> { fillColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f); borderColor = MaterialTheme.colorScheme.primary; labelChar = null; iconVec = Icons.Default.MusicNote }
                        else -> { fillColor = MaterialTheme.colorScheme.surface; borderColor = MaterialTheme.colorScheme.outline; labelChar = if (file.extension.isNotEmpty()) file.extension.take(3).uppercase() else "?"; iconVec = null }
                    }
                    Box(
                        modifier = Modifier
                            .size(52.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(fillColor)
                            .border(1.dp, borderColor, RoundedCornerShape(12.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        if (iconVec != null) {
                            Icon(imageVector = iconVec, contentDescription = null, tint = borderColor, modifier = Modifier.size(26.dp))
                        } else if (labelChar != null) {
                            Text(text = labelChar, color = borderColor, fontWeight = FontWeight.Bold, fontSize = if (labelChar.length > 1) 11.sp else 20.sp)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = file.name,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(2.dp))

                val gridDetails = if (file.isFolder) {
                    val count = file.itemCount ?: 0
                    if (file.sizeBytes > 0) {
                        "$count items • ${Formatter.formatShortFileSize(context, file.sizeBytes)}"
                    } else {
                        "$count items"
                    }
                } else {
                    Formatter.formatShortFileSize(context, file.sizeBytes)
                }

                Text(
                    text = gridDetails,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 11.sp,
                    maxLines = 1,
                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                )
            }
        }
    }
}
