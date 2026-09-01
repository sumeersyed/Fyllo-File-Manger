package com.fyllo.filemanager.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.viewmodel.compose.viewModel
import android.net.Uri
import android.os.Environment
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.fyllo.filemanager.di.ViewModelFactory
import com.fyllo.filemanager.ui.screens.CategoriesScreen
import androidx.navigation.navArgument
import androidx.navigation.NavType
import com.fyllo.filemanager.ui.screens.EditScreen
import com.fyllo.filemanager.ui.screens.FilesScreen
import com.fyllo.filemanager.ui.screens.FilesViewModel
import com.fyllo.filemanager.ui.screens.HomeDashboardScreen
import com.fyllo.filemanager.ui.screens.HomeViewModel
import com.fyllo.filemanager.ui.screens.ManageScreen
import com.fyllo.filemanager.ui.screens.ManageViewModel
import com.fyllo.filemanager.ui.screens.MediaScreen
import com.fyllo.filemanager.ui.screens.MediaViewModel
import com.fyllo.filemanager.ui.screens.SafeFolderScreen
import com.fyllo.filemanager.ui.screens.SearchScreen
import com.fyllo.filemanager.ui.screens.SearchViewModel
import com.fyllo.filemanager.ui.screens.SettingsScreen
import com.fyllo.filemanager.ui.screens.SettingsViewModel
import com.fyllo.filemanager.ui.screens.TrashScreen
import com.fyllo.filemanager.ui.screens.TrashViewModel

sealed class Screen(val route: String) {
    object Onboarding : Screen("onboarding")
    object Home : Screen("home")
    object Categories : Screen("categories")
    object Manage : Screen("manage")
    object Media : Screen("media/{filterType}") {
        fun createRoute(filterType: String) = "media/$filterType"
    }
    object Files : Screen("files?uri={uri}") {
        fun createRoute(uri: String? = null): String {
            return if (uri != null) "files?uri=${android.net.Uri.encode(uri)}" else "files"
        }
    }
    object Apps : Screen("apps/{filterGames}") {
        fun createRoute(filterGames: Boolean) = "apps/$filterGames"
    }
    object Search : Screen("search")
    object Settings : Screen("settings")
    object Edit : Screen("edit/{uri}") {
        fun createRoute(uri: String) = "edit/${android.net.Uri.encode(uri)}"
    }
    object SafeFolder : Screen("safe_folder")
    object Cleanup : Screen("cleanup")
    object Trash : Screen("trash")
    object PdfViewer : Screen("pdf_viewer/{uri}") {
        fun createRoute(uri: String) = "pdf_viewer/${android.net.Uri.encode(uri)}"
    }
    object TextViewer : Screen("text_viewer/{uri}") {
        fun createRoute(uri: String) = "text_viewer/${android.net.Uri.encode(uri)}"
    }
    object AppLock : Screen("app_lock")
    object MediaViewer : Screen("media_viewer/{uri}") {
        fun createRoute(uri: String) = "media_viewer/${android.net.Uri.encode(uri)}"
    }
    object AudioPlayer : Screen("audio_player")
}

