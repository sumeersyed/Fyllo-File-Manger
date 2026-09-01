package com.fyllo.filemanager.ui.screens

import android.text.format.Formatter
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.outlined.Android
import androidx.compose.material.icons.outlined.Code
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.FolderZip
import androidx.compose.material.icons.outlined.Image
import androidx.compose.material.icons.outlined.InsertDriveFile
import androidx.compose.material.icons.outlined.Movie
import androidx.compose.material.icons.outlined.MusicNote
import androidx.compose.material.icons.outlined.PictureAsPdf
import androidx.compose.material.icons.outlined.TextFields
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.fyllo.filemanager.domain.model.FileItem
import com.fyllo.filemanager.domain.model.StorageStats
import com.fyllo.filemanager.ui.theme.LocalEInkMode
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ManageScreen(
    storageStats: StorageStats?,
    junkSize: Long,
    onBackClick: () -> Unit,
    viewModel: ManageViewModel? = null,
    onFileClick: ((FileItem) -> Unit)? = null,
    onFolderClick: ((String) -> Unit)? = null
) {
    val context = LocalContext.current
    val totalUsed = storageStats?.usedBytes ?: 0L
    val formattedUsed = Formatter.formatFileSize(context, totalUsed)

    val categoryDetails by (viewModel?.categoryDetails ?: kotlinx.coroutines.flow.MutableStateFlow(emptyList())).collectAsState()
    val largestFiles by (viewModel?.largestFiles ?: kotlinx.coroutines.flow.MutableStateFlow(emptyList())).collectAsState()
    val folderSpaceList by (viewModel?.folderSpaceList ?: kotlinx.coroutines.flow.MutableStateFlow(emptyList())).collectAsState()

    val totalFileCount = categoryDetails.sumOf { it.itemCount }

    LaunchedEffect(Unit) {
        viewModel?.loadAnalyzerData()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Storage analyzer",
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
                actions = {
                    IconButton(onClick = { /* Menu */ }) {
                        Icon(
                            Icons.Default.MoreVert,
                            contentDescription = "More",
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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            // ─── 1. TOP STATS CARD WITH SEGMENTED MULTI-COLOR BAR & LEGENDS ───
            StorageTopSummaryCard(
                formattedUsed = formattedUsed,
                totalFilesCount = totalFileCount,
                categoryDetails = categoryDetails,
                totalUsedBytes = totalUsed
            )

            Spacer(modifier = Modifier.height(24.dp))

            // ─── 2. BY CATEGORY SECTION ─────────────────────────────────────
            Text(
                text = "By category",
                color = Color(0xFF3366FF),
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(vertical = 8.dp)
            )

            CategoryWavyList(
                categoryDetails = categoryDetails,
                maxCategoryBytes = categoryDetails.maxOfOrNull { it.sizeBytes }?.coerceAtLeast(1L) ?: 1L
            )

            Spacer(modifier = Modifier.height(28.dp))

            // ─── 3. SPACE MAP SECTION ───────────────────────────────────────
            Text(
                text = "Space map",
                color = Color(0xFF3366FF),
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Tap a folder to drill in — bigger tiles use more space.",
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                fontSize = 13.sp,
                modifier = Modifier.padding(top = 2.dp, bottom = 12.dp)
            )
            Text(
                text = "Internal storage",
                color = MaterialTheme.colorScheme.onBackground,
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(bottom = 10.dp)
            )

            SpaceMapGrid(
                folders = folderSpaceList,
                onFolderClick = { path -> onFolderClick?.invoke(path) }
            )

            Spacer(modifier = Modifier.height(28.dp))

            // ─── 4. LARGEST FILES SECTION ───────────────────────────────────
            Text(
                text = "Largest files",
                color = Color(0xFF3366FF),
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Long-press to select, then delete or share to free up space.",
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                fontSize = 13.sp,
                modifier = Modifier.padding(top = 2.dp, bottom = 12.dp)
            )

            LargestFilesList(
                files = largestFiles,
                onFileClick = { file -> onFileClick?.invoke(file) }
            )

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

// ─── COMPONENT: TOP SUMMARY CARD ─────────────────────────────────────────────
@Composable
private fun StorageTopSummaryCard(
    formattedUsed: String,
    totalFilesCount: Int,
    categoryDetails: List<StorageCategoryDetail>,
    totalUsedBytes: Long
) {
    val isEInk = LocalEInkMode.current
    val shape = RoundedCornerShape(24.dp)

    val cardModifier = if (isEInk) {
        Modifier
            .fillMaxWidth()
            .clip(shape)
            .border(1.dp, MaterialTheme.colorScheme.outline, shape)
            .background(Color.Transparent)
            .padding(20.dp)
    } else {
        Modifier
            .fillMaxWidth()
            .clip(shape)
            .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.7f))
            .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.12f), shape)
            .padding(20.dp)
    }

    Column(modifier = cardModifier) {
        // Used storage headline
        Text(
            text = "$formattedUsed used",
            color = MaterialTheme.colorScheme.onBackground,
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = "across $totalFilesCount files",
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
            fontSize = 14.sp,
            modifier = Modifier.padding(top = 2.dp, bottom = 16.dp)
        )

        // Segmented Progress Bar
        SegmentedStorageBar(
            categoryDetails = categoryDetails,
            totalUsedBytes = totalUsedBytes
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Legend Items Grid
        LegendGrid(
            categoryDetails = categoryDetails,
            totalUsedBytes = totalUsedBytes
        )
    }
}

// ─── COMPONENT: SEGMENTED STORAGE PROGRESS BAR ──────────────────────────────
@Composable
private fun SegmentedStorageBar(
    categoryDetails: List<StorageCategoryDetail>,
    totalUsedBytes: Long
) {
    val isEInk = LocalEInkMode.current
    val validDetails = categoryDetails.filter { it.sizeBytes > 0 }
    val totalBytes = totalUsedBytes.coerceAtLeast(1L)

    var animationPlayed by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        animationPlayed = true
    }

    val animProgress by animateFloatAsState(
        targetValue = if (animationPlayed) 1f else 0f,
        animationSpec = tween(durationMillis = 1000, easing = FastOutSlowInEasing),
        label = "barAnimation"
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(28.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(if (isEInk) MaterialTheme.colorScheme.outline.copy(alpha = 0.2f) else Color(0xFFEEEEEE))
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .clip(RoundedCornerShape(14.dp))
        ) {
            validDetails.forEachIndexed { index, item ->
                val fraction = (item.sizeBytes.toFloat() / totalBytes.toFloat()) * animProgress
                if (fraction > 0.005f) {
                    Box(
                        modifier = Modifier
                            .weight(fraction)
                            .fillMaxHeight()
                            .background(if (isEInk) MaterialTheme.colorScheme.onBackground else item.color)
                            .padding(end = if (index < validDetails.size - 1) 2.dp else 0.dp)
                    )
                }
            }
        }
    }
}

// ─── COMPONENT: LEGEND ITEMS GRID ────────────────────────────────────────────
@Composable
private fun LegendGrid(
    categoryDetails: List<StorageCategoryDetail>,
    totalUsedBytes: Long
) {
    val total = totalUsedBytes.coerceAtLeast(1L)

    // Flow layout or row chunks of 3
    val chunked = categoryDetails.chunked(3)
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        chunked.forEach { rowItems ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                rowItems.forEach { item ->
                    val percent = ((item.sizeBytes.toFloat() / total.toFloat()) * 100).toInt()
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(item.color)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "${item.name} $percent%",
                            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.85f),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
                // Fill empty slots if last row has less than 3
                repeat(3 - rowItems.size) {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

// ─── COMPONENT: BY CATEGORY LIST WITH WAVY / PROGRESS BARS ───────────────────
@Composable
private fun CategoryWavyList(
    categoryDetails: List<StorageCategoryDetail>,
    maxCategoryBytes: Long
) {
    val isEInk = LocalEInkMode.current
    val context = LocalContext.current
    val shape = RoundedCornerShape(20.dp)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape)
            .background(if (isEInk) Color.Transparent else MaterialTheme.colorScheme.surface.copy(alpha = 0.7f))
            .border(1.dp, if (isEInk) MaterialTheme.colorScheme.outline else MaterialTheme.colorScheme.outline.copy(alpha = 0.12f), shape)
            .padding(vertical = 6.dp)
    ) {
        categoryDetails.forEachIndexed { index, item ->
            val iconVec = getCategoryIcon(item.name)
            val formattedSize = Formatter.formatShortFileSize(context, item.sizeBytes)
            val fraction = (item.sizeBytes.toFloat() / maxCategoryBytes.toFloat()).coerceIn(0.01f, 1f)

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 10.dp)
            ) {
                // Header: Icon + Category Name + Size · Count
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(item.color.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = iconVec,
                            contentDescription = item.name,
                            tint = item.color,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Text(
                        text = item.name,
                        color = MaterialTheme.colorScheme.onBackground,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.weight(1f)
                    )

                    Text(
                        text = "$formattedSize · ${item.itemCount}",
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Smooth wavy / organic curved progress line
                WavyProgressIndicator(
                    fraction = fraction,
                    color = item.color,
                    hasWaves = index < 3 // top categories (Images, Audio, Videos) get rich wavy strokes
                )
            }

            if (index < categoryDetails.size - 1) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(1.dp)
                        .padding(horizontal = 16.dp)
                        .background(MaterialTheme.colorScheme.outline.copy(alpha = 0.08f))
                )
            }
        }
    }
}

