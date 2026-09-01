package com.fyllo.filemanager.ui.screens

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
import com.fyllo.filemanager.domain.model.FileItem

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditMetadataDialog(
    file: FileItem,
    onDismiss: () -> Unit,
    onConfirm: (newName: String, newDescription: String) -> Unit
) {
    var label by remember { mutableStateOf(file.name) }
    var description by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Color(0xFF1E1E1E),
        title = {
            Text(
                "Edit Metadata",
                color = Color.White,
                fontSize = 20.sp,
                modifier = Modifier.fillMaxWidth(),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Label TextField
                TextField(
                    value = label,
                    onValueChange = { label = it },
                    label = { Text("Label", color = Color.Gray) },
                    colors = TextFieldDefaults.textFieldColors(
                        containerColor = Color(0xFF383838),
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent,
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                // Description TextField
                TextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Description", color = Color.Gray) },
                    colors = TextFieldDefaults.textFieldColors(
                        containerColor = Color(0xFF383838),
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent,
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth().height(120.dp),
                    maxLines = 5
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onConfirm(label, description) },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF8AB4F8)),
                shape = RoundedCornerShape(24.dp),
                modifier = Modifier.height(48.dp)
            ) {
                Text("Confirm", color = Color.Black, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF404040)),
                shape = RoundedCornerShape(24.dp),
                modifier = Modifier.height(48.dp)
            ) {
                Text("Cancel", color = Color.White, fontWeight = FontWeight.Medium)
            }
        },
        modifier = Modifier.clip(RoundedCornerShape(24.dp))
    )
}
