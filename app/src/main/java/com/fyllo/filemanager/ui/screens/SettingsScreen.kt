package com.fyllo.filemanager.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.Analytics
import androidx.compose.material.icons.outlined.BrightnessMedium
import androidx.compose.material.icons.outlined.CleaningServices
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.FolderSpecial
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Language
import androidx.compose.material.icons.outlined.Palette
import androidx.compose.material.icons.outlined.DarkMode
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material.icons.outlined.Code
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Security
import androidx.compose.material.icons.outlined.Fingerprint
import androidx.compose.material.icons.outlined.VolumeUp
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.automirrored.outlined.Backspace
import androidx.compose.material3.Surface
import androidx.compose.ui.draw.clip
import com.fyllo.filemanager.ui.theme.LocalEInkMode
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.res.stringResource
import com.fyllo.filemanager.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel,
    onNavigateToManage: () -> Unit,
    onBackClick: () -> Unit
) {
    val state by viewModel.settingsState.collectAsState()
    
    var showThemeModeDialog by remember { mutableStateOf(false) }
    var showColorThemeDialog by remember { mutableStateOf(false) }
    var showLanguageDialog by remember { mutableStateOf(false) }
    var showRecycleBinDialog by remember { mutableStateOf(false) }
    var showAboutDialog by remember { mutableStateOf(false) }
    var showJunkCleanIntervalDialog by remember { mutableStateOf(false) }

    val context = LocalContext.current
    val appLockManager = remember { com.fyllo.filemanager.core.AppLockManager(context) }
    var isAppLockEnabled by remember { mutableStateOf(appLockManager.isAppLockEnabled) }
    var isBiometricEnabled by remember { mutableStateOf(appLockManager.isBiometricEnabled) }
    var showPasscodeSetupDialog by remember { mutableStateOf(false) }

    val deviceAdminManager = remember { com.fyllo.filemanager.core.admin.DeviceAdminManager(context) }
    var isDeviceAdminActive by remember { mutableStateOf(deviceAdminManager.isAdminActive) }
    var showDeactivateAdminDialog by remember { mutableStateOf(false) }
    var adminPinInput by remember { mutableStateOf("") }
    var adminPinError by remember { mutableStateOf<String?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.onBackground,
                    navigationIconContentColor = MaterialTheme.colorScheme.onBackground
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
        ) {
            Spacer(modifier = Modifier.height(8.dp))

            // ─── Appearance Section ─────────────────────────────
            SettingsCardSection(title = "Appearance") {
                SettingsItem(
                    icon = Icons.Outlined.BrightnessMedium,
                    title = stringResource(R.string.theme),
                    subtitle = state.themeMode,
                    onClick = { showThemeModeDialog = true }
                )
                HorizontalDivider(color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f))
                SettingsItem(
                    icon = Icons.Outlined.Palette,
                    title = "Color Theme",
                    subtitle = state.colorTheme,
                    onClick = { showColorThemeDialog = true }
                )
                HorizontalDivider(color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f))
                SettingsSwitchItem(
                    icon = Icons.Outlined.DarkMode,
                    title = stringResource(R.string.amoled_black),
                    checked = state.amoledBlack,
                    onCheckedChange = { viewModel.setAmoledBlack(it) }
                )
                HorizontalDivider(color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f))
                SettingsSwitchItem(
                    icon = Icons.Outlined.BrightnessMedium, // Reusing icon, maybe better one exists but this is fine
                    title = "E-ink Mode",
                    checked = state.eInkMode,
                    onCheckedChange = { viewModel.setEInkMode(it) }
                )
            }

            // ─── General Section ────────────────────────────────
            SettingsCardSection(title = "General") {
                SettingsItem(
                    icon = Icons.Outlined.Language,
                    title = stringResource(R.string.language),
                    subtitle = state.language,
                    onClick = { showLanguageDialog = true }
                )
                HorizontalDivider(color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f))
                SettingsSwitchItem(
                    icon = Icons.Outlined.Visibility,
                    title = stringResource(R.string.show_hidden_files),
                    checked = state.showHiddenFiles,
                    onCheckedChange = { viewModel.setShowHiddenFiles(it) }
                )
                HorizontalDivider(color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f))
                SettingsSwitchItem(
                    icon = Icons.Outlined.VolumeUp,
                    title = "Media Scroll Sound",
                    subtitle = "Subtle iOS tick click sound when scrolling photos and videos",
                    checked = state.enableScrollSound,
                    onCheckedChange = { viewModel.setEnableScrollSound(it) }
                )
                HorizontalDivider(color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f))
                SettingsItem(
                    icon = Icons.Outlined.Delete,
                    title = stringResource(R.string.keep_in_recycle_bin),
                    subtitle = "${state.recycleBinDuration} days",
                    onClick = { showRecycleBinDialog = true }
                )
            }

            // ─── Storage Section ────────────────────────────────
            SettingsCardSection(title = "Storage") {
                SettingsItem(
                    icon = Icons.Outlined.Analytics,
                    title = "Analyze Storage",
                    subtitle = "View storage usage details",
                    onClick = { onNavigateToManage() }
                )
                HorizontalDivider(color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f))
                SettingsSwitchItem(
                    icon = Icons.Outlined.CleaningServices,
                    title = stringResource(R.string.auto_clean_junk),
                    checked = state.autoCleanJunk,
                    onCheckedChange = { viewModel.setAutoCleanJunk(it) }
                )
                if (state.autoCleanJunk) {
                    HorizontalDivider(color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f))
                    SettingsItem(
                        icon = Icons.Outlined.CleaningServices,
                        title = "Junk Clean Interval",
                        subtitle = state.junkCleanInterval,
                        onClick = { showJunkCleanIntervalDialog = true }
                    )
                }
            }

            // ─── Security & Privacy Section ─────────────────────
    SettingsCardSection(title = "Security & Privacy") {
        SettingsSwitchItem(
            icon = Icons.Outlined.Security,
            title = "Uninstall Protection (Device Admin)",
            subtitle = if (isDeviceAdminActive) "Active - Admin Passcode required to uninstall" else "Tap to enable Device Admin protection",
            checked = isDeviceAdminActive,
            onCheckedChange = { enabled ->
                if (enabled) {
                    if (!appLockManager.hasAppLockPin()) {
                        showPasscodeSetupDialog = true
                    }
                    if (context is android.app.Activity) {
                        deviceAdminManager.requestAdminRights(context)
                    }
                    isDeviceAdminActive = deviceAdminManager.isAdminActive
                } else {
                    if (appLockManager.hasAppLockPin()) {
                        adminPinInput = ""
                        adminPinError = null
                        showDeactivateAdminDialog = true
                    } else {
                        deviceAdminManager.deactivateAdminRights()
                        isDeviceAdminActive = false
                        android.widget.Toast.makeText(context, "Uninstall Protection deactivated", android.widget.Toast.LENGTH_SHORT).show()
                    }
                }
            }
        )
        HorizontalDivider(color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f))
        SettingsSwitchItem(
            icon = Icons.Outlined.Lock,
            title = "App Lock",
            checked = isAppLockEnabled,
            onCheckedChange = { enabled ->
                appLockManager.isAppLockEnabled = enabled
                isAppLockEnabled = enabled
                if (enabled && !appLockManager.hasAppLockPin()) {
                    showPasscodeSetupDialog = true
                }
            }
        )
        HorizontalDivider(color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f))
        SettingsItem(
            icon = Icons.Outlined.Lock,
            title = "Set App Lock Passcode",
            subtitle = if (appLockManager.hasAppLockPin()) "Passcode is set" else "Tap to set 4-digit passcode",
            onClick = { showPasscodeSetupDialog = true }
        )
        HorizontalDivider(color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f))
        SettingsSwitchItem(
            icon = Icons.Outlined.Fingerprint,
            title = "Biometric Unlock",
            checked = isBiometricEnabled,
            onCheckedChange = { enabled ->
                appLockManager.isBiometricEnabled = enabled
                isBiometricEnabled = enabled
            }
        )
        HorizontalDivider(color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f))
        SettingsItem(
            icon = Icons.Outlined.Delete,
            title = "Uninstall File Manager",
            subtitle = "Requires Admin Passcode to deactivate protection",
            onClick = {
                adminPinInput = ""
                adminPinError = null
                showDeactivateAdminDialog = true
            }
        )
    }

            // ─── Other Section ──────────────────────────────────
            SettingsCardSection(title = "Other") {
                SettingsItem(
                    icon = Icons.Outlined.Info,
                    title = stringResource(R.string.about),
                    subtitle = stringResource(R.string.version),
                    onClick = { showAboutDialog = true }
                )
                HorizontalDivider(color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f))
                SettingsItem(
                    icon = Icons.Outlined.Code,
                    title = stringResource(R.string.developer),
                    subtitle = "Raven Team",
                    onClick = { showAboutDialog = true }
                )
            }
            
            Spacer(modifier = Modifier.height(32.dp))
        }
    }

    if (showPasscodeSetupDialog) {
        PasscodeSetupDialog(
            onPasscodeSet = { pin ->
                appLockManager.setAppLockPin(pin)
                showPasscodeSetupDialog = false
                android.widget.Toast.makeText(context, "Passcode updated successfully", android.widget.Toast.LENGTH_SHORT).show()
            },
            onDismiss = { showPasscodeSetupDialog = false }
        )
    }

    if (showDeactivateAdminDialog) {
        AlertDialog(
            onDismissRequest = { showDeactivateAdminDialog = false },
            icon = { Icon(Icons.Outlined.Security, contentDescription = null, tint = MaterialTheme.colorScheme.error) },
            title = { Text("Enter Admin Passcode") },
            text = {
                Column {
                    Text("Enter your 4-digit Admin Passcode to deactivate Device Administrator rights and proceed with uninstallation.")
                    Spacer(modifier = Modifier.height(16.dp))
                    OutlinedTextField(
                        value = adminPinInput,
                        onValueChange = { if (it.length <= 4 && it.all { char -> char.isDigit() }) adminPinInput = it },
                        label = { Text("4-Digit Passcode") },
                        singleLine = true,
                        isError = adminPinError != null,
                        supportingText = adminPinError?.let { { Text(it, color = MaterialTheme.colorScheme.error) } },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (appLockManager.verifyAppLockPin(adminPinInput) || appLockManager.verifySafeFolderPin(adminPinInput)) {
                            deviceAdminManager.deactivateAdminRights()
                            isDeviceAdminActive = false
                            showDeactivateAdminDialog = false
                            android.widget.Toast.makeText(context, "Admin verified. Deactivating protection...", android.widget.Toast.LENGTH_SHORT).show()
                            try {
                                val uninstallIntent = android.content.Intent(android.content.Intent.ACTION_DELETE).apply {
                                    data = android.net.Uri.parse("package:${context.packageName}")
                                }
                                context.startActivity(uninstallIntent)
                            } catch (e: Exception) {
                                e.printStackTrace()
                            }
                        } else {
                            adminPinError = "Incorrect Admin Passcode"
                        }
                    }
                ) {
                    Text("Confirm & Uninstall")
                }
            },
            dismissButton = {
                androidx.compose.material3.TextButton(onClick = { showDeactivateAdminDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    if (showThemeModeDialog) {
        OptionsDialog(
            title = "Theme Mode",
            options = listOf("Light", "Dark", "System Default"),
            selectedOption = state.themeMode,
            onOptionSelected = { viewModel.setThemeMode(it); showThemeModeDialog = false },
            onDismiss = { showThemeModeDialog = false }
        )
    }

    if (showColorThemeDialog) {
        OptionsDialog(
            title = stringResource(R.string.theme),
            options = listOf("Default", "Purple", "Pink", "Ocean Blue", "Forest Green", "E-ink"),
            selectedOption = state.colorTheme,
            onOptionSelected = { viewModel.setColorTheme(it); showColorThemeDialog = false },
            onDismiss = { showColorThemeDialog = false }
        )
    }

    if (showLanguageDialog) {
        OptionsDialog(
            title = stringResource(R.string.language),
            options = listOf("System Default", "English", "Arabic", "Spanish", "French", "German", "Italian", "Portuguese", "Russian", "Japanese", "Korean", "Chinese"),
            selectedOption = state.language,
            onOptionSelected = { viewModel.setLanguage(it); showLanguageDialog = false },
            onDismiss = { showLanguageDialog = false }
        )
    }

    if (showRecycleBinDialog) {
        OptionsDialog(
            title = stringResource(R.string.keep_in_recycle_bin),
            options = listOf("7", "14", "30", "60"),
            selectedOption = state.recycleBinDuration.toString(),
            onOptionSelected = { viewModel.setRecycleBinDuration(it.toInt()); showRecycleBinDialog = false },
            onDismiss = { showRecycleBinDialog = false },
            postfix = " days"
        )
    }

    if (showJunkCleanIntervalDialog) {
        OptionsDialog(
            title = "Junk Clean Interval",
            options = listOf("Daily", "Weekly", "Monthly"),
            selectedOption = state.junkCleanInterval,
            onOptionSelected = { viewModel.setJunkCleanInterval(it); showJunkCleanIntervalDialog = false },
            onDismiss = { showJunkCleanIntervalDialog = false }
        )
    }

    if (showAboutDialog) {
        AboutDialog(onDismiss = { showAboutDialog = false })
    }
}

@Composable
fun SettingsCardSection(
    title: String,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
    ) {
        SettingsCategoryTitle(title)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp))
                .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.65f))
                .border(
                    width = 1.5.dp,
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.25f),
                    shape = RoundedCornerShape(20.dp)
                )
        ) {
            Column(
                modifier = Modifier.fillMaxWidth()
            ) {
                content()
            }
        }
    }
}

