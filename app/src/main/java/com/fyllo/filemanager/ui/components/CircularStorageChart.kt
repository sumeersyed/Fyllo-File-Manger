package com.fyllo.filemanager.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
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
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.graphics.asComposeRenderEffect
import androidx.compose.ui.graphics.graphicsLayer
import com.fyllo.filemanager.ui.theme.*

data class StorageSegment(val color: Color, val fraction: Float)

@Composable
fun CircularStorageChart(
    segments: List<StorageSegment>,
    totalUsedPercentage: Int, // 0 to 100
    modifier: Modifier = Modifier
) {
    var animationPlayed by remember { mutableStateOf(false) }

    LaunchedEffect(key1 = true) {
        animationPlayed = true
    }

    val sweepAngle = animateFloatAsState(
        targetValue = if (animationPlayed) 360f else 0f,
        animationSpec = tween(
            durationMillis = 1000,
            easing = FastOutSlowInEasing
        ),
        label = "chartAnimation"
    )

    Box(
        modifier = modifier
            .aspectRatio(1f)
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        // Blurred Glow Layer
        Canvas(modifier = Modifier.fillMaxSize().graphicsLayer {
            renderEffect = android.graphics.RenderEffect.createBlurEffect(20f, 20f, android.graphics.Shader.TileMode.DECAL).asComposeRenderEffect()
        }) {
            val strokeWidth = 8.dp.toPx()
            val size = size.minDimension - strokeWidth
            val topLeft = Offset(strokeWidth / 2, strokeWidth / 2)

            var startAngle = -90f
            val gapAngle = 6f
            segments.forEach { segment ->
                val segmentSweep = (segment.fraction * sweepAngle.value)
                val actualSweep = (segmentSweep - gapAngle).coerceAtLeast(0.1f)
                
                if (segmentSweep > 0) {
                    drawArc(
                        color = segment.color,
                        startAngle = startAngle + (gapAngle / 2),
                        sweepAngle = actualSweep,
                        useCenter = false,
                        topLeft = topLeft,
                        size = Size(size, size),
                        style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                    )
                }
                startAngle += (segment.fraction * 360f)
            }
        }

        // Main Sharp Layer
        Canvas(modifier = Modifier.fillMaxSize()) {
            val strokeWidth = 10.dp.toPx()
            val size = size.minDimension - strokeWidth
            val topLeft = Offset(strokeWidth / 2, strokeWidth / 2)

            // Background track
            drawArc(
                color = Color(0xFF1E2228),
                startAngle = 0f,
                sweepAngle = 360f,
                useCenter = false,
                topLeft = topLeft,
                size = Size(size, size),
                style = Stroke(width = strokeWidth)
            )

            var startAngle = -90f
            val gapAngle = 6f // Degrees of gap between segments
            
            segments.forEach { segment ->
                val segmentSweep = (segment.fraction * sweepAngle.value)
                val actualSweep = (segmentSweep - gapAngle).coerceAtLeast(0.1f)
                
                if (segmentSweep > 0) {
                    drawArc(
                        color = segment.color,
                        startAngle = startAngle + (gapAngle / 2),
                        sweepAngle = actualSweep,
                        useCenter = false,
                        topLeft = topLeft,
                        size = Size(size, size),
                        style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                    )
                }
                startAngle += (segment.fraction * 360f)
            }
        }

        // Center Text
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "$totalUsedPercentage%",
                color = Color.White,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "used",
                color = Color(0xFFA0A0A0),
                fontSize = 12.sp
            )
        }
    }
}
