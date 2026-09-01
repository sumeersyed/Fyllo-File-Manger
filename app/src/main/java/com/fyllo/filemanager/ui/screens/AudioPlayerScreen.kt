package com.fyllo.filemanager.ui.screens

import android.text.format.Formatter
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import com.fyllo.filemanager.domain.model.FileItem
import kotlinx.coroutines.delay

// All audio formats supported by ExoPlayer/Media3
val AUDIO_EXTENSIONS = setOf(
    "mp3", "m4a", "wav", "ogg", "flac", "aac", "wma", "opus",
    "aiff", "aif", "amr", "mid", "midi", "3gp", "mp4", "mkv", "webm"
)

fun isAudioFile(file: FileItem): Boolean {
    val ext = file.extension.lowercase()
    return file.mimeType?.startsWith("audio/") == true || ext in AUDIO_EXTENSIONS
}

/**
 * Extracts a clean, human-readable display name from a FileItem.
 */
private fun FileItem.displayName(): String {
    val cleaned = name.substringBeforeLast('.')
    val looksLikeId = cleaned.all { it.isDigit() || it == '-' || it == '+' }
    if (!looksLikeId && cleaned.isNotBlank()) return cleaned

    val uriPath = uri.lastPathSegment ?: uri.path ?: name
    val fromPath = uriPath.substringAfterLast('/').substringAfterLast(':')
    val fromPathCleaned = fromPath.substringBeforeLast('.')
    if (fromPathCleaned.isNotBlank() && fromPathCleaned.length > 3) return fromPathCleaned

    if (path.isNotBlank()) {
        val fromFilePath = path.substringAfterLast('/').substringBeforeLast('.')
        if (fromFilePath.isNotBlank()) return fromFilePath
    }

    return cleaned.ifBlank { name }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AudioPlayerScreen(
    onBackClick: () -> Unit
) {
    val playlist = AudioPlaylist.files
    val initialIndex = AudioPlaylist.initialIndex.coerceIn(0, (playlist.size - 1).coerceAtLeast(0))

    var currentIndex by remember { mutableStateOf(initialIndex) }
    var isPlaying by remember { mutableStateOf(false) }
    var isShuffled by remember { mutableStateOf(false) }
    var repeatMode by remember { mutableStateOf(Player.REPEAT_MODE_OFF) }
    var currentPositionMs by remember { mutableStateOf(0L) }
    var durationMs by remember { mutableStateOf(0L) }
    var isUserScrubbing by remember { mutableStateOf(false) }

    val context = LocalContext.current
    val currentFile = playlist.getOrNull(currentIndex)

    // Playlist lazy column state — auto scroll to active song
    val listState = rememberLazyListState()

    LaunchedEffect(currentIndex) {
        if (playlist.isNotEmpty()) {
            listState.animateScrollToItem(
                index = currentIndex.coerceIn(0, playlist.size - 1),
                scrollOffset = -100
            )
        }
    }

    // ── ExoPlayer setup ──────────────────────────────────────────────────────
    val audioAttributes = remember {
        AudioAttributes.Builder()
            .setUsage(C.USAGE_MEDIA)
            .setContentType(C.AUDIO_CONTENT_TYPE_MUSIC)
            .build()
    }

    val exoPlayer = remember {
        ExoPlayer.Builder(context)
            .setAudioAttributes(audioAttributes, true)
            .build().apply {
                repeatMode = Player.REPEAT_MODE_OFF
            }
    }

    LaunchedEffect(currentIndex) {
        val file = playlist.getOrNull(currentIndex) ?: return@LaunchedEffect
        exoPlayer.setMediaItem(MediaItem.fromUri(file.uri))
        exoPlayer.prepare()
        exoPlayer.playWhenReady = true
        isPlaying = true
    }

    LaunchedEffect(repeatMode) { exoPlayer.repeatMode = repeatMode }

    LaunchedEffect(exoPlayer) {
        while (true) {
            if (!isUserScrubbing) {
                currentPositionMs = exoPlayer.currentPosition.coerceAtLeast(0)
                durationMs = exoPlayer.duration.takeIf { it > 0 } ?: 0L
            }
            delay(500)
        }
    }

    DisposableEffect(exoPlayer) {
        val listener = object : Player.Listener {
            override fun onPlaybackStateChanged(state: Int) {
                if (state == Player.STATE_ENDED && repeatMode == Player.REPEAT_MODE_OFF) {
                    currentIndex = if (isShuffled) {
                        (0 until playlist.size).filter { it != currentIndex }.randomOrNull() ?: 0
                    } else {
                        (currentIndex + 1).takeIf { it < playlist.size } ?: 0
                    }
                }
            }
            override fun onIsPlayingChanged(playing: Boolean) { isPlaying = playing }
        }
        exoPlayer.addListener(listener)
        onDispose {
            exoPlayer.removeListener(listener)
            exoPlayer.release()
        }
    }

    // Music note pulsing animation
    val infiniteTransition = rememberInfiniteTransition(label = "note_pulse")
    val noteScale by infiniteTransition.animateFloat(
        initialValue = 0.95f, targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(900, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ), label = "pulse"
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Now Playing", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = {
                        exoPlayer.release()
                        onBackClick()
                    }) {
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
    ) { padding ->
        if (playlist.isEmpty() || currentFile == null) {
            Box(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    "No audio files to play",
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                    fontSize = 16.sp
                )
            }
            return@Scaffold
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // ─── CONSTANT / FIXED NOW PLAYING PANEL ──────────────────────────
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(8.dp))

                // Album Art Box (Compact & Polished)
                Box(
                    modifier = Modifier
                        .size(180.dp)
                        .shadow(16.dp, RoundedCornerShape(24.dp))
                        .clip(RoundedCornerShape(24.dp))
                        .background(
                            Brush.radialGradient(
                                colors = listOf(
                                    MaterialTheme.colorScheme.primary.copy(alpha = 0.55f),
                                    MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
                                )
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.MusicNote,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier
                            .size(84.dp)
                            .graphicsLayer(scaleX = noteScale, scaleY = noteScale)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Track Title
                Text(
                    text = currentFile.displayName(),
                    color = MaterialTheme.colorScheme.onBackground,
                    fontSize = 19.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(4.dp))

                // Track Meta
                Text(
                    text = buildString {
                        val ext = currentFile.extension.uppercase()
                        if (ext.isNotBlank()) append(ext)
                        if (currentFile.sizeBytes > 0) {
                            if (isNotEmpty()) append(" • ")
                            append(Formatter.formatFileSize(context, currentFile.sizeBytes))
                        }
                        if (playlist.size > 1) {
                            if (isNotEmpty()) append(" • ")
                            append("${currentIndex + 1} of ${playlist.size}")
                        }
                    },
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.55f),
                    fontSize = 12.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Seek Bar Slider
                val progress = if (durationMs > 0) currentPositionMs.toFloat() / durationMs else 0f
                Slider(
                    value = progress.coerceIn(0f, 1f),
                    onValueChange = { v ->
                        isUserScrubbing = true
                        currentPositionMs = (v * durationMs).toLong()
                    },
                    onValueChangeFinished = {
                        exoPlayer.seekTo(currentPositionMs)
                        isUserScrubbing = false
                    },
                    colors = SliderDefaults.colors(
                        thumbColor = MaterialTheme.colorScheme.primary,
                        activeTrackColor = MaterialTheme.colorScheme.primary,
                        inactiveTrackColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = formatDuration(currentPositionMs),
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.55f),
                        fontSize = 11.sp
                    )
                    Text(
                        text = formatDuration(durationMs),
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.55f),
                        fontSize = 11.sp
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Playback Controls Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Shuffle Toggle
                    IconButton(onClick = { isShuffled = !isShuffled }) {
                        Icon(
                            imageVector = Icons.Default.Shuffle,
                            contentDescription = "Shuffle",
                            tint = if (isShuffled) MaterialTheme.colorScheme.primary
                                   else MaterialTheme.colorScheme.onBackground.copy(alpha = 0.45f),
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    // Previous Track
                    IconButton(
                        onClick = {
                            currentIndex = if (isShuffled) {
                                (0 until playlist.size).filter { it != currentIndex }.randomOrNull() ?: 0
                            } else {
                                (currentIndex - 1 + playlist.size) % playlist.size
                            }
                        },
                        modifier = Modifier.size(48.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.SkipPrevious,
                            contentDescription = "Previous",
                            tint = MaterialTheme.colorScheme.onBackground,
                            modifier = Modifier.size(32.dp)
                        )
                    }

                    // Large Play / Pause Button
                    Box(
                        modifier = Modifier
                            .size(60.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primary)
                            .clickable {
                                if (exoPlayer.isPlaying) exoPlayer.pause() else exoPlayer.play()
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                            contentDescription = if (isPlaying) "Pause" else "Play",
                            tint = Color.White,
                            modifier = Modifier.size(34.dp)
                        )
                    }

                    // Next Track
                    IconButton(
                        onClick = {
                            currentIndex = if (isShuffled) {
                                (0 until playlist.size).filter { it != currentIndex }.randomOrNull() ?: 0
                            } else {
                                (currentIndex + 1) % playlist.size
                            }
                        },
                        modifier = Modifier.size(48.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.SkipNext,
                            contentDescription = "Next",
                            tint = MaterialTheme.colorScheme.onBackground,
                            modifier = Modifier.size(32.dp)
                        )
                    }

                    // Repeat Toggle
                    IconButton(
                        onClick = {
                            repeatMode = when (repeatMode) {
                                Player.REPEAT_MODE_OFF -> Player.REPEAT_MODE_ONE
                                Player.REPEAT_MODE_ONE -> Player.REPEAT_MODE_ALL
                                else -> Player.REPEAT_MODE_OFF
                            }
                        }
                    ) {
                        Icon(
                            imageVector = when (repeatMode) {
                                Player.REPEAT_MODE_ONE -> Icons.Default.RepeatOne
                                else -> Icons.Default.Repeat
                            },
                            contentDescription = "Repeat",
                            tint = if (repeatMode != Player.REPEAT_MODE_OFF) MaterialTheme.colorScheme.primary
                                   else MaterialTheme.colorScheme.onBackground.copy(alpha = 0.45f),
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Playlist Header Bar
                if (playlist.size > 1) {
                    HorizontalDivider(color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.08f))
                    Spacer(modifier = Modifier.height(10.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Playlist",
                            color = MaterialTheme.colorScheme.onBackground,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "${playlist.size} songs",
                            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f),
                            fontSize = 12.sp
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }

            // ─── SCROLLABLE PLAYLIST SECTION ─────────────────────────────────
            if (playlist.size > 1) {
                LazyColumn(
                    state = listState,
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    contentPadding = PaddingValues(bottom = 24.dp)
                ) {
                    itemsIndexed(
                        items = playlist,
                        key = { idx, file -> "${file.id}_$idx" }
                    ) { index, file ->
                        val isCurrentTrack = index == currentIndex
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(
                                    if (isCurrentTrack)
                                        MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
                                    else Color.Transparent
                                )
                                .clickable { currentIndex = index }
                                .padding(horizontal = 12.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Track Number / Sound Indicator Badge
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(
                                        MaterialTheme.colorScheme.primary.copy(
                                            alpha = if (isCurrentTrack) 0.25f else 0.08f
                                        )
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                if (isCurrentTrack && isPlaying) {
                                    Icon(
                                        Icons.Default.VolumeUp,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(18.dp)
                                    )
                                } else {
                                    Text(
                                        text = "${index + 1}",
                                        color = if (isCurrentTrack)
                                            MaterialTheme.colorScheme.primary
                                        else
                                            MaterialTheme.colorScheme.onBackground.copy(alpha = 0.45f),
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.width(12.dp))

                            // Track Name & Duration
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = file.displayName(),
                                    color = if (isCurrentTrack)
                                        MaterialTheme.colorScheme.primary
                                    else
                                        MaterialTheme.colorScheme.onBackground,
                                    fontSize = 14.sp,
                                    fontWeight = if (isCurrentTrack) FontWeight.SemiBold else FontWeight.Normal,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                if (file.durationMs != null && file.durationMs > 0) {
                                    Text(
                                        text = formatDuration(file.durationMs),
                                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.4f),
                                        fontSize = 11.sp
                                    )
                                }
                            }
                        }

                        if (index < playlist.size - 1) {
                            HorizontalDivider(
                                modifier = Modifier.padding(start = 60.dp),
                                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.04f),
                                thickness = 0.5.dp
                            )
                        }
                    }
                }
            }
        }
    }
}

private fun formatDuration(ms: Long): String {
    if (ms <= 0) return "0:00"
    val totalSec = ms / 1000
    val min = totalSec / 60
    val sec = totalSec % 60
    return "$min:${sec.toString().padStart(2, '0')}"
}
