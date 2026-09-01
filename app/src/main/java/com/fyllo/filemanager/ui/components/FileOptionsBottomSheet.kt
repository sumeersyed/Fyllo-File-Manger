package com.fyllo.filemanager.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Compress
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DriveFileMove
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Unarchive
import androidx.compose.material.icons.outlined.DriveFileRenameOutline
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.fyllo.filemanager.ui.theme.SoftBackground

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FileOptionsBottomSheet(
    fileName: String,
    onDismiss: () -> Unit,
    onDeleteClick: () -> Unit,
    onRenameClick: () -> Unit,
    onMoveClick: () -> Unit,
    onCopyClick: () -> Unit,
    onCompressClick: () -> Unit,
    onCopyClipboardClick: () -> Unit = onCopyClick,
    onCutClipboardClick: () -> Unit = onMoveClick,
    onShareClick: () -> Unit = {},
    onDetailsClick: () -> Unit = {},
    onFavoriteClick: () -> Unit = {},
    onSafeFolderClick: () -> Unit = {},
    onExtractClick: () -> Unit = {},
    isArchive: Boolean = false
) {
    val sheetState = rememberModalBottomSheetState()

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = androidx.compose.material3.MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 32.dp)
        ) {
            Text(
                text = fileName,
                color = androidx.compose.material3.MaterialTheme.colorScheme.onSurface,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
                modifier = Modifier.padding(horizontal = 24.dp, vertical = 12.dp)
            )

            FileOptionItem(icon = Icons.Default.Share, text = "Share", onClick = onShareClick)
            FileOptionItem(icon = Icons.Default.ContentCopy, text = "Copy (to Clipboard)", onClick = onCopyClipboardClick)
            FileOptionItem(icon = Icons.Default.DriveFileMove, text = "Move / Cut (to Clipboard)", onClick = onCutClipboardClick)
            FileOptionItem(icon = Icons.Outlined.DriveFileRenameOutline, text = "Rename", onClick = onRenameClick)
            if (isArchive) {
                FileOptionItem(icon = Icons.Default.Unarchive, text = "Extract Archive", onClick = onExtractClick)
            } else {
                FileOptionItem(icon = Icons.Default.Compress, text = "Compress to Zip", onClick = onCompressClick)
            }
            FileOptionItem(icon = Icons.Default.Info, text = "Details", onClick = onDetailsClick)
            FileOptionItem(icon = Icons.Default.Favorite, text = "Add to Favorites", onClick = onFavoriteClick)
            FileOptionItem(icon = Icons.Default.Lock, text = "Move to Vault", onClick = onSafeFolderClick)
            
            FileOptionItem(
                icon = Icons.Default.Delete,
                text = "Delete",
                color = androidx.compose.material3.MaterialTheme.colorScheme.error,
                onClick = onDeleteClick
            )
        }
    }
}

@Composable
fun FileOptionItem(
    icon: ImageVector,
    text: String,
    color: Color = androidx.compose.material3.MaterialTheme.colorScheme.onSurface,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 24.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = color,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.width(16.dp))
        Text(
            text = text,
            color = color,
            fontSize = 15.sp,
            fontWeight = FontWeight.Medium
        )
    }
}
