package com.fyllo.filemanager.ui.screens

import android.net.Uri
import android.os.Environment
import android.text.format.Formatter
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.outlined.Article
import androidx.compose.material.icons.outlined.CleaningServices
import androidx.compose.material.icons.outlined.Code
import androidx.compose.material.icons.outlined.DarkMode
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.Download
import androidx.compose.material.icons.outlined.FolderZip
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.PictureAsPdf
import androidx.compose.material.icons.outlined.PieChart
import androidx.compose.material.icons.outlined.SdStorage
import androidx.compose.material.icons.outlined.Security
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.Slideshow
import androidx.compose.material.icons.outlined.StarBorder
import androidx.compose.material.icons.outlined.Usb
import androidx.compose.material.icons.outlined.Widgets
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.NavigationDrawerItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.fyllo.filemanager.R
import com.fyllo.filemanager.domain.model.FileItem
import com.fyllo.filemanager.domain.model.StorageStats
import com.fyllo.filemanager.ui.components.CategoryCard
import com.fyllo.filemanager.ui.components.DashboardNavTab
import com.fyllo.filemanager.ui.components.DashboardShortcutRow
import com.fyllo.filemanager.ui.components.FloatingBottomDock
import com.fyllo.filemanager.ui.components.RecentFileCard
import com.fyllo.filemanager.ui.components.StorageInfoCard
import com.fyllo.filemanager.ui.theme.LocalEInkMode
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Suppress("UNUSED_PARAMETER")
@Composable
fun HomeDashboardScreen(
    storageStats: StorageStats?,
    recentFiles: List<FileItem>,
    categoryCounts: Map<String, Int> = emptyMap(),
    onNavigateToManage: () -> Unit,
    onNavigateToCleanup: () -> Unit = {},
    onNavigateToFiles: (String?) -> Unit,
    onNavigateToMedia: (String) -> Unit,
    onNavigateToApps: (Boolean) -> Unit,
    onNavigateToTrash: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onNavigateToSearch: () -> Unit,
    onNavigateToSafeFolder: () -> Unit = {},
    isDarkTheme: Boolean = true,
    onToggleDarkMode: ((Boolean) -> Unit)? = null
) {
    val context = LocalContext.current
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    val formattedTotal = storageStats?.let { Formatter.formatFileSize(context, it.totalBytes) } ?: "0 B"
    val formattedUsed = storageStats?.let { Formatter.formatFileSize(context, it.usedBytes) } ?: "0 B"
    val formattedFree = storageStats?.let { Formatter.formatFileSize(context, it.freeBytes) } ?: "0 B"

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet(
                drawerContainerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.92f),
                modifier = Modifier
                    .width(300.dp)
                    .border(
                        width = 1.dp,
                        brush = androidx.compose.ui.graphics.Brush.verticalGradient(
                            listOf(
                                MaterialTheme.colorScheme.primary.copy(alpha = 0.35f),
                                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.1f)
                            )
                        ),
                        shape = RoundedCornerShape(topEnd = 24.dp, bottomEnd = 24.dp)
                    )
            ) {
                Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
                    // Header
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(bottom = 24.dp, top = 16.dp)
                    ) {
                        androidx.compose.foundation.Image(
                            painter = painterResource(id = R.drawable.app_logo),
                            contentDescription = "Logo",
                            modifier = Modifier.size(36.dp)
                        )
                        Spacer(modifier = Modifier.width(16.dp))
                        Text(
                            "Fylo",
                            color = MaterialTheme.colorScheme.onBackground,
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    val isEInk = LocalEInkMode.current
                    val eInkBorder = if (isEInk) Modifier.padding(horizontal = 8.dp, vertical = 2.dp).border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(100)) else Modifier
                    val eInkBorderSelected = if (isEInk) Modifier.padding(horizontal = 8.dp, vertical = 2.dp).border(2.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(100)) else Modifier

                    NavigationDrawerItem(
                        label = { Text(stringResource(R.string.home), color = if (isEInk) MaterialTheme.colorScheme.onBackground else MaterialTheme.colorScheme.primary) },
                        icon = { Icon(Icons.Default.Home, contentDescription = stringResource(R.string.home), tint = if (isEInk) MaterialTheme.colorScheme.onBackground else MaterialTheme.colorScheme.primary) },
                        selected = true,
                        onClick = { scope.launch { drawerState.close() } },
                        modifier = eInkBorderSelected,
                        colors = NavigationDrawerItemDefaults.colors(
                            selectedContainerColor = if (isEInk) Color.Transparent else MaterialTheme.colorScheme.primary.copy(alpha = 0.2f),
                            unselectedContainerColor = Color.Transparent
                        )
                    )

                    NavigationDrawerItem(
                        label = {
                            Column {
                                Text(stringResource(R.string.internal_storage), color = MaterialTheme.colorScheme.onBackground)
                                Text("$formattedUsed / $formattedTotal", color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f), fontSize = 12.sp)
                            }
                        },
                        icon = { Icon(Icons.Outlined.SdStorage, contentDescription = stringResource(R.string.internal_storage), tint = MaterialTheme.colorScheme.onBackground.copy(alpha=0.6f)) },
                        selected = false,
                        onClick = {
                            scope.launch { drawerState.close() }
                            onNavigateToFiles(null)
                        },
                        modifier = eInkBorder,
                        colors = NavigationDrawerItemDefaults.colors(unselectedContainerColor = Color.Transparent)
                    )

                    NavigationDrawerItem(
                        label = {
                            Column {
                                Text(stringResource(R.string.sd_card), color = MaterialTheme.colorScheme.onBackground)
                                Text(stringResource(R.string.not_inserted), color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f), fontSize = 12.sp)
                            }
                        },
                        icon = { Icon(Icons.Outlined.SdStorage, contentDescription = stringResource(R.string.sd_card), tint = MaterialTheme.colorScheme.onBackground.copy(alpha=0.6f)) },
                        selected = false,
                        onClick = { /* SD Card */ },
                        modifier = eInkBorder,
                        colors = NavigationDrawerItemDefaults.colors(unselectedContainerColor = Color.Transparent)
                    )

                    NavigationDrawerItem(
                        label = {
                            Column {
                                Text(stringResource(R.string.usb_drive), color = MaterialTheme.colorScheme.onBackground)
                                Text(stringResource(R.string.not_inserted), color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f), fontSize = 12.sp)
                            }
                        },
                        icon = { Icon(Icons.Outlined.Usb, contentDescription = stringResource(R.string.usb_drive), tint = MaterialTheme.colorScheme.onBackground.copy(alpha=0.6f)) },
                        selected = false,
                        onClick = { /* USB */ },
                        modifier = eInkBorder,
                        colors = NavigationDrawerItemDefaults.colors(unselectedContainerColor = Color.Transparent)
                    )

                    HorizontalDivider(color = MaterialTheme.colorScheme.onBackground.copy(alpha=0.2f), modifier = Modifier.padding(vertical = 8.dp))

                    NavigationDrawerItem(
                        label = { Text(stringResource(R.string.recent_files), color = MaterialTheme.colorScheme.onBackground) },
                        icon = { Icon(Icons.Outlined.History, contentDescription = stringResource(R.string.recent_files), tint = MaterialTheme.colorScheme.onBackground.copy(alpha=0.6f)) },
                        selected = false,
                        onClick = {
                            scope.launch { drawerState.close() }
                            onNavigateToMedia("recent")
                        },
                        modifier = eInkBorder,
                        colors = NavigationDrawerItemDefaults.colors(unselectedContainerColor = Color.Transparent)
                    )

                    NavigationDrawerItem(
                        label = { Text(stringResource(R.string.favorites), color = MaterialTheme.colorScheme.onBackground) },
                        icon = { Icon(Icons.Outlined.StarBorder, contentDescription = stringResource(R.string.favorites), tint = MaterialTheme.colorScheme.onBackground.copy(alpha=0.6f)) },
                        selected = false,
                        onClick = {
                            scope.launch { drawerState.close() }
                            onNavigateToMedia("favorites")
                        },
                        modifier = eInkBorder,
                        colors = NavigationDrawerItemDefaults.colors(unselectedContainerColor = Color.Transparent)
                    )

                    NavigationDrawerItem(
                        label = { Text("Vault", color = MaterialTheme.colorScheme.onBackground) },
                        icon = { Icon(Icons.Outlined.Security, contentDescription = "Vault", tint = MaterialTheme.colorScheme.onBackground.copy(alpha=0.6f)) },
                        selected = false,
                        onClick = {
                            scope.launch { drawerState.close() }
                            onNavigateToSafeFolder()
                        },
                        modifier = eInkBorder,
                        colors = NavigationDrawerItemDefaults.colors(unselectedContainerColor = Color.Transparent)
                    )

                    NavigationDrawerItem(
                        label = { Text(stringResource(R.string.recycle_bin), color = MaterialTheme.colorScheme.onBackground) },
                        icon = { Icon(Icons.Outlined.Delete, contentDescription = stringResource(R.string.recycle_bin), tint = MaterialTheme.colorScheme.onBackground.copy(alpha=0.6f)) },
                        selected = false,
                        onClick = {
                            scope.launch { drawerState.close() }
                            onNavigateToTrash()
                        },
                        modifier = eInkBorder,
                        colors = NavigationDrawerItemDefaults.colors(unselectedContainerColor = Color.Transparent)
                    )

                    HorizontalDivider(color = MaterialTheme.colorScheme.onBackground.copy(alpha=0.2f), modifier = Modifier.padding(vertical = 8.dp))

                    NavigationDrawerItem(
                        label = { Text(stringResource(R.string.settings), color = MaterialTheme.colorScheme.onBackground) },
                        icon = { Icon(Icons.Outlined.Settings, contentDescription = stringResource(R.string.settings), tint = MaterialTheme.colorScheme.onBackground.copy(alpha=0.6f)) },
                        selected = false,
                        onClick = {
                            scope.launch { drawerState.close() }
                            onNavigateToSettings()
                        },
                        modifier = eInkBorder,
                        colors = NavigationDrawerItemDefaults.colors(unselectedContainerColor = Color.Transparent)
                    )

                    Spacer(modifier = Modifier.weight(1f))

                    Row(
                        modifier = Modifier.fillMaxWidth().padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Outlined.DarkMode, contentDescription = stringResource(R.string.dark_mode), tint = MaterialTheme.colorScheme.onBackground.copy(alpha=0.6f))
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(stringResource(R.string.dark_mode), color = MaterialTheme.colorScheme.onBackground)
                        }
                        Switch(
                            checked = isDarkTheme,
                            onCheckedChange = { checked ->
                                if (onToggleDarkMode != null) {
                                    onToggleDarkMode(checked)
                                }
                            },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = MaterialTheme.colorScheme.onPrimary,
                                checkedTrackColor = MaterialTheme.colorScheme.primary
                            )
                        )
                    }
                }
            }
        }
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            text = "Fylo",
                            color = MaterialTheme.colorScheme.onBackground,
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = { scope.launch { drawerState.open() } }) {
                            Icon(
                                imageVector = Icons.Default.Menu,
                                contentDescription = "Menu",
                                tint = MaterialTheme.colorScheme.onBackground
                            )
                        }
                    },
                    actions = {
                        IconButton(onClick = onNavigateToSettings) {
                            Icon(
                                imageVector = Icons.Outlined.Settings,
                                contentDescription = "Settings",
                                tint = MaterialTheme.colorScheme.onBackground
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.background
                    )
                )
            },
            containerColor = MaterialTheme.colorScheme.background
        ) { paddingValues ->
            if (storageStats == null) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                }
                return@Scaffold
            }

            Box(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(bottom = 90.dp) // Leave room for floating dock
                ) {
                    // ─── 1. STORAGE SECTION ─────────────────────────────────
                    Text(
                        text = "Storage",
                        color = MaterialTheme.colorScheme.onBackground,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)
                    )

                    Box(modifier = Modifier.padding(horizontal = 16.dp)) {
                        StorageInfoCard(
                            usedBytes = storageStats.usedBytes,
                            totalBytes = storageStats.totalBytes,
                            formattedUsed = formattedUsed,
                            formattedTotal = formattedTotal,
                            formattedFree = formattedFree,
                            onClick = onNavigateToManage
                        )
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // ─── 2. CATEGORIES SECTION ──────────────────────────────
                    Text(
                        text = "Categories",
                        color = MaterialTheme.colorScheme.onBackground,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)
                    )

                    val imagesCount = categoryCounts["Images"] ?: 0
                    val videosCount = categoryCounts["Videos"] ?: 0
                    val audioCount = categoryCounts["Audio"] ?: 0
                    val docsCount = categoryCounts["Documents"] ?: 0
                    val apksCount = categoryCounts["APKs"] ?: 0
                    val pdfsCount = categoryCounts["PDFs"] ?: 0
                    val slidesCount = categoryCounts["Slides"] ?: 0
                    val sheetsCount = categoryCounts["Spreadsheets"] ?: 0
                    val archivesCount = categoryCounts["Archives"] ?: 0
                    val codeCount = categoryCounts["Code"] ?: 0
                    val textCount = categoryCounts["Text files"] ?: 0

                    LazyRow(
                        modifier = Modifier.fillMaxWidth(),
                        contentPadding = PaddingValues(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        item {
                            CategoryCard(
                                title = "Images",
                                itemCountText = "$imagesCount items",
                                icon = Icons.Default.Image,
                                accentColor = Color(0xFF2E7D32),
                                containerGradient = listOf(Color(0xFFE0F2E9), Color(0xFFE8F5E9)),
                                onClick = { onNavigateToMedia("images") }
                            )
                        }
                        item {
                            CategoryCard(
                                title = "Videos",
                                itemCountText = "$videosCount items",
                                icon = Icons.Default.Movie,
                                accentColor = Color(0xFFD32F2F),
                                containerGradient = listOf(Color(0xFFFDE8E8), Color(0xFFFFEBEE)),
                                onClick = { onNavigateToMedia("videos") }
                            )
                        }
                        item {
                            CategoryCard(
                                title = "Audio",
                                itemCountText = "$audioCount items",
                                icon = Icons.Default.MusicNote,
                                accentColor = Color(0xFFE65100),
                                containerGradient = listOf(Color(0xFFFFF3E0), Color(0xFFFBE9E7)),
                                onClick = { onNavigateToMedia("audio") }
                            )
                        }
                        item {
                            CategoryCard(
                                title = "Documents",
                                itemCountText = "$docsCount items",
                                icon = Icons.Outlined.Description,
                                accentColor = Color(0xFF1565C0),
                                containerGradient = listOf(Color(0xFFE3F2FD), Color(0xFFE8EAF6)),
                                onClick = { onNavigateToMedia("documents") }
                            )
                        }
                        item {
                            CategoryCard(
                                title = "PDFs",
                                itemCountText = if (pdfsCount > 0) "$pdfsCount items" else "–",
                                icon = Icons.Outlined.PictureAsPdf,
                                accentColor = Color(0xFFC62828),
                                containerGradient = listOf(Color(0xFFFFEBEE), Color(0xFFFFCDD2)),
                                onClick = { onNavigateToMedia("pdfs") }
                            )
                        }
                        item {
                            CategoryCard(
                                title = "Slides",
                                itemCountText = if (slidesCount > 0) "$slidesCount items" else "–",
                                icon = Icons.Outlined.Slideshow,
                                accentColor = Color(0xFFEF6C00),
                                containerGradient = listOf(Color(0xFFFFF3E0), Color(0xFFFFE0B2)),
                                onClick = { onNavigateToMedia("slides") }
                            )
                        }
                        item {
                            CategoryCard(
                                title = "Archives",
                                itemCountText = if (archivesCount > 0) "$archivesCount items" else "–",
                                icon = Icons.Outlined.FolderZip,
                                accentColor = Color(0xFF4527A0),
                                containerGradient = listOf(Color(0xFFEDE7F6), Color(0xFFD1C4E9)),
                                onClick = { onNavigateToMedia("archives") }
                            )
                        }
                        item {
                            CategoryCard(
                                title = "APKs",
                                itemCountText = "$apksCount items",
                                icon = Icons.Outlined.Widgets,
                                accentColor = Color(0xFF6A1B9A),
                                containerGradient = listOf(Color(0xFFF3E5F5), Color(0xFFEDE7F6)),
                                onClick = { onNavigateToApps(false) }
                            )
                        }
                        item {
                            CategoryCard(
                                title = "Code",
                                itemCountText = if (codeCount > 0) "$codeCount items" else "–",
                                icon = Icons.Outlined.Code,
                                accentColor = Color(0xFF00838F),
                                containerGradient = listOf(Color(0xFFE0F7FA), Color(0xFFB2EBF2)),
                                onClick = { onNavigateToMedia("code") }
                            )
                        }
                        item {
                            CategoryCard(
                                title = "Text files",
                                itemCountText = if (textCount > 0) "$textCount items" else "–",
                                icon = Icons.Outlined.Description,
                                accentColor = Color(0xFF37474F),
                                containerGradient = listOf(Color(0xFFECEFF1), Color(0xFFCFD8DC)),
                                onClick = { onNavigateToMedia("text") }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // ─── 3. SHORTCUTS SECTION ───────────────────────────────
                    Text(
                        text = "Shortcuts",
                        color = MaterialTheme.colorScheme.onBackground,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)
                    )

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        DashboardShortcutRow(
                            title = "Recent files",
                            subtitle = "Recently opened and modified",
                            icon = Icons.Outlined.History,
                            iconTint = Color(0xFF3366FF),
                            iconBgColor = Color(0xFFE8EFFF),
                            isTop = true,
                            onClick = { onNavigateToMedia("recent") }
                        )

                        DashboardShortcutRow(
                            title = "Downloads",
                            subtitle = "Newest downloads first",
                            icon = Icons.Outlined.Download,
                            iconTint = Color(0xFF3366FF),
                            iconBgColor = Color(0xFFE8EFFF),
                            onClick = { onNavigateToFiles(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).toString()) }
                        )

                        DashboardShortcutRow(
                            title = "Storage analyzer",
                            subtitle = "See what's using space",
                            icon = Icons.Outlined.PieChart,
                            iconTint = Color(0xFF3366FF),
                            iconBgColor = Color(0xFFE8EFFF),
                            onClick = onNavigateToManage
                        )

                        DashboardShortcutRow(
                            title = "Storage cleanup",
                            subtitle = "Large, old & duplicate files",
                            icon = Icons.Outlined.CleaningServices,
                            iconTint = Color(0xFF3366FF),
                            iconBgColor = Color(0xFFE8EFFF),
                            onClick = onNavigateToCleanup
                        )

                        DashboardShortcutRow(
                            title = "Recycle bin",
                            subtitle = "Restore deleted items",
                            icon = Icons.Outlined.DeleteOutline,
                            iconTint = Color(0xFF3366FF),
                            iconBgColor = Color(0xFFE8EFFF),
                            isBottom = true,
                            onClick = onNavigateToTrash
                        )
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // ─── 4. RECENTS SECTION ─────────────────────────────────
                    if (recentFiles.isNotEmpty()) {
                        Text(
                            text = "Recents",
                            color = MaterialTheme.colorScheme.onBackground,
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)
                        )

                        LazyRow(
                            modifier = Modifier.fillMaxWidth(),
                            contentPadding = PaddingValues(horizontal = 16.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(recentFiles, key = { it.id }) { file ->
                                RecentFileCard(
                                    file = file,
                                    onClick = {
                                        val ext = file.extension.lowercase()
                                        if (file.mimeType?.startsWith("image/") == true || file.mimeType?.startsWith("video/") == true) {
                                            onNavigateToMedia("recent")
                                        } else {
                                            onNavigateToFiles(file.path)
                                        }
                                    }
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))
                    }
                }

                // ─── 5. FLOATING BOTTOM DOCK & FAB ──────────────────────────
                FloatingBottomDock(
                    selectedTab = DashboardNavTab.HOME,
                    onTabSelected = { tab ->
                        when (tab) {
                            DashboardNavTab.HOME -> { /* Already on Home */ }
                            DashboardNavTab.FILES -> onNavigateToFiles(null)
                            DashboardNavTab.SEARCH -> onNavigateToSearch()
                            DashboardNavTab.APPS -> onNavigateToApps(false)
                        }
                    },
                    onFabClick = { onNavigateToFiles(null) },
                    modifier = Modifier.align(Alignment.BottomCenter)
                )
            }
        }
    }
}



