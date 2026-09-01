package com.fyllo.filemanager.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.unit.dp
import com.fyllo.filemanager.ui.theme.LocalEInkMode
import androidx.compose.material3.MaterialTheme

data class ActivityRingData(
    val color: Color,
    val progress: Float,
    val icon: ImageVector
)

@Composable
fun ActivityRingsChart(
    rings: List<ActivityRingData>,
    modifier: Modifier = Modifier
) {
    val isEInk = LocalEInkMode.current
    var animationPlayed by remember { mutableStateOf(false) }

    LaunchedEffect(key1 = true) {
        animationPlayed = true
    }

    val animatedProgress = animateFloatAsState(
        targetValue = if (animationPlayed) 1f else 0f,
        animationSpec = tween(
            durationMillis = 1500,
            easing = FastOutSlowInEasing
        ),
        label = "ringAnimation"
    )

    // Load painters for the icons outside Canvas
    val painters = rings.map { rememberVectorPainter(image = it.icon) }

    val eInkOutline = MaterialTheme.colorScheme.outline
    val eInkOnBackground = MaterialTheme.colorScheme.onBackground
    val eInkSurface = MaterialTheme.colorScheme.surface

    Box(
        modifier = modifier
            .aspectRatio(1f)
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val strokeWidth = 14.dp.toPx()
            val spacing = 2.dp.toPx()
            
            // Loop from outer (index 0) to inner (index 2)
            rings.forEachIndexed { index, ring ->
                val ringInset = index * (strokeWidth + spacing)
                val ringSize = size.minDimension - strokeWidth - (ringInset * 2)
                val topLeft = Offset(strokeWidth / 2 + ringInset, strokeWidth / 2 + ringInset)
                val rawSweep = ring.progress * 360f
                // For a highly overlapping aesthetic if it exceeds 100%, or just capping it. Activity rings can overlap.
                val sweepAngle = rawSweep * animatedProgress.value
                
                // Draw Background Track
                drawArc(
                    color = if (isEInk) eInkOutline.copy(alpha=0.2f) else ring.color.copy(alpha = 0.15f),
                    startAngle = 0f,
                    sweepAngle = 360f,
                    useCenter = false,
                    topLeft = topLeft,
                    size = Size(ringSize, ringSize),
                    style = Stroke(width = if (isEInk) 1.dp.toPx() else strokeWidth)
                )

                // Draw Progress Arc
                if (sweepAngle > 0f) {
                    drawArc(
                        color = if (isEInk) eInkOnBackground else ring.color,
                        startAngle = -90f,
                        sweepAngle = sweepAngle.coerceAtLeast(0.1f),
                        useCenter = false,
                        topLeft = topLeft,
                        size = Size(ringSize, ringSize),
                        style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                    )
                }

                // Draw Icon at the top
                val painter = painters[index]
                val iconSize = strokeWidth * 0.6f
                val iconOffset = Offset(
                    x = size.width / 2 - iconSize / 2,
                    y = strokeWidth / 2 + ringInset - iconSize / 2
                )
                
                translate(
                    left = iconOffset.x,
                    top = iconOffset.y
                ) {
                    with(painter) {
                        draw(
                            size = Size(iconSize, iconSize),
                            colorFilter = ColorFilter.tint(if (isEInk) eInkSurface else Color.Black)
                        )
                    }
                }
            }
        }
    }
}
