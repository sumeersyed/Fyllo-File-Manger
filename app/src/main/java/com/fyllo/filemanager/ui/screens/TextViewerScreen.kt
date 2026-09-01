package com.fyllo.filemanager.ui.screens

import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.File
import java.io.InputStreamReader

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TextViewerScreen(
    uriStr: String,
    onBackClick: () -> Unit
) {
    val context = LocalContext.current
    var textContent by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var fileName by remember { mutableStateOf("Text Document") }

    LaunchedEffect(uriStr) {
        withContext(Dispatchers.IO) {
            try {
                val uri = Uri.parse(uriStr)
                fileName = uri.lastPathSegment ?: "Text Document"
                
                val inputStream = if (uri.scheme == "content") {
                    context.contentResolver.openInputStream(uri)
                } else {
                    val file = File(uri.path ?: "")
                    fileName = file.name
                    file.inputStream()
                }

                if (inputStream != null) {
                    val reader = BufferedReader(InputStreamReader(inputStream))
                    val builder = StringBuilder()
                    var line: String?
                    var lineCount = 0
                    // Read up to 5000 lines for performance
                    while (reader.readLine().also { line = it } != null && lineCount < 5000) {
                        builder.append(line).append("\n")
                        lineCount++
                    }
                    reader.close()
                    inputStream.close()
                    textContent = builder.toString()
                } else {
                    errorMessage = "Could not read file content"
                }
            } catch (e: Exception) {
                errorMessage = e.localizedMessage ?: "Failed to load file"
            } finally {
                isLoading = false
            }
        }
    }

    var isEditing by remember { mutableStateOf(false) }
    var editedText by remember { mutableStateOf("") }
    var isSaving by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(textContent) {
        editedText = textContent
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(fileName, maxLines = 1) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    if (!isLoading && errorMessage == null) {
                        if (isEditing) {
                            TextButton(
                                onClick = {
                                    scope.launch {
                                        isSaving = true
                                        withContext(Dispatchers.IO) {
                                            try {
                                                val uri = Uri.parse(uriStr)
                                                if (uri.scheme == "content") {
                                                    context.contentResolver.openOutputStream(uri, "wt")?.use { out ->
                                                        out.write(editedText.toByteArray(Charsets.UTF_8))
                                                    }
                                                } else {
                                                    val file = File(uri.path ?: "")
                                                    file.writeText(editedText, Charsets.UTF_8)
                                                }
                                                withContext(Dispatchers.Main) {
                                                    textContent = editedText
                                                    isEditing = false
                                                    android.widget.Toast.makeText(context, "Saved successfully", android.widget.Toast.LENGTH_SHORT).show()
                                                }
                                            } catch (e: Exception) {
                                                withContext(Dispatchers.Main) {
                                                    android.widget.Toast.makeText(context, "Failed to save: ${e.message}", android.widget.Toast.LENGTH_SHORT).show()
                                                }
                                            } finally {
                                                isSaving = false
                                            }
                                        }
                                    }
                                }
                            ) {
                                Text("Save", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                            }
                        } else {
                            IconButton(onClick = { isEditing = true }) {
                                Icon(Icons.Default.Edit, contentDescription = "Edit", tint = MaterialTheme.colorScheme.primary)
                            }
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.onBackground
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            if (isLoading || isSaving) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            } else if (errorMessage != null) {
                Text(
                    text = errorMessage ?: "Error",
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.align(Alignment.Center)
                )
            } else if (isEditing) {
                TextField(
                    value = editedText,
                    onValueChange = { editedText = it },
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(8.dp),
                    textStyle = androidx.compose.ui.text.TextStyle(
                        fontFamily = FontFamily.Monospace,
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onBackground
                    ),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.5f),
                        unfocusedContainerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.3f),
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent
                    )
                )
            } else {
                val verticalScrollState = rememberScrollState()
                val horizontalScrollState = rememberScrollState()
                
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                        .verticalScroll(verticalScrollState)
                        .horizontalScroll(horizontalScrollState)
                ) {
                    Text(
                        text = textContent,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                }
            }
        }
    }
}