// ─── COMPONENT: WAVY / CURVED PROGRESS INDICATOR ─────────────────────────────
@Composable
private fun WavyProgressIndicator(
    fraction: Float,
    color: Color,
    hasWaves: Boolean
) {
    val isEInk = LocalEInkMode.current
    val onBgColor = MaterialTheme.colorScheme.onBackground
    val strokeColor = if (isEInk) onBgColor else color
    val trackBgColor = if (isEInk) Color.Gray.copy(alpha = 0.2f) else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f)

    var animationPlayed by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        animationPlayed = true
    }

    val animatedFraction by animateFloatAsState(
        targetValue = if (animationPlayed) fraction else 0f,
        animationSpec = tween(durationMillis = 1100, easing = FastOutSlowInEasing),
        label = "wavyProgress"
    )

    Canvas(
        modifier = Modifier
            .fillMaxWidth()
            .height(16.dp)
    ) {
        val width = size.width
        val trackY = size.height / 2
        val activeWidth = width * animatedFraction

        // Background track - full width
        drawLine(
            color = trackBgColor,
            start = Offset(0f, trackY),
            end = Offset(width, trackY),
            strokeWidth = 2.5.dp.toPx(),
            cap = StrokeCap.Round
        )

        // Trailing end dot
        drawCircle(
            color = strokeColor,
            radius = 2.5.dp.toPx(),
            center = Offset(width - 2.5.dp.toPx(), trackY)
        )

        // Active Segment
        if (activeWidth > 0f) {
            if (hasWaves && activeWidth > 16.dp.toPx()) {
                val wavePath = Path()
                val halfWave = 14.dp.toPx() // Half wave period for ultra smooth crests
                val amplitude = 2.5.dp.toPx() // Gentle subtle ripple amplitude

                wavePath.moveTo(0f, trackY)
                var currentX = 0f
                var isUp = true

                while (currentX < activeWidth) {
                    val nextX = (currentX + halfWave).coerceAtMost(activeWidth)
                    val controlX = (currentX + nextX) / 2
                    val controlY = if (isUp) trackY - amplitude else trackY + amplitude

                    wavePath.quadraticBezierTo(controlX, controlY, nextX, trackY)
                    currentX = nextX
                    isUp = !isUp
                }

                drawPath(
                    path = wavePath,
                    color = strokeColor,
                    style = Stroke(width = 3.2.dp.toPx(), cap = StrokeCap.Round)
                )
            } else {
                drawLine(
                    color = strokeColor,
                    start = Offset(0f, trackY),
                    end = Offset(activeWidth, trackY),
                    strokeWidth = 3.2.dp.toPx(),
                    cap = StrokeCap.Round
                )
            }
        }
    }
}

