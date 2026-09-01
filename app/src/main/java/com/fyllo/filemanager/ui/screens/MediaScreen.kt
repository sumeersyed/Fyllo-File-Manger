package com.fyllo.filemanager.ui.screens

import android.net.Uri
import kotlinx.coroutines.launch
import com.fyllo.filemanager.domain.model.FileOperationState
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.*
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.rememberTransformableState
import androidx.compose.foundation.gestures.transformable

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.material3.FloatingActionButton
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.items
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items as lazyItems
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Tv
import androidx.compose.material.icons.filled.PictureInPicture
import androidx.compose.material.icons.filled.PlayCircleOutline
import androidx.compose.material.icons.filled.SelectAll
import androidx.compose.material.icons.outlined.ContentCopy
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.automirrored.outlined.DriveFileMove
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.Replay10
import androidx.compose.material.icons.filled.Forward10
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.outlined.BrightnessMedium
import androidx.compose.material.icons.outlined.Language
import androidx.compose.material.icons.automirrored.outlined.VolumeUp
import androidx.compose.material.icons.automirrored.outlined.VolumeDown
import androidx.compose.material.icons.automirrored.outlined.VolumeOff
import androidx.compose.material.icons.outlined.BrightnessHigh
import androidx.compose.material.icons.outlined.BrightnessLow
import android.app.Activity
import android.content.ContextWrapper
import androidx.compose.ui.input.pointer.PointerEventPass
import kotlin.math.abs
import kotlin.math.roundToInt
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import kotlinx.coroutines.delay
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import androidx.compose.foundation.lazy.staggeredgrid.rememberLazyStaggeredGridState
import coil.compose.AsyncImage
import coil.compose.SubcomposeAsyncImage
import coil.request.ImageRequest
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.animation.core.*
import com.fyllo.filemanager.domain.model.FileItem
import com.fyllo.filemanager.ui.theme.NeonCyan
import androidx.compose.material3.MaterialTheme
import kotlin.random.Random
import android.content.Intent
import android.content.Context
import android.content.ClipboardManager
import android.content.ClipData
import android.widget.Toast
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MediaScreen(
    mediaFiles: List<FileItem>,
    isLoading: Boolean,
    filterType: String = "all",
    operationState: FileOperationState = FileOperationState.Idle,
    onCancelOperation: () -> Unit = {},
    onBackClick: () -> Unit,
    onLoadMedia: () -> Unit,
    onNavigateToEdit: (FileItem) -> Unit,
    onDeleteClick: (FileItem) -> Unit,
    onDeleteMultipleClick: (List<FileItem>) -> Unit,
    onDeleteWithOptions: (List<Uri>, Boolean) -> Unit = { _, _ -> },
    onCopyToClipboard: (List<Uri>, Boolean) -> Unit = { _, _ -> },
    onCopyMultipleClick: (List<FileItem>, Uri) -> Unit,
    onMoveMultipleClick: (List<FileItem>, Uri) -> Unit,
    onRenameClick: (FileItem, String) -> Unit,
    onCompressClick: (FileItem) -> Unit,
    onPlayAudio: ((FileItem, List<FileItem>) -> Unit)? = null,
    onFileClick: ((FileItem) -> Unit)? = null,
    enableScrollSound: Boolean = true
) {
    var selectedMedia by remember { mutableStateOf<FileItem?>(null) }
    var selectionMode by remember { mutableStateOf(false) }
    var showDeleteConfirmDialog by remember { mutableStateOf(false) }
    val selectedFiles = remember { mutableStateListOf<FileItem>() }
    val context = LocalContext.current
    val isAudioMode = filterType == "audio"
    val isDocumentMode = filterType in listOf("documents", "pdfs", "slides", "spreadsheets", "archives", "code", "text")
    
    val moveLauncher = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocumentTree()) { uri ->
        if (uri != null) {
            onMoveMultipleClick(selectedFiles.toList(), uri)
            selectionMode = false
            selectedFiles.clear()
        }
    }
    
    val copyLauncher = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocumentTree()) { uri ->
        if (uri != null) {
            onCopyMultipleClick(selectedFiles.toList(), uri)
            selectionMode = false
            selectedFiles.clear()
        }
    }

    LaunchedEffect(Unit) {
        onLoadMedia()
    }

    // Wave Progress Dialog
    if (operationState is FileOperationState.Running) {
        com.fyllo.filemanager.ui.components.WaveOperationProgressDialog(
            operationTitle = "Processing Media",
            state = operationState,
            onCancel = onCancelOperation
        )
    }

    // Delete confirmation dialog
    if (showDeleteConfirmDialog && selectedFiles.isNotEmpty()) {
        com.fyllo.filemanager.ui.components.WaveDeleteConfirmationDialog(
            itemCount = selectedFiles.size,
            onConfirmDelete = { permanent ->
                onDeleteWithOptions(selectedFiles.map { it.uri }, permanent)
                showDeleteConfirmDialog = false
                selectionMode = false
                selectedFiles.clear()
            },
            onDismiss = { showDeleteConfirmDialog = false }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    val titleText = when (filterType) {
                        "images" -> "Images"
                        "videos" -> "Videos"
                        "audio" -> "Music"
                        "documents" -> "Documents"
                        "pdfs" -> "PDF Documents"
                        "slides" -> "Presentations & Slides"
                        "spreadsheets" -> "Spreadsheets & Sheets"
                        "archives" -> "Archives & Compressed"
                        "code" -> "Code & Scripts"
                        "text" -> "Text Files"
                        "recent" -> "Recent"
                        "favorites" -> "Favorites"
                        else -> "Photo & Video"
                    }
                    Text(
                        titleText,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                },
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
        Box(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
            if (isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = NeonCyan)
                }
            } else if (mediaFiles.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No media found", color = Color(0xFFA0A0A0))
                }
            } else if (isAudioMode) {
                // ─── Audio List View ─────────────────────────────────────
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(vertical = 8.dp)
                ) {
                    lazyItems(mediaFiles, key = { it.id }) { file ->
                        AudioListItem(
                            file = file,
                            onClick = {
                                if (onPlayAudio != null) {
                                    onPlayAudio(file, mediaFiles)
                                } else {
                                    try {
                                        val intent = Intent(Intent.ACTION_VIEW).apply {
                                            setDataAndType(file.uri, file.mimeType ?: "audio/*")
                                            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                        }
                                        context.startActivity(intent)
                                    } catch (_: Exception) {
                                        android.widget.Toast.makeText(context, "No app found to play audio", android.widget.Toast.LENGTH_SHORT).show()
                                    }
                                }
                            }
                        )
                    }
                }
            } else if (isDocumentMode) {
                // ─── Documents List View ─────────────────────────────────
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp)
                ) {
                    lazyItems(mediaFiles, key = { it.id }) { file ->
                        val isSelected = selectedFiles.contains(file)
                        DocumentListItem(
                            file = file,
                            isSelected = isSelected,
                            isSelectionMode = selectionMode,
                            onClick = {
                                if (selectionMode) {
                                    if (isSelected) selectedFiles.remove(file) else selectedFiles.add(file)
                                    if (selectedFiles.isEmpty()) selectionMode = false
                                } else {
                                    if (onFileClick != null) {
                                        onFileClick(file)
                                    } else {
                                        try {
                                            val intent = Intent(Intent.ACTION_VIEW).apply {
                                                setDataAndType(file.uri, file.mimeType ?: "*/*")
                                                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                            }
                                            context.startActivity(intent)
                                        } catch (_: Exception) {
                                            android.widget.Toast.makeText(context, "No app found to open this document", android.widget.Toast.LENGTH_SHORT).show()
                                        }
                                    }
                                }
                            },
                            onLongClick = {
                                if (!selectionMode) {
                                    selectionMode = true
                                    selectedFiles.add(file)
                                }
                            }
                        )
                    }
                }
            } else {
                val gridState = rememberLazyStaggeredGridState()
                
                val audioManager = remember { context.getSystemService(android.content.Context.AUDIO_SERVICE) as? android.media.AudioManager }
                var lastItemIndex by remember { mutableStateOf(-1) }
                
                LaunchedEffect(gridState.firstVisibleItemIndex, gridState.firstVisibleItemScrollOffset) {
                    if (enableScrollSound && gridState.isScrollInProgress) {
                        val currentIndex = gridState.firstVisibleItemIndex
                        if (currentIndex != lastItemIndex) {
                            lastItemIndex = currentIndex
                            try {
                                audioManager?.playSoundEffect(android.media.AudioManager.FX_KEY_CLICK, 0.25f)
                            } catch (_: Exception) {}
                        }
                    }
                }

                LazyVerticalStaggeredGrid(
                    state = gridState,
                    columns = StaggeredGridCells.Fixed(2),
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(12.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalItemSpacing = 12.dp
                ) {
                items(mediaFiles, key = { it.id }) { file ->
                    val isSelected = selectedFiles.contains(file)
                    val selectionIndex = selectedFiles.indexOf(file) + 1
                    MediaMasonryItem(
                        file = file,
                        isSelectionMode = selectionMode,
                        isSelected = isSelected,
                        selectionIndex = selectionIndex,
                        onSelectToggle = {
                            if (isSelected) selectedFiles.remove(file) else selectedFiles.add(file)
                            if (selectedFiles.isEmpty()) selectionMode = false
                        },
                        onHold2s = {
                            if (!selectionMode) {
                                selectionMode = true
                                selectedFiles.add(file)
                            }
                        },
                        onClick = { 
                            if (selectionMode) {
                                if (isSelected) selectedFiles.remove(file) else selectedFiles.add(file)
                                if (selectedFiles.isEmpty()) selectionMode = false
                            } else {
                                selectedMedia = file 
                            }
                        }
                    )
                }
            }
        }

        
        // Multi-selection Top Bar
        AnimatedVisibility(
            visible = selectionMode,
            enter = slideInVertically { -it },
            exit = slideOutVertically { -it },
            modifier = Modifier.align(Alignment.TopCenter).padding(top = 16.dp)
        ) {
            Box(
                modifier = Modifier
                    .padding(horizontal = 16.dp)
                    .height(48.dp)
                    .clip(RoundedCornerShape(24.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 8.dp).fillMaxHeight(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = { selectionMode = false; selectedFiles.clear() }) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Text("${selectedFiles.size}", color = MaterialTheme.colorScheme.onSurfaceVariant, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 8.dp))
                    Spacer(modifier = Modifier.weight(1f))
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(24.dp))
                            .clickable { 
                                selectedFiles.clear()
                                selectedFiles.addAll(mediaFiles)
                            }
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(Icons.Default.SelectAll, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(18.dp))
                        Text("Select all", color = MaterialTheme.colorScheme.onSurfaceVariant, fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }
        
        // Multi-selection Bottom Bar
        AnimatedVisibility(
            visible = selectionMode && selectedFiles.isNotEmpty() && operationState !is FileOperationState.Running,
            enter = slideInVertically { it },
            exit = slideOutVertically { it },
            modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 32.dp)
        ) {
            Box(
                modifier = Modifier
                    .wrapContentWidth()
                    .height(64.dp)
                    .clip(RoundedCornerShape(32.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 24.dp).fillMaxHeight(),
                    horizontalArrangement = Arrangement.spacedBy(24.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    SelectionAction(Icons.Outlined.Share, "Share", MaterialTheme.colorScheme.onSurfaceVariant) {
                        val shareIntent = Intent(Intent.ACTION_SEND_MULTIPLE).apply {
                            type = "*/*"
                            putParcelableArrayListExtra(Intent.EXTRA_STREAM, ArrayList(selectedFiles.map { it.uri }))
                        }
                        context.startActivity(Intent.createChooser(shareIntent, "Share Media"))
                    }
                    SelectionAction(Icons.Outlined.FavoriteBorder, "Favorite", MaterialTheme.colorScheme.onSurfaceVariant) {
                        val sharedPrefs = context.getSharedPreferences("media_favorites", Context.MODE_PRIVATE)
                        val editor = sharedPrefs.edit()
                        selectedFiles.forEach { editor.putBoolean(it.uri.toString(), true) }
                        editor.apply()
                        selectionMode = false; selectedFiles.clear()
                    }
                    SelectionAction(Icons.Outlined.ContentCopy, "Copy", MaterialTheme.colorScheme.onSurfaceVariant) {
                        onCopyToClipboard(selectedFiles.map { it.uri }, false)
                        selectionMode = false
                        selectedFiles.clear()
                        Toast.makeText(context, "Copied to Clipboard. Open Files to Paste.", Toast.LENGTH_SHORT).show()
                    }
                    SelectionAction(Icons.AutoMirrored.Outlined.DriveFileMove, "Move", MaterialTheme.colorScheme.onSurfaceVariant) {
                        onCopyToClipboard(selectedFiles.map { it.uri }, true)
                        selectionMode = false
                        selectedFiles.clear()
                        Toast.makeText(context, "Cut to Clipboard. Open Files to Paste.", Toast.LENGTH_SHORT).show()
                    }
                    SelectionAction(Icons.Outlined.Delete, "Delete", MaterialTheme.colorScheme.onSurfaceVariant) { 
                        showDeleteConfirmDialog = true
                    }
                }
            }
        }
        
            }
        }
        
        // Peek View Overlay removed
    if (selectedMedia != null) {
        val initialIndex = mediaFiles.indexOf(selectedMedia).coerceAtLeast(0)
        ImageViewerOverlay(
            mediaFiles = mediaFiles,
            initialIndex = initialIndex,
            onDismiss = { selectedMedia = null },
            onEdit = { file -> onNavigateToEdit(file) },
            onDeleteClick = onDeleteClick,
            onRenameClick = onRenameClick,
            onCopy = { file -> 
                selectedFiles.clear()
                selectedFiles.add(file)
                copyLauncher.launch(Uri.parse("content://")) 
            },
            onMove = { file -> 
                selectedFiles.clear()
                selectedFiles.add(file)
                moveLauncher.launch(Uri.parse("content://")) 
            },
            onCompress = { file, _ ->
                onCompressClick(file)
            },
            onSafeFolderClick = { file ->
                onMoveMultipleClick(listOf(file), Uri.parse("file://${context.filesDir.absolutePath}/SafeFolder"))
            }
        )
    }
}

