# Fyllo-File-Manger

[![iOS Build](https://github.com/sumeersyed/Fyllo-File-Manger/actions/workflows/ios-build.yml/badge.svg)](https://github.com/sumeersyed/Fyllo-File-Manger/actions/workflows/ios-build.yml)
[![Platform](https://img.shields.io/badge/Platform-iOS%2014.0%2B%20%7C%20Android-blue.svg)](https://github.com/sumeersyed/Fyllo-File-Manger)
[![Swift](https://img.shields.io/badge/Swift-5.9%2B-orange.svg)](https://swift.org)
[![Kotlin](https://img.shields.io/badge/Kotlin-Compose-purple.svg)](https://kotlinlang.org)

A modern, fast, and feature-rich File Manager with cross-platform support (Native Swift/SwiftUI for iOS and Jetpack Compose for Android).

## ✨ Features

- 📂 **Full File Management**: Hierarchical file & folder browser, breadcrumbs, search, multi-selection, and ZIP compression.
- 🎨 **Rich Neon Aesthetics**: Dynamic dark/light themes (Neon Purple, Cyan, Green, Pink, Ocean Blue, Forest Green, AMOLED Black, E-Ink).
- 🔒 **Safe Vault**: AES-256 military-grade encrypted storage protected by Face ID, Touch ID, and device biometric authentication.
- 🎵 **Audio Player Engine**: Background lock screen playback controls (`MPNowPlayingInfoCenter` / `AVAudioSession`), queue playlists, shuffle & repeat.
- 🖼️ **Media Gallery & Photo Editor**: Fullscreen zoomable photo & video player with CoreImage filters and rotations.
- 📄 **Built-in Document Viewers**: Native PDFKit reader and monospace code/text viewer.
- 🧹 **Storage Cleanup**: One-tap cache and temporary junk cleaner, large file analyzer, and SHA-256 duplicate file finder.

## 🚀 Building the iOS App

### Method 1: Cloud Build via GitHub Actions (Free, No Mac needed)
1. Go to the **Actions** tab in this GitHub repository.
2. Select the **Build iOS App** workflow.
3. Click **Run workflow** — GitHub will automatically compile the Swift/SwiftUI application on macOS runners and upload the `.app` bundle.

### Method 2: Local Mac Build
1. Open `ios/FylloFileManager.xcodeproj` in **Xcode 15+**.
2. Select your device / simulator and press `Cmd + R` to run.