@Composable
fun AppNavigation(
    navController: NavHostController = rememberNavController(),
    viewModelFactory: ViewModelFactory,
    startDestination: String
) {
    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        composable(Screen.Onboarding.route) {
            val settingsViewModel: com.fyllo.filemanager.ui.screens.SettingsViewModel = viewModel(factory = viewModelFactory)
            com.fyllo.filemanager.ui.screens.OnboardingScreen(
                onFinish = {
                    settingsViewModel.setOnboardingCompleted(true)
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Onboarding.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.Home.route) {
            val homeViewModel: HomeViewModel = viewModel(factory = viewModelFactory)
            val settingsViewModel: com.fyllo.filemanager.ui.screens.SettingsViewModel = viewModel(factory = viewModelFactory)
            val settingsState by settingsViewModel.settingsState.collectAsState()
            val storageStats by homeViewModel.storageStats.collectAsState()
            val recentFiles by homeViewModel.recentFiles.collectAsState()
            val categoryCounts by homeViewModel.categoryCounts.collectAsState()

            androidx.compose.runtime.LaunchedEffect(Unit) {
                homeViewModel.refreshRecentFiles()
            }

            val isDark = when (settingsState.themeMode) {
                "Dark" -> true
                "Light" -> false
                else -> androidx.compose.foundation.isSystemInDarkTheme()
            }

            HomeDashboardScreen(
                storageStats = storageStats,
                recentFiles = recentFiles,
                categoryCounts = categoryCounts,
                onNavigateToManage = { navController.navigate(Screen.Manage.route) },
                onNavigateToCleanup = { navController.navigate(Screen.Cleanup.route) },
                onNavigateToFiles = { uri -> navController.navigate(Screen.Files.createRoute(uri)) },
                onNavigateToMedia = { filterType -> navController.navigate(Screen.Media.createRoute(filterType)) },
                onNavigateToApps = { filterGames -> navController.navigate(Screen.Apps.createRoute(filterGames)) },
                onNavigateToTrash = { navController.navigate(Screen.Trash.route) },
                onNavigateToSettings = { navController.navigate(Screen.Settings.route) },
                onNavigateToSearch = { navController.navigate(Screen.Search.route) },
                onNavigateToSafeFolder = { navController.navigate(Screen.SafeFolder.route) },
                isDarkTheme = isDark,
                onToggleDarkMode = { enableDark ->
                    settingsViewModel.setThemeMode(if (enableDark) "Dark" else "Light")
                }
            )
        }
        
        composable(Screen.Categories.route) {
            CategoriesScreen(
                onBackClick = { navController.navigateUp() }
            )
        }
        
        composable(Screen.Manage.route) {
            val manageViewModel: ManageViewModel = viewModel(factory = viewModelFactory)
            val storageStats by manageViewModel.storageStats.collectAsState()
            val junkSize by manageViewModel.junkSize.collectAsState()

            ManageScreen(
                storageStats = storageStats,
                junkSize = junkSize,
                onBackClick = { navController.navigateUp() },
                viewModel = manageViewModel,
                onFileClick = { file ->
                    if (file.mimeType?.startsWith("image/") == true || file.mimeType?.startsWith("video/") == true) {
                        navController.navigate(Screen.MediaViewer.createRoute(file.uri.toString()))
                    } else {
                        val encoded = java.net.URLEncoder.encode(file.path, "UTF-8")
                        navController.navigate(Screen.Files.createRoute(encoded))
                    }
                },
                onFolderClick = { path ->
                    val encoded = java.net.URLEncoder.encode(path, "UTF-8")
                    navController.navigate(Screen.Files.createRoute(encoded))
                }
            )
        }

        composable(Screen.Cleanup.route) {
            val cleanupViewModel: com.fyllo.filemanager.ui.screens.StorageCleanupViewModel = viewModel(factory = viewModelFactory)
            com.fyllo.filemanager.ui.screens.StorageCleanupScreen(
                viewModel = cleanupViewModel,
                onBackClick = { navController.navigateUp() },
                onFileClick = { file ->
                    if (file.mimeType?.startsWith("image/") == true || file.mimeType?.startsWith("video/") == true) {
                        navController.navigate(Screen.MediaViewer.createRoute(file.uri.toString()))
                    } else {
                        val encoded = java.net.URLEncoder.encode(file.path, "UTF-8")
                        navController.navigate(Screen.Files.createRoute(encoded))
                    }
                }
            )
        }
        
        composable(
            route = Screen.Files.route,
            arguments = listOf(navArgument("uri") { 
                type = NavType.StringType 
                nullable = true
                defaultValue = null
            })
        ) { backStackEntry ->
            val filesViewModel: FilesViewModel = viewModel(factory = viewModelFactory)
            val files by filesViewModel.files.collectAsState()
            val isLoading by filesViewModel.isLoading.collectAsState()
            val currentUri by filesViewModel.currentPath.collectAsState()
            val isGridView by filesViewModel.isGridView.collectAsState()
            val currentSortOption by filesViewModel.currentSortOption.collectAsState()
            val clipboardState by filesViewModel.clipboard.collectAsState()
            val operationState by filesViewModel.operationState.collectAsState()
            val conflictDialogState by filesViewModel.conflictDialogState.collectAsState()
            val context = androidx.compose.ui.platform.LocalContext.current
            val initialUriStr = backStackEntry.arguments?.getString("uri")

            LaunchedEffect(initialUriStr) {
                val uriToLoad = if (initialUriStr != null) {
                    val decoded = if (initialUriStr.startsWith("%") || initialUriStr.contains("%2F") || initialUriStr.contains("%3A")) {
                        try { java.net.URLDecoder.decode(initialUriStr, "UTF-8") } catch (_: Exception) { initialUriStr }
                    } else {
                        initialUriStr
                    }
                    if (decoded.startsWith("/")) {
                        Uri.fromFile(java.io.File(decoded))
                    } else if (decoded.startsWith("file:/") || decoded.startsWith("content:/")) {
                        Uri.parse(decoded)
                    } else {
                        Uri.fromFile(java.io.File(decoded))
                    }
                } else if (currentUri == null) {
                    Uri.fromFile(Environment.getExternalStorageDirectory())
                } else {
                    currentUri
                }
                
                if (uriToLoad != null) {
                    filesViewModel.loadFiles(uriToLoad)
                }
            }

            FilesScreen(
                files = files,
                isLoading = isLoading,
                currentPath = currentUri?.path ?: "",
                isGridView = isGridView,
                currentSortOption = currentSortOption,
                clipboardState = clipboardState,
                operationState = operationState,
                conflictDialogState = conflictDialogState,
                onResolveConflict = { filesViewModel.resolveConflict(it) },
                onCancelConflict = { filesViewModel.cancelConflict() },
                onCancelOperation = { filesViewModel.cancelOperation() },
                onPasteClick = { filesViewModel.pasteFromClipboard() },
                onClearClipboardClick = { filesViewModel.clearClipboard() },
                onDeleteMultiple = { uris -> filesViewModel.deleteMultipleFiles(uris) },
                onDeleteWithOptions = { uris, permanent -> filesViewModel.deleteMultipleFiles(uris, permanent) },
                onCopyMultiple = { uris, isCut -> filesViewModel.copyToClipboard(uris, isCut) },
                onCompressMultiple = { uris -> filesViewModel.compressMultipleFiles(uris) },
                onExtractClick = { file -> filesViewModel.extractFile(file.uri) },
                onSetGridView = { filesViewModel.setGridView(it) },
                onSortFiles = { filesViewModel.sortFiles(it) },
                onCreateFolder = { filesViewModel.createFolder(it) },
                onBackClick = { navController.navigateUp() },
                onNavigateToSearch = { navController.navigate(Screen.Search.route) },
                onNavigateToHome = { navController.navigate(Screen.Home.route) },
                onNavigateToApps = { navController.navigate(Screen.Apps.createRoute(false)) },
                onNavigateToFolderByPath = { path -> filesViewModel.loadFiles(Uri.fromFile(java.io.File(path))) },
                onNavigateUp = { filesViewModel.navigateUp() },
                onFileClick = { file -> 
                    if (file.isFolder) {
                        filesViewModel.navigateToFolder(file.uri)
                    } else {
                        val ext = file.extension.lowercase()
                        val isAudio = file.mimeType?.startsWith("audio/") == true ||
                                ext in com.fyllo.filemanager.ui.screens.AUDIO_EXTENSIONS
                        val isMedia = file.mimeType?.startsWith("image/") == true ||
                                file.mimeType?.startsWith("video/") == true ||
                                ext in listOf("jpg", "jpeg", "png", "gif", "webp", "heic", "bmp", "mp4", "mkv", "avi", "mov", "3gp", "webm", "ts", "flv", "wmv", "m4v")

                        when {
                            isAudio -> {
                                val audioFiles = files.filter { f ->
                                    f.mimeType?.startsWith("audio/") == true ||
                                    f.extension.lowercase() in com.fyllo.filemanager.ui.screens.AUDIO_EXTENSIONS
                                }
                                val playlist = if (audioFiles.isEmpty()) listOf(file) else audioFiles
                                val index = playlist.indexOfFirst { it.uri == file.uri }.coerceAtLeast(0)
                                com.fyllo.filemanager.ui.screens.AudioPlaylist.files = playlist
                                com.fyllo.filemanager.ui.screens.AudioPlaylist.initialIndex = index
                                navController.navigate(Screen.AudioPlayer.route)
                            }
                            isMedia -> navController.navigate(Screen.MediaViewer.createRoute(file.uri.toString()))
                            ext in listOf("zip", "rar", "7z", "tar", "gz", "tgz", "bz2") ->
                                filesViewModel.extractFile(file.uri)
                            ext.equals("apk", ignoreCase = true) || file.mimeType == "application/vnd.android.package-archive" || file.name.endsWith(".apk", ignoreCase = true) || file.path.endsWith(".apk", ignoreCase = true) ->
                                com.fyllo.filemanager.core.ApkInstaller.installApk(context, file)
                            ext == "pdf" -> navController.navigate(Screen.PdfViewer.createRoute(file.uri.toString()))
                            ext in listOf("txt", "json", "xml", "kt", "py", "java", "csv", "md", "log", "html", "css", "js", "ts", "sh") ->
                                navController.navigate(Screen.TextViewer.createRoute(file.uri.toString()))
                            else -> {
                                if (file.mimeType == "application/vnd.android.package-archive" || file.name.endsWith(".apk", ignoreCase = true)) {
                                    com.fyllo.filemanager.core.ApkInstaller.installApk(context, file)
                                } else {
                                    try {
                                        val intent = android.content.Intent(android.content.Intent.ACTION_VIEW)
                                        val authority = "${context.packageName}.fileprovider"
                                        val uriToLaunch = if (file.uri.scheme == "file") {
                                            val javaFile = java.io.File(file.uri.path!!)
                                            androidx.core.content.FileProvider.getUriForFile(context, authority, javaFile)
                                        } else {
                                            file.uri
                                        }
                                        
                                        val mimeType = file.mimeType ?: "*/*"
                                        intent.setDataAndType(uriToLaunch, mimeType)
                                        intent.addFlags(android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                        intent.addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK)
                                        context.startActivity(intent)
                                    } catch (e: Exception) {
                                        android.widget.Toast.makeText(context, "Cannot open file: ${e.message}", android.widget.Toast.LENGTH_SHORT).show()
                                    }
                                }
                            }
                        }
                    }
                },
                onRenameClick = { file, newName -> filesViewModel.renameFile(file.uri, newName) },
                onMoveClick = { file, targetUri -> filesViewModel.moveFile(file.uri, targetUri) },
                onCopyClick = { file, targetUri -> filesViewModel.copyFile(file.uri, targetUri) },
                onCompressClick = { file -> filesViewModel.compressFile(file.uri, file.name) },
                onDeleteClick = { file -> filesViewModel.deleteFile(file.uri) }
            )
        }

        composable(
            route = Screen.Media.route,
            arguments = listOf(navArgument("filterType") { type = NavType.StringType })
        ) { backStackEntry ->
            val filterType = backStackEntry.arguments?.getString("filterType") ?: "all"
            val context = androidx.compose.ui.platform.LocalContext.current
            
            val mediaViewModel: MediaViewModel = viewModel(factory = viewModelFactory)
            val settingsViewModel: com.fyllo.filemanager.ui.screens.SettingsViewModel = viewModel(factory = viewModelFactory)
            val settingsState by settingsViewModel.settingsState.collectAsState()
            val filesViewModel: com.fyllo.filemanager.ui.screens.FilesViewModel = viewModel(factory = viewModelFactory)
            val mediaFiles by mediaViewModel.mediaFiles.collectAsState()
            val isLoading by mediaViewModel.isLoading.collectAsState()
            val mediaOperationState by mediaViewModel.operationState.collectAsState()

            LaunchedEffect(filterType) {
                mediaViewModel.loadMedia(filterType)
            }

            MediaScreen(
                mediaFiles = mediaFiles,
                isLoading = isLoading,
                filterType = filterType,
                operationState = mediaOperationState,
                enableScrollSound = settingsState.enableScrollSound,
                onCancelOperation = { mediaViewModel.cancelOperation() },
                onBackClick = { navController.navigateUp() },
                onLoadMedia = { mediaViewModel.loadMedia(filterType) },
                onNavigateToEdit = { file -> 
                    navController.navigate(Screen.Edit.createRoute(file.uri.toString()))
                },
                onDeleteClick = { file -> mediaViewModel.deleteMedia(file.uri) },
                onDeleteMultipleClick = { files -> mediaViewModel.deleteMultipleMedia(files.map { it.uri }) },
                onDeleteWithOptions = { uris, permanent -> mediaViewModel.deleteMultipleMedia(uris, permanent) },
                onCopyToClipboard = { uris, isCut -> filesViewModel.copyToClipboard(uris, isCut) },
                onCopyMultipleClick = { files, targetDir -> mediaViewModel.copyMultipleMedia(files.map { it.uri }, targetDir) },
                onMoveMultipleClick = { files, targetDir -> mediaViewModel.moveMultipleMedia(files.map { it.uri }, targetDir) },
                onRenameClick = { file, newName -> mediaViewModel.renameFile(file.uri, newName) },
                onCompressClick = { file -> filesViewModel.compressFile(file.uri, file.name) },
                onPlayAudio = { file, playlist ->
                    val index = playlist.indexOfFirst { it.uri == file.uri }.coerceAtLeast(0)
                    com.fyllo.filemanager.ui.screens.AudioPlaylist.files = playlist
                    com.fyllo.filemanager.ui.screens.AudioPlaylist.initialIndex = index
                    navController.navigate(Screen.AudioPlayer.route)
                },
                onFileClick = { file ->
                    val ext = file.extension.lowercase()
                    when {
                        ext == "pdf" -> navController.navigate(Screen.PdfViewer.createRoute(file.uri.toString()))
                        ext in listOf("txt", "json", "xml", "kt", "py", "java", "csv", "md", "log", "html", "css", "js", "ts", "sh", "doc", "docx") ->
                            navController.navigate(Screen.TextViewer.createRoute(file.uri.toString()))
                        else -> {
                            try {
                                val intent = android.content.Intent(android.content.Intent.ACTION_VIEW).apply {
                                    setDataAndType(file.uri, file.mimeType ?: "*/*")
                                    addFlags(android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                    addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK)
                                }
                                context.startActivity(intent)
                            } catch (e: Exception) {
                                android.widget.Toast.makeText(context, "Cannot open document: ${e.message}", android.widget.Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                }
            )
        }

        composable(
            route = Screen.Edit.route,
            arguments = listOf(navArgument("uri") { type = NavType.StringType })
        ) { backStackEntry ->
            val uriStr = backStackEntry.arguments?.getString("uri") ?: ""
            EditScreen(
                uriStr = uriStr,
                onBackClick = { navController.navigateUp() }
            )
        }
        
        composable(Screen.Search.route) {
            val searchViewModel: SearchViewModel = viewModel(factory = viewModelFactory)
            val filesViewModel: FilesViewModel = viewModel(factory = viewModelFactory)
            val searchQuery by searchViewModel.searchQuery.collectAsState()
            val searchFilter by searchViewModel.searchFilter.collectAsState()
            val searchResults by searchViewModel.searchResults.collectAsState()
            val isLoading by searchViewModel.isLoading.collectAsState()

            val context = androidx.compose.ui.platform.LocalContext.current
            SearchScreen(
                searchQuery = searchQuery,
                searchFilter = searchFilter,
                searchResults = searchResults,
                isLoading = isLoading,
                onQueryChange = { query -> searchViewModel.onQueryChange(query) },
                onFilterChange = { filter -> searchViewModel.onFilterChange(filter) },
                onBackClick = { navController.navigateUp() },
                onFileOpen = { file ->
                    val ext = file.extension.lowercase()
                    val isAudio = file.mimeType?.startsWith("audio/") == true ||
                            ext in com.fyllo.filemanager.ui.screens.AUDIO_EXTENSIONS
                    val isMedia = file.mimeType?.startsWith("image/") == true ||
                            file.mimeType?.startsWith("video/") == true ||
                            ext in listOf("jpg", "jpeg", "png", "gif", "webp", "heic", "bmp", "mp4", "mkv", "avi", "mov", "3gp", "webm")

                    when {
                        isAudio -> {
                            com.fyllo.filemanager.ui.screens.AudioPlaylist.files = listOf(file)
                            com.fyllo.filemanager.ui.screens.AudioPlaylist.initialIndex = 0
                            navController.navigate(Screen.AudioPlayer.route)
                        }
                        isMedia -> navController.navigate(Screen.MediaViewer.createRoute(file.uri.toString()))
                        ext in listOf("zip", "rar", "7z", "tar", "gz", "tgz", "bz2") ->
                            filesViewModel.extractFile(file.uri)
                        ext.equals("apk", ignoreCase = true) || file.mimeType == "application/vnd.android.package-archive" || file.name.endsWith(".apk", ignoreCase = true) || file.path.endsWith(".apk", ignoreCase = true) ->
                            com.fyllo.filemanager.core.ApkInstaller.installApk(context, file)
                        ext == "pdf" -> navController.navigate(Screen.PdfViewer.createRoute(file.uri.toString()))
                        ext in listOf("txt", "json", "xml", "kt", "py", "java", "csv", "md", "log", "html", "css", "js", "ts", "sh") ->
                            navController.navigate(Screen.TextViewer.createRoute(file.uri.toString()))
                        else -> {
                            try {
                                val intent = android.content.Intent(android.content.Intent.ACTION_VIEW)
                                val authority = "${context.packageName}.fileprovider"
                                val uriToLaunch = if (file.uri.scheme == "file") {
                                    val javaFile = java.io.File(file.uri.path!!)
                                    androidx.core.content.FileProvider.getUriForFile(context, authority, javaFile)
                                } else {
                                    file.uri
                                }
                                val mimeType = file.mimeType
                                    ?: when (ext) {
                                        "mp3", "wav", "ogg", "m4a", "flac" -> "audio/*"
                                        "mp4", "mkv", "avi", "mov" -> "video/*"
                                        "jpg", "jpeg", "png", "gif", "webp", "heic" -> "image/*"
                                        "doc", "docx" -> "application/msword"
                                        "xls", "xlsx" -> "application/vnd.ms-excel"
                                        "ppt", "pptx" -> "application/vnd.ms-powerpoint"
                                        "zip" -> "application/zip"
                                        else -> "*/*"
                                    }
                                intent.setDataAndType(uriToLaunch, mimeType)
                                intent.addFlags(android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                intent.addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK)
                                context.startActivity(intent)
                            } catch (e: Exception) {
                                android.widget.Toast.makeText(context, "Cannot open: ${e.message}", android.widget.Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                },
                onNavigateToFolder = { file ->
                    navController.navigate(Screen.Files.createRoute(file.uri.toString()))
                }
            )
        }
        
        composable(
            route = Screen.Apps.route,
            arguments = listOf(navArgument("filterGames") { type = NavType.BoolType })
        ) { backStackEntry ->
            val filterGames = backStackEntry.arguments?.getBoolean("filterGames") ?: false
            val appsViewModel: com.fyllo.filemanager.ui.screens.AppsViewModel = viewModel(factory = viewModelFactory)
            val apps by appsViewModel.apps.collectAsState()
            val installedApps by appsViewModel.installedApps.collectAsState()
            val apkFiles by appsViewModel.apkFiles.collectAsState()
            val isLoading by appsViewModel.isLoading.collectAsState()

            com.fyllo.filemanager.ui.screens.AppsScreen(
                installedApps = installedApps,
                apkFiles = apkFiles,
                apps = apps,
                isLoading = isLoading,
                onBackClick = { navController.navigateUp() },
                onLoadApps = { appsViewModel.loadApps(filterGames) }
            )
        }
        
        composable(Screen.Settings.route) {
            val settingsViewModel: SettingsViewModel = viewModel(factory = viewModelFactory)
            SettingsScreen(
                viewModel = settingsViewModel,
                onNavigateToManage = { navController.navigate(Screen.Manage.route) },
                onBackClick = { navController.navigateUp() }
            )
        }

        composable(Screen.SafeFolder.route) {
            SafeFolderScreen(
                onBackClick = { navController.navigateUp() }
            )
        }

        composable(Screen.Trash.route) {
            val trashViewModel: TrashViewModel = viewModel(factory = viewModelFactory)
            TrashScreen(
                viewModel = trashViewModel,
                onBackClick = { navController.navigateUp() }
            )
        }

        composable(
            route = Screen.PdfViewer.route,
            arguments = listOf(navArgument("uri") { type = NavType.StringType })
        ) { backStackEntry ->
            val uriStr = backStackEntry.arguments?.getString("uri") ?: ""
            com.fyllo.filemanager.ui.screens.PdfViewerScreen(
                uriStr = uriStr,
                onBackClick = { navController.navigateUp() }
            )
        }

        composable(
            route = Screen.TextViewer.route,
            arguments = listOf(navArgument("uri") { type = NavType.StringType })
        ) { backStackEntry ->
            val uriStr = backStackEntry.arguments?.getString("uri") ?: ""
            com.fyllo.filemanager.ui.screens.TextViewerScreen(
                uriStr = uriStr,
                onBackClick = { navController.navigateUp() }
            )
        }

        composable(Screen.AppLock.route) {
            com.fyllo.filemanager.ui.screens.AppLockScreen(
                onUnlockSuccess = { navController.navigate(Screen.Home.route) { popUpTo(Screen.AppLock.route) { inclusive = true } } }
            )
        }

        composable(
            route = Screen.MediaViewer.route,
            arguments = listOf(navArgument("uri") { type = NavType.StringType })
        ) { backStackEntry ->
            val uriStr = backStackEntry.arguments?.getString("uri") ?: ""
            com.fyllo.filemanager.ui.screens.MediaViewerScreen(
                uriStr = uriStr,
                onBackClick = { navController.navigateUp() },
                onNavigateToEdit = { file ->
                    navController.navigate(Screen.Edit.createRoute(file.uri.toString()))
                }
            )
        }

        composable(Screen.AudioPlayer.route) {
            com.fyllo.filemanager.ui.screens.AudioPlayerScreen(
                onBackClick = { navController.navigateUp() }
            )
        }
    }
}