@Composable
fun MediaMasonryItem(
    file: FileItem,
    isSelectionMode: Boolean,
    isSelected: Boolean,
    selectionIndex: Int,
    onSelectToggle: () -> Unit,
    onHold2s: () -> Unit,
    onClick: () -> Unit
) {
    val isVideo = file.mimeType?.startsWith("video/") == true
    val context = LocalContext.current

    // Use rememberUpdatedState so lambdas inside pointerInput always see the latest values
    // without needing to restart the gesture handler (which would cause double-firing).
    val currentIsSelectionMode by androidx.compose.runtime.rememberUpdatedState(isSelectionMode)
    val currentOnSelectToggle by androidx.compose.runtime.rememberUpdatedState(onSelectToggle)
    val currentOnHold by androidx.compose.runtime.rememberUpdatedState(onHold2s)
    val currentOnClick by androidx.compose.runtime.rememberUpdatedState(onClick)

    val imageLoader = remember(context) {
        coil.ImageLoader.Builder(context)
            .components {
                add(coil.decode.VideoFrameDecoder.Factory())
            }
            .memoryCachePolicy(coil.request.CachePolicy.ENABLED)
            .diskCachePolicy(coil.request.CachePolicy.ENABLED)
            .build()
    }

    val aspectRatio = remember(file.dimensions, file.id) {
        val parts = file.dimensions?.split("x")
        if (parts?.size == 2) {
            val width = parts[0].toFloatOrNull()
            val height = parts[1].toFloatOrNull()
            if (width != null && height != null && height > 0) {
                (width / height).coerceIn(0.56f, 1.8f)
            } else {
                if (file.id.hashCode() % 3 == 0) 0.65f else if (file.id.hashCode() % 3 == 1) 0.85f else 1.15f
            }
        } else {
            // Varied dynamic height for natural masonry rhythm
            when ((file.id.hashCode() and 0x7FFFFFFF) % 3) {
                0 -> 0.65f
                1 -> 0.85f
                else -> 1.1f
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f))
            .then(
                if (isSelected) Modifier.border(2.5.dp, MaterialTheme.colorScheme.primary, RoundedCornerShape(16.dp))
                else Modifier.border(0.5.dp, Color.White.copy(alpha = 0.08f), RoundedCornerShape(16.dp))
            )
            .pointerInput(Unit) {
                detectTapGestures(
                    onLongPress = {
                        currentOnHold()
                    },
                    onTap = {
                        if (currentIsSelectionMode) currentOnSelectToggle() else currentOnClick()
                    }
                )
            }
    ) {
        SubcomposeAsyncImage(
            model = ImageRequest.Builder(context)
                .data(file.uri)
                .crossfade(true)
                .build(),
            imageLoader = imageLoader,
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(aspectRatio),
            loading = {
                SkeletonBox(modifier = Modifier.fillMaxSize())
            }
        )
        
        if (isVideo) {
            // Subtle bottom gradient shadow
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .align(Alignment.BottomCenter)
                    .background(
                        androidx.compose.ui.graphics.Brush.verticalGradient(
                            colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.65f))
                        )
                    )
            )

            // Sleek frosted translucent play badge at center
            Box(
                modifier = Modifier
                    .align(Alignment.Center)
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(Color.Black.copy(alpha = 0.52f))
                    .border(1.dp, Color.White.copy(alpha = 0.25f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.PlayArrow,
                    contentDescription = "Play Video",
                    tint = Color.White,
                    modifier = Modifier.size(24.dp)
                )
            }

            // File size or time badge bottom start
            val sizeStr = remember(file.sizeBytes) {
                android.text.format.Formatter.formatFileSize(context, file.sizeBytes)
            }
            Text(
                text = sizeStr,
                color = Color.White,
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(horizontal = 10.dp, vertical = 6.dp)
            )
        }
        
        if (isSelectionMode) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(8.dp)
                    .size(24.dp)
                    .clip(CircleShape)
                    .background(if (isSelected) MaterialTheme.colorScheme.primary else Color.Black.copy(alpha = 0.45f))
                    .border(1.5.dp, Color.White, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                if (isSelected) {
                    Text(text = "$selectionIndex", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun AudioListItem(file: FileItem, onClick: () -> Unit) {
    val context = LocalContext.current
    val sizeStr = android.text.format.Formatter.formatFileSize(context, file.sizeBytes)
    androidx.compose.material3.ListItem(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(horizontal = 8.dp, vertical = 2.dp),
        leadingContent = {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.MusicNote,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
            }
        },
        headlineContent = {
            Text(
                text = file.name,
                color = MaterialTheme.colorScheme.onBackground,
                maxLines = 1,
                overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
            )
        },
        supportingContent = {
            Text(text = sizeStr, color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f), fontSize = 12.sp)
        },
        trailingContent = {
            Icon(
                Icons.Default.PlayCircleOutline,
                contentDescription = "Play",
                tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f),
                modifier = Modifier.size(28.dp)
            )
        },
        colors = androidx.compose.material3.ListItemDefaults.colors(containerColor = androidx.compose.ui.graphics.Color.Transparent)
    )
}

