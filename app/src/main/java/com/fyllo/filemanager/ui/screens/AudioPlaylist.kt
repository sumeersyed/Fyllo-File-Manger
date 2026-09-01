package com.fyllo.filemanager.ui.screens

import com.fyllo.filemanager.domain.model.FileItem

/**
 * Singleton holder for the audio player playlist.
 * Set before navigating to AudioPlayerScreen so the full playlist
 * is available without needing to pass it through navigation args.
 */
object AudioPlaylist {
    var files: List<FileItem> = emptyList()
    var initialIndex: Int = 0
}
