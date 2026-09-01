package com.fyllo.filemanager.ui.screens

import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.InsertDriveFile
import androidx.compose.material.icons.filled.Android
import androidx.compose.material.icons.filled.AudioFile
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.FolderZip
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material.icons.filled.SelectAll
import androidx.compose.material.icons.outlined.DeleteForever
import androidx.compose.material.icons.outlined.GridView
import androidx.compose.material.icons.outlined.Restore
import androidx.compose.material.icons.outlined.ViewList
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.waitForUpOrCancellation
import kotlinx.coroutines.delay
import coil.compose.SubcomposeAsyncImage
import coil.request.ImageRequest
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.fyllo.filemanager.R
import com.fyllo.filemanager.domain.model.TrashItem
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TrashScreen(
    viewModel: TrashViewModel,
    onBackClick: () -> Unit
) {
    val context = LocalContext.current
    val imageLoader = remember(context) {
        coil.ImageLoader.Builder(context)
            .components { add(coil.decode.VideoFrameDecoder.Factory()) }
            .memoryCachePolicy(coil.request.CachePolicy.ENABLED)
            .diskCachePolicy(coil.request.CachePolicy.ENABLED)
            .build()
    }
    
    val trashItems by viewModel.trashItems.collectAsState()
    var selectionMode by remember { mutableStateOf(false) }
    val selectedItems = remember { mutableStateListOf<TrashItem>() }

    var isGridView by remember { mutableStateOf(true) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.recycle_bin), color = MaterialTheme.colorScheme.onBackground) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = MaterialTheme.colorScheme.onBackground)
                    }
                },
                actions = {
                    IconButton(onClick = { isGridView = !isGridView }) {
                        Icon(
                            imageVector = if (isGridView) Icons.Outlined.ViewList else Icons.Outlined.GridView,
                            contentDescription = "Toggle Grid/List",
                            tint = MaterialTheme.colorScheme.onBackground
                        )
                    }
                    if (trashItems.isNotEmpty()) {
                        TextButton(onClick = { viewModel.emptyTrash() }) {
                            Text("Empty", color = Color(0xFFFF3B30), fontWeight = FontWeight.Bold)
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
            Column(modifier = Modifier.fillMaxSize()) {
                // Retention Notice Header Pill
                Surface(
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 6.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = "Items in the Recycle Bin will be automatically deleted after 30 days.",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 12.sp,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(vertical = 8.dp, horizontal = 12.dp)
                    )
                }

                if (trashItems.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("Recycle Bin is empty", color = Color(0xFFA0A0A0))
                    }
                } else if (isGridView) {
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(3),
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(12.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(trashItems, key = { it.id }) { item ->
                            val isSelected = selectedItems.contains(item)
                            TrashItemGridCard(
                                item = item,
                                isSelectionMode = selectionMode,
                                isSelected = isSelected,
                                onToggleSelect = {
                                    if (isSelected) selectedItems.remove(item)
                                    else selectedItems.add(item)
                                    if (selectedItems.isEmpty()) selectionMode = false
                                },
                                onLongClick = {
                                    if (!selectionMode) {
                                        selectionMode = true
                                        selectedItems.add(item)
                                    }
                                },
                                imageLoader = imageLoader
                            )
                        }
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(trashItems, key = { it.id }) { item ->
                            val isSelected = selectedItems.contains(item)
                            TrashItemRow(
                                item = item,
                                isSelectionMode = selectionMode,
                                isSelected = isSelected,
                                onToggleSelect = {
                                    if (isSelected) selectedItems.remove(item)
                                    else selectedItems.add(item)
                                    if (selectedItems.isEmpty()) selectionMode = false
                                },
                                onLongClick = {
                                    if (!selectionMode) {
                                        selectionMode = true
                                        selectedItems.add(item)
                                    }
                                },
                                imageLoader = imageLoader
                            )
                        }
                    }
                }
            }

            // Top Selection Bar
            AnimatedVisibility(
                visible = selectionMode,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically(),
                modifier = Modifier.align(Alignment.TopCenter)
            ) {
                Surface(
                    color = MaterialTheme.colorScheme.surface,
                    tonalElevation = 8.dp,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(onClick = { selectionMode = false; selectedItems.clear() }) {
                            Icon(Icons.Default.Close, contentDescription = stringResource(R.string.close), tint = MaterialTheme.colorScheme.onBackground)
                        }
                        Text("${selectedItems.size}", color = MaterialTheme.colorScheme.onBackground, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 8.dp))
                        Spacer(modifier = Modifier.weight(1f))
                        Row(
                            modifier = Modifier
                                .clip(RoundedCornerShape(16.dp))
                                .clickable {
                                    if (selectedItems.size == trashItems.size) selectedItems.clear()
                                    else { selectedItems.clear(); selectedItems.addAll(trashItems) }
                                }
                                .padding(horizontal = 12.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(Icons.Default.SelectAll, contentDescription = null, tint = MaterialTheme.colorScheme.onBackground, modifier = Modifier.size(18.dp))
                            Text(stringResource(R.string.select_all), color = MaterialTheme.colorScheme.onBackground, fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
            }

            // Selection Bottom Bar
            AnimatedVisibility(
                visible = selectionMode && selectedItems.isNotEmpty(),
                enter = slideInVertically { it },
                exit = slideOutVertically { it },
                modifier = Modifier.align(Alignment.BottomCenter)
            ) {
                BottomAppBar(
                    containerColor = MaterialTheme.colorScheme.surface,
                    contentColor = MaterialTheme.colorScheme.onSurface
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .clickable {
                                    viewModel.restoreItems(selectedItems.toList())
                                    selectionMode = false
                                    selectedItems.clear()
                                }
                                .padding(vertical = 8.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(Icons.Outlined.Restore, contentDescription = "Restore")
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("Restore", fontSize = 12.sp)
                        }
                        
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .clickable {
                                    viewModel.deleteItemsPermanently(selectedItems.toList())
                                    selectionMode = false
                                    selectedItems.clear()
                                }
                                .padding(vertical = 8.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(Icons.Outlined.DeleteForever, contentDescription = "Delete")
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("Delete", fontSize = 12.sp)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun TrashItemGridCard(
    item: TrashItem,
    isSelectionMode: Boolean,
    isSelected: Boolean,
    onToggleSelect: () -> Unit,
    onLongClick: () -> Unit,
    imageLoader: coil.ImageLoader
) {
    var isPressed by remember { mutableStateOf(false) }
    
    LaunchedEffect(isPressed) {
        if (isPressed) {
            kotlinx.coroutines.delay(2000)
            onLongClick()
        }
    }

    val fileExtension = item.name.substringAfterLast('.', "").lowercase()
    val isImage = fileExtension in listOf("jpg", "jpeg", "png", "gif", "bmp", "webp", "heic")
    val isVideo = fileExtension in listOf("mp4", "mkv", "avi", "mov", "webm", "ts")
    val isAudio = fileExtension in listOf("mp3", "wav", "ogg", "flac", "m4a")
    val isArchive = fileExtension in listOf("zip", "rar", "7z", "tar", "gz")
    val isPdf = fileExtension == "pdf"
    val isDocument = fileExtension in listOf("doc", "docx", "xls", "xlsx", "ppt", "pptx", "txt")
    val isApk = fileExtension == "apk"

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(0.85f)
            .clip(RoundedCornerShape(16.dp))
            .border(
                1.dp,
                if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.primary.copy(alpha = 0.2f),
                RoundedCornerShape(16.dp)
            )
            .pointerInput(isSelectionMode) {
                awaitEachGesture {
                    awaitFirstDown()
                    isPressed = true
                    val up = waitForUpOrCancellation()
                    isPressed = false
                    if (up != null) {
                        if (isSelectionMode) onToggleSelect()
                    }
                }
            },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) MaterialTheme.colorScheme.surfaceVariant else MaterialTheme.colorScheme.surface.copy(alpha = 0.6f)
        )
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(8.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(54.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                    contentAlignment = Alignment.Center
                ) {
                    if (isImage || isVideo) {
                        val context = LocalContext.current
                        SubcomposeAsyncImage(
                            model = ImageRequest.Builder(context)
                                .data(java.io.File(item.trashPath))
                                .crossfade(true)
                                .build(),
                            imageLoader = imageLoader,
                            contentDescription = null,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = androidx.compose.ui.layout.ContentScale.Crop
                        )
                        if (isVideo) {
                            Icon(
                                Icons.Filled.PlayCircle,
                                contentDescription = "Video",
                                tint = Color.White,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    } else {
                        val icon = when {
                            item.isFolder -> Icons.Filled.Folder
                            isAudio -> Icons.Filled.AudioFile
                            isArchive -> Icons.Filled.FolderZip
                            isPdf -> Icons.Filled.PictureAsPdf
                            isDocument -> Icons.Filled.Description
                            isApk -> Icons.Filled.Android
                            else -> Icons.AutoMirrored.Filled.InsertDriveFile
                        }
                        val iconTint = when {
                            item.isFolder -> Color(0xFFFFD54F)
                            isAudio -> Color(0xFFF06292)
                            isArchive -> Color(0xFFBA68C8)
                            isPdf -> Color(0xFFE57373)
                            isDocument -> Color(0xFF64B5F6)
                            isApk -> Color(0xFFAED581)
                            else -> Color(0xFFA0A0A0)
                        }
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            tint = iconTint,
                            modifier = Modifier.size(32.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = item.name,
                    color = MaterialTheme.colorScheme.onBackground,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )

                Text(
                    text = formatSize(item.sizeBytes),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 10.sp,
                    textAlign = TextAlign.Center
                )
            }

            if (isSelectionMode) {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(6.dp)
                        .size(20.dp)
                        .clip(CircleShape)
                        .background(if (isSelected) Color(0xFF4CAF50) else Color(0x66000000))
                        .clickable { onToggleSelect() },
                    contentAlignment = Alignment.Center
                ) {
                    if (isSelected) {
                        Icon(Icons.Default.Close, contentDescription = null, tint = Color.White, modifier = Modifier.size(14.dp))
                    }
                }
            }
        }
    }
}

@Composable
fun TrashItemRow(
    item: TrashItem,
    isSelectionMode: Boolean,
    isSelected: Boolean,
    onToggleSelect: () -> Unit,
    onLongClick: () -> Unit,
    imageLoader: coil.ImageLoader
) {
    var isPressed by remember { mutableStateOf(false) }
    
    LaunchedEffect(isPressed) {
        if (isPressed) {
            kotlinx.coroutines.delay(2000)
            onLongClick()
        }
    }
    
    val formatter = SimpleDateFormat("MMM dd, yyyy • h:mm a", Locale.getDefault())
    val dateString = formatter.format(Date(item.timestamp))
    val sizeString = formatSize(item.sizeBytes)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(if (isSelected) MaterialTheme.colorScheme.surfaceVariant else MaterialTheme.colorScheme.surface.copy(alpha = 0.6f))
            .border(1.dp, if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.primary.copy(alpha = 0.25f), RoundedCornerShape(20.dp))
            .pointerInput(isSelectionMode) {
                awaitEachGesture {
                    awaitFirstDown()
                    isPressed = true
                    val up = waitForUpOrCancellation()
                    isPressed = false
                    if (up != null) {
                        if (isSelectionMode) onToggleSelect()
                    }
                }
            }
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (isSelectionMode) {
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .clip(CircleShape)
                    .background(if (isSelected) Color(0xFF4CAF50) else Color.Transparent)
                    .clickable { onToggleSelect() },
                contentAlignment = Alignment.Center
            ) {
                if (isSelected) {
                    Icon(Icons.Default.Close, contentDescription = null, tint = MaterialTheme.colorScheme.onBackground, modifier = Modifier.size(16.dp))
                } else {
                    Box(modifier = Modifier.fillMaxSize().background(Color(0xFF404040)))
                }
            }
            Spacer(modifier = Modifier.width(16.dp))
        }

        // Thumbnail or Icon
        val fileExtension = item.name.substringAfterLast('.', "").lowercase()
        val isImage = fileExtension in listOf("jpg", "jpeg", "png", "gif", "bmp", "webp", "heic")
        val isVideo = fileExtension in listOf("mp4", "mkv", "avi", "mov", "webm", "ts")
        val isAudio = fileExtension in listOf("mp3", "wav", "ogg", "flac", "m4a")
        val isArchive = fileExtension in listOf("zip", "rar", "7z", "tar", "gz")
        val isPdf = fileExtension == "pdf"
        val isDocument = fileExtension in listOf("doc", "docx", "xls", "xlsx", "ppt", "pptx", "txt")
        val isApk = fileExtension == "apk"

        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(RoundedCornerShape(14.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
            contentAlignment = Alignment.Center
        ) {
            if (isImage || isVideo) {
                val context = LocalContext.current
                SubcomposeAsyncImage(
                    model = ImageRequest.Builder(context)
                        .data(java.io.File(item.trashPath))
                        .crossfade(true)
                        .build(),
                    imageLoader = imageLoader,
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = androidx.compose.ui.layout.ContentScale.Crop,
                    loading = {
                        Box(modifier = Modifier.fillMaxSize().background(Color.Gray.copy(alpha = 0.3f)))
                    }
                )
                if (isVideo) {
                    Icon(
                        Icons.Filled.PlayCircle,
                        contentDescription = "Video",
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                }
            } else {
                val icon = when {
                    item.isFolder -> Icons.Filled.Folder
                    isAudio -> Icons.Filled.AudioFile
                    isArchive -> Icons.Filled.FolderZip
                    isPdf -> Icons.Filled.PictureAsPdf
                    isDocument -> Icons.Filled.Description
                    isApk -> Icons.Filled.Android
                    else -> Icons.AutoMirrored.Filled.InsertDriveFile
                }
                val iconTint = when {
                    item.isFolder -> Color(0xFFFFD54F)
                    isAudio -> Color(0xFFF06292)
                    isArchive -> Color(0xFFBA68C8)
                    isPdf -> Color(0xFFE57373)
                    isDocument -> Color(0xFF64B5F6)
                    isApk -> Color(0xFFAED581)
                    else -> Color(0xFFA0A0A0)
                }
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconTint,
                    modifier = Modifier.size(28.dp)
                )
            }
        }
        
        Spacer(modifier = Modifier.width(16.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = item.name,
                color = MaterialTheme.colorScheme.onBackground,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Original path: ${item.originalPath}",
                color = Color(0xFFA0A0A0),
                fontSize = 12.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
        
        Spacer(modifier = Modifier.width(16.dp))
        
        Column(horizontalAlignment = Alignment.End) {
            Text(text = sizeString, color = MaterialTheme.colorScheme.onBackground, fontSize = 14.sp)
            Text(text = dateString, color = Color(0xFFA0A0A0), fontSize = 12.sp)
        }
    }
}

private fun formatSize(size: Long): String {
    if (size <= 0) return "0 B"
    val units = arrayOf("B", "KB", "MB", "GB", "TB")
    val digitGroups = (Math.log10(size.toDouble()) / Math.log10(1024.0)).toInt()
    return String.format(Locale.US, "%.1f %s", size / Math.pow(1024.0, digitGroups.toDouble()), units[digitGroups])
}
