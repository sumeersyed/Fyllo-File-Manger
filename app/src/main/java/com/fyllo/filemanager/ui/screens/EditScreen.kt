package com.fyllo.filemanager.ui.screens

import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapShader
import android.graphics.Canvas
import android.graphics.ColorMatrixColorFilter
import android.graphics.ImageDecoder
import android.graphics.Paint
import android.graphics.Shader
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Redo
import androidx.compose.material.icons.automirrored.filled.Undo
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ColorMatrix
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.asAndroidPath
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.canhub.cropper.CropImageContract
import com.canhub.cropper.CropImageContractOptions
import com.canhub.cropper.CropImageOptions
import com.fyllo.filemanager.ui.components.HorizontalScrubber
import com.fyllo.filemanager.ui.icons.InkHighlighter
import com.fyllo.filemanager.ui.icons.InkMarker
import com.fyllo.filemanager.ui.icons.Stylus
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.segmentation.subject.SubjectSegmentation
import com.google.mlkit.vision.segmentation.subject.SubjectSegmenterOptions
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import kotlin.math.roundToInt

enum class ToolType {
    STYLUS, HIGHLIGHTER, MARKER, BLUR, MOSAIC
}

class EditState {
    var brightness by mutableStateOf(1f)
    var contrast by mutableStateOf(1f)
    var saturation by mutableStateOf(1f)
    var warmth by mutableStateOf(0.5f)
    var tint by mutableStateOf(0.5f)
    var tone by mutableStateOf(0.5f)
    var blackPoint by mutableStateOf(0.5f)
    var whitePoint by mutableStateOf(0.5f)
    var highlights by mutableStateOf(0.5f)
    var shadows by mutableStateOf(0.5f)
    var vignette by mutableStateOf(0f)
    var skinTone by mutableStateOf(0.5f)
    var blueTone by mutableStateOf(0.5f)
    var hue by mutableStateOf(0.5f)
    var bw by mutableStateOf(0f)
    var customMatrix by mutableStateOf<ColorMatrix?>(null)
}

data class DrawingPath(
    val path: Path,
    val color: Color,
    val strokeWidth: Float,
    val alpha: Float = 1f,
    val blendMode: BlendMode = BlendMode.SrcOver,
    val toolType: ToolType = ToolType.STYLUS
)