@Composable
fun DocumentListItem(
    file: FileItem,
    isSelected: Boolean = false,
    isSelectionMode: Boolean = false,
    onClick: () -> Unit,
    onLongClick: () -> Unit = {}
) {
    val context = LocalContext.current
    val sizeStr = android.text.format.Formatter.formatFileSize(context, file.sizeBytes)
    val ext = file.extension.lowercase()

    val (iconBg, iconTint, badgeText) = when {
        ext == "pdf" -> Triple(Color(0xFFFFEBEE), Color(0xFFD32F2F), "PDF")
        ext in listOf("doc", "docx") -> Triple(Color(0xFFE3F2FD), Color(0xFF1976D2), "DOC")
        ext in listOf("xls", "xlsx", "csv") -> Triple(Color(0xFFE8F5E9), Color(0xFF388E3C), "XLS")
        ext in listOf("ppt", "pptx", "key") -> Triple(Color(0xFFFBE9E7), Color(0xFFD84315), "PPT")
        ext in listOf("txt", "log") -> Triple(Color(0xFFECEFF1), Color(0xFF455A64), "TXT")
        ext in listOf("zip", "rar", "7z", "tar", "gz") -> Triple(Color(0xFFEDE7F6), Color(0xFF512DA8), "ZIP")
        ext in listOf("kt", "java", "py", "js", "ts", "json", "xml", "html", "css", "cpp", "c", "sh") -> Triple(Color(0xFFE0F7FA), Color(0xFF00838F), "CODE")
        ext == "epub" -> Triple(Color(0xFFF3E5F5), Color(0xFF7B1FA2), "EPUB")
        else -> Triple(Color(0xFFE0F7FA), Color(0xFF0097A7), ext.take(4).uppercase())
    }

    androidx.compose.material3.ListItem(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .clickable { onClick() }
            .padding(horizontal = 8.dp, vertical = 2.dp),
        leadingContent = {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(iconBg)
                    .border(1.dp, iconTint.copy(alpha = 0.3f), RoundedCornerShape(10.dp)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = badgeText,
                    color = iconTint,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp
                )
            }
        },
        headlineContent = {
            Text(
                text = file.name,
                color = MaterialTheme.colorScheme.onBackground,
                fontWeight = FontWeight.Medium,
                maxLines = 1,
                overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
            )
        },
        supportingContent = {
            val dateStr = java.text.SimpleDateFormat("MMM dd, yyyy", java.util.Locale.getDefault()).format(java.util.Date(file.lastModified))
            Text(text = "$sizeStr • $dateStr", color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f), fontSize = 12.sp)
        },
        trailingContent = {
            if (isSelectionMode) {
                androidx.compose.material3.Checkbox(
                    checked = isSelected,
                    onCheckedChange = { onClick() },
                    colors = androidx.compose.material3.CheckboxDefaults.colors(checkedColor = MaterialTheme.colorScheme.primary)
                )
            } else {
                Icon(
                    Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.2f),
                    modifier = Modifier.size(16.dp).graphicsLayer { rotationZ = 180f }
                )
            }
        },
        colors = androidx.compose.material3.ListItemDefaults.colors(containerColor = androidx.compose.ui.graphics.Color.Transparent)
    )
}

@Composable
fun SkeletonBox(modifier: Modifier = Modifier) {
    val infiniteTransition = rememberInfiniteTransition(label = "skeleton")
    val alpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 0.7f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "alpha"
    )
    Box(
        modifier = modifier
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = alpha))
    )
}

