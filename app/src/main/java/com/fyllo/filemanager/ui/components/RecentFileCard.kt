package com.fyllo.filemanager.ui.components

import android.text.format.Formatter
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.Image
import androidx.compose.material.icons.outlined.MusicNote
import androidx.compose.material.icons.outlined.PlayCircle
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.fyllo.filemanager.domain.model.FileItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.foundation.border
import com.fyllo.filemanager.ui.theme.LocalEInkMode
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun RecentFileCard(
    file: FileItem,
    onClick: () -> Unit
) {
    val isMedia = file.mimeType?.startsWith("image/") == true || file.mimeType?.startsWith("video/") == true
    val isVideo = file.mimeType?.startsWith("video/") == true
    
    val iconVec = when {
        isVideo -> Icons.Outlined.PlayCircle
        file.mimeType?.startsWith("image/") == true -> Icons.Outlined.Image
        file.mimeType?.startsWith("audio/") == true -> Icons.Outlined.MusicNote
        else -> Icons.Outlined.Description
    }

    val isEInk = LocalEInkMode.current
    val shape = RoundedCornerShape(22.dp)

    val containerModifier = if (isEInk) {
        Modifier
            .width(135.dp)
            .height(175.dp)
            .clip(shape)
            .border(1.5.dp, MaterialTheme.colorScheme.outline, shape)
            .clickable(onClick = onClick)
            .background(Color.Transparent)
    } else {
        Modifier
            .width(135.dp)
            .height(175.dp)
            .clip(shape)
            .clickable(onClick = onClick)
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.12f), shape)
    }

    Box(
        modifier = containerModifier
    ) {
        // Media Preview or Fallback
        if (isMedia) {
            AsyncImage(
                model = file.uri,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        } else {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.surfaceVariant),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = iconVec,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f),
                    modifier = Modifier.size(44.dp)
                )
            }
        }

        // Soft Gradient Overlay
        if (!isEInk) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(90.dp)
                    .align(Alignment.BottomCenter)
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color.Transparent,
                                Color.Black.copy(alpha = 0.75f)
                            )
                        )
                    )
            )
        }

        // Bottom File Name Text / Badge
        Column(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .fillMaxWidth()
                .padding(horizontal = 10.dp, vertical = 10.dp)
        ) {
            Text(
                text = file.name,
                color = if (isEInk && !isMedia) MaterialTheme.colorScheme.onBackground else Color.White,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                modifier = if (isEInk && isMedia) Modifier.background(Color.Black.copy(alpha = 0.6f)).padding(horizontal = 4.dp) else Modifier
            )
        }

        // Top-left/center Play icon badge if video
        if (isVideo) {
            Box(
                modifier = Modifier
                    .align(Alignment.Center)
                    .size(36.dp)
                    .clip(RoundedCornerShape(18.dp))
                    .background(Color.Black.copy(alpha = 0.45f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Outlined.PlayCircle,
                    contentDescription = "Video",
                    tint = Color.White,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}