data class EditSnapshot(
    val uri: Uri,
    val brightness: Float,
    val contrast: Float,
    val saturation: Float,
    val warmth: Float,
    val tint: Float,
    val tone: Float,
    val blackPoint: Float,
    val whitePoint: Float,
    val highlights: Float,
    val shadows: Float,
    val vignette: Float,
    val paths: List<DrawingPath>,
    val customMatrix: ColorMatrix?
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditScreen(
    uriStr: String,
    onBackClick: () -> Unit
) {
    var currentUri by remember { mutableStateOf(Uri.parse(uriStr)) }
    var activeTab by remember { mutableStateOf("Filters") }
    var activeTool by remember { mutableStateOf<String?>(null) }
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    var isSaving by remember { mutableStateOf(false) }
    var isProcessingCutout by remember { mutableStateOf(false) }
    var isProcessingEffect by remember { mutableStateOf(false) }
    
    // Drawing states
    var paths by remember { mutableStateOf(listOf<DrawingPath>()) }
    var currentPath by remember { mutableStateOf<Path?>(null) }
    var activeColor by remember { mutableStateOf(Color.Red) }
    
    // Layer visibility & management
    var isLayersVisible by remember { mutableStateOf(true) }
    var showLayersPanel by remember { mutableStateOf(false) }
    
    // Loaded Bitmaps for real Blur & Mosaic drawing
    var baseBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var blurredBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var mosaicBitmap by remember { mutableStateOf<Bitmap?>(null) }

    // Load Bitmaps asynchronously
    LaunchedEffect(currentUri) {
        withContext(Dispatchers.IO) {
            try {
                val source = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    ImageDecoder.createSource(context.contentResolver, currentUri)
                } else {
                    null
                }
                val bmp = source?.let {
                    ImageDecoder.decodeBitmap(it) { decoder, _, _ -> decoder.isMutableRequired = true }
                }
                bmp?.let {
                    baseBitmap = it
                    mosaicBitmap = createMosaicBitmap(it)
                    blurredBitmap = createBlurredBitmap(it)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    // Zoom and Pan states
    var scale by remember { mutableStateOf(1f) }
    var offsetX by remember { mutableStateOf(0f) }
    var offsetY by remember { mutableStateOf(0f) }
    
    val editState = remember { EditState() }
    
    // Undo / Redo History Stack
    val historyStack = remember { mutableStateListOf<EditSnapshot>() }
    var historyIndex by remember { mutableStateOf(0) }

    fun captureSnapshot(): EditSnapshot {
        return EditSnapshot(
            uri = currentUri,
            brightness = editState.brightness,
            contrast = editState.contrast,
            saturation = editState.saturation,
            warmth = editState.warmth,
            tint = editState.tint,
            tone = editState.tone,
            blackPoint = editState.blackPoint,
            whitePoint = editState.whitePoint,
            highlights = editState.highlights,
            shadows = editState.shadows,
            vignette = editState.vignette,
            paths = paths.toList(),
            customMatrix = editState.customMatrix
        )
    }

    fun pushHistory() {
        val snapshot = captureSnapshot()
        if (historyIndex < historyStack.size - 1) {
            while (historyStack.size > historyIndex + 1) {
                historyStack.removeAt(historyStack.size - 1)
            }
        }
        historyStack.add(snapshot)
        historyIndex = historyStack.size - 1
    }

    fun restoreSnapshot(snapshot: EditSnapshot) {
        currentUri = snapshot.uri
        editState.brightness = snapshot.brightness
        editState.contrast = snapshot.contrast
        editState.saturation = snapshot.saturation
        editState.warmth = snapshot.warmth
        editState.tint = snapshot.tint
        editState.tone = snapshot.tone
        editState.blackPoint = snapshot.blackPoint
        editState.whitePoint = snapshot.whitePoint
        editState.highlights = snapshot.highlights
        editState.shadows = snapshot.shadows
        editState.vignette = snapshot.vignette
        paths = snapshot.paths
        editState.customMatrix = snapshot.customMatrix
    }

    // Initial snapshot
    LaunchedEffect(Unit) {
        if (historyStack.isEmpty()) {
            historyStack.add(captureSnapshot())
        }
    }
    
    val cropImage = rememberLauncherForActivityResult(CropImageContract()) { result ->
        if (result.isSuccessful) {
            result.uriContent?.let { croppedUri ->
                currentUri = croppedUri
                pushHistory()
            }
        }
    }
    
    val colorMatrix = remember(
        editState.brightness, editState.contrast, editState.saturation, 
        editState.customMatrix, editState.bw, editState.warmth,
        editState.tint, editState.tone, editState.blackPoint, editState.whitePoint,
        editState.highlights, editState.shadows, editState.skinTone, editState.blueTone, editState.hue
    ) {
        val matrix = android.graphics.ColorMatrix()
        
        if (editState.customMatrix != null) {
            matrix.postConcat(android.graphics.ColorMatrix(editState.customMatrix!!.values))
        }
        
        if (editState.saturation != 1f) {
            val satMatrix = android.graphics.ColorMatrix()
            satMatrix.setSaturation(editState.saturation)
            matrix.postConcat(satMatrix)
        }
        
        if (editState.brightness != 1f || editState.contrast != 1f) {
            val scaleVal = editState.contrast
            val translate = (-.5f * scaleVal + .5f) * 255f + (editState.brightness - 1f) * 255f
            val contrastBrightnessMatrix = android.graphics.ColorMatrix(
                floatArrayOf(
                    scaleVal, 0f, 0f, 0f, translate,
                    0f, scaleVal, 0f, 0f, translate,
                    0f, 0f, scaleVal, 0f, translate,
                    0f, 0f, 0f, 1f, 0f
                )
            )
            matrix.postConcat(contrastBrightnessMatrix)
        }
        
        if (editState.warmth != 0.5f || editState.tint != 0.5f) {
            val rScale = 1f + (editState.warmth - 0.5f) * 0.4f
            val bScale = 1f - (editState.warmth - 0.5f) * 0.4f
            val gScale = 1f + (editState.tint - 0.5f) * 0.3f
            val colorTempMatrix = android.graphics.ColorMatrix(
                floatArrayOf(
                    rScale, 0f, 0f, 0f, 0f,
                    0f, gScale, 0f, 0f, 0f,
                    0f, 0f, bScale, 0f, 0f,
                    0f, 0f, 0f, 1f, 0f
                )
            )
            matrix.postConcat(colorTempMatrix)
        }
        
        if (editState.bw > 0f) {
            val bwMatrix = android.graphics.ColorMatrix()
            bwMatrix.setSaturation(1f - editState.bw)
            matrix.postConcat(bwMatrix)
        }
        
        if (editState.skinTone != 0.5f) {
            val skinScale = 1f + (editState.skinTone - 0.5f) * 0.5f
            val skinMatrix = android.graphics.ColorMatrix(
                floatArrayOf(
                    skinScale, 0f, 0f, 0f, (editState.skinTone - 0.5f) * 20f,
                    0f, 1f, 0f, 0f, 0f,
                    0f, 0f, 1f, 0f, 0f,
                    0f, 0f, 0f, 1f, 0f
                )
            )
            matrix.postConcat(skinMatrix)
        }
        
        if (editState.blueTone != 0.5f) {
            val blueScale = 1f + (editState.blueTone - 0.5f) * 0.6f
            val blueMatrix = android.graphics.ColorMatrix(
                floatArrayOf(
                    1f, 0f, 0f, 0f, 0f,
                    0f, 1f, 0f, 0f, 0f,
                    0f, 0f, blueScale, 0f, (editState.blueTone - 0.5f) * 25f,
                    0f, 0f, 0f, 1f, 0f
                )
            )
            matrix.postConcat(blueMatrix)
        }

        if (editState.hue != 0.5f) {
            val hueShift = (editState.hue - 0.5f) * 180f
            val hueMatrixR = android.graphics.ColorMatrix()
            hueMatrixR.setRotate(0, hueShift)
            matrix.postConcat(hueMatrixR)
            val hueMatrixG = android.graphics.ColorMatrix()
            hueMatrixG.setRotate(1, hueShift)
            matrix.postConcat(hueMatrixG)
            val hueMatrixB = android.graphics.ColorMatrix()
            hueMatrixB.setRotate(2, hueShift)
            matrix.postConcat(hueMatrixB)
        }
        
        ColorMatrix(matrix.array)
    }

    Scaffold(
        topBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surface)
                    .statusBarsPadding()
            ) {
                EditTopAppBar(
                    onBackClick = onBackClick,
                    onSaveClick = {
                        if (!isSaving) {
                            isSaving = true
                            coroutineScope.launch {
                                val success = saveEditedImage(context, currentUri, colorMatrix, paths, blurredBitmap, mosaicBitmap)
                                isSaving = false
                                if (success) {
                                    Toast.makeText(context, "Image saved successfully!", Toast.LENGTH_SHORT).show()
                                    onBackClick()
                                } else {
                                    Toast.makeText(context, "Failed to save image.", Toast.LENGTH_SHORT).show()
                                }
                            }
                        }
                    },
                    isSaving = isSaving,
                    canUndo = historyIndex > 0,
                    canRedo = historyIndex < historyStack.size - 1,
                    onUndoClick = {
                        if (historyIndex > 0) {
                            historyIndex--
                            restoreSnapshot(historyStack[historyIndex])
                        }
                    },
                    onRedoClick = {
                        if (historyIndex < historyStack.size - 1) {
                            historyIndex++
                            restoreSnapshot(historyStack[historyIndex])
                        }
                    },
                    onResetClick = {
                        if (historyStack.isNotEmpty()) {
                            historyIndex = 0
                            restoreSnapshot(historyStack[0])
                        }
                    }
                )
                SecondaryTopBar(
                    onCropClick = { cropImage.launch(CropImageContractOptions(currentUri, CropImageOptions())) },
                    onRatio1Click = { cropImage.launch(CropImageContractOptions(currentUri, CropImageOptions(fixAspectRatio = true, aspectRatioX = 1, aspectRatioY = 1))) },
                    onRatio169Click = { cropImage.launch(CropImageContractOptions(currentUri, CropImageOptions(fixAspectRatio = true, aspectRatioX = 16, aspectRatioY = 9))) },
                    onFlipClick = { cropImage.launch(CropImageContractOptions(currentUri, CropImageOptions(flipHorizontally = true))) },
                    onRotateClick = { cropImage.launch(CropImageContractOptions(currentUri, CropImageOptions(initialRotation = 90))) }
                )
            }
        },
        bottomBar = {
            Box(
                modifier = Modifier
                    .background(MaterialTheme.colorScheme.surface)
                    .navigationBarsPadding()
            ) {
                BottomToolPanel(
                    activeTab = activeTab,
                    onTabSelected = { activeTab = it },
                    activeTool = activeTool,
                    onToolSelected = { toolName -> 
                        activeTool = toolName
                        if (toolName == "Posterize" || toolName == "Edges") {
                            isProcessingEffect = true
                            coroutineScope.launch {
                                val effectUri = EditEffects.applyEffect(context, currentUri, toolName)
                                if (effectUri != null) {
                                    currentUri = effectUri
                                    pushHistory()
                                }
                                isProcessingEffect = false
                                activeTool = null
                            }
                        }
                    },
                    editState = editState,
                    onCutoutRequested = {
                        isProcessingCutout = true
                        coroutineScope.launch {
                            val cutoutUri = processCutout(context, currentUri)
                            if (cutoutUri != null) {
                                currentUri = cutoutUri
                                pushHistory()
                            } else {
                                Toast.makeText(context, "Could not extract subject cutout", Toast.LENGTH_SHORT).show()
                            }
                            isProcessingCutout = false
                        }
                    },
                    onLayersToggle = {
                        showLayersPanel = !showLayersPanel
                    },
                    onHistoryChange = { pushHistory() }
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
            // Main Image Editor Canvas View
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(12.dp)
                    .pointerInput(Unit) {
                        detectTransformGestures { _, pan, zoom, _ ->
                            scale = (scale * zoom).coerceIn(1f, 5f)
                            if (scale > 1f) {
                                offsetX += pan.x
                                offsetY += pan.y
                            } else {
                                offsetX = 0f
                                offsetY = 0f
                            }
                        }
                    },
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .graphicsLayer(
                            scaleX = scale, scaleY = scale,
                            translationX = offsetX, translationY = offsetY
                        )
                ) {
                    AsyncImage(
                        model = currentUri,
                        contentDescription = "Editing Image",
                        contentScale = ContentScale.Fit,
                        colorFilter = ColorFilter.colorMatrix(colorMatrix),
                        modifier = Modifier.fillMaxSize()
                    )
                    
                    if (editState.vignette > 0f) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(
                                    Brush.radialGradient(
                                        colors = listOf(Color.Transparent, Color.Black.copy(alpha = editState.vignette)),
                                        radius = 2000f
                                    )
                                )
                        )
                    }
                
                    // Drawing Canvas overlay
                    if (isLayersVisible && activeTab == "Markup" && activeTool in listOf("Stylus", "Highlighter", "Marker", "Blur", "Mosaic")) {
                        val currentToolType = when (activeTool) {
                            "Highlighter" -> ToolType.HIGHLIGHTER
                            "Marker" -> ToolType.MARKER
                            "Blur" -> ToolType.BLUR
                            "Mosaic" -> ToolType.MOSAIC
                            else -> ToolType.STYLUS
                        }

                        Canvas(
                            modifier = Modifier
                                .fillMaxSize()
                                .pointerInput(activeTool) {
                                    detectDragGestures(
                                        onDragStart = { offset ->
                                            val newPath = Path()
                                            newPath.moveTo(offset.x, offset.y)
                                            currentPath = newPath
                                        },
                                        onDrag = { change, _ ->
                                            currentPath?.lineTo(change.position.x, change.position.y)
                                        },
                                        onDragEnd = {
                                            currentPath?.let { p ->
                                                val strokeWidth = when (currentToolType) {
                                                    ToolType.HIGHLIGHTER -> 40f
                                                    ToolType.MARKER -> 20f
                                                    ToolType.BLUR, ToolType.MOSAIC -> 60f
                                                    else -> 10f
                                                }
                                                val alpha = when (currentToolType) {
                                                    ToolType.HIGHLIGHTER -> 0.4f
                                                    ToolType.MARKER -> 0.85f
                                                    else -> 1f
                                                }
                                                val blendMode = if (currentToolType == ToolType.HIGHLIGHTER) BlendMode.Multiply else BlendMode.SrcOver
                                                paths = paths + DrawingPath(p, activeColor, strokeWidth, alpha, blendMode, currentToolType)
                                                pushHistory()
                                            }
                                            currentPath = null
                                        },
                                        onDragCancel = { currentPath = null }
                                    )
                                }
                        ) {
                            // Render saved paths
                            paths.forEach { dp ->
                                drawDrawingPath(dp, blurredBitmap, mosaicBitmap)
                            }
                            // Render current drawing path
                            currentPath?.let { p ->
                                val strokeWidth = when (currentToolType) {
                                    ToolType.HIGHLIGHTER -> 40f
                                    ToolType.MARKER -> 20f
                                    ToolType.BLUR, ToolType.MOSAIC -> 60f
                                    else -> 10f
                                }
                                val alpha = when (currentToolType) {
                                    ToolType.HIGHLIGHTER -> 0.4f
                                    ToolType.MARKER -> 0.85f
                                    else -> 1f
                                }
                                val blendMode = if (currentToolType == ToolType.HIGHLIGHTER) BlendMode.Multiply else BlendMode.SrcOver
                                drawDrawingPath(DrawingPath(p, activeColor, strokeWidth, alpha, blendMode, currentToolType), blurredBitmap, mosaicBitmap)
                            }
                        }
                    }
                }

                // Layers & Clear Overlay Panel
                if (showLayersPanel) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(16.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.9f))
                            .border(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.3f), RoundedCornerShape(16.dp))
                            .padding(12.dp)
                    ) {
                        Column {
                            Text("Smart Layers", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface, fontSize = 14.sp)
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("Markup Layer", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface)
                                Spacer(modifier = Modifier.width(8.dp))
                                IconButton(onClick = { isLayersVisible = !isLayersVisible }, modifier = Modifier.size(28.dp)) {
                                    Icon(
                                        imageVector = if (isLayersVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                        contentDescription = "Toggle Layer",
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                            if (paths.isNotEmpty()) {
                                TextButton(
                                    onClick = {
                                        paths = emptyList()
                                        pushHistory()
                                    }
                                ) {
                                    Text("Clear Drawings", color = MaterialTheme.colorScheme.error, fontSize = 12.sp)
                                }
                            }
                        }
                    }
                }
                
                if (isProcessingCutout || isProcessingEffect) {
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                }
            }
        }
    }
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawDrawingPath(
    dp: DrawingPath,
    blurredBitmap: Bitmap?,
    mosaicBitmap: Bitmap?
) {
    if (dp.toolType == ToolType.BLUR && blurredBitmap != null) {
        drawIntoCanvas { canvas ->
            val paint = Paint().apply {
                isAntiAlias = true
                style = Paint.Style.STROKE
                strokeWidth = dp.strokeWidth
                strokeCap = Paint.Cap.ROUND
                strokeJoin = Paint.Join.ROUND
                shader = BitmapShader(blurredBitmap, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP)
            }
            canvas.nativeCanvas.drawPath(dp.path.asAndroidPath(), paint)
        }
    } else if (dp.toolType == ToolType.MOSAIC && mosaicBitmap != null) {
        drawIntoCanvas { canvas ->
            val paint = Paint().apply {
                isAntiAlias = true
                style = Paint.Style.STROKE
                strokeWidth = dp.strokeWidth
                strokeCap = Paint.Cap.ROUND
                strokeJoin = Paint.Join.ROUND
                shader = BitmapShader(mosaicBitmap, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP)
            }
            canvas.nativeCanvas.drawPath(dp.path.asAndroidPath(), paint)
        }
    } else {
        drawPath(
            dp.path,
            color = dp.color.copy(alpha = dp.alpha),
            style = Stroke(width = dp.strokeWidth, cap = StrokeCap.Round, join = StrokeJoin.Round),
            blendMode = dp.blendMode
        )
    }
}