@Composable
fun SelectionAction(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    tint: Color = MaterialTheme.colorScheme.onSurfaceVariant,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.clickable { onClick() }
    ) {
        Icon(icon, contentDescription = label, tint = tint, modifier = Modifier.size(24.dp))
        Spacer(modifier = Modifier.height(2.dp))
        Text(label, color = tint, fontSize = 10.sp)
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ImageViewerOverlay(
    mediaFiles: List<FileItem>,
    initialIndex: Int,
    onDismiss: () -> Unit,
    onEdit: (FileItem) -> Unit,
    onDeleteClick: (FileItem) -> Unit,
    onRenameClick: (FileItem, String) -> Unit,
    onCopy: (FileItem) -> Unit,
    onMove: (FileItem) -> Unit,
    onCompress: (FileItem, android.net.Uri) -> Unit,
    onSafeFolderClick: (FileItem) -> Unit
) {
    androidx.activity.compose.BackHandler(onBack = onDismiss)
    
    var showUi by remember { mutableStateOf(true) }
    var showInfoSheet by remember { mutableStateOf(false) }
    var showEditDialog by remember { mutableStateOf(false) }
    var showDetailsDialog by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val sharedPrefs = remember { context.getSharedPreferences("media_favorites", Context.MODE_PRIVATE) }
    
    val view = androidx.compose.ui.platform.LocalView.current
    val window = (context as? android.app.Activity)?.window
    
    DisposableEffect(window, view) {
        if (window != null) {
            val insetsController = androidx.core.view.WindowCompat.getInsetsController(window, view)
            insetsController.systemBarsBehavior = androidx.core.view.WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            insetsController.hide(androidx.core.view.WindowInsetsCompat.Type.systemBars())
        }
        onDispose {
            if (window != null) {
                val insetsController = androidx.core.view.WindowCompat.getInsetsController(window, view)
                insetsController.show(androidx.core.view.WindowInsetsCompat.Type.systemBars())
            }
        }
    }

    LaunchedEffect(showUi) {
        if (showUi) {
            delay(3000)
            showUi = false
        }
    }

    val pagerState = rememberPagerState(initialPage = initialIndex) { mediaFiles.size }
    val scaleMap = remember { androidx.compose.runtime.mutableStateMapOf<Int, Float>() }
    val currentScale = scaleMap[pagerState.currentPage] ?: 1f

    // 5x Faster Pre-fetching of adjacent images
    LaunchedEffect(pagerState.currentPage, mediaFiles) {
        val imageLoader = coil.Coil.imageLoader(context)
        val prevIdx = pagerState.currentPage - 1
        val nextIdx = pagerState.currentPage + 1
        if (prevIdx >= 0) {
            val req = coil.request.ImageRequest.Builder(context).data(mediaFiles[prevIdx].uri).build()
            imageLoader.enqueue(req)
        }
        if (nextIdx < mediaFiles.size) {
            val req = coil.request.ImageRequest.Builder(context).data(mediaFiles[nextIdx].uri).build()
            imageLoader.enqueue(req)
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .pointerInput(Unit) { detectTapGestures { } } // Block touches from passing through
    ) {
        HorizontalPager(
            state = pagerState,
            userScrollEnabled = currentScale <= 1.05f,
            modifier = Modifier.fillMaxSize()
        ) { page ->
            val file = mediaFiles[page]
            val isVideo = file.mimeType?.startsWith("video/") == true || file.extension.lowercase() in listOf("mp4", "mkv", "avi", "mov", "3gp", "webm", "ts", "flv", "wmv", "m4v")
            
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                if (isVideo) {
                    val coroutineScope = rememberCoroutineScope()
                    VideoPlayer(
                        uri = file.uri,
                        modifier = Modifier.fillMaxSize(),
                        fileName = file.name,
                        isActive = (pagerState.currentPage == page),
                        onDismiss = onDismiss,
                        onScaleChanged = { s -> scaleMap[page] = s },
                        onNext = if (page < mediaFiles.size - 1) {
                            {
                                coroutineScope.launch {
                                    pagerState.animateScrollToPage(page + 1)
                                }
                            }
                        } else null,
                        onPrevious = if (page > 0) {
                            {
                                coroutineScope.launch {
                                    pagerState.animateScrollToPage(page - 1)
                                }
                            }
                        } else null
                    )
                } else {
                    ZoomableImage(
                        model = file.uri,
                        scaleMap = scaleMap,
                        page = page,
                        modifier = Modifier.fillMaxSize(),
                        onTap = { showUi = !showUi }
                    )
                }
            }
        }
            
        val isCurrentVideo = mediaFiles.getOrNull(pagerState.currentPage)?.let {
            it.mimeType?.startsWith("video/") == true || it.extension.lowercase() in listOf("mp4", "mkv", "avi", "mov", "3gp", "webm", "ts", "flv", "wmv", "m4v")
        } ?: false

        // Top Bar — always white icons on a dark scrim pill for readability over any photo
        AnimatedVisibility(
            visible = showUi && !isCurrentVideo,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier
                .align(Alignment.TopStart)
                .fillMaxWidth()
                .padding(top = 8.dp, start = 8.dp, end = 8.dp)
        ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Left pill: close + info
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(50))
                            .background(Color.Black.copy(alpha = 0.45f))
                            .padding(horizontal = 4.dp),
                        horizontalArrangement = Arrangement.spacedBy(0.dp)
                    ) {
                        IconButton(onClick = onDismiss) {
                            Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.White)
                        }
                        IconButton(onClick = { showInfoSheet = true }) {
                            Icon(Icons.Default.Info, contentDescription = "Info", tint = Color.White)
                        }
                    }
                    // Right pill: cast
                    Box(
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(Color.Black.copy(alpha = 0.45f))
                    ) {
                        IconButton(onClick = { /* TODO: Cast */ }) {
                            Icon(Icons.Default.Tv, contentDescription = "Cast", tint = Color.White)
                        }
                    }
                }
            }

            // Bottom Navigation Bar
            AnimatedVisibility(
                visible = showUi && !isCurrentVideo,
                enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
                exit = slideOutVertically(targetOffsetY = { it }) + fadeOut(),
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 48.dp)
            ) {
                if (mediaFiles.isNotEmpty()) {
                    val currentFile = mediaFiles[pagerState.currentPage]
                    var isFavorite by remember(currentFile.uri) { 
                        mutableStateOf(sharedPrefs.getBoolean(currentFile.uri.toString(), false)) 
                    }
                    
                    Box(
                        modifier = Modifier
                            .wrapContentWidth()
                            .height(45.dp)
                            .clip(RoundedCornerShape(22.dp))
                            .background(Color.Black.copy(alpha = 0.55f))
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 16.dp).fillMaxHeight(),
                            horizontalArrangement = Arrangement.spacedBy(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            IconButton(
                                onClick = { 
                                    val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                        type = currentFile.mimeType ?: "image/*"
                                        putExtra(Intent.EXTRA_STREAM, currentFile.uri)
                                    }
                                    context.startActivity(Intent.createChooser(shareIntent, "Share Media"))
                                },
                                modifier = Modifier.size(36.dp)
                            ) {
                                Icon(Icons.Outlined.Share, contentDescription = "Share", tint = Color.White, modifier = Modifier.size(20.dp))
                            }
                            IconButton(
                                onClick = { 
                                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                    val clip = ClipData.newUri(context.contentResolver, "Media", currentFile.uri)
                                    clipboard.setPrimaryClip(clip)
                                },
                                modifier = Modifier.size(36.dp)
                            ) {
                                Icon(Icons.Outlined.ContentCopy, contentDescription = "Copy", tint = Color.White, modifier = Modifier.size(20.dp))
                            }
                            IconButton(
                                onClick = { 
                                    isFavorite = !isFavorite
                                    sharedPrefs.edit().putBoolean(currentFile.uri.toString(), isFavorite).apply()
                                },
                                modifier = Modifier.size(36.dp)
                            ) {
                                Icon(
                                    if (isFavorite) Icons.Default.Favorite else Icons.Outlined.FavoriteBorder, 
                                    contentDescription = "Favorite", 
                                    tint = if (isFavorite) Color.Red else Color.White,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            IconButton(
                                onClick = { onEdit(currentFile) },
                                modifier = Modifier.size(36.dp)
                            ) {
                                Icon(Icons.Outlined.Edit, contentDescription = "Edit", tint = Color.White, modifier = Modifier.size(20.dp))
                            }
                            IconButton(
                                onClick = { 
                                    onDeleteClick(currentFile)
                                },
                                modifier = Modifier.size(36.dp)
                            ) {
                                Icon(Icons.Outlined.Delete, contentDescription = "Delete", tint = Color.White, modifier = Modifier.size(20.dp))
                            }
                        }
                    }
                }
            }
        }
        
        if (showInfoSheet && mediaFiles.isNotEmpty()) {
            val currentFile = mediaFiles[pagerState.currentPage]
            com.fyllo.filemanager.ui.components.FileOptionsBottomSheet(
                fileName = currentFile.name,
                onDismiss = { showInfoSheet = false },
                onDeleteClick = {
                    onDeleteClick(currentFile)
                    showInfoSheet = false
                },
                onRenameClick = {
                    showInfoSheet = false
                    showEditDialog = true
                },
                onMoveClick = {
                    onMove(currentFile)
                    showInfoSheet = false
                },
                onCopyClick = {
                    onCopy(currentFile)
                    showInfoSheet = false
                },
                onCompressClick = {
                    onCompress(currentFile, android.net.Uri.fromFile(android.os.Environment.getExternalStorageDirectory()))
                    showInfoSheet = false
                },
                onShareClick = {
                    val shareIntent = Intent(Intent.ACTION_SEND).apply {
                        type = currentFile.mimeType ?: "image/*"
                        putExtra(Intent.EXTRA_STREAM, currentFile.uri)
                    }
                    context.startActivity(Intent.createChooser(shareIntent, "Share Media"))
                    showInfoSheet = false
                },
                onDetailsClick = {
                    showDetailsDialog = true
                    showInfoSheet = false
                },
                onFavoriteClick = {
                    val isFavorite = sharedPrefs.getBoolean(currentFile.uri.toString(), false)
                    sharedPrefs.edit().putBoolean(currentFile.uri.toString(), !isFavorite).apply()
                    showInfoSheet = false
                },
                onSafeFolderClick = {
                    onSafeFolderClick(currentFile)
                    showInfoSheet = false
                }
            )
        }

        if (showDetailsDialog && mediaFiles.isNotEmpty()) {
            com.fyllo.filemanager.ui.components.DetailsDialog(
                file = mediaFiles[pagerState.currentPage],
                onDismiss = { showDetailsDialog = false }
            )
        }

        if (showEditDialog && mediaFiles.isNotEmpty()) {
            EditMetadataDialog(
                file = mediaFiles[pagerState.currentPage],
                onDismiss = { showEditDialog = false },
                onConfirm = { newName, _ -> 
                    onRenameClick(mediaFiles[pagerState.currentPage], newName) 
                    showEditDialog = false
                }
            )
        }
    }

@Composable
fun ZoomControlsBar(
    currentScale: Float,
    onSelectScale: (Float) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(20.dp))
            .background(Color.Black.copy(alpha = 0.65f))
            .border(1.dp, Color.White.copy(alpha = 0.15f), RoundedCornerShape(20.dp))
            .padding(horizontal = 4.dp, vertical = 3.dp),
        horizontalArrangement = Arrangement.spacedBy(3.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        listOf(1f, 2f, 3f, 4f).forEach { preset ->
            val isSelected = abs(currentScale - preset) < 0.25f
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(16.dp))
                    .background(if (isSelected) NeonCyan else Color.Transparent)
                    .clickable { onSelectScale(preset) }
                    .padding(horizontal = 9.dp, vertical = 4.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "${preset.toInt()}x",
                    color = if (isSelected) Color.Black else Color.White,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                    fontSize = 12.sp
                )
            }
        }
    }
}

