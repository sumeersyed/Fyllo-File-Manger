package com.fyllo.filemanager.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.fyllo.filemanager.ui.theme.LocalEInkMode

enum class DashboardNavTab {
    HOME, FILES, SEARCH, APPS
}

@Composable
fun FloatingBottomDock(
    selectedTab: DashboardNavTab = DashboardNavTab.HOME,
    onTabSelected: (DashboardNavTab) -> Unit,
    onFabClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isEInk = LocalEInkMode.current

    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 16.dp)
    ) {
        // Floating Pill Bar centered/aligned left-center
        Surface(
            modifier = Modifier
                .align(Alignment.Center)
                .shadow(
                    elevation = if (isEInk) 0.dp else 12.dp,
                    shape = RoundedCornerShape(32.dp),
                    spotColor = Color.Black.copy(alpha = 0.15f)
                )
                .border(
                    width = 1.dp,
                    color = if (isEInk) MaterialTheme.colorScheme.outline else MaterialTheme.colorScheme.onBackground.copy(alpha = 0.08f),
                    shape = RoundedCornerShape(32.dp)
                ),
            shape = RoundedCornerShape(32.dp),
            color = if (isEInk) MaterialTheme.colorScheme.surface else MaterialTheme.colorScheme.surface.copy(alpha = 0.95f)
        ) {
            Row(
                modifier = Modifier
                    .padding(horizontal = 8.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                DockNavItem(
                    title = "Home",
                    icon = Icons.Default.Home,
                    isSelected = selectedTab == DashboardNavTab.HOME,
                    onClick = { onTabSelected(DashboardNavTab.HOME) }
                )

                DockNavItem(
                    title = null,
                    icon = Icons.Default.Folder,
                    isSelected = selectedTab == DashboardNavTab.FILES,
                    onClick = { onTabSelected(DashboardNavTab.FILES) }
                )

                DockNavItem(
                    title = null,
                    icon = Icons.Default.Search,
                    isSelected = selectedTab == DashboardNavTab.SEARCH,
                    onClick = { onTabSelected(DashboardNavTab.SEARCH) }
                )

                DockNavItem(
                    title = null,
                    icon = Icons.Default.GridView,
                    isSelected = selectedTab == DashboardNavTab.APPS,
                    onClick = { onTabSelected(DashboardNavTab.APPS) }
                )
            }
        }

        // FAB (+) floating at the right corner
        FloatingActionButton(
            onClick = onFabClick,
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .size(52.dp)
                .shadow(
                    elevation = if (isEInk) 0.dp else 6.dp,
                    shape = CircleShape,
                    spotColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.35f)
                ),
            shape = CircleShape,
            containerColor = if (isEInk) MaterialTheme.colorScheme.surface else MaterialTheme.colorScheme.primary,
            contentColor = if (isEInk) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onPrimary,
            elevation = FloatingActionButtonDefaults.elevation(defaultElevation = 0.dp, pressedElevation = 2.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = "New / Add",
                modifier = Modifier.size(24.dp)
            )
        }
    }
}

@Composable
private fun DockNavItem(
    title: String?,
    icon: ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val isEInk = LocalEInkMode.current

    if (isSelected && title != null) {
        // Pill capsule selected button
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(24.dp))
                .background(if (isEInk) MaterialTheme.colorScheme.outline.copy(alpha = 0.2f) else MaterialTheme.colorScheme.primary.copy(alpha = 0.15f))
                .clickable(onClick = onClick)
                .padding(horizontal = 14.dp, vertical = 8.dp),
            contentAlignment = Alignment.Center
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = if (isEInk) MaterialTheme.colorScheme.onBackground else MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = title,
                    color = if (isEInk) MaterialTheme.colorScheme.onBackground else MaterialTheme.colorScheme.primary,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    } else {
        // Simple Icon button
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .clickable(onClick = onClick),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = if (isSelected) {
                    if (isEInk) MaterialTheme.colorScheme.onBackground else MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.onSurface.copy(alpha = 0.65f)
                },
                modifier = Modifier.size(22.dp)
            )
        }
    }
}
