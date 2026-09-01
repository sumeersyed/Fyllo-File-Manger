package com.fyllo.filemanager.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.fyllo.filemanager.ui.theme.NeonCyan
import com.fyllo.filemanager.ui.theme.NeonPink
import com.fyllo.filemanager.ui.theme.NeonPurple
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import android.app.Activity
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import com.fyllo.filemanager.R
import androidx.compose.foundation.Image
import androidx.compose.ui.res.painterResource
@Composable
fun OnboardingScreen(
    onFinish: () -> Unit
) {
    var showSplash by remember { mutableStateOf(true) }
    
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = false
            WindowCompat.getInsetsController(window, view).isAppearanceLightNavigationBars = false
        }
    }

    Box(modifier = Modifier.fillMaxSize().background(Color(0xFF07090C))) {
        if (showSplash) {
            SplashLoadingScreen(
                onLoadingFinished = {
                    showSplash = false
                }
            )
        } else {
            OnboardingPagerScreen(onFinish = onFinish)
        }
    }
}

@Composable
fun SplashLoadingScreen(onLoadingFinished: () -> Unit) {
    val progress = remember { Animatable(0f) }
    
    LaunchedEffect(Unit) {
        progress.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 2000, easing = LinearOutSlowInEasing)
        )
        delay(300)
        onLoadingFinished()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Spacer(modifier = Modifier.weight(1f))
        
        // Center Graphic
        Box(
            modifier = Modifier
                .size(200.dp),
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = painterResource(id = R.drawable.app_logo),
                contentDescription = "App Logo",
                modifier = Modifier.fillMaxSize()
            )
        }

        Spacer(modifier = Modifier.height(48.dp))

        // Title
        Text(
            text = buildAnnotatedString {
                withStyle(style = SpanStyle(color = Color.White)) {
                    append("File ")
                }
                withStyle(style = SpanStyle(color = Color(0xFF6B4EE6))) { // Light Purple
                    append("Manager")
                }
            },
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Manage. Organize. Simplify.",
            color = Color.LightGray,
            fontSize = 16.sp
        )

        Spacer(modifier = Modifier.weight(1f))

        // Loading Bar
        Box(
            modifier = Modifier
                .width(150.dp)
                .height(4.dp)
                .clip(RoundedCornerShape(2.dp))
                .background(Color.DarkGray)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(progress.value)
                    .background(
                        brush = Brush.horizontalGradient(
                            colors = listOf(NeonPurple, NeonPink)
                        )
                    )
            )
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text("Loading...", color = Color.Gray, fontSize = 14.sp)
        
        Spacer(modifier = Modifier.height(48.dp))
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun OnboardingPagerScreen(onFinish: () -> Unit) {
    val pagerState = rememberPagerState(pageCount = { 5 })
    val coroutineScope = rememberCoroutineScope()

    Column(modifier = Modifier.fillMaxSize()) {
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.weight(1f)
        ) { page ->
            when (page) {
                0 -> OnboardingPage(
                    title = "All your files, perfectly organized",
                    graphic = { FoldersGraphic() }
                )
                1 -> OnboardingPage(
                    title = "All Your Files\nin One Place",
                    subtitle = "Manage all your photos, videos, documents, music and more in one secure location.",
                    titleColor = Color(0xFFD500F9), // Purple highlight for "One Place"
                    graphic = { OrbitFolderGraphic() }
                )
                2 -> OnboardingPage(
                    title = "Keep Your Files\nSafe & Secure",
                    subtitle = "Advanced security features to keep your data protected and private.",
                    titleColor = Color(0xFFD500F9),
                    graphic = { ShieldGraphic() }
                )
                3 -> OnboardingPage(
                    title = "Fast Performance\nBetter Experience",
                    subtitle = "Optimized for speed and efficiency to give you the best experience.",
                    titleColor = Color(0xFFD500F9),
                    graphic = { RocketGraphic() }
                )
                4 -> FeaturesPage()
            }
        }

        // Bottom Controls
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(32.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (pagerState.currentPage < 4) {
                // Page Indicators
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    repeat(5) { index ->
                        val isSelected = pagerState.currentPage == index
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(if (isSelected) Color(0xFFD500F9) else Color.DarkGray)
                        )
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "Skip",
                        color = Color.Gray,
                        modifier = Modifier
                            .clickable { onFinish() }
                            .padding(end = 24.dp),
                        fontSize = 16.sp
                    )
                    Box(
                        modifier = Modifier
                            .size(56.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF6B4EE6))
                            .clickable {
                                coroutineScope.launch {
                                    pagerState.animateScrollToPage(pagerState.currentPage + 1)
                                }
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.ArrowForward, contentDescription = "Next", tint = Color.White)
                    }
                }
            } else {
                Button(
                    onClick = onFinish,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                    contentPadding = PaddingValues(0.dp) // Reset padding for gradient
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                brush = Brush.horizontalGradient(
                                    colors = listOf(Color(0xFF6B4EE6), Color(0xFFD500F9))
                                ),
                                shape = RoundedCornerShape(28.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("Get Started", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.SemiBold)
                            Spacer(modifier = Modifier.width(8.dp))
                            Icon(Icons.Default.ArrowForward, contentDescription = null, tint = Color.White)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun OnboardingPage(
    title: String,
    subtitle: String? = null,
    titleColor: Color? = null,
    graphic: @Composable () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(80.dp))
        
        Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
            graphic()
        }

        Spacer(modifier = Modifier.height(40.dp))

        if (title.contains("\n")) {
            val parts = title.split("\n")
            Text(parts[0], color = Color.White, fontSize = 28.sp, fontWeight = FontWeight.Bold)
            Text(parts[1], color = titleColor ?: Color.White, fontSize = 28.sp, fontWeight = FontWeight.Bold)
        } else {
            Text(
                text = buildAnnotatedString {
                    withStyle(style = SpanStyle(color = Color.White)) {
                        append("File ")
                    }
                    withStyle(style = SpanStyle(color = Color(0xFF6B4EE6))) {
                        append("Manager")
                    }
                },
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(title, color = Color.LightGray, fontSize = 16.sp)
        }

        if (subtitle != null) {
            Spacer(modifier = Modifier.height(16.dp))
            Text(subtitle, color = Color.Gray, fontSize = 16.sp, modifier = Modifier.padding(horizontal = 16.dp))
        }

        Spacer(modifier = Modifier.height(80.dp))
    }
}

@Composable
fun FeaturesPage() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(80.dp))
        
        Text("Welcome to", color = Color.White, fontSize = 24.sp)
        Text(
            text = buildAnnotatedString {
                withStyle(style = SpanStyle(color = Color.White)) {
                    append("File ")
                }
                withStyle(style = SpanStyle(color = Color(0xFF6B4EE6))) {
                    append("Manager")
                }
            },
            fontSize = 36.sp,
            fontWeight = FontWeight.Bold
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            "Your smart and powerful\nfile management solution",
            color = Color.LightGray,
            fontSize = 16.sp,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(48.dp))

        FeatureRow(Icons.Default.Folder, "Easy Organization", "Keep your files and folders\norganized with ease.", NeonPurple)
        FeatureRow(Icons.Default.Security, "Secure & Private", "Protect your important files\nand personal data.", NeonCyan) // Using cyan instead of green for theme fit, or NeonGreen
        FeatureRow(Icons.Default.FlashOn, "Fast & Efficient", "Optimized performance for\na seamless experience.", Color(0xFFFFD600))
        FeatureRow(Icons.Default.CloudQueue, "Access Anywhere", "Access your files anytime,\nanywhere.", Color(0xFF2196F3))
    }
}

