import SwiftUI

public struct AppRootView: View {
    @StateObject private var settingsVM = SettingsViewModel()
    @ObservedObject private var audioPlayer = AudioPlayerService.shared
    
    @State private var selectedTab: MainTab = .home
    @State private var isAppUnlocked = false
    
    // Sheets & Full Screen Modals
    @State private var activeMediaViewerItem: FileItem? = nil
    @State private var activePdfViewerItem: FileItem? = nil
    @State private var activeTextViewerItem: FileItem? = nil
    @State private var activeImageEditorItem: FileItem? = nil
    @State private var showingAudioPlayer = false
    @State private var showingSafeFolder = false
    @State private var showingTrash = false
    @State private var showingSearch = false
    @State private var initialDirectoryForFiles: URL? = nil
    @State private var initialMediaCategory: FileCategory = .image
    
    public var body: some View {
        ZStack {
            if !settingsVM.settings.isOnboardingCompleted {
                OnboardingView {
                    settingsVM.settings.isOnboardingCompleted = true
                }
            } else if settingsVM.settings.isAppLockEnabled && !isAppUnlocked {
                AppLockView {
                    isAppUnlocked = true
                }
            } else {
                // Main App Content
                ZStack(alignment: .bottom) {
                    Group {
                        switch selectedTab {
                        case .home:
                            HomeDashboardView(
                                onNavigateToFiles: { url in
                                    initialDirectoryForFiles = url
                                    selectedTab = .files
                                },
                                onNavigateToMedia: { cat in
                                    initialMediaCategory = cat
                                    selectedTab = .media
                                },
                                onNavigateToSafeFolder: { showingSafeFolder = true },
                                onNavigateToCleanup: { selectedTab = .cleanup },
                                onNavigateToTrash: { showingTrash = true },
                                onNavigateToSearch: { showingSearch = true },
                                onOpenFile: { openFile($0) }
                            )
                        case .files:
                            FilesView(initialDirectory: initialDirectoryForFiles) { file in
                                openFile(file)
                            }
                        case .media:
                            MediaView(initialCategory: initialMediaCategory) { file in
                                openFile(file)
                            }
                        case .cleanup:
                            StorageCleanupView {
                                selectedTab = .home
                            }
                        case .settings:
                            SettingsView {
                                selectedTab = .home
                            }
                        }
                    }
                    
                    // Floating Overlays: Mini Audio Player & Floating Dock
                    VStack(spacing: 8) {
                        if audioPlayer.currentTrack != nil {
                            MiniAudioPlayer {
                                showingAudioPlayer = true
                            }
                            .transition(.move(edge: .bottom).combined(with: .opacity))
                        }
                        
                        FloatingBottomDock(selectedTab: $selectedTab)
                    }
                    .padding(.bottom, 8)
                }
                .ignoresSafeArea(.keyboard)
            }
        }
        .environmentObject(settingsVM)
        .fullScreenCover(item: $activeMediaViewerItem) { item in
            MediaViewerSheet(
                item: item,
                onDismiss: { activeMediaViewerItem = nil },
                onDelete: {
                    _ = try? FileManagerService.shared.moveToTrash(item: item)
                    activeMediaViewerItem = nil
                },
                onEdit: {
                    activeImageEditorItem = item
                    activeMediaViewerItem = nil
                }
            )
        }
        .fullScreenCover(isPresented: $showingAudioPlayer) {
            AudioPlayerView {
                showingAudioPlayer = false
            }
        }
        .sheet(item: $activePdfViewerItem) { item in
            PdfViewer(item: item) {
                activePdfViewerItem = nil
            }
        }
        .sheet(item: $activeTextViewerItem) { item in
            TextViewer(item: item) {
                activeTextViewerItem = nil
            }
        }
        .sheet(item: $activeImageEditorItem) { item in
            ImageEditorView(item: item, onDismiss: { activeImageEditorItem = nil }, onSaved: {})
        }
        .sheet(isPresented: $showingSafeFolder) {
            SafeFolderView {
                showingSafeFolder = false
            }
            .environmentObject(settingsVM)
        }
        .sheet(isPresented: $showingTrash) {
            TrashView {
                showingTrash = false
            }
            .environmentObject(settingsVM)
        }
        .sheet(isPresented: $showingSearch) {
            SearchView(onDismiss: { showingSearch = false }) { file in
                openFile(file)
            }
            .environmentObject(settingsVM)
        }
    }
    
    private func openFile(_ file: FileItem) {
        if file.category == .audio {
            audioPlayer.loadPlaylist([file], startIndex: 0)
            showingAudioPlayer = true
        } else if file.category == .image || file.category == .video {
            activeMediaViewerItem = file
        } else if file.category == .pdf {
            activePdfViewerItem = file
        } else if file.category == .document {
            activeTextViewerItem = file
        }
    }
}