@Composable
fun SettingsCategoryTitle(title: String) {
    Text(
        text = title,
        color = MaterialTheme.colorScheme.primary,
        fontSize = 14.sp,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(start = 8.dp, bottom = 6.dp, top = 6.dp)
    )
}

@Composable
fun SettingsItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    ListItem(
        headlineContent = { Text(title, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurface) },
        supportingContent = if (subtitle.isNotBlank()) {
            { Text(subtitle, color = MaterialTheme.colorScheme.primary) }
        } else null,
        leadingContent = {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
            }
        },
        modifier = Modifier.clickable { onClick() },
        colors = ListItemDefaults.colors(containerColor = Color.Transparent)
    )
}

@Composable
fun SettingsSwitchItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    subtitle: String? = null
) {
    ListItem(
        headlineContent = { Text(title, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurface) },
        supportingContent = if (subtitle != null) { { Text(subtitle, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant) } } else null,
        trailingContent = {
            Switch(
                checked = checked,
                onCheckedChange = onCheckedChange,
                colors = SwitchDefaults.colors(
                    checkedThumbColor = Color.White,
                    checkedTrackColor = MaterialTheme.colorScheme.primary,
                    uncheckedThumbColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                    uncheckedTrackColor = MaterialTheme.colorScheme.surfaceVariant
                )
            )
        },
        leadingContent = {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
            }
        },
        colors = ListItemDefaults.colors(containerColor = Color.Transparent)
    )
}

