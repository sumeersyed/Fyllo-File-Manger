package com.fyllo.filemanager.ui.screens

import android.content.Intent
import android.graphics.drawable.Drawable
import android.net.Uri
import android.text.format.Formatter
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.SelectAll
import androidx.compose.material.icons.outlined.Android
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.SystemUpdate
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.graphics.drawable.toBitmap
import com.fyllo.filemanager.domain.model.AppItem
import com.fyllo.filemanager.ui.theme.LocalEInkMode

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppsScreen(
    installedApps: List<AppItem> = emptyList(),
    apkFiles: List<AppItem> = emptyList(),
    apps: List<AppItem>,
    isLoading: Boolean,
    onBackClick: () -> Unit,
    onLoadApps: () -> Unit
) {
    val context = LocalContext.current
    val isEInk = LocalEInkMode.current
    var selectedTab by remember { mutableStateOf(0) }
    var appToUninstall by remember { mutableStateOf<AppItem?>(null) }
    
    // Selection state
    var isSelectionMode by remember { mutableStateOf(false) }
    val selectedApps = remember { mutableStateListOf<AppItem>() }
    var showBatchDeleteDialog by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        onLoadApps()
    }

    // Reset selection when tab changes
    LaunchedEffect(selectedTab) {
        isSelectionMode = false
        selectedApps.clear()
    }

    val displayList = remember(selectedTab, installedApps, apkFiles, apps) {
        when (selectedTab) {
            0 -> if (installedApps.isNotEmpty()) installedApps else apps.filter { !it.isApk }
            1 -> if (apkFiles.isNotEmpty()) apkFiles else apps.filter { it.isApk }
            else -> apps
        }
    }

    Scaffold(
        topBar = {
            if (isSelectionMode) {
                TopAppBar(
                    title = { 
                        Text(
                            "${selectedApps.size} selected", 
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        ) 
                    },
                    navigationIcon = {
                        IconButton(onClick = {
                            isSelectionMode = false
                            selectedApps.clear()
                        }) {
                            Icon(Icons.Default.Close, contentDescription = "Close selection")
                        }
                    },
                    actions = {
                        // Select All / Deselect All
                        IconButton(onClick = {
                            if (selectedApps.size == displayList.size) {
                                selectedApps.clear()
                            } else {
                                selectedApps.clear()
                                selectedApps.addAll(displayList)
                            }
                        }) {
                            Icon(
                                Icons.Default.SelectAll,
                                contentDescription = "Select All",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }

                        // Delete Selected
                        if (selectedApps.isNotEmpty()) {
                            IconButton(onClick = { showBatchDeleteDialog = true }) {
                                Icon(
                                    Icons.Outlined.Delete,
                                    contentDescription = "Delete Selected",
                                    tint = MaterialTheme.colorScheme.error
                                )
                            }
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                )
            } else {
                TopAppBar(
                    title = { Text("Apps & APKs", fontWeight = FontWeight.Bold) },
                    navigationIcon = {
                        IconButton(onClick = onBackClick) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.background
                    )
                )
            }
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = MaterialTheme.colorScheme.background,
                contentColor = MaterialTheme.colorScheme.primary,
                indicator = { tabPositions ->
                    TabRowDefaults.SecondaryIndicator(
                        modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            ) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = { 
                        Text(
                            text = "Installed (${if (installedApps.isNotEmpty()) installedApps.size else apps.count { !it.isApk }})",
                            fontWeight = if (selectedTab == 0) FontWeight.Bold else FontWeight.Normal
                        ) 
                    }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = { 
                        Text(
                            text = "APK Files (${if (apkFiles.isNotEmpty()) apkFiles.size else apps.count { it.isApk }})",
                            fontWeight = if (selectedTab == 1) FontWeight.Bold else FontWeight.Normal
                        ) 
                    }
                )
            }

            Box(modifier = Modifier.fillMaxSize().weight(1f)) {
                if (isLoading) {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                } else if (displayList.isEmpty()) {
                    Text(
                        text = if (selectedTab == 0) "No installed apps found" else "No APK files found on storage",
                        modifier = Modifier.align(Alignment.Center),
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                    )
                } else {
                    LazyColumn(modifier = Modifier.fillMaxSize()) {
                        items(displayList, key = { it.id + it.isApk }) { app ->
                            val isSelected = selectedApps.contains(app)
                            AppListItem(
                                app = app,
                                isEInk = isEInk,
                                isSelectionMode = isSelectionMode,
                                isSelected = isSelected,
                                onAppClick = {
                                    if (isSelectionMode) {
                                        if (isSelected) selectedApps.remove(app) else selectedApps.add(app)
                                        if (selectedApps.isEmpty()) isSelectionMode = false
                                    } else {
                                        try {
                                            if (app.isApk && app.apkPath != null) {
                                                com.fyllo.filemanager.core.ApkInstaller.installApk(
                                                    context = context,
                                                    uri = Uri.fromFile(java.io.File(app.apkPath)),
                                                    path = app.apkPath
                                                )
                                            } else {
                                                val launchIntent = context.packageManager.getLaunchIntentForPackage(app.packageName)
                                                if (launchIntent != null) {
                                                    launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                                    context.startActivity(launchIntent)
                                                } else {
                                                    android.widget.Toast.makeText(context, "${app.name} cannot be launched", android.widget.Toast.LENGTH_SHORT).show()
                                                }
                                            }
                                        } catch (e: Exception) {
                                            android.widget.Toast.makeText(context, "Error: ${e.message}", android.widget.Toast.LENGTH_SHORT).show()
                                        }
                                    }
                                },
                                onLongClick = {
                                    if (!isSelectionMode) {
                                        isSelectionMode = true
                                        selectedApps.add(app)
                                    }
                                },
                                onUninstallClick = {
                                    appToUninstall = app
                                }
                            )
                        }
                    }
                }
            }
        }
    }

    // Single Uninstall / Delete Confirmation Dialog
    appToUninstall?.let { app ->
        AlertDialog(
            onDismissRequest = { appToUninstall = null },
            containerColor = MaterialTheme.colorScheme.surface,
            title = {
                Text(
                    text = if (app.isApk) "Delete APK File" else "Uninstall App",
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            },
            text = {
                Text(
                    text = if (app.isApk) "Do you want to delete ${app.name} APK?" else "Do you want to uninstall ${app.name}?",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 15.sp
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        val targetApp = app
                        appToUninstall = null
                        try {
                            if (targetApp.isApk && targetApp.apkPath != null) {
                                val file = java.io.File(targetApp.apkPath)
                                if (file.exists()) {
                                    file.delete()
                                    onLoadApps()
                                }
                            } else {
                                val intent = Intent(Intent.ACTION_UNINSTALL_PACKAGE).apply {
                                    data = Uri.parse("package:${targetApp.packageName}")
                                    putExtra(Intent.EXTRA_RETURN_RESULT, true)
                                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                }
                                context.startActivity(intent)
                            }
                        } catch (e: Exception) {
                            try {
                                val intent = Intent(Intent.ACTION_DELETE).apply {
                                    data = Uri.parse("package:${targetApp.packageName}")
                                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                }
                                context.startActivity(intent)
                            } catch (ex: Exception) {
                                android.widget.Toast.makeText(context, "Cannot uninstall: ${ex.message}", android.widget.Toast.LENGTH_SHORT).show()
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("YES", color = MaterialTheme.colorScheme.onError, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { appToUninstall = null }) {
                    Text("NO", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                }
            }
        )
    }

    // Batch Delete Dialog
    if (showBatchDeleteDialog && selectedApps.isNotEmpty()) {
        val count = selectedApps.size
        val hasOnlyApks = selectedApps.all { it.isApk }
        AlertDialog(
            onDismissRequest = { showBatchDeleteDialog = false },
            containerColor = MaterialTheme.colorScheme.surface,
            title = {
                Text(
                    text = if (hasOnlyApks) "Delete $count APK Files" else "Remove $count Items",
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            },
            text = {
                Text(
                    text = "Are you sure you want to delete/uninstall the $count selected items?",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 15.sp
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        val items = selectedApps.toList()
                        showBatchDeleteDialog = false
                        isSelectionMode = false
                        selectedApps.clear()

                        for (item in items) {
                            try {
                                if (item.isApk && item.apkPath != null) {
                                    val f = java.io.File(item.apkPath)
                                    if (f.exists()) f.delete()
                                } else {
                                    val intent = Intent(Intent.ACTION_UNINSTALL_PACKAGE).apply {
                                        data = Uri.parse("package:${item.packageName}")
                                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                    }
                                    context.startActivity(intent)
                                }
                            } catch (_: Exception) {}
                        }
                        onLoadApps()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("DELETE ALL", color = MaterialTheme.colorScheme.onError, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showBatchDeleteDialog = false }) {
                    Text("CANCEL", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                }
            }
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun AppListItem(
    app: AppItem,
    isEInk: Boolean,
    isSelectionMode: Boolean = false,
    isSelected: Boolean = false,
    onAppClick: () -> Unit,
    onLongClick: () -> Unit = {},
    onUninstallClick: () -> Unit
) {
    val context = LocalContext.current
    val sizeStr = Formatter.formatFileSize(context, app.sizeBytes)

    val icon: Drawable? = remember(app.packageName, app.apkPath) {
        runCatching {
            if (!app.isApk) {
                context.packageManager.getApplicationIcon(app.packageName)
            } else if (app.apkPath != null) {
                val pm = context.packageManager
                val pi = pm.getPackageArchiveInfo(app.apkPath, 0)
                if (pi != null) {
                    pi.applicationInfo.sourceDir = app.apkPath
                    pi.applicationInfo.publicSourceDir = app.apkPath
                    pm.getApplicationIcon(pi.applicationInfo)
                } else null
            } else null
        }.getOrNull()
    }

    val rowModifier = if (isEInk) {
        Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp)
            .border(1.5.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
    } else {
        Modifier.fillMaxWidth()
    }

    ListItem(
        modifier = rowModifier
            .combinedClickable(
                onClick = onAppClick,
                onLongClick = onLongClick
            ),
        headlineContent = { Text(app.name, maxLines = 1, overflow = TextOverflow.Ellipsis, fontWeight = FontWeight.Medium) },
        supportingContent = {
            Text(
                text = if (app.isApk) "$sizeStr • APK File" else "$sizeStr • Installed",
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        },
        leadingContent = {
            if (isSelectionMode) {
                Checkbox(
                    checked = isSelected,
                    onCheckedChange = { onAppClick() },
                    colors = CheckboxDefaults.colors(checkedColor = MaterialTheme.colorScheme.primary)
                )
            } else if (icon != null) {
                Image(
                    bitmap = icon.toBitmap(48, 48).asImageBitmap(),
                    contentDescription = app.name,
                    modifier = Modifier.size(44.dp).clip(RoundedCornerShape(8.dp))
                )
            } else {
                Icon(
                    if (app.isApk) Icons.Outlined.SystemUpdate else Icons.Outlined.Android,
                    contentDescription = null,
                    modifier = Modifier.size(44.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        },
        trailingContent = {
            if (!isSelectionMode) {
                IconButton(onClick = onUninstallClick) {
                    Icon(
                        Icons.Outlined.Delete,
                        contentDescription = if (app.isApk) "Delete APK" else "Uninstall",
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }
        },
        colors = ListItemDefaults.colors(containerColor = androidx.compose.ui.graphics.Color.Transparent)
    )
}
