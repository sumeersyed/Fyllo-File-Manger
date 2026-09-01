import SwiftUI

public struct MediaView: View {
    @StateObject private var viewModel = MediaViewModel()
    @EnvironmentObject private var settingsVM: SettingsViewModel
    
    public var initialCategory: FileCategory = .image
    public var onSelectMedia: (FileItem) -> Void
    
    @State private var selectedTab: FileCategory = .image
    
    public init(initialCategory: FileCategory = .image, onSelectMedia: @escaping (FileItem) -> Void) {
        self.initialCategory = initialCategory
        self.onSelectMedia = onSelectMedia
    }
    
    public var body: some View {
        ZStack {
            Color.backgroundColor(
                for: settingsVM.settings.colorTheme,
                isDark: settingsVM.settings.themeMode != .light,
                amoled: settingsVM.settings.amoledBlack
            )
            .ignoresSafeArea()
            
            VStack(spacing: 12) {
                // Header
                HStack {
                    Text("Media Gallery")
                        .font(.title2).bold()
                        .foregroundColor(.white)
                    Spacer()
                }
                .padding(.horizontal)
                .padding(.top, 8)
                
                // Category Switcher Pills
                ScrollView(.horizontal, showsIndicators: false) {
                    HStack(spacing: 8) {
                        CategoryPill(title: "Images", icon: "photo.fill", isSelected: selectedTab == .image, color: .neonPink) {
                            selectedTab = .image
                            viewModel.setCategory(.image)
                        }
                        CategoryPill(title: "Videos", icon: "film.fill", isSelected: selectedTab == .video, color: .neonPurple) {
                            selectedTab = .video
                            viewModel.setCategory(.video)
                        }
                        CategoryPill(title: "Audio", icon: "music.note", isSelected: selectedTab == .audio, color: .neonGreen) {
                            selectedTab = .audio
                            viewModel.setCategory(.audio)
                        }
                        CategoryPill(title: "Docs", icon: "doc.text.fill", isSelected: selectedTab == .document, color: .neonCyan) {
                            selectedTab = .document
                            viewModel.setCategory(.document)
                        }
                    }
                    .padding(.horizontal)
                }
                
                // Content Grid
                if viewModel.isLoading {
                    Spacer()
                    ProgressView().accentColor(.neonCyan)
                    Spacer()
                } else if viewModel.mediaItems.isEmpty {
                    Spacer()
                    VStack(spacing: 12) {
                        Image(systemName: selectedTab.iconName)
                            .font(.system(size: 44))
                            .foregroundColor(.gray)
                        Text("No \(selectedTab.rawValue) found")
                            .font(.headline)
                            .foregroundColor(.gray)
                    }
                    Spacer()
                } else {
                    ScrollView(showsIndicators: false) {
                        LazyVGrid(columns: [GridItem(.flexible()), GridItem(.flexible()), GridItem(.flexible())], spacing: 4) {
                            ForEach(viewModel.mediaItems) { item in
                                Button(action: { onSelectMedia(item) }) {
                                    MediaTile(item: item)
                                }
                            }
                        }
                        .padding(.horizontal, 4)
                        .padding(.bottom, 120)
                    }
                }
            }
        }
        .onAppear {
            selectedTab = initialCategory
            viewModel.setCategory(initialCategory)
        }
    }
}

private struct CategoryPill: View {
    var title: String
    var icon: String
    var isSelected: Bool
    var color: Color
    var onTap: () -> Void
    
    var body: some View {
        Button(action: onTap) {
            HStack(spacing: 6) {
                Image(systemName: icon)
                    .font(.system(size: 13))
                Text(title)
                    .font(.subheadline).bold()
            }
            .foregroundColor(isSelected ? .white : .gray)
            .padding(.horizontal, 14)
            .padding(.vertical, 8)
            .background(isSelected ? color : Color.white.opacity(0.08))
            .clipShape(Capsule())
        }
    }
}

private struct MediaTile: View {
    var item: FileItem
    
    var body: some View {
        ZStack(alignment: .bottomTrailing) {
            ZStack {
                Rectangle()
                    .fill(Color.white.opacity(0.05))
                    .aspectRatio(1, contentMode: .fit)
                
                Image(systemName: item.systemIconName)
                    .font(.system(size: 28))
                    .foregroundColor(item.categoryColor)
            }
            
            if item.category == .video, let duration = item.durationSeconds {
                Text(formatDuration(duration))
                    .font(.system(size: 10, weight: .bold))
                    .foregroundColor(.white)
                    .padding(.horizontal, 6)
                    .padding(.vertical, 2)
                    .background(Color.black.opacity(0.7))
                    .clipShape(RoundedRectangle(cornerRadius: 4))
                    .padding(4)
            }
        }
        .clipped()
        .cornerRadius(6)
    }
    
    private func formatDuration(_ seconds: Double) -> String {
        let mins = Int(seconds) / 60
        let secs = Int(seconds) % 60
        return String(format: "%d:%02d", mins, secs)
    }
}
