package com.fyllo.filemanager

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.Settings
import androidx.appcompat.app.AppCompatActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import java.util.concurrent.TimeUnit
import com.fyllo.filemanager.di.ViewModelFactory
import com.fyllo.filemanager.ui.navigation.AppNavigation
import com.fyllo.filemanager.ui.theme.SFileManagerTheme
import com.fyllo.filemanager.worker.JunkCleanWorker

class MainActivity : AppCompatActivity() {

    private var hasPermissions by mutableStateOf(false)

    private val permissionLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) {
        checkPermissions()
    }

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        checkPermissions()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        val prefs = getSharedPreferences("app_settings", android.content.Context.MODE_PRIVATE)
        val savedLanguage = prefs.getString("language", "System Default") ?: "System Default"
        val initialLangCode = when (savedLanguage) {
            "English" -> "en"
            "Arabic" -> "ar"
            "Spanish" -> "es"
            "French" -> "fr"
            "German" -> "de"
            "Italian" -> "it"
            "Portuguese" -> "pt"
            "Russian" -> "ru"
            "Japanese" -> "ja"
            "Korean" -> "ko"
            "Chinese" -> "zh"
            else -> ""
        }
        val currentLocales = androidx.appcompat.app.AppCompatDelegate.getApplicationLocales()
        val currentTag = if (!currentLocales.isEmpty) currentLocales.toLanguageTags() else ""
        if (currentTag != initialLangCode) {
            androidx.appcompat.app.AppCompatDelegate.setApplicationLocales(
                androidx.core.os.LocaleListCompat.forLanguageTags(initialLangCode)
            )
        }

        val isOnboardingCompletedInitial = prefs.getBoolean("isOnboardingCompleted", false)
        if (isOnboardingCompletedInitial) {
            checkPermissions()
        }
        
        val appContainer = (application as SFileManagerApp).container
        val viewModelFactory = ViewModelFactory(appContainer)

        setContent {
            val settingsViewModel: com.fyllo.filemanager.ui.screens.SettingsViewModel = viewModel(factory = viewModelFactory)
            val settingsState by settingsViewModel.settingsState.collectAsState()

            val context = androidx.compose.ui.platform.LocalContext.current
            androidx.compose.runtime.LaunchedEffect(settingsState.autoCleanJunk, settingsState.junkCleanInterval) {
                if (settingsState.autoCleanJunk) {
                    val intervalHours = when (settingsState.junkCleanInterval) {
                        "Daily" -> 24L
                        "Monthly" -> 30 * 24L
                        else -> 7 * 24L // Weekly
                    }
                    val request = PeriodicWorkRequestBuilder<JunkCleanWorker>(intervalHours, TimeUnit.HOURS)
                        .build()
                    WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                        "JunkCleanWorker",
                        ExistingPeriodicWorkPolicy.UPDATE,
                        request
                    )
                } else {
                    WorkManager.getInstance(context).cancelUniqueWork("JunkCleanWorker")
                }
            }

            androidx.compose.runtime.LaunchedEffect(settingsState.isOnboardingCompleted, hasPermissions) {
                if (settingsState.isOnboardingCompleted && !hasPermissions) {
                    requestStoragePermissions()
                }
            }

            SFileManagerTheme(settingsState = settingsState) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    if (hasPermissions || !settingsState.isOnboardingCompleted) {
                        val appLockManager = remember { com.fyllo.filemanager.core.AppLockManager(applicationContext) }
                        val startDest = when {
                            !settingsState.isOnboardingCompleted -> com.fyllo.filemanager.ui.navigation.Screen.Onboarding.route
                            appLockManager.isAppLockEnabled -> com.fyllo.filemanager.ui.navigation.Screen.AppLock.route
                            else -> com.fyllo.filemanager.ui.navigation.Screen.Home.route
                        }
                        AppNavigation(viewModelFactory = viewModelFactory, startDestination = startDest)
                    } else {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text("Please grant storage permissions to use the app.", color = MaterialTheme.colorScheme.onBackground)
                        }
                    }
                }
            }
        }
    }

    private fun checkPermissions() {
        hasPermissions = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            Environment.isExternalStorageManager()
        } else {
            // Simplify for Android 10
            checkSelfPermission(android.Manifest.permission.READ_EXTERNAL_STORAGE) == android.content.pm.PackageManager.PERMISSION_GRANTED
        }
    }

    private fun requestStoragePermissions() {
        val permissionsToRequest = mutableListOf<String>()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permissionsToRequest.add(android.Manifest.permission.POST_NOTIFICATIONS)
            permissionsToRequest.add(android.Manifest.permission.READ_MEDIA_IMAGES)
            permissionsToRequest.add(android.Manifest.permission.READ_MEDIA_VIDEO)
            permissionsToRequest.add(android.Manifest.permission.READ_MEDIA_AUDIO)
        } else {
            permissionsToRequest.add(android.Manifest.permission.READ_EXTERNAL_STORAGE)
            if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.P) {
                permissionsToRequest.add(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
            }
        }

        if (permissionsToRequest.isNotEmpty()) {
            requestPermissionLauncher.launch(permissionsToRequest.toTypedArray())
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R && !Environment.isExternalStorageManager()) {
            try {
                val intent = Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION)
                intent.addCategory("android.intent.category.DEFAULT")
                intent.data = Uri.parse(String.format("package:%s", applicationContext.packageName))
                permissionLauncher.launch(intent)
            } catch (e: Exception) {
                val intent = Intent(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION)
                permissionLauncher.launch(intent)
            }
        }
    }
}

