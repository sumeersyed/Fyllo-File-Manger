package com.fyllo.filemanager.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.fyllo.filemanager.ui.theme.SoftBackground
import com.fyllo.filemanager.ui.theme.SoftSurface

@Composable
fun DeleteConfirmationDialog(
    fileName: String = "",
    itemCount: Int = 1,
    onConfirm: () -> Unit,
    onConfirmWithOptions: ((permanent: Boolean) -> Unit)? = null,
    onDismiss: () -> Unit
) {
    WaveDeleteConfirmationDialog(
        itemCount = itemCount,
        fileName = fileName,
        onConfirmDelete = { permanent ->
            if (onConfirmWithOptions != null) {
                onConfirmWithOptions(permanent)
            } else {
                onConfirm()
            }
        },
        onDismiss = onDismiss
    )
}