@Composable
fun ZoomableImage(
    model: Any?, 
    scaleMap: androidx.compose.runtime.snapshots.SnapshotStateMap<Int, Float>,
    page: Int,
    modifier: Modifier = Modifier, 
    onTap: () -> Unit = {}
) {
    var scale by remember(page) { mutableStateOf(1f) }
    var offset by remember(page) { mutableStateOf(Offset.Zero) }
    var containerSize by remember { mutableStateOf(IntSize(1080, 1920)) }
    val coroutineScope = rememberCoroutineScope()
    var showZoomHud by remember { mutableStateOf(false) }
    var zoomHudHideJob by remember { mutableStateOf<kotlinx.coroutines.Job?>(null) }
    var lastTapTime by remember { mutableStateOf(0L) }
    var lastTapPos by remember { mutableStateOf(Offset.Zero) }
    var singleTapJob by remember { mutableStateOf<kotlinx.coroutines.Job?>(null) }

    fun showZoomHudTemporarily() {
        showZoomHud = true
        zoomHudHideJob?.cancel()
        zoomHudHideJob = coroutineScope.launch {
            delay(1500)
            showZoomHud = false
        }
    }

    LaunchedEffect(page, scale) {
        scaleMap[page] = scale
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .onSizeChanged { containerSize = it }
            .pointerInput(Unit) {
                awaitEachGesture {
                    val down = awaitFirstDown(requireUnconsumed = false, pass = PointerEventPass.Initial)
                    val startPos = down.position
                    val startTime = System.currentTimeMillis()
                    val width = size.width.toFloat().coerceAtLeast(1f)
                    val height = size.height.toFloat().coerceAtLeast(1f)
                    var isMultiTouch = false
                    var lastPointerPos = startPos

                    while (true) {
                        val event = awaitPointerEvent(pass = PointerEventPass.Initial)

                        if (event.changes.size > 1) {
                            isMultiTouch = true
                            val p0 = event.changes[0]
                            val p1 = event.changes[1]
                            val prevDist = (p0.previousPosition - p1.previousPosition).getDistance()
                            val currentDist = (p0.position - p1.position).getDistance()
                            val panDelta = ((p0.position + p1.position) / 2f) - ((p0.previousPosition + p1.previousPosition) / 2f)

                            if (prevDist > 0f) {
                                val zoomFactor = currentDist / prevDist
                                val newScale = (scale * zoomFactor).coerceIn(1f, 5f)
                                scale = newScale
                                scaleMap[page] = newScale
                                showZoomHudTemporarily()

                                if (newScale > 1.01f) {
                                    val maxOffsetX = (width * (newScale - 1f)) / 2f
                                    val maxOffsetY = (height * (newScale - 1f)) / 2f
                                    offset = Offset(
                                        (offset.x + panDelta.x).coerceIn(-maxOffsetX, maxOffsetX),
                                        (offset.y + panDelta.y).coerceIn(-maxOffsetY, maxOffsetY)
                                    )
                                } else {
                                    offset = Offset.Zero
                                }
                            }
                            p0.consume()
                            p1.consume()
                            if (event.changes.none { it.pressed }) break
                            continue
                        }

                        val change = event.changes.firstOrNull { it.id == down.id } ?: break
                        if (!change.pressed) break
                        lastPointerPos = change.position

                        if (scale > 1.05f) {
                            change.consume()
                            val moveDeltaX = change.position.x - change.previousPosition.x
                            val moveDeltaY = change.position.y - change.previousPosition.y
                            val maxOffsetX = (width * (scale - 1f)) / 2f
                            val maxOffsetY = (height * (scale - 1f)) / 2f
                            offset = Offset(
                                (offset.x + moveDeltaX).coerceIn(-maxOffsetX, maxOffsetX),
                                (offset.y + moveDeltaY).coerceIn(-maxOffsetY, maxOffsetY)
                            )
                        }
                    }

                    if (isMultiTouch) {
                        if (scale < 1.05f) {
                            scale = 1f
                            offset = Offset.Zero
                            scaleMap[page] = 1f
                        }
                    } else {
                        val elapsed = System.currentTimeMillis() - startTime
                        val moveDist = (lastPointerPos - startPos).getDistance()
                        if (elapsed < 300 && moveDist < 25f) {
                            val now = System.currentTimeMillis()
                            if (now - lastTapTime < 350 && (startPos - lastTapPos).getDistance() < 80f) {
                                singleTapJob?.cancel()
                                lastTapTime = 0L
                                if (scale > 1.05f) {
                                    scale = 1f
                                    offset = Offset.Zero
                                    scaleMap[page] = 1f
                                } else {
                                    scale = 2.5f
                                    offset = Offset.Zero
                                    scaleMap[page] = 2.5f
                                }
                                showZoomHudTemporarily()
                            } else {
                                lastTapTime = now
                                lastTapPos = startPos
                                singleTapJob?.cancel()
                                singleTapJob = coroutineScope.launch {
                                    delay(300)
                                    onTap()
                                }
                            }
                        }
                    }
                }
            },
        contentAlignment = Alignment.Center
    ) {
        AsyncImage(
            model = model,
            contentDescription = null,
            contentScale = ContentScale.Fit,
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer(
                    scaleX = scale,
                    scaleY = scale,
                    translationX = offset.x,
                    translationY = offset.y
                )
        )

        // Live Zoom Badge (Top Center)
        AnimatedVisibility(
            visible = showZoomHud,
            enter = fadeIn(animationSpec = androidx.compose.animation.core.tween(150)),
            exit = fadeOut(animationSpec = androidx.compose.animation.core.tween(300)),
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 80.dp)
        ) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color.Black.copy(alpha = 0.75f))
                    .border(1.dp, NeonCyan.copy(alpha = 0.4f), RoundedCornerShape(16.dp))
                    .padding(horizontal = 14.dp, vertical = 6.dp)
            ) {
                Text(
                    text = String.format(java.util.Locale.US, "%.1fx", scale),
                    color = NeonCyan,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
            }
        }

        // 1x, 2x, 3x, 4x Zoom Preset Bar
        if (scale > 1.02f) {
            ZoomControlsBar(
                currentScale = scale,
                onSelectScale = { targetScale ->
                    scale = targetScale
                    scaleMap[page] = targetScale
                    if (targetScale <= 1f) {
                        offset = Offset.Zero
                    } else {
                        val maxOffsetX = (containerSize.width.toFloat() * (targetScale - 1f)) / 2f
                        val maxOffsetY = (containerSize.height.toFloat() * (targetScale - 1f)) / 2f
                        offset = Offset(
                            offset.x.coerceIn(-maxOffsetX, maxOffsetX),
                            offset.y.coerceIn(-maxOffsetY, maxOffsetY)
                        )
                    }
                    showZoomHudTemporarily()
                },
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(top = 70.dp, end = 16.dp)
            )
        }
    }
}

private fun Context.findActivity(): Activity? {
    var ctx = this
    while (ctx is ContextWrapper) {
        if (ctx is Activity) return ctx
        ctx = ctx.baseContext
    }
    return null
}