// ─── COMPONENT: SPACE MAP MOSAIC GRID ─────────────────────────────────────────
@Composable
private fun SpaceMapGrid(
    folders: List<FolderSpaceItem>,
    onFolderClick: (String) -> Unit
) {
    val context = LocalContext.current
    val isEInk = LocalEInkMode.current

    if (folders.isEmpty()) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(140.dp)
                .clip(RoundedCornerShape(20.dp))
                .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.5f)),
            contentAlignment = Alignment.Center
        ) {
            Text("Calculating space map...", color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
        }
        return
    }

    // Curated vibrant pastel palette for space map folders
    val folderPalettes = listOf(
        Pair(Color(0xFFE8EEFF), Color(0xFF3366FF)), // Lavender Blue (Primary)
        Pair(Color(0xFFE8F5E9), Color(0xFF2E7D32)), // Mint Green
        Pair(Color(0xFFFFF3E0), Color(0xFFE65100)), // Soft Amber/Orange
        Pair(Color(0xFFF3E5F5), Color(0xFF7B1FA2)), // Soft Purple
        Pair(Color(0xFFFFEBEE), Color(0xFFC2185B)), // Soft Coral
        Pair(Color(0xFFE0F7FA), Color(0xFF00838F)), // Soft Cyan
        Pair(Color(0xFFFFF8E1), Color(0xFFF57F17)), // Soft Golden
        Pair(Color(0xFFECEFF1), Color(0xFF455A64))  // Soft Slate
    )

    val item0 = folders.getOrNull(0)
    val item1 = folders.getOrNull(1)
    val item2 = folders.getOrNull(2)
    val item3 = folders.getOrNull(3)
    val item4 = folders.getOrNull(4)

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(150.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Big Column 1 (Folder 1)
            item0?.let { folder ->
                val pal = folderPalettes[0]
                SpaceTile(
                    title = folder.name,
                    size = Formatter.formatShortFileSize(context, folder.sizeBytes),
                    bgColor = if (isEInk) MaterialTheme.colorScheme.surface else pal.first,
                    accentColor = if (isEInk) MaterialTheme.colorScheme.onBackground else pal.second,
                    modifier = Modifier
                        .weight(1.1f)
                        .fillMaxHeight()
                        .clickable { onFolderClick(folder.path) }
                )
            }

            // Big Column 2 (Folder 2)
            item1?.let { folder ->
                val pal = folderPalettes[1]
                SpaceTile(
                    title = folder.name,
                    size = Formatter.formatShortFileSize(context, folder.sizeBytes),
                    bgColor = if (isEInk) MaterialTheme.colorScheme.surface else pal.first,
                    accentColor = if (isEInk) MaterialTheme.colorScheme.onBackground else pal.second,
                    modifier = Modifier
                        .weight(1.1f)
                        .fillMaxHeight()
                        .clickable { onFolderClick(folder.path) }
                )
            }

            // Column 3 (Folder 3)
            item2?.let { folder ->
                val pal = folderPalettes[2]
                SpaceTile(
                    title = folder.name,
                    size = Formatter.formatShortFileSize(context, folder.sizeBytes),
                    bgColor = if (isEInk) MaterialTheme.colorScheme.surface else pal.first,
                    accentColor = if (isEInk) MaterialTheme.colorScheme.onBackground else pal.second,
                    modifier = Modifier
                        .weight(1.0f)
                        .fillMaxHeight()
                        .clickable { onFolderClick(folder.path) }
                )
            }
        }

        // Row 2: Wider tile (e.g. Download) & secondary tile (DCIM)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(72.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            item3?.let { folder ->
                val pal = folderPalettes[3]
                SpaceTile(
                    title = folder.name,
                    size = Formatter.formatShortFileSize(context, folder.sizeBytes),
                    bgColor = if (isEInk) MaterialTheme.colorScheme.surface else pal.first,
                    accentColor = if (isEInk) MaterialTheme.colorScheme.onBackground else pal.second,
                    modifier = Modifier
                        .weight(2.0f)
                        .fillMaxHeight()
                        .clickable { onFolderClick(folder.path) }
                )
            }

            item4?.let { folder ->
                val pal = folderPalettes[4]
                SpaceTile(
                    title = folder.name,
                    size = Formatter.formatShortFileSize(context, folder.sizeBytes),
                    bgColor = if (isEInk) MaterialTheme.colorScheme.surface else pal.first,
                    accentColor = if (isEInk) MaterialTheme.colorScheme.onBackground else pal.second,
                    modifier = Modifier
                        .weight(1.1f)
                        .fillMaxHeight()
                        .clickable { onFolderClick(folder.path) }
                )
            }
        }
    }
}

