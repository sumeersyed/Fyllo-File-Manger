package com.fyllo.filemanager.ui.screens

import android.net.Uri
import android.text.format.Formatter
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.CleaningServices
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.Image
import androidx.compose.material.icons.outlined.Movie
import androidx.compose.material.icons.outlined.MusicNote
import androidx.compose.material.icons.outlined.OpenInFull
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.fyllo.filemanager.domain.model.FileItem
import com.fyllo.filemanager.ui.theme.LocalEInkMode
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StorageCleanupScreen(
    viewModel: StorageCleanupViewModel,
    onBackClick: () -> Unit,
    onFileClick: (FileItem) -> Unit
) {
    val context = LocalContext.current
    val currentTab by viewModel.currentTab.collectAsState()
    val isScanning by viewModel.isScanning.collectAsState()
    val selectedUris by viewModel.selectedUris.collectAsState()

    val largeFiles by viewModel.largeFiles.collectAsState()
    val oldFiles by viewModel.oldFiles.collectAsState()
    val duplicateFiles by viewModel.duplicateFiles.collectAsState()
    val junkFiles by viewModel.junkFiles.collectAsState()

    val isEInk = LocalEInkMode.current

    val currentFiles = when (currentTab) {
        CleanupTab.LARGE -> largeFiles
        CleanupTab.OLD -> oldFiles
        CleanupTab.DUPLICATE -> duplicateFiles
        CleanupTab.JUNK -> emptyList()
    }

    val selectedBytes = currentFiles.filter { selectedUris.contains(it.uri) }.sumOf { it.sizeBytes }
    val formattedSelectedBytes = Formatter.formatFileSize(context, selectedBytes)

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Storage cleanup",
                        color = MaterialTheme.colorScheme.onBackground,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = MaterialTheme.colorScheme.onBackground
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        bottomBar = {
            AnimatedVisibility(
                visible = selectedUris.isNotEmpty() || (currentTab == CleanupTab.JUNK && junkFiles.isNotEmpty()),
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.background)
                        .padding(16.dp)
                ) {
                    if (currentTab == CleanupTab.JUNK) {
                        Button(
                            onClick = { viewModel.cleanJunk() },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp)
                                .clip(RoundedCornerShape(16.dp)),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isEInk) MaterialTheme.colorScheme.onBackground else Color(0xFFD32F2F)
                            )
                        ) {
                            Icon(Icons.Outlined.CleaningServices, contentDescription = null, tint = Color.White)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                "Clean ${junkFiles.size} junk items",
                                color = Color.White,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    } else {
                        Button(
                            onClick = { viewModel.deleteSelectedFiles(permanent = true) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp)
                                .clip(RoundedCornerShape(16.dp)),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isEInk) MaterialTheme.colorScheme.onBackground else Color(0xFFD32F2F)
                            )
                        ) {
                            Icon(Icons.Outlined.Delete, contentDescription = null, tint = Color.White)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                "Delete selected (${selectedUris.size} files · $formattedSelectedBytes)",
                                color = Color.White,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Tabs
            val tabs = listOf(
                CleanupTab.LARGE to "Large files",
                CleanupTab.OLD to "Old files",
                CleanupTab.DUPLICATE to "Duplicates",
                CleanupTab.JUNK to "Junk files"
            )

            ScrollableTabRow(
                selectedTabIndex = tabs.indexOfFirst { it.first == currentTab },
                containerColor = MaterialTheme.colorScheme.background,
                contentColor = Color(0xFF3366FF),
                edgePadding = 16.dp,
                indicator = { tabPositions ->
                    val index = tabs.indexOfFirst { it.first == currentTab }
                    if (index >= 0 && index < tabPositions.size) {
                        TabRowDefaults.SecondaryIndicator(
                            modifier = Modifier.tabIndicatorOffset(tabPositions[index]),
                            color = Color(0xFF3366FF)
                        )
                    }
                },
                divider = { HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.12f)) }
            ) {
                tabs.forEach { (tab, title) ->
                    Tab(
                        selected = currentTab == tab,
                        onClick = { viewModel.selectTab(tab) },
                        text = {
                            Text(
                                text = title,
                                color = if (currentTab == tab) Color(0xFF3366FF) else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                                fontWeight = if (currentTab == tab) FontWeight.Bold else FontWeight.Medium,
                                fontSize = 14.sp
                            )
                        }
                    )
                }
            }

            if (isScanning) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        CircularProgressIndicator(color = Color(0xFF3366FF))
                        Spacer(modifier = Modifier.height(16.dp))
                        Text("Performing deep scan...", color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f))
                    }
                }
                return@Scaffold
            }

            if (currentTab == CleanupTab.JUNK) {
                // Junk list view
                if (junkFiles.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("No junk files found! Storage is clean.", color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(junkFiles) { file ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.7f))
                                    .padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(40.dp)
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(Color(0xFFE8EFFF)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(Icons.Outlined.CleaningServices, contentDescription = null, tint = Color(0xFF3366FF), modifier = Modifier.size(20.dp))
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = file.name,
                                        color = MaterialTheme.colorScheme.onBackground,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Text(
                                        text = file.parent ?: "",
                                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                                        fontSize = 12.sp,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }
                        }
                    }
                }
                return@Scaffold
            }

            // Header for selection list
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                val headerTitle = when (currentTab) {
                    CleanupTab.LARGE -> "Large files"
                    CleanupTab.OLD -> "Old files (6+ months)"
                    CleanupTab.DUPLICATE -> "Duplicate files"
                    CleanupTab.JUNK -> ""
                }
                Text(
                    text = headerTitle,
                    color = MaterialTheme.colorScheme.onBackground,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )

                TextButton(onClick = { viewModel.selectAll() }) {
                    Text(
                        text = if (selectedUris.size == currentFiles.size && currentFiles.isNotEmpty()) "Deselect all" else "Select all",
                        color = Color(0xFF3366FF),
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 14.sp
                    )
                }
            }

            if (currentFiles.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No files found in this category", color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    items(currentFiles, key = { it.id }) { file ->
                        val isSelected = selectedUris.contains(file.uri)
                        val formattedSize = Formatter.formatFileSize(context, file.sizeBytes)
                        val isMedia = file.mimeType?.startsWith("image/") == true || file.mimeType?.startsWith("video/") == true
                        val isVideo = file.mimeType?.startsWith("video/") == true
                        val isAudio = file.mimeType?.startsWith("audio/") == true

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(if (isSelected) Color(0xFF3366FF).copy(alpha = 0.08f) else MaterialTheme.colorScheme.surface.copy(alpha = 0.6f))
                                .clickable { viewModel.toggleSelection(file.uri) }
                                .padding(horizontal = 12.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Thumbnail / Icon with badge overlay
                            Box(
                                modifier = Modifier
                                    .size(46.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(
                                        when {
                                            isAudio -> Color(0xFFFFF3E0)
                                            isMedia -> MaterialTheme.colorScheme.surfaceVariant
                                            else -> Color(0xFFE8EFFF)
                                        }
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                if (isMedia) {
                                    AsyncImage(
                                        model = file.uri,
                                        contentDescription = file.name,
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier.fillMaxSize()
                                    )
                                    // Expand badge on thumbnail corner (matches screenshot 2)
                                    Box(
                                        modifier = Modifier
                                            .align(Alignment.BottomEnd)
                                            .padding(3.dp)
                                            .size(16.dp)
                                            .clip(CircleShape)
                                            .background(Color(0xFF3366FF)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Outlined.OpenInFull,
                                            contentDescription = null,
                                            tint = Color.White,
                                            modifier = Modifier.size(10.dp)
                                        )
                                    }
                                } else if (isAudio) {
                                    Icon(
                                        imageVector = Icons.Outlined.MusicNote,
                                        contentDescription = null,
                                        tint = Color(0xFFE65100),
                                        modifier = Modifier.size(24.dp)
                                    )
                                    Box(
                                        modifier = Modifier
                                            .align(Alignment.BottomEnd)
                                            .padding(3.dp)
                                            .size(16.dp)
                                            .clip(CircleShape)
                                            .background(Color(0xFF3366FF)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Outlined.OpenInFull,
                                            contentDescription = null,
                                            tint = Color.White,
                                            modifier = Modifier.size(10.dp)
                                        )
                                    }
                                } else {
                                    Icon(
                                        imageVector = Icons.Outlined.Description,
                                        contentDescription = null,
                                        tint = Color(0xFF3366FF),
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.width(12.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = file.name,
                                    color = MaterialTheme.colorScheme.onBackground,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = formattedSize,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.65f),
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }

                            Checkbox(
                                checked = isSelected,
                                onCheckedChange = { viewModel.toggleSelection(file.uri) },
                                colors = CheckboxDefaults.colors(
                                    checkedColor = Color(0xFF3366FF),
                                    uncheckedColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                                )
                            )
                        }
                    }
                }
            }
        }
    }
}