@Composable
fun EditTopAppBar(
    onBackClick: () -> Unit,
    onSaveClick: () -> Unit,
    isSaving: Boolean,
    canUndo: Boolean,
    canRedo: Boolean,
    onUndoClick: () -> Unit,
    onRedoClick: () -> Unit,
    onResetClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onBackClick) {
            Icon(Icons.Default.Close, contentDescription = "Close", tint = MaterialTheme.colorScheme.onSurface)
        }

        // Undo, Redo, Reset Action Group
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onUndoClick, enabled = canUndo) {
                Icon(
                    Icons.AutoMirrored.Filled.Undo,
                    contentDescription = "Undo",
                    tint = if (canUndo) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
                )
            }
            IconButton(onClick = onRedoClick, enabled = canRedo) {
                Icon(
                    Icons.AutoMirrored.Filled.Redo,
                    contentDescription = "Redo",
                    tint = if (canRedo) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
                )
            }
            IconButton(onClick = onResetClick) {
                Icon(
                    Icons.Default.RestartAlt,
                    contentDescription = "Reset Original",
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }

        Button(
            onClick = onSaveClick,
            enabled = !isSaving,
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = Color.White
            ),
            shape = RoundedCornerShape(50)
        ) {
            if (isSaving) {
                CircularProgressIndicator(modifier = Modifier.size(16.dp), color = Color.White, strokeWidth = 2.dp)
            } else {
                Text("Save Copy", fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun SecondaryTopBar(
    onCropClick: () -> Unit,
    onRatio1Click: () -> Unit,
    onRatio169Click: () -> Unit,
    onFlipClick: () -> Unit,
    onRotateClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onCropClick) {
            Icon(Icons.Default.Crop, contentDescription = "Crop", tint = MaterialTheme.colorScheme.onSurface)
        }
        IconButton(onClick = onRatio1Click) {
            Icon(Icons.Default.CropSquare, contentDescription = "1:1 Aspect Ratio", tint = MaterialTheme.colorScheme.onSurface)
        }
        IconButton(onClick = onRatio169Click) {
            Icon(Icons.Default.Crop169, contentDescription = "16:9 Aspect Ratio", tint = MaterialTheme.colorScheme.onSurface)
        }
        IconButton(onClick = onFlipClick) {
            Icon(Icons.Default.Flip, contentDescription = "Flip Horizontally", tint = MaterialTheme.colorScheme.onSurface)
        }
        IconButton(onClick = onRotateClick) {
            Icon(Icons.Default.RotateRight, contentDescription = "Rotate", tint = MaterialTheme.colorScheme.onSurface)
        }
    }
}

@Composable
fun BottomToolPanel(
    activeTab: String,
    onTabSelected: (String) -> Unit,
    activeTool: String?,
    onToolSelected: (String?) -> Unit,
    editState: EditState,
    onCutoutRequested: () -> Unit,
    onLayersToggle: () -> Unit,
    onHistoryChange: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface)
            .padding(bottom = 12.dp)
    ) {
        // Adjustment Scrubber Bar
        if ((activeTab == "Lighting" || activeTab == "Colour") && activeTool != null) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
            ) {
                val value = when (activeTool) {
                    "Brightness" -> editState.brightness
                    "Contrast" -> editState.contrast
                    "Saturation" -> editState.saturation
                    "Warmth" -> editState.warmth
                    "Tint" -> editState.tint
                    "Tone" -> editState.tone
                    "BlackPoint" -> editState.blackPoint
                    "WhitePoint" -> editState.whitePoint
                    "Highlights" -> editState.highlights
                    "Shadows" -> editState.shadows
                    "Vignette" -> editState.vignette
                    "Skin tone" -> editState.skinTone
                    "Blue Tone" -> editState.blueTone
                    "Hue" -> editState.hue
                    "B/W" -> editState.bw
                    else -> 0.5f
                }
                
                val range = if (activeTool in listOf("Brightness", "Contrast", "Saturation")) 0f..2f else 0f..1f
                val defaultVal = when(activeTool) {
                    "Brightness", "Contrast", "Saturation" -> 1f
                    "Vignette", "B/W" -> 0f
                    else -> 0.5f
                }
                
                HorizontalScrubber(
                    allowNegative = range.start < 0f,
                    currentValue = value,
                    defaultValue = defaultVal,
                    minValue = range.start,
                    maxValue = range.endInclusive,
                    textColor = MaterialTheme.colorScheme.onSurface,
                    normalColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                    highlightedColor = MaterialTheme.colorScheme.primary,
                    arrowColor = MaterialTheme.colorScheme.primary,
                    displayValue = { (it * 100).roundToInt().toString() },
                    onValueChanged = { isScrolling, newValue ->
                        when(activeTool) {
                            "Brightness" -> editState.brightness = newValue
                            "Contrast" -> editState.contrast = newValue
                            "Saturation" -> editState.saturation = newValue
                            "Warmth" -> editState.warmth = newValue
                            "Tint" -> editState.tint = newValue
                            "Tone" -> editState.tone = newValue
                            "BlackPoint" -> editState.blackPoint = newValue
                            "WhitePoint" -> editState.whitePoint = newValue
                            "Highlights" -> editState.highlights = newValue
                            "Shadows" -> editState.shadows = newValue
                            "Vignette" -> editState.vignette = newValue
                            "Skin tone" -> editState.skinTone = newValue
                            "Blue Tone" -> editState.blueTone = newValue
                            "Hue" -> editState.hue = newValue
                            "B/W" -> editState.bw = newValue
                        }
                        if (!isScrolling) {
                            onHistoryChange()
                        }
                    }
                )
            }
        } else {
            Spacer(modifier = Modifier.height(48.dp))
        }

        // Circular Tools Row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            when (activeTab) {
                "Smart" -> {
                    ToolItem("Cutout", Icons.Default.ContentCut, activeTool == "Cutout") {
                        onCutoutRequested()
                    }
                    ToolItem("Layers", Icons.Default.Layers, false) {
                        onLayersToggle()
                    }
                }
                "Lighting" -> {
                    val lightingTools = listOf(
                        "Brightness" to Icons.Default.LightMode,
                        "Tone" to Icons.Default.Tune,
                        "Contrast" to Icons.Default.Contrast,
                        "BlackPoint" to Icons.Default.ExposureNeg1,
                        "WhitePoint" to Icons.Default.ExposurePlus1,
                        "Highlights" to Icons.Default.Highlight,
                        "Shadows" to Icons.Default.WbTwilight,
                        "Vignette" to Icons.Default.Vignette
                    )
                    lightingTools.forEach { (name, icon) ->
                        ToolItem(name, icon, activeTool == name) {
                            onToolSelected(if (activeTool == name) null else name)
                        }
                    }
                }
                "Colour" -> {
                    val colourTools = listOf(
                        "Saturation" to Icons.Default.Palette,
                        "Warmth" to Icons.Default.WbSunny,
                        "Tint" to Icons.Default.Colorize,
                        "Skin tone" to Icons.Default.Face,
                        "Blue Tone" to Icons.Default.Water,
                        "Hue" to Icons.Default.Gradient,
                        "B/W" to Icons.Default.FilterBAndW
                    )
                    colourTools.forEach { (name, icon) ->
                        ToolItem(name, icon, activeTool == name) {
                            onToolSelected(if (activeTool == name) null else name)
                        }
                    }
                }
                "Filters" -> {
                    val filters = EditFilters.getFilters()
                    filters.forEach { filter ->
                        FilterOption(filter.name, activeTool == filter.name) {
                            onToolSelected(filter.name)
                            editState.customMatrix = filter.matrix
                            onHistoryChange()
                        }
                    }
                }
                "Markup" -> {
                    val markupTools = listOf(
                        "Stylus" to Icons.Stylus,
                        "Highlighter" to Icons.InkHighlighter,
                        "Marker" to Icons.InkMarker,
                        "Blur" to Icons.Default.BlurOn,
                        "Mosaic" to Icons.Default.GridOn
                    )
                    markupTools.forEach { (name, icon) ->
                        ToolItem(name, icon, activeTool == name) {
                            onToolSelected(if (activeTool == name) null else name)
                        }
                    }
                }
                "Effects" -> {
                    val effectsTools = listOf(
                        "Posterize" to Icons.Default.PhotoFilter,
                        "Edges" to Icons.Default.BorderOuter,
                        "Borders" to Icons.Default.CropSquare
                    )
                    effectsTools.forEach { (name, icon) ->
                        ToolItem(name, icon, activeTool == name) {
                            onToolSelected(if (activeTool == name) null else name)
                        }
                    }
                }
            }
        }

        // Tabs Row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            val tabs = listOf("Smart", "Lighting", "Filters", "Markup", "Colour", "Effects")
            tabs.forEach { tab ->
                val isSelected = tab == activeTab
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(50))
                        .background(if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant)
                        .clickable { onTabSelected(tab); onToolSelected(null) }
                        .padding(horizontal = 18.dp, vertical = 8.dp)
                ) {
                    Text(
                        tab,
                        color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 14.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                    )
                }
            }
        }
    }
}