@Composable
private fun SpaceTile(
    title: String,
    size: String,
    bgColor: Color,
    accentColor: Color,
    modifier: Modifier = Modifier
) {
    val shape = RoundedCornerShape(16.dp)
    val isEInk = LocalEInkMode.current
    val borderColor = if (isEInk) MaterialTheme.colorScheme.outline else accentColor.copy(alpha = 0.25f)

    Box(
        modifier = modifier
            .clip(shape)
            .background(bgColor)
            .border(1.dp, borderColor, shape)
            .padding(12.dp)
    ) {
        Column(modifier = Modifier.align(Alignment.TopStart)) {
            Text(
                text = title,
                color = if (isEInk) MaterialTheme.colorScheme.onBackground else Color(0xFF1E293B),
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = size,
                color = if (isEInk) MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f) else accentColor,
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

// ─── COMPONENT: LARGEST FILES LIST ───────────────────────────────────────────
@Composable
private fun LargestFilesList(
    files: List<FileItem>,
    onFileClick: (FileItem) -> Unit
) {
    val context = LocalContext.current
    val isEInk = LocalEInkMode.current
    val shape = RoundedCornerShape(20.dp)

    if (files.isEmpty()) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(100.dp)
                .clip(shape)
                .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.5f)),
            contentAlignment = Alignment.Center
        ) {
            Text("No large files found", color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
        }
        return
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape)
            .background(if (isEInk) Color.Transparent else MaterialTheme.colorScheme.surface.copy(alpha = 0.7f))
            .border(1.dp, if (isEInk) MaterialTheme.colorScheme.outline else MaterialTheme.colorScheme.outline.copy(alpha = 0.12f), shape)
    ) {
        files.take(10).forEachIndexed { index, file ->
            val formattedSize = Formatter.formatShortFileSize(context, file.sizeBytes)
            val dateStr = SimpleDateFormat("dd MMM yyyy h:mm a", Locale.getDefault()).format(Date(file.lastModified))
            val isMedia = file.mimeType?.startsWith("image/") == true || file.mimeType?.startsWith("video/") == true

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onFileClick(file) }
                    .padding(horizontal = 14.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Thumbnail or Type Icon
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant),
                    contentAlignment = Alignment.Center
                ) {
                    if (isMedia) {
                        AsyncImage(
                            model = file.uri,
                            contentDescription = file.name,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    } else {
                        Icon(
                            imageVector = getCategoryIcon(file.extension),
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.width(14.dp))

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
                        text = "$formattedSize · $dateStr",
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                        fontSize = 12.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                IconButton(onClick = { /* File options */ }) {
                    Icon(
                        imageVector = Icons.Default.MoreVert,
                        contentDescription = "Options",
                        tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                    )
                }
            }

            if (index < files.take(10).size - 1) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(1.dp)
                        .padding(horizontal = 16.dp)
                        .background(MaterialTheme.colorScheme.outline.copy(alpha = 0.08f))
                )
            }
        }
    }
}