@Composable
fun OptionsDialog(
    title: String,
    options: List<String>,
    selectedOption: String,
    onOptionSelected: (String) -> Unit,
    onDismiss: () -> Unit,
    postfix: String = ""
) {
    AlertDialog(
        modifier = Modifier.border(1.5.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.3f), RoundedCornerShape(24.dp)),
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surface,
        title = { Text(text = title, color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold) },
        text = {
            Column {
                options.forEach { option ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .clickable { onOptionSelected(option) }
                            .padding(vertical = 10.dp, horizontal = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = option == selectedOption,
                            onClick = { onOptionSelected(option) }
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(text = "$option$postfix", color = MaterialTheme.colorScheme.onSurface, fontSize = 16.sp)
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
            }
        }
    )
}

@Composable
fun AboutDialog(onDismiss: () -> Unit) {

    AlertDialog(
        modifier = Modifier.border(1.5.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.3f), RoundedCornerShape(24.dp)),
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surface,
        title = { Text(text = "About File Manager", color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold) },
        text = {
            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                androidx.compose.foundation.Image(
                    painter = androidx.compose.ui.res.painterResource(id = R.drawable.app_logo),
                    contentDescription = "Logo",
                    modifier = Modifier
                        .size(72.dp)
                        .padding(bottom = 16.dp)
                )
                Text(stringResource(R.string.version), color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 14.sp)
                Spacer(modifier = Modifier.height(8.dp))
                Text("Developed by Raven Team", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                Spacer(modifier = Modifier.height(4.dp))
                Text("Developer: SYED SUMEER", color = MaterialTheme.colorScheme.primary, fontSize = 13.sp, fontWeight = FontWeight.Bold)
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Close", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
            }
        }
    )
}