@androidx.annotation.OptIn(androidx.media3.common.util.UnstableApi::class)
@Composable
fun VideoPlayer(
    uri: Uri,
    modifier: Modifier = Modifier,
    fileName: String = "Video",
    isActive: Boolean = true,
    onDismiss: (() -> Unit)? = null,
    onScaleChanged: ((Float) -> Unit)? = null,
    onNext: (() -> Unit)? = null,
    onPrevious: (() -> Unit)? = null
) {
    val context = LocalContext.current
    val activity = remember(context) { context.findActivity() }
    val audioManager = remember(context) { context.getSystemService(Context.AUDIO_SERVICE) as? android.media.AudioManager }

    val audioAttributes = remember {
        AudioAttributes.Builder()
            .setUsage(C.USAGE_MEDIA)
            .setContentType(C.AUDIO_CONTENT_TYPE_MOVIE)
            .build()
    }

    val exoPlayer = remember(uri) {
        ExoPlayer.Builder(context)
            .setAudioAttributes(audioAttributes, true)
            .build().apply {
                setMediaItem(MediaItem.fromUri(uri))
                prepare()
                playWhenReady = isActive
            }
    }

    var isPlaying by remember { mutableStateOf(isActive) }
    var userManuallyPaused by remember { mutableStateOf(false) }
    var currentPositionMs by remember { mutableStateOf(0L) }
    var durationMs by remember { mutableStateOf(0L) }
    var isControlsVisible by remember { mutableStateOf(true) }
    var isLocked by remember { mutableStateOf(false) }

    var scale by remember { mutableStateOf(1f) }
    var offset by remember { mutableStateOf(Offset.Zero) }

    // Synchronize playback state with active page visibility in HorizontalPager
    LaunchedEffect(isActive) {
        if (isActive) {
            if (!userManuallyPaused) {
                if (exoPlayer.playbackState == androidx.media3.common.Player.STATE_ENDED) {
                    exoPlayer.seekTo(0)
                    currentPositionMs = 0L
                }
                exoPlayer.playWhenReady = true
            }
        } else {
            exoPlayer.pause()
            exoPlayer.playWhenReady = false
        }
    }

    // Android Lifecycle handling: Pause when app backgrounded/screen locked, resume only if active
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner, exoPlayer, isActive, userManuallyPaused) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_PAUSE,
                Lifecycle.Event.ON_STOP -> {
                    exoPlayer.pause()
                    exoPlayer.playWhenReady = false
                }
                Lifecycle.Event.ON_RESUME -> {
                    if (isActive && !userManuallyPaused) {
                        exoPlayer.playWhenReady = true
                    }
                }
                else -> {}
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    // Gesture State: Brightness & Volume HUD
    fun getCurrentBrightness(): Float {
        val cur = activity?.window?.attributes?.screenBrightness ?: -1f
        return if (cur >= 0f) cur else {
            try {
                android.provider.Settings.System.getInt(
                    context.contentResolver,
                    android.provider.Settings.System.SCREEN_BRIGHTNESS,
                    128
                ) / 255f
            } catch (_: Exception) { 0.5f }
        }
    }

    var brightnessLevel by remember { mutableStateOf(getCurrentBrightness()) }
    var showBrightnessOverlay by remember { mutableStateOf(false) }

    val maxVolume = remember(audioManager) {
        val max = audioManager?.getStreamMaxVolume(android.media.AudioManager.STREAM_MUSIC) ?: 15
        if (max <= 0) 15 else max
    }

    fun getCurrentVolume(): Int {
        return audioManager?.getStreamVolume(android.media.AudioManager.STREAM_MUSIC) ?: 7
    }

    var currentVolume by remember { mutableStateOf(getCurrentVolume()) }
    var showVolumeOverlay by remember { mutableStateOf(false) }

    val coroutineScope = rememberCoroutineScope()
    var brightnessHideJob by remember { mutableStateOf<kotlinx.coroutines.Job?>(null) }
    var volumeHideJob by remember { mutableStateOf<kotlinx.coroutines.Job?>(null) }
    var containerSize by remember { mutableStateOf(IntSize(1080, 1920)) }
    var showZoomHud by remember { mutableStateOf(false) }
    var zoomHudHideJob by remember { mutableStateOf<kotlinx.coroutines.Job?>(null) }
    var singleTapJob by remember { mutableStateOf<kotlinx.coroutines.Job?>(null) }
    var lastTapTime by remember { mutableStateOf(0L) }
    var lastTapPos by remember { mutableStateOf(Offset.Zero) }

    fun showZoomHudTemporarily() {
        showZoomHud = true
        zoomHudHideJob?.cancel()
        zoomHudHideJob = coroutineScope.launch {
            delay(1500)
            showZoomHud = false
        }
    }

    fun showBrightnessHud(newBrightness: Float) {
        val clamped = newBrightness.coerceIn(0.01f, 1f)
        brightnessLevel = clamped
        activity?.let { act ->
            val lp = act.window.attributes
            lp.screenBrightness = clamped
            act.window.attributes = lp
        }
        showBrightnessOverlay = true
        showVolumeOverlay = false
        brightnessHideJob?.cancel()
        volumeHideJob?.cancel()
    }

    fun showVolumeHud(newVol: Int) {
        val clampedVol = newVol.coerceIn(0, maxVolume)
        currentVolume = clampedVol
        try {
            audioManager?.setStreamVolume(android.media.AudioManager.STREAM_MUSIC, clampedVol, 0)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        showVolumeOverlay = true
        showBrightnessOverlay = false
        volumeHideJob?.cancel()
        brightnessHideJob?.cancel()
    }

    fun scheduleHudDismiss() {
        if (showBrightnessOverlay) {
            brightnessHideJob?.cancel()
            brightnessHideJob = coroutineScope.launch {
                delay(1200)
                showBrightnessOverlay = false
            }
        }
        if (showVolumeOverlay) {
            volumeHideJob?.cancel()
            volumeHideJob = coroutineScope.launch {
                delay(1200)
                showVolumeOverlay = false
            }
        }
    }

    // Auto-hide controls timer
    LaunchedEffect(isControlsVisible, isPlaying) {
        if (isControlsVisible && isPlaying) {
            delay(3500)
            isControlsVisible = false
        }
    }

    // Track playback time and state
    LaunchedEffect(exoPlayer) {
        while (true) {
            currentPositionMs = exoPlayer.currentPosition.coerceAtLeast(0L)
            durationMs = exoPlayer.duration.takeIf { it > 0 } ?: 0L
            isPlaying = exoPlayer.isPlaying
            delay(250)
        }
    }

    DisposableEffect(exoPlayer) {
        val listener = object : androidx.media3.common.Player.Listener {
            override fun onIsPlayingChanged(playing: Boolean) {
                isPlaying = playing
            }
            override fun onPlaybackStateChanged(playbackState: Int) {
                if (playbackState == androidx.media3.common.Player.STATE_ENDED) {
                    isPlaying = false
                    isControlsVisible = true
                }
            }
        }
        exoPlayer.addListener(listener)
        onDispose {
            exoPlayer.removeListener(listener)
            exoPlayer.stop()
            exoPlayer.release()
        }
    }

    fun formatTime(ms: Long): String {
        val totalSec = (ms / 1000).coerceAtLeast(0)
        val minutes = totalSec / 60
        val seconds = totalSec % 60
        val hours = minutes / 60
        return if (hours > 0) {
            String.format("%02d:%02d:%02d", hours, minutes % 60, seconds)
        } else {
            String.format("%02d:%02d", minutes, seconds)
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black)
            .onSizeChanged { containerSize = it }
            .pointerInput(isLocked) {
                awaitEachGesture {
                    val down = awaitFirstDown(requireUnconsumed = false, pass = PointerEventPass.Initial)
                    val startPos = down.position
                    val startTime = System.currentTimeMillis()
                    val width = size.width.toFloat().coerceAtLeast(1f)
                    val height = size.height.toFloat().coerceAtLeast(1f)
                    val isLeft = startPos.x < width * 0.5f

                    if (isLocked) {
                        var moved = false
                        while (true) {
                            val event = awaitPointerEvent(pass = PointerEventPass.Initial)
                            val change = event.changes.firstOrNull { it.id == down.id } ?: break
                            if ((change.position - startPos).getDistance() > 20f) moved = true
                            if (!change.pressed) {
                                if (!moved && System.currentTimeMillis() - startTime < 300) {
                                    isControlsVisible = !isControlsVisible
                                }
                                break
                            }
                        }
                        return@awaitEachGesture
                    }

                    var isDragDetected = false
                    var isMultiTouch = false
                    var dragMode = 0 // 1: Brightness, 2: Volume, 3: Pan
                    var initialBrightness = brightnessLevel
                    var initialVolume = currentVolume
                    var lastPointerPos = startPos

                    while (true) {
                        val event = awaitPointerEvent(pass = PointerEventPass.Initial)

                        // Multi-touch Pinch Zoom & Pan
                        if (event.changes.size > 1) {
                            isMultiTouch = true
                            brightnessHideJob?.cancel()
                            volumeHideJob?.cancel()
                            showBrightnessOverlay = false
                            showVolumeOverlay = false

                            val p0 = event.changes[0]
                            val p1 = event.changes[1]
                            val prevDist = (p0.previousPosition - p1.previousPosition).getDistance()
                            val currentDist = (p0.position - p1.position).getDistance()
                            val panDelta = ((p0.position + p1.position) / 2f) - ((p0.previousPosition + p1.previousPosition) / 2f)

                            if (prevDist > 0f) {
                                val zoomFactor = currentDist / prevDist
                                val newScale = (scale * zoomFactor).coerceIn(1f, 5f)
                                scale = newScale
                                onScaleChanged?.invoke(newScale)
                                showZoomHudTemporarily()

                                if (newScale > 1.01f) {
                                    val maxOffsetX = (width * (newScale - 1f)) / 2f
                                    val maxOffsetY = (height * (newScale - 1f)) / 2f
                                    offset = Offset(
                                        (offset.x + panDelta.x).coerceIn(-maxOffsetX, maxOffsetX),
                                        (offset.y + panDelta.y).coerceIn(-maxOffsetY, maxOffsetY)
                                    )
                                } else {
                                    offset = Offset.Zero
                                }
                            }
                            p0.consume()
                            p1.consume()
                            if (event.changes.none { it.pressed }) break
                            continue
                        }

                        val change = event.changes.firstOrNull { it.id == down.id } ?: break
                        if (!change.pressed) {
                            break
                        }
                        lastPointerPos = change.position

                        val deltaFromStart = change.position - startPos
                        val diffX = abs(deltaFromStart.x)
                        val diffY = abs(deltaFromStart.y)

                        if (!isDragDetected && !isMultiTouch) {
                            if (scale > 1.05f) {
                                if ((change.position - change.previousPosition).getDistance() > 1.5f) {
                                    isDragDetected = true
                                    dragMode = 3
                                }
                            } else {
                                if (diffY > 15f && diffY > diffX * 0.75f) {
                                    isDragDetected = true
                                    if (isLeft) {
                                        dragMode = 1
                                        initialBrightness = getCurrentBrightness()
                                        showBrightnessHud(initialBrightness)
                                    } else {
                                        dragMode = 2
                                        initialVolume = getCurrentVolume()
                                        showVolumeHud(initialVolume)
                                    }
                                }
                            }
                        }

                        if (isDragDetected) {
                            change.consume()
                            val totalDeltaY = -(change.position.y - startPos.y)
                            val moveDeltaX = change.position.x - change.previousPosition.x
                            val moveDeltaY = change.position.y - change.previousPosition.y

                            when (dragMode) {
                                1 -> {
                                    val deltaFraction = totalDeltaY / (height * 0.65f)
                                    val newBrightness = (initialBrightness + deltaFraction).coerceIn(0.01f, 1f)
                                    showBrightnessHud(newBrightness)
                                }
                                2 -> {
                                    val deltaFraction = totalDeltaY / (height * 0.65f)
                                    val newVol = (initialVolume + (deltaFraction * maxVolume)).roundToInt().coerceIn(0, maxVolume)
                                    showVolumeHud(newVol)
                                }
                                3 -> {
                                    val maxOffsetX = (width * (scale - 1f)) / 2f
                                    val maxOffsetY = (height * (scale - 1f)) / 2f
                                    offset = Offset(
                                        (offset.x + moveDeltaX).coerceIn(-maxOffsetX, maxOffsetX),
                                        (offset.y + moveDeltaY).coerceIn(-maxOffsetY, maxOffsetY)
                                    )
                                }
                            }
                        }
                    }

                    if (isDragDetected) {
                        scheduleHudDismiss()
                    } else if (isMultiTouch) {
                        if (scale < 1.05f) {
                            scale = 1f
                            offset = Offset.Zero
                            onScaleChanged?.invoke(1f)
                        }
                    } else {
                        val elapsed = System.currentTimeMillis() - startTime
                        val moveDist = (lastPointerPos - startPos).getDistance()
                        if (elapsed < 300 && moveDist < 25f) {
                            val now = System.currentTimeMillis()
                            if (now - lastTapTime < 350 && (startPos - lastTapPos).getDistance() < 80f) {
                                singleTapJob?.cancel()
                                lastTapTime = 0L
                                if (scale > 1.05f) {
                                    scale = 1f
                                    offset = Offset.Zero
                                    onScaleChanged?.invoke(1f)
                                    showZoomHudTemporarily()
                                } else {
                                    if (startPos.x < width * 0.35f) {
                                        val newPos = (exoPlayer.currentPosition - 10000).coerceAtLeast(0)
                                        exoPlayer.seekTo(newPos)
                                        currentPositionMs = newPos
                                    } else if (startPos.x > width * 0.65f) {
                                        val newPos = (exoPlayer.currentPosition + 10000).coerceAtMost(exoPlayer.duration)
                                        exoPlayer.seekTo(newPos)
                                        currentPositionMs = newPos
                                    } else {
                                        scale = 2f
                                        offset = Offset.Zero
                                        onScaleChanged?.invoke(2f)
                                        showZoomHudTemporarily()
                                    }
                                }
                            } else {
                                lastTapTime = now
                                lastTapPos = startPos
                                singleTapJob?.cancel()
                                singleTapJob = coroutineScope.launch {
                                    delay(300)
                                    isControlsVisible = !isControlsVisible
                                }
                            }
                        }
                    }
                }
            }
    ) {
        // Video Surface with direct, lag-free Pinch Zoom & Pan
        AndroidView(
            factory = { ctx ->
                PlayerView(ctx).apply {
                    player = exoPlayer
                    useController = false
                    keepScreenOn = true
                    setShutterBackgroundColor(android.graphics.Color.TRANSPARENT)
                    layoutParams = FrameLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT
                    )
                }
            },
            update = { playerView ->
                if (playerView.player != exoPlayer) {
                    playerView.player = exoPlayer
                }
                playerView.keepScreenOn = isPlaying
            },
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer(
                    scaleX = scale,
                    scaleY = scale,
                    translationX = offset.x,
                    translationY = offset.y
                )
        )

        // Live Zoom Badge (Top Center)
        AnimatedVisibility(
            visible = showZoomHud,
            enter = fadeIn(animationSpec = androidx.compose.animation.core.tween(150)),
            exit = fadeOut(animationSpec = androidx.compose.animation.core.tween(300)),
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 80.dp)
        ) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color.Black.copy(alpha = 0.75f))
                    .border(1.dp, NeonCyan.copy(alpha = 0.4f), RoundedCornerShape(16.dp))
                    .padding(horizontal = 14.dp, vertical = 6.dp)
            ) {
                Text(
                    text = String.format(java.util.Locale.US, "%.1fx", scale),
                    color = NeonCyan,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
            }
        }

        // 1x, 2x, 3x, 4x Zoom Preset Bar (Visible when zoomed in OR controls visible)
        AnimatedVisibility(
            visible = scale > 1.02f || isControlsVisible,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(top = 70.dp, end = 16.dp)
        ) {
            ZoomControlsBar(
                currentScale = scale,
                onSelectScale = { targetScale ->
                    scale = targetScale
                    onScaleChanged?.invoke(targetScale)
                    if (targetScale <= 1f) {
                        offset = Offset.Zero
                    } else {
                        val maxOffsetX = (containerSize.width.toFloat() * (targetScale - 1f)) / 2f
                        val maxOffsetY = (containerSize.height.toFloat() * (targetScale - 1f)) / 2f
                        offset = Offset(
                            offset.x.coerceIn(-maxOffsetX, maxOffsetX),
                            offset.y.coerceIn(-maxOffsetY, maxOffsetY)
                        )
                    }
                    showZoomHudTemporarily()
                }
            )
        }

        // Custom Overlay UI
        AnimatedVisibility(
            visible = isControlsVisible,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier.fillMaxSize()
        ) {
            Box(modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.35f))) {
                // Top Action Bar
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.TopCenter)
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(onClick = { onDismiss?.invoke() }) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = fileName,
                            color = Color.White,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold,
                            maxLines = 1,
                            overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
                            modifier = Modifier.widthIn(max = 220.dp)
                        )
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(onClick = {
                            val intent = Intent(Intent.ACTION_SEND).apply {
                                type = "video/*"
                                putExtra(Intent.EXTRA_STREAM, uri)
                            }
                            context.startActivity(Intent.createChooser(intent, "Share Video"))
                        }) {
                            Icon(Icons.Outlined.Share, contentDescription = "Share", tint = Color.White)
                        }
                    }
                }

                if (!isLocked) {
                    // Center Playback Controls
                    Row(
                        modifier = Modifier.align(Alignment.Center),
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Previous Video
                        if (onPrevious != null) {
                            IconButton(
                                onClick = { onPrevious.invoke() },
                                modifier = Modifier
                                    .size(46.dp)
                                    .clip(CircleShape)
                                    .background(Color.Black.copy(alpha = 0.45f))
                            ) {
                                Icon(
                                    Icons.Default.SkipPrevious,
                                    contentDescription = "Previous Video",
                                    tint = Color.White,
                                    modifier = Modifier.size(26.dp)
                                )
                            }
                        }

                        // Rewind 10s
                        IconButton(
                            onClick = {
                                val newPos = (exoPlayer.currentPosition - 10000).coerceAtLeast(0)
                                exoPlayer.seekTo(newPos)
                                currentPositionMs = newPos
                            },
                            modifier = Modifier
                                .size(50.dp)
                                .clip(CircleShape)
                                .background(Color.Black.copy(alpha = 0.45f))
                        ) {
                            Icon(
                                Icons.Default.Replay10,
                                contentDescription = "Rewind 10s",
                                tint = Color.White,
                                modifier = Modifier.size(28.dp)
                            )
                        }

                        // Play/Pause
                        IconButton(
                            onClick = {
                                if (exoPlayer.isPlaying) {
                                    userManuallyPaused = true
                                    exoPlayer.pause()
                                    exoPlayer.playWhenReady = false
                                } else {
                                    userManuallyPaused = false
                                    if (exoPlayer.playbackState == androidx.media3.common.Player.STATE_ENDED ||
                                        (durationMs > 0 && currentPositionMs >= durationMs - 500)) {
                                        exoPlayer.seekTo(0)
                                        currentPositionMs = 0L
                                    }
                                    exoPlayer.playWhenReady = true
                                    exoPlayer.play()
                                }
                            },
                            modifier = Modifier
                                .size(64.dp)
                                .clip(CircleShape)
                                .background(Color.Black.copy(alpha = 0.6f))
                        ) {
                            Icon(
                                if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                                contentDescription = if (isPlaying) "Pause" else "Play",
                                tint = Color.White,
                                modifier = Modifier.size(36.dp)
                            )
                        }

                        // Forward 10s
                        IconButton(
                            onClick = {
                                val newPos = (exoPlayer.currentPosition + 10000).coerceAtMost(exoPlayer.duration)
                                exoPlayer.seekTo(newPos)
                                currentPositionMs = newPos
                            },
                            modifier = Modifier
                                .size(50.dp)
                                .clip(CircleShape)
                                .background(Color.Black.copy(alpha = 0.45f))
                        ) {
                            Icon(
                                Icons.Default.Forward10,
                                contentDescription = "Forward 10s",
                                tint = Color.White,
                                modifier = Modifier.size(28.dp)
                            )
                        }

                        // Next Video
                        if (onNext != null) {
                            IconButton(
                                onClick = { onNext.invoke() },
                                modifier = Modifier
                                    .size(46.dp)
                                    .clip(CircleShape)
                                    .background(Color.Black.copy(alpha = 0.45f))
                            ) {
                                Icon(
                                    Icons.Default.SkipNext,
                                    contentDescription = "Next Video",
                                    tint = Color.White,
                                    modifier = Modifier.size(26.dp)
                                )
                            }
                        }
                    }
                }

                // Bottom Controls Scrim & Slider
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 16.dp, start = 16.dp, end = 16.dp)
                ) {
                    // Time row
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = formatTime(currentPositionMs),
                            color = Color.White,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = formatTime(durationMs),
                            color = Color.White.copy(alpha = 0.7f),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    // Progress Slider
                    androidx.compose.material3.Slider(
                        value = if (durationMs > 0) (currentPositionMs.toFloat() / durationMs).coerceIn(0f, 1f) else 0f,
                        onValueChange = { ratio ->
                            if (!isLocked && durationMs > 0) {
                                val target = (ratio * durationMs).toLong()
                                currentPositionMs = target
                                exoPlayer.seekTo(target)
                            }
                        },
                        colors = androidx.compose.material3.SliderDefaults.colors(
                            thumbColor = Color.White,
                            activeTrackColor = Color.White,
                            inactiveTrackColor = Color.White.copy(alpha = 0.3f)
                        ),
                        modifier = Modifier.fillMaxWidth().height(24.dp)
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Bottom pill controls: Lock, Brightness, Volume, Language
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Lock Button
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .clip(RoundedCornerShape(20.dp))
                                .clickable { isLocked = !isLocked }
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Icon(
                                if (isLocked) Icons.Default.Lock else Icons.Default.LockOpen,
                                contentDescription = "Lock Controls",
                                tint = if (isLocked) NeonCyan else Color.White,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = if (isLocked) "Locked" else "Lock",
                                color = Color.White,
                                fontSize = 12.sp
                            )
                        }

                        // Brightness slider
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 4.dp)
                        ) {
                            Icon(Icons.Outlined.BrightnessMedium, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            androidx.compose.material3.Slider(
                                value = brightnessLevel,
                                onValueChange = {
                                    if (!isLocked) {
                                        showBrightnessHud(it)
                                    }
                                },
                                modifier = Modifier.width(60.dp).height(20.dp),
                                colors = androidx.compose.material3.SliderDefaults.colors(
                                    thumbColor = Color.White,
                                    activeTrackColor = Color.White,
                                    inactiveTrackColor = Color.White.copy(alpha = 0.3f)
                                )
                            )
                        }

                        // Volume slider
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 4.dp)
                        ) {
                            Icon(Icons.AutoMirrored.Outlined.VolumeUp, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            androidx.compose.material3.Slider(
                                value = (currentVolume.toFloat() / maxVolume).coerceIn(0f, 1f),
                                onValueChange = { ratio ->
                                    if (!isLocked && audioManager != null) {
                                        val newV = (ratio * maxVolume).roundToInt()
                                        showVolumeHud(newV)
                                    }
                                },
                                modifier = Modifier.width(60.dp).height(20.dp),
                                colors = androidx.compose.material3.SliderDefaults.colors(
                                    thumbColor = Color.White,
                                    activeTrackColor = Color.White,
                                    inactiveTrackColor = Color.White.copy(alpha = 0.3f)
                                )
                            )
                        }

                        // Language / Audio Track Button
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .clip(RoundedCornerShape(20.dp))
                                .clickable {
                                    android.widget.Toast.makeText(context, "Track: Default Audio", android.widget.Toast.LENGTH_SHORT).show()
                                }
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Icon(Icons.Outlined.Language, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Language", color = Color.White, fontSize = 12.sp)
                        }
                    }
                }
            }
        }

        // Left Brightness Overlay HUD (Rendered on top of controls and video)
        AnimatedVisibility(
            visible = showBrightnessOverlay,
            enter = fadeIn(animationSpec = androidx.compose.animation.core.tween(150)),
            exit = fadeOut(animationSpec = androidx.compose.animation.core.tween(300)),
            modifier = Modifier.align(Alignment.CenterStart).padding(start = 28.dp)
        ) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .background(Color.Black.copy(alpha = 0.78f))
                    .border(1.dp, Color.White.copy(alpha = 0.2f), RoundedCornerShape(20.dp))
                    .padding(horizontal = 14.dp, vertical = 18.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        when {
                            brightnessLevel > 0.66f -> Icons.Outlined.BrightnessHigh
                            brightnessLevel > 0.33f -> Icons.Outlined.BrightnessMedium
                            else -> Icons.Outlined.BrightnessLow
                        },
                        contentDescription = "Brightness",
                        tint = Color(0xFFFFD54F),
                        modifier = Modifier.size(28.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Box(
                        modifier = Modifier
                            .width(6.dp)
                            .height(110.dp)
                            .clip(RoundedCornerShape(3.dp))
                            .background(Color.White.copy(alpha = 0.25f)),
                        contentAlignment = Alignment.BottomCenter
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .fillMaxHeight(brightnessLevel.coerceIn(0.01f, 1f))
                                .clip(RoundedCornerShape(3.dp))
                                .background(Color(0xFFFFD54F))
                        )
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "${(brightnessLevel * 100).toInt()}%",
                        color = Color.White,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        // Right Volume Overlay HUD (Rendered on top of controls and video)
        AnimatedVisibility(
            visible = showVolumeOverlay,
            enter = fadeIn(animationSpec = androidx.compose.animation.core.tween(150)),
            exit = fadeOut(animationSpec = androidx.compose.animation.core.tween(300)),
            modifier = Modifier.align(Alignment.CenterEnd).padding(end = 28.dp)
        ) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .background(Color.Black.copy(alpha = 0.78f))
                    .border(1.dp, NeonCyan.copy(alpha = 0.35f), RoundedCornerShape(20.dp))
                    .padding(horizontal = 14.dp, vertical = 18.dp),
                contentAlignment = Alignment.Center
            ) {
                val volFraction = (currentVolume.toFloat() / maxVolume).coerceIn(0f, 1f)
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        when {
                            currentVolume == 0 -> Icons.AutoMirrored.Outlined.VolumeOff
                            volFraction <= 0.5f -> Icons.AutoMirrored.Outlined.VolumeDown
                            else -> Icons.AutoMirrored.Outlined.VolumeUp
                        },
                        contentDescription = "Volume",
                        tint = if (currentVolume == 0) Color.Red.copy(alpha = 0.9f) else NeonCyan,
                        modifier = Modifier.size(28.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Box(
                        modifier = Modifier
                            .width(6.dp)
                            .height(110.dp)
                            .clip(RoundedCornerShape(3.dp))
                            .background(Color.White.copy(alpha = 0.25f)),
                        contentAlignment = Alignment.BottomCenter
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .fillMaxHeight(volFraction)
                                .clip(RoundedCornerShape(3.dp))
                                .background(if (currentVolume == 0) Color.Red.copy(alpha = 0.9f) else NeonCyan)
                        )
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "${(volFraction * 100).toInt()}%",
                        color = Color.White,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}
