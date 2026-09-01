package com.fyllo.filemanager.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.fyllo.filemanager.ui.theme.LocalEInkMode

@Composable
fun CategoryCard(
    title: String,
    itemCountText: String,
    icon: ImageVector,
    accentColor: Color,
    containerGradient: List<Color>,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isEInk = LocalEInkMode.current
    val isDarkTheme = MaterialTheme.colorScheme.background.luminance() < 0.5f
    val shape = RoundedCornerShape(20.dp)

    // In dark theme, use dark glass containers with subtle accent tint so text pops.
    // In light theme, use soft pastel gradients.
    val backgroundModifier = if (isEInk) {
        Modifier
            .background(Color.Transparent, shape)
            .border(1.5.dp, MaterialTheme.colorScheme.outline, shape)
    } else if (isDarkTheme) {
        Modifier
            .background(
                brush = Brush.verticalGradient(
                    listOf(
                        accentColor.copy(alpha = 0.22f),
                        MaterialTheme.colorScheme.surface.copy(alpha = 0.95f)
                    )
                ),
                shape = shape
            )
            .border(1.dp, accentColor.copy(alpha = 0.35f), shape)
    } else {
        Modifier
            .background(
                brush = Brush.verticalGradient(containerGradient),
                shape = shape
            )
            .border(1.dp, accentColor.copy(alpha = 0.20f), shape)
    }

    Box(
        modifier = modifier
            .width(96.dp)
            .height(130.dp)
            .clip(shape)
            .then(backgroundModifier)
            .clickable(onClick = onClick)
            .padding(vertical = 14.dp, horizontal = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Icon Container Box
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(
                        if (isEInk) MaterialTheme.colorScheme.surface
                        else if (isDarkTheme) accentColor.copy(alpha = 0.28f)
                        else accentColor.copy(alpha = 0.18f)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = if (isEInk) MaterialTheme.colorScheme.onBackground else accentColor,
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Title - High contrast according to active theme
            Text(
                text = title,
                color = MaterialTheme.colorScheme.onBackground,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(2.dp))

            // Count
            Text(
                text = itemCountText,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                fontSize = 11.sp,
                fontWeight = FontWeight.Normal,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

