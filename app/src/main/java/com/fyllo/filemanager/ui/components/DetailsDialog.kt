package com.fyllo.filemanager.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.fyllo.filemanager.domain.model.FileItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun DetailsDialog(
    file: FileItem,
    onDismiss: () -> Unit
) {
    val formatter = SimpleDateFormat("d MMMM yyyy • h:mm a", Locale.getDefault())
    val dateString = formatter.format(Date(file.lastModified))
    
    var sizeStr by remember { mutableStateOf(if (file.isFolder) "Calculating..." else formatSize(file.sizeBytes)) }
    var itemsCountStr by remember { mutableStateOf<String?>(null) }
    
    LaunchedEffect(file) {
        if (file.isFolder) {
            withContext(Dispatchers.IO) {
                var totalBytes = 0L
                var count = 0
                val f = File(file.path ?: "")
                if (f.exists() && f.isDirectory) {
                    val list = f.listFiles()
                    if (list != null) {
                        count = list.size
                    }
                    f.walkTopDown().forEach { child ->
                        if (child.isFile) {
                            totalBytes += child.length()
                        }
                    }
                }
                sizeStr = formatSize(totalBytes)
                itemsCountStr = "$count items"
            }
        }
    }

    Dialog(onDismissRequest = onDismiss) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(Color.Black)
                .padding(24.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Text(
                    text = "File Details",
                    color = Color.White,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )

                DetailRow(label = "Name", value = file.name)
                DetailRow(label = "Path", value = file.path ?: "Unknown")
                DetailRow(label = "Type", value = if (file.isFolder) "Folder" else (file.mimeType ?: "Unknown"))
                if (file.isFolder && itemsCountStr != null) {
                    DetailRow(label = "Contents", value = itemsCountStr!!)
                }
                DetailRow(label = "Size", value = sizeStr)
                DetailRow(label = "Modified", value = dateString)

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Close", color = Color.White, fontWeight = FontWeight.Medium)
                    }
                }
            }
        }
    }
}

@Composable
fun DetailRow(label: String, value: String) {
    Column {
        Text(
            text = label,
            color = Color.Gray,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = value,
            color = Color.White,
            fontSize = 14.sp
        )
    }
}

private fun formatSize(size: Long): String {
    if (size <= 0) return "0 B"
    val units = arrayOf("B", "KB", "MB", "GB", "TB")
    val digitGroups = (Math.log10(size.toDouble()) / Math.log10(1024.0)).toInt()
    return String.format(Locale.US, "%.2f %s", size / Math.pow(1024.0, digitGroups.toDouble()), units[digitGroups])
}