@Composable
fun ToolItem(name: String, icon: ImageVector, isSelected: Boolean, onClick: () -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.clickable(interactionSource = remember { MutableInteractionSource() }, indication = null) { onClick() }
    ) {
        Text(
            name,
            color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
            fontSize = 12.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        Box(
            modifier = Modifier
                .size(52.dp)
                .clip(CircleShape)
                .background(if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant)
                .border(
                    width = if (isSelected) 2.dp else 1.dp,
                    color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                    shape = CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                icon,
                contentDescription = name,
                tint = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.size(22.dp)
            )
        }
    }
}

@Composable
fun FilterOption(name: String, isSelected: Boolean, onClick: () -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clickable(interactionSource = remember { MutableInteractionSource() }, indication = null, onClick = onClick)
            .padding(horizontal = 8.dp)
    ) {
        Text(
            text = name,
            color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
            fontSize = 12.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        Box(
            modifier = Modifier
                .size(52.dp)
                .clip(RoundedCornerShape(14.dp))
                .background(if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant)
                .border(
                    width = if (isSelected) 2.dp else 1.dp,
                    color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                    shape = RoundedCornerShape(14.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Outlined.PhotoLibrary,
                contentDescription = null,
                tint = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.size(22.dp)
            )
        }
    }
}

private fun createMosaicBitmap(src: Bitmap): Bitmap {
    val scale = 0.04f
    val smallW = (src.width * scale).toInt().coerceAtLeast(8)
    val smallH = (src.height * scale).toInt().coerceAtLeast(8)
    val small = Bitmap.createScaledBitmap(src, smallW, smallH, false)
    val mosaic = Bitmap.createScaledBitmap(small, src.width, src.height, false)
    small.recycle()
    return mosaic
}

private fun createBlurredBitmap(src: Bitmap): Bitmap {
    val scale = 0.15f
    val smallW = (src.width * scale).toInt().coerceAtLeast(16)
    val smallH = (src.height * scale).toInt().coerceAtLeast(16)
    val small = Bitmap.createScaledBitmap(src, smallW, smallH, true)
    val blurred = fastBlur(small, 14)
    return Bitmap.createScaledBitmap(blurred, src.width, src.height, true)
}

private fun fastBlur(sentBitmap: Bitmap, radius: Int): Bitmap {
    val bitmap = sentBitmap.copy(sentBitmap.config, true)
    if (radius < 1) return bitmap
    val w = bitmap.width
    val h = bitmap.height
    val pix = IntArray(w * h)
    bitmap.getPixels(pix, 0, w, 0, 0, w, h)
    val wm = w - 1
    val hm = h - 1
    val wh = w * h
    val div = radius + radius + 1
    val r = IntArray(wh)
    val g = IntArray(wh)
    val b = IntArray(wh)
    var rsum: Int
    var gsum: Int
    var bsum: Int
    var x: Int
    var y: Int
    var i: Int
    var p: Int
    var yp: Int
    var yi: Int
    var yw: Int
    val vmin = IntArray(Math.max(w, h))
    var divsum = div + 1 shr 1
    divsum *= divsum
    val dv = IntArray(256 * divsum)
    i = 0
    while (i < 256 * divsum) {
        dv[i] = i / divsum
        i++
    }
    yi = 0
    yw = 0
    val stack = Array(div) { IntArray(3) }
    var stackpointer: Int
    var stackstart: Int
    var rbs: Int
    val r1 = radius + 1
    var rsumin: Int
    var gsumin: Int
    var bsumin: Int
    var rsumout: Int
    var gsumout: Int
    var bsumout: Int
    y = 0
    while (y < h) {
        bsum = 0
        gsum = bsum
        rsum = gsum
        bsumin = rsum
        gsumin = bsumin
        rsumin = gsumin
        bsumout = rsumin
        gsumout = bsumout
        rsumout = gsumout
        i = -radius
        while (i <= radius) {
            p = pix[yi + Math.min(wm, Math.max(i, 0))]
            val sir = stack[i + radius]
            sir[0] = p and 0xff0000 shr 16
            sir[1] = p and 0x00ff00 shr 8
            sir[2] = p and 0x0000ff
            rbs = r1 - Math.abs(i)
            rsum += sir[0] * rbs
            gsum += sir[1] * rbs
            bsum += sir[2] * rbs
            if (i > 0) {
                rsumin += sir[0]
                gsumin += sir[1]
                bsumin += sir[2]
            } else {
                rsumout += sir[0]
                gsumout += sir[1]
                bsumout += sir[2]
            }
            i++
        }
        stackpointer = radius
        x = 0
        while (x < w) {
            r[yi] = dv[rsum]
            g[yi] = dv[gsum]
            b[yi] = dv[bsum]
            rsum -= rsumout
            gsum -= gsumout
            bsum -= bsumout
            stackstart = stackpointer - radius + div
            val sir = stack[stackstart % div]
            rsumout -= sir[0]
            gsumout -= sir[1]
            bsumout -= sir[2]
            if (y == 0) {
                vmin[x] = Math.min(x + radius + 1, wm)
            }
            p = pix[yw + vmin[x]]
            sir[0] = p and 0xff0000 shr 16
            sir[1] = p and 0x00ff00 shr 8
            sir[2] = p and 0x0000ff
            rsumin += sir[0]
            gsumin += sir[1]
            bsumin += sir[2]
            rsum += rsumin
            gsum += gsumin
            bsum += bsumin
            stackpointer = (stackpointer + 1) % div
            val sir2 = stack[stackpointer % div]
            rsumout += sir2[0]
            gsumout += sir2[1]
            bsumout += sir2[2]
            rsumin -= sir2[0]
            gsumin -= sir2[1]
            bsumin -= sir2[2]
            yi++
            x++
        }
        yw += w
        y++
    }
    x = 0
    while (x < w) {
        bsum = 0
        gsum = bsum
        rsum = gsum
        bsumin = rsum
        gsumin = bsumin
        rsumin = gsumin
        bsumout = rsumin
        gsumout = bsumout
        rsumout = gsumout
        yp = -radius * w
        i = -radius
        while (i <= radius) {
            yi = Math.max(0, yp) + x
            val sir = stack[i + radius]
            sir[0] = r[yi]
            sir[1] = g[yi]
            sir[2] = b[yi]
            rbs = r1 - Math.abs(i)
            rsum += r[yi] * rbs
            gsum += g[yi] * rbs
            bsum += b[yi] * rbs
            if (i > 0) {
                rsumin += sir[0]
                gsumin += sir[1]
                bsumin += sir[2]
            } else {
                rsumout += sir[0]
                gsumout += sir[1]
                bsumout += sir[2]
            }
            if (i < hm) {
                yp += w
            }
            i++
        }
        yi = x
        stackpointer = radius
        y = 0
        while (y < h) {
            pix[yi] = -0x1000000 and pix[yi] or (dv[rsum] shl 16) or (dv[gsum] shl 8) or dv[bsum]
            rsum -= rsumout
            gsum -= gsumout
            bsum -= bsumout
            stackstart = stackpointer - radius + div
            val sir = stack[stackstart % div]
            rsumout -= sir[0]
            gsumout -= sir[1]
            bsumout -= sir[2]
            if (x == 0) {
                vmin[y] = Math.min(y + r1, hm) * w
            }
            p = x + vmin[y]
            sir[0] = r[p]
            sir[1] = g[p]
            sir[2] = b[p]
            rsumin += sir[0]
            gsumin += sir[1]
            bsumin += sir[2]
            rsum += rsumin
            gsum += gsumin
            bsum += bsumin
            stackpointer = (stackpointer + 1) % div
            val sir2 = stack[stackpointer % div]
            rsumout += sir2[0]
            gsumout += sir2[1]
            bsumout += sir2[2]
            rsumin -= sir2[0]
            gsumin -= sir2[1]
            bsumin -= sir2[2]
            yi += w
            y++
        }
        x++
    }
    bitmap.setPixels(pix, 0, w, 0, 0, w, h)
    return bitmap
}

suspend fun processCutout(context: android.content.Context, uri: Uri): Uri? {
    return withContext(Dispatchers.IO) {
        try {
            val options = SubjectSegmenterOptions.Builder()
                .enableForegroundBitmap()
                .enableForegroundConfidenceMask()
                .build()
            val segmenter = SubjectSegmentation.getClient(options)
            val image = InputImage.fromFilePath(context, uri)
            
            val result = segmenter.process(image).await()
            val fgBitmap = result.foregroundBitmap
            val maskBuffer = result.foregroundConfidenceMask

            if (fgBitmap != null && maskBuffer != null) {
                val width = fgBitmap.width
                val height = fgBitmap.height
                val cutoutBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
                val canvas = Canvas(cutoutBitmap)
                val paint = Paint(Paint.ANTI_ALIAS_FLAG)
                canvas.drawBitmap(fgBitmap, 0f, 0f, paint)

                val pixels = IntArray(width * height)
                cutoutBitmap.getPixels(pixels, 0, width, 0, 0, width, height)
                maskBuffer.rewind()
                
                for (i in 0 until (width * height)) {
                    if (maskBuffer.hasRemaining()) {
                        val confidence = maskBuffer.get()
                        val alpha = (confidence * 255).toInt().coerceIn(0, 255)
                        val originalColor = pixels[i]
                        pixels[i] = (alpha shl 24) or (originalColor and 0x00FFFFFF)
                    }
                }
                cutoutBitmap.setPixels(pixels, 0, width, 0, 0, width, height)

                val tempFile = File(context.cacheDir, "cutout_${System.currentTimeMillis()}.png")
                FileOutputStream(tempFile).use { out ->
                    cutoutBitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
                }
                Uri.fromFile(tempFile)
            } else if (fgBitmap != null) {
                val tempFile = File(context.cacheDir, "cutout_${System.currentTimeMillis()}.png")
                FileOutputStream(tempFile).use { out ->
                    fgBitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
                }
                Uri.fromFile(tempFile)
            } else {
                null
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}

suspend fun saveEditedImage(
    context: android.content.Context, 
    uri: Uri, 
    colorMatrix: androidx.compose.ui.graphics.ColorMatrix,
    paths: List<DrawingPath> = emptyList(),
    blurredBitmap: Bitmap? = null,
    mosaicBitmap: Bitmap? = null
): Boolean {
    return withContext(Dispatchers.IO) {
        try {
            val source = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                ImageDecoder.createSource(context.contentResolver, uri)
            } else {
                return@withContext false
            }
            val originalBitmap = ImageDecoder.decodeBitmap(source) { decoder, _, _ ->
                decoder.isMutableRequired = true
            }
            
            val resultBitmap = Bitmap.createBitmap(originalBitmap.width, originalBitmap.height, originalBitmap.config)
            val canvas = Canvas(resultBitmap)
            val paint = Paint().apply {
                colorFilter = ColorMatrixColorFilter(colorMatrix.values)
            }
            
            canvas.drawBitmap(originalBitmap, 0f, 0f, paint)
            
            if (paths.isNotEmpty()) {
                val scaleFactor = originalBitmap.width / 1080f
                paths.forEach { dp ->
                    val pathPaint = Paint().apply {
                        isAntiAlias = true
                        style = Paint.Style.STROKE
                        strokeWidth = dp.strokeWidth * scaleFactor
                        strokeCap = Paint.Cap.ROUND
                        strokeJoin = Paint.Join.ROUND
                        color = android.graphics.Color.argb(
                            (dp.alpha * 255).toInt(),
                            (dp.color.red * 255).toInt(),
                            (dp.color.green * 255).toInt(),
                            (dp.color.blue * 255).toInt()
                        )
                        if (dp.toolType == ToolType.BLUR && blurredBitmap != null) {
                            shader = BitmapShader(blurredBitmap, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP)
                        } else if (dp.toolType == ToolType.MOSAIC && mosaicBitmap != null) {
                            shader = BitmapShader(mosaicBitmap, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP)
                        } else if (dp.blendMode == BlendMode.Multiply) {
                            blendMode = android.graphics.BlendMode.MULTIPLY
                        }
                    }
                    val androidPath = dp.path.asAndroidPath()
                    canvas.drawPath(androidPath, pathPaint)
                }
            }
            
            val values = ContentValues().apply {
                put(MediaStore.Images.Media.DISPLAY_NAME, "Edited_${System.currentTimeMillis()}.jpg")
                put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
                put(MediaStore.Images.Media.RELATIVE_PATH, Environment.DIRECTORY_PICTURES + "/FileManager")
            }
            
            val destUri = context.contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)
            if (destUri != null) {
                context.contentResolver.openOutputStream(destUri)?.use { out ->
                    resultBitmap.compress(Bitmap.CompressFormat.JPEG, 100, out)
                }
                true
            } else {
                false
            }
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
}