// ─── HELPER: CATEGORY ICON PICKER ───────────────────────────────────────────
private fun getCategoryIcon(name: String): ImageVector {
    val lower = name.lowercase()
    return when {
        lower.contains("image") || lower in listOf("jpg", "jpeg", "png", "webp", "gif") -> Icons.Outlined.Image
        lower.contains("audio") || lower in listOf("mp3", "wav", "flac", "m4a", "ogg") -> Icons.Outlined.MusicNote
        lower.contains("video") || lower in listOf("mp4", "mkv", "avi", "mov", "webm") -> Icons.Outlined.Movie
        lower.contains("apk") -> Icons.Outlined.Android
        lower.contains("archive") || lower in listOf("zip", "rar", "7z", "tar", "gz") -> Icons.Outlined.FolderZip
        lower.contains("pdf") -> Icons.Outlined.PictureAsPdf
        lower.contains("code") || lower in listOf("kt", "java", "py", "js", "ts", "json", "html") -> Icons.Outlined.Code
        lower.contains("text") || lower == "txt" -> Icons.Outlined.TextFields
        lower.contains("document") || lower in listOf("doc", "docx", "xls", "xlsx", "ppt", "pptx") -> Icons.Outlined.Description
        else -> Icons.Outlined.InsertDriveFile
    }
}