@Composable
fun FeatureRow(icon: ImageVector, title: String, desc: String, color: Color) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .background(Color(0xFF13151A), RoundedCornerShape(16.dp))
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(56.dp)
                .background(color.copy(alpha = 0.15f), RoundedCornerShape(12.dp)),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(28.dp))
        }
        Spacer(modifier = Modifier.width(16.dp))
        Column {
            Text(title, color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
            Spacer(modifier = Modifier.height(4.dp))
            Text(desc, color = Color.Gray, fontSize = 14.sp)
        }
    }
}

// ---------------- GRAPHICS ----------------

@Composable
fun GlowingFolder() {
    Box(
        modifier = Modifier
            .size(100.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(
                brush = Brush.linearGradient(
                    colors = listOf(Color(0xFFFF4081), Color(0xFFD500F9), Color(0xFF00E5FF))
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        // Inner dark hole to make it look like an outline
        Box(
            modifier = Modifier
                .size(94.dp)
                .clip(RoundedCornerShape(14.dp))
                .background(Color(0xFF07090C))
        )
    }
}

@Composable
fun FloatingIcons() {
    val infiniteTransition = rememberInfiniteTransition()
    val floatAnim by infiniteTransition.animateFloat(
        initialValue = -10f,
        targetValue = 10f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        )
    )

    Box(modifier = Modifier.size(240.dp)) {
        FloatingIcon(Icons.Default.MusicNote, NeonCyan, Modifier.offset(x = 20.dp, y = (40 + floatAnim).dp))
        FloatingIcon(Icons.Default.Image, Color(0xFF38F570), Modifier.offset(x = 160.dp, y = (20 - floatAnim).dp))
        FloatingIcon(Icons.Default.Description, NeonPurple, Modifier.offset(x = 10.dp, y = (140 - floatAnim).dp))
        FloatingIcon(Icons.Default.PlayArrow, Color(0xFF2196F3), Modifier.offset(x = 170.dp, y = (150 + floatAnim).dp))
    }
}

@Composable
fun FloatingIcon(icon: ImageVector, color: Color, modifier: Modifier) {
    Box(
        modifier = modifier
            .size(48.dp)
            .background(color.copy(alpha = 0.15f), RoundedCornerShape(12.dp)),
        contentAlignment = Alignment.Center
    ) {
        Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(24.dp))
    }
}

@Composable
fun FoldersGraphic() {
    Box(contentAlignment = Alignment.Center, modifier = Modifier.size(240.dp)) {
        FloatingIcons()
        // Simple stacked folders representation
        Icon(Icons.Default.Folder, contentDescription = null, tint = Color.DarkGray, modifier = Modifier.size(140.dp).offset(y = (-20).dp))
        Icon(Icons.Default.Folder, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(150.dp).offset(y = (-10).dp))
        Icon(Icons.Default.Folder, contentDescription = null, tint = Color(0xFF00E5FF), modifier = Modifier.size(160.dp))
    }
}

@Composable
fun OrbitFolderGraphic() {
    Box(contentAlignment = Alignment.Center, modifier = Modifier.size(240.dp)) {
        // Orbit ring
        Box(
            modifier = Modifier
                .width(220.dp)
                .height(80.dp)
                .clip(RoundedCornerShape(100.dp))
                .background(NeonPurple.copy(alpha = 0.2f))
        )
        FloatingIcons()
        Icon(Icons.Default.Folder, contentDescription = null, tint = NeonPurple, modifier = Modifier.size(120.dp))
    }
}

@Composable
fun ShieldGraphic() {
    Box(contentAlignment = Alignment.Center, modifier = Modifier.size(240.dp)) {
        // Orbit ring
        Box(
            modifier = Modifier
                .width(220.dp)
                .height(80.dp)
                .clip(RoundedCornerShape(100.dp))
                .background(NeonPurple.copy(alpha = 0.2f))
        )
        FloatingIcons()
        Icon(Icons.Default.Security, contentDescription = null, tint = NeonPurple, modifier = Modifier.size(120.dp))
    }
}

@Composable
fun RocketGraphic() {
    Box(contentAlignment = Alignment.Center, modifier = Modifier.size(240.dp)) {
        // Base platform (dark rounded square)
        Box(
            modifier = Modifier
                .width(160.dp)
                .height(40.dp)
                .offset(y = 60.dp)
                .background(Color(0xFF13151A), RoundedCornerShape(20.dp))
        )
        // Fire/Thrust gradient
        Box(
            modifier = Modifier
                .width(30.dp)
                .height(80.dp)
                .offset(y = 30.dp)
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(Color(0xFF00E5FF), Color(0xFFFFD600), Color.Transparent)
                    )
                )
        )
        FloatingIcons()
        // Rocket (using Send icon rotated up)
        Box(
            modifier = Modifier
                .size(100.dp)
                .offset(y = (-30).dp)
                .background(NeonPurple.copy(alpha = 0.2f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                Icons.Default.Send,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier
                    .size(60.dp)
                    .offset(x = (-4).dp, y = 4.dp)
                    .rotate(-45f)
            )
        }
    }
}