@Composable
fun PasscodeSetupDialog(
    onPasscodeSet: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var enterPin by remember { mutableStateOf("") }
    var confirmPin by remember { mutableStateOf("") }
    var isConfirming by remember { mutableStateOf(false) }
    var errorMsg by remember { mutableStateOf<String?>(null) }
    val maxPinLength = 4

    val currentPinDisplay = if (isConfirming) confirmPin else enterPin

    val onNumberClick: (String) -> Unit = { digit ->
        errorMsg = null
        if (!isConfirming) {
            if (enterPin.length < maxPinLength) {
                enterPin += digit
                if (enterPin.length == maxPinLength) {
                    isConfirming = true
                }
            }
        } else {
            if (confirmPin.length < maxPinLength) {
                confirmPin += digit
                if (confirmPin.length == maxPinLength) {
                    if (confirmPin == enterPin) {
                        onPasscodeSet(confirmPin)
                    } else {
                        errorMsg = "Passcodes do not match. Try again."
                        confirmPin = ""
                        enterPin = ""
                        isConfirming = false
                    }
                }
            }
        }
    }

    val onDeleteClick = {
        errorMsg = null
        if (!isConfirming) {
            if (enterPin.isNotEmpty()) enterPin = enterPin.dropLast(1)
        } else {
            if (confirmPin.isNotEmpty()) confirmPin = confirmPin.dropLast(1)
        }
    }

    androidx.compose.ui.window.Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(28.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp,
            modifier = Modifier
                .fillMaxWidth()
                .border(1.5.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.25f), RoundedCornerShape(28.dp))
        ) {
            Column(
                modifier = Modifier
                    .padding(24.dp)
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Lock,
                        contentDescription = "Lock",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(28.dp)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = if (isConfirming) "Confirm Passcode" else "Set 4-Digit Passcode",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = when {
                        errorMsg != null -> errorMsg!!
                        isConfirming -> "Re-enter your 4-digit code to confirm"
                        else -> "Enter a 4-digit passcode for App Lock"
                    },
                    fontSize = 13.sp,
                    color = if (errorMsg != null) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(24.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    for (i in 0 until maxPinLength) {
                        val isActive = i < currentPinDisplay.length
                        Box(
                            modifier = Modifier
                                .size(16.dp)
                                .clip(CircleShape)
                                .background(
                                    if (isActive) MaterialTheme.colorScheme.primary
                                    else MaterialTheme.colorScheme.surfaceVariant
                                )
                                .then(
                                    if (!isActive) Modifier.border(1.5.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.35f), CircleShape)
                                    else Modifier
                                )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                val rows = listOf(
                    listOf("1", "2", "3"),
                    listOf("4", "5", "6"),
                    listOf("7", "8", "9")
                )

                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    rows.forEach { row ->
                        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                            row.forEach { num ->
                                KeypadButton(text = num, onClick = { onNumberClick(num) })
                            }
                        }
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        Spacer(modifier = Modifier.size(60.dp))
                        KeypadButton(text = "0", onClick = { onNumberClick("0") })
                        Box(
                            modifier = Modifier
                                .size(60.dp)
                                .clip(CircleShape)
                                .clickable { onDeleteClick() },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Outlined.Backspace,
                                contentDescription = "Delete",
                                tint = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                TextButton(onClick = onDismiss) {
                    Text("Cancel", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
private fun KeypadButton(text: String, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .size(60.dp)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f))
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}
