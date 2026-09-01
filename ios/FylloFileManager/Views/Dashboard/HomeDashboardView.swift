import SwiftUI

public struct HomeDashboardView: View {
    @StateObject private var viewModel = HomeViewModel()
    @EnvironmentObject private var settingsVM: SettingsViewModel
    
    public var onNavigateToFiles: (URL?) -> Void
    public var onNavigateToMedia: (FileCategory) -> Void
    public var onNavigateToSafeFolder: () -> Void
    public var onNavigateToCleanup: () -> Void
    public var onNavigateToTrash: () -> Void
    public var onNavigateToSearch: () -> Void
    public var onOpenFile: (FileItem) -> Void
    
    @State private var showingDocPicker = false
    
    public var body: some View {
        NavigationView {
            ZStack {
                Color.backgroundColor(
                    for: settingsVM.settings.colorTheme,
                    isDark: settingsVM.settings.themeMode != .light,
                    amoled: settingsVM.settings.amoledBlack
                )
                .ignoresSafeArea()
                
                ScrollView(showsIndicators: false) {
                    VStack(spacing: 20) {
                        // Top Header Bar
                        HStack {
                            VStack(alignment: .leading, spacing: 2) {
                                Text("Fyllo")
                                    .font(.title2).bold()
                                    .foregroundColor(.white)
                                Text("File Manager")
                                    .font(.subheadline)
                                    .foregroundColor(.neonCyan)
                            }
                            
                            Spacer()
                            
                            HStack(spacing: 12) {
                                Button(action: onNavigateToSearch) {
                                    Image(systemName: "magnifyingglass")
                                        .font(.system(size: 18, weight: .semibold))
                                        .foregroundColor(.white)
                                        .frame(width: 40, height: 40)
                                        .background(Color.white.opacity(0.1))
                                        .clipShape(Circle())
                                }
                                
                                Button(action: { showingDocPicker = true }) {
                                    Image(systemName: "plus")
                                        .font(.system(size: 18, weight: .semibold))
                                        .foregroundColor(.white)
                                        .frame(width: 40, height: 40)
                                        .background(Color.neonPurple)
                                        .clipShape(Circle())
                                }
                            }
                        }
                        .padding(.horizontal)
                        .padding(.top, 8)
                        
                        // Storage Card
                        StorageInfoCard(stats: viewModel.storageStats) {
                            onNavigateToCleanup()
                        }
                        .padding(.horizontal)
                        
                        // Quick Action Tools
                        HStack(spacing: 12) {
                            QuickToolButton(icon: "lock.shield.fill", title: "Safe Vault", color: .neonPurple, onTap: onNavigateToSafeFolder)
                            QuickToolButton(icon: "sparkles", title: "Clean Up", color: .neonGreen, onTap: onNavigateToCleanup)
                            QuickToolButton(icon: "trash.fill", title: "Trash", color: .neonPink, onTap: onNavigateToTrash)
                        }
                        .padding(.horizontal)
                        
                        // Categories Grid
                        VStack(alignment: .leading, spacing: 12) {
                            Text("Categories")
                                .font(.headline)
                                .foregroundColor(.white)
                                .padding(.horizontal)
                            
                            LazyVGrid(columns: [GridItem(.flexible()), GridItem(.flexible())], spacing: 12) {
                                CategoryCard(category: .image, count: viewModel.categoryCounts[.image] ?? 0) {
                                    onNavigateToMedia(.image)
                                }
                                CategoryCard(category: .video, count: viewModel.categoryCounts[.video] ?? 0) {
                                    onNavigateToMedia(.video)
                                }
                                CategoryCard(category: .audio, count: viewModel.categoryCounts[.audio] ?? 0) {
                                    onNavigateToMedia(.audio)
                                }
                                CategoryCard(category: .document, count: viewModel.categoryCounts[.document] ?? 0) {
                                    onNavigateToMedia(.document)
                                }
                                CategoryCard(category: .pdf, count: viewModel.categoryCounts[.pdf] ?? 0) {
                                    onNavigateToMedia(.pdf)
                                }
                                CategoryCard(category: .archive, count: viewModel.categoryCounts[.archive] ?? 0) {
                                    onNavigateToMedia(.archive)
                                }
                            }
                            .padding(.horizontal)
                        }
                        
                        // Recent Files Carousel
                        if !viewModel.recentFiles.isEmpty {
                            VStack(alignment: .leading, spacing: 12) {
                                HStack {
                                    Text("Recent Files")
                                        .font(.headline)
                                        .foregroundColor(.white)
                                    Spacer()
                                    Button(action: { onNavigateToFiles(nil) }) {
                                        Text("See all")
                                            .font(.caption).bold()
                                            .foregroundColor(.neonCyan)
                                    }
                                }
                                .padding(.horizontal)
                                
                                ScrollView(.horizontal, showsIndicators: false) {
                                    HStack(spacing: 12) {
                                        ForEach(viewModel.recentFiles) { file in
                                            RecentFileCard(file: file) {
                                                onOpenFile(file)
                                            }
                                        }
                                    }
                                    .padding(.horizontal)
                                }
                            }
                        }
                        
                        Spacer().frame(height: 100) // Padding for bottom dock
                    }
                }
            }
            .navigationBarHidden(true)
            .sheet(isPresented: $showingDocPicker) {
                DocumentPickerView { urls in
                    for url in urls {
                        let dest = FileManagerService.shared.documentsDirectory.appendingPathComponent(url.lastPathComponent)
                        try? FileManagerService.shared.copyItem(at: url, to: dest)
                    }
                    viewModel.refreshDashboard()
                }
            }
            .onAppear {
                viewModel.refreshDashboard()
            }
        }
        .navigationViewStyle(StackNavigationViewStyle())
    }
}

private struct QuickToolButton: View {
    var icon: String
    var title: String
    var color: Color
    var onTap: () -> Void
    
    var body: some View {
        Button(action: onTap) {
            VStack(spacing: 8) {
                Image(systemName: icon)
                    .font(.system(size: 20, weight: .semibold))
                    .foregroundColor(color)
                Text(title)
                    .font(.caption).bold()
                    .foregroundColor(.white)
            }
            .frame(maxWidth: .infinity)
            .padding(.vertical, 14)
            .glassCard(cornerRadius: 16, strokeColor: color.opacity(0.2), backgroundColor: Color.darkSurface)
        }
        .buttonStyle(PlainButtonStyle())
    }
}
