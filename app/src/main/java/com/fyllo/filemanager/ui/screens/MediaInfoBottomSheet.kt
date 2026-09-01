package com.fyllo.filemanager.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.fyllo.filemanager.domain.model.FileItem
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MediaInfoBottomSheet(
    file: FileItem,
    onDismiss: () -> Unit,
    onEditLabel: () -> Unit,
    onShare: () -> Unit,
    onCopyClipboard: () -> Unit,
    onHide: () -> Unit,
    onUseAs: () -> Unit,
    onCopy: () -> Unit,
    onMove: () -> Unit,
    onAddToCollection: () -> Unit
) {
    val context = LocalContext.current
    val formatter = SimpleDateFormat("d MMMM yy • h:mm a", Locale.getDefault())
    val dateString = formatter.format(Date(file.lastModified))
    
    // For dimensions, we might need to extract it, but for now we show placeholder if unknown
    val dimensionsStr = "1080 × 1920 • 2 MP"
    val sizeStr = formatSize(file.sizeBytes)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = Color(0xFF1E1E1E).copy(alpha = 0.85f),
        dragHandle = { BottomSheetDefaults.DragHandle(color = Color.Gray) }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            // Header
            Text(
                text = dateString,
                color = MaterialTheme.colorScheme.onBackground,
                fontSize = 18.sp,
                fontWeight = FontWeight.Medium
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Add a description",
                color = Color.Gray,
                fontSize = 14.sp,
                modifier = Modifier.clickable { onEditLabel() }
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Remove metadata button
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFF2C2C2C))
                    .clickable { /* TODO */ }
                    .padding(vertical = 12.dp),
                contentAlignment = Alignment.Center
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(Icons.Outlined.LocalFireDepartment, contentDescription = null, tint = MaterialTheme.colorScheme.onBackground, modifier = Modifier.size(16.dp))
                    Text("Remove metadata", color = MaterialTheme.colorScheme.onBackground, fontSize = 14.sp, fontWeight = FontWeight.Medium)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Metadata card
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color(0xFF2C2C2C))
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Label
                InfoRow(
                    icon = Icons.Outlined.Image,
                    title = "Label",
                    subtitle = file.name,
                    action = {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(16.dp))
                                .background(Color(0xFF404040))
                                .clickable { onEditLabel() }
                                .padding(horizontal = 16.dp, vertical = 6.dp)
                        ) {
                            Text("Edit", color = MaterialTheme.colorScheme.onBackground, fontSize = 14.sp)
                        }
                    }
                )
                
                // Path
                InfoRow(
                    icon = Icons.Outlined.Info,
                    title = "Path",
                    subtitle = file.path ?: "Unknown path"
                )
                
                // Dimensions
                InfoRow(
                    icon = Icons.Outlined.PhotoSizeSelectActual,
                    title = "Dimensions",
                    subtitle = "$dimensionsStr • $sizeStr"
                )
                
                // View all metadata
                InfoRow(
                    icon = Icons.Outlined.Info,
                    title = "View all metadata",
                    subtitle = "Metadata"
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Actions Grid
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                ActionChip(icon = Icons.Outlined.Share, text = "Share", modifier = Modifier.weight(1f)) { onShare() }
                ActionChip(icon = Icons.Outlined.ContentCopy, text = "Copy to clipboard", modifier = Modifier.weight(1f)) { onCopyClipboard() }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                ActionChip(icon = Icons.Outlined.Lock, text = "Hide", modifier = Modifier.weight(1f)) { onHide() }
                ActionChip(icon = Icons.Outlined.OpenInNew, text = "Use as", modifier = Modifier.weight(1f)) { onUseAs() }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                ActionChip(icon = Icons.Outlined.ContentCopy, text = "Copy", modifier = Modifier.weight(1f)) { onCopy() }
                ActionChip(icon = Icons.Outlined.DriveFileMove, text = "Move", modifier = Modifier.weight(1f)) { onMove() }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                ActionChip(icon = Icons.Outlined.Edit, text = "Edit", modifier = Modifier.weight(1f)) { onEditLabel() }
                ActionChip(icon = Icons.Outlined.Collections, text = "Add to collection", modifier = Modifier.weight(1f)) { onAddToCollection() }
            }
            
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
fun InfoRow(
    icon: ImageVector,
    title: String,
    subtitle: String,
    action: (@Composable () -> Unit)? = null
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(Color(0xFF383838)),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.onBackground, modifier = Modifier.size(20.dp))
        }
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(title, color = MaterialTheme.colorScheme.onBackground, fontSize = 16.sp, fontWeight = FontWeight.Medium)
            Spacer(modifier = Modifier.height(2.dp))
            Text(subtitle, color = Color.Gray, fontSize = 14.sp, maxLines = 2, overflow = TextOverflow.Ellipsis)
        }
        if (action != null) {
            Spacer(modifier = Modifier.width(8.dp))
            action()
        }
    }
}

@Composable
fun ActionChip(icon: ImageVector, text: String, modifier: Modifier = Modifier, onClick: () -> Unit) {
    Box(
        modifier = modifier
            .height(48.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0xFF2C2C2C))
            .clickable { onClick() }
            .padding(horizontal = 16.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.onBackground, modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.width(12.dp))
            Text(text, color = MaterialTheme.colorScheme.onBackground, fontSize = 14.sp, fontWeight = FontWeight.Medium)
        }
    }
}

private fun formatSize(size: Long): String {
    if (size <= 0) return "0 B"
    val units = arrayOf("B", "KB", "MB", "GB", "TB")
    val digitGroups = (Math.log10(size.toDouble()) / Math.log10(1024.0)).toInt()
    return String.format(Locale.US, "%.2f %s", size / Math.pow(1024.0, digitGroups.toDouble()), units[digitGroups])
}
