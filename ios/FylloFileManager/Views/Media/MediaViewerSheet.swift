import SwiftUI
import AVKit

public struct MediaViewerSheet: View {
    public var item: FileItem
    public var onDismiss: () -> Void
    public var onDelete: () -> Void
    public var onEdit: () -> Void
    
    @State private var showingInfo = false
    @State private var player: AVPlayer? = nil
    @State private var shareURL: URL? = nil
    
    public var body: some View {
        ZStack {
            Color.black.ignoresSafeArea()
            
            // Content
            if item.category == .image {
                AsyncImagePreview(url: item.url)
            } else if item.category == .video {
                if let p = player {
                    VideoPlayer(player: p)
                        .ignoresSafeArea()
                } else {
                    ProgressView().accentColor(.white)
                }
            } else {
                VStack(spacing: 16) {
                    Image(systemName: item.systemIconName)
                        .font(.system(size: 64))
                        .foregroundColor(item.categoryColor)
                    Text(item.name)
                        .font(.headline)
                        .foregroundColor(.white)
                }
            }
            
            // Top Controls Overlay
            VStack {
                HStack {
                    Button(action: onDismiss) {
                        Image(systemName: "xmark")
                            .font(.system(size: 16, weight: .bold))
                            .foregroundColor(.white)
                            .frame(width: 36, height: 36)
                            .background(Color.black.opacity(0.6))
                            .clipShape(Circle())
                    }
                    
                    Spacer()
                    
                    Text(item.name)
                        .font(.subheadline).bold()
                        .foregroundColor(.white)
                        .lineLimit(1)
                    
                    Spacer()
                    
                    HStack(spacing: 8) {
                        if item.category == .image {
                            Button(action: onEdit) {
                                Image(systemName: "slider.horizontal.3")
                                    .font(.system(size: 16, weight: .bold))
                                    .foregroundColor(.white)
                                    .frame(width: 36, height: 36)
                                    .background(Color.black.opacity(0.6))
                                    .clipShape(Circle())
                            }
                        }
                        
                        Button(action: { showingInfo.toggle() }) {
                            Image(systemName: "info.circle")
                                .font(.system(size: 16, weight: .bold))
                                .foregroundColor(.white)
                                .frame(width: 36, height: 36)
                                .background(Color.black.opacity(0.6))
                                .clipShape(Circle())
                        }
                        
                        Button(action: { shareURL = item.url }) {
                            Image(systemName: "square.and.arrow.up")
                                .font(.system(size: 16, weight: .bold))
                                .foregroundColor(.white)
                                .frame(width: 36, height: 36)
                                .background(Color.black.opacity(0.6))
                                .clipShape(Circle())
                        }
                    }
                }
                .padding(.horizontal)
                .padding(.top, 44)
                
                Spacer()
            }
        }
        .onAppear {
            if item.category == .video {
                player = AVPlayer(url: item.url)
                player?.play()
            }
        }
        .onDisappear {
            player?.pause()
            player = nil
        }
        .sheet(isPresented: $showingInfo) {
            MediaInfoSheet(item: item)
        }
        .sheet(item: Binding(
            get: { shareURL != nil ? ShareItem(url: shareURL!) : nil },
            set: { _ in shareURL = nil }
        )) { share in
            ShareSheet(items: [share.url])
        }
    }
}

private struct AsyncImagePreview: View {
    var url: URL
    @State private var scale: CGFloat = 1.0
    
    var body: some View {
        if let data = try? Data(contentsOf: url), let uiImage = UIImage(data: data) {
            Image(uiImage: uiImage)
                .resizable()
                .scaledToFit()
                .scaleEffect(scale)
                .gesture(
                    MagnificationGesture()
                        .onChanged { val in scale = val }
                        .onEnded { _ in withAnimation { scale = 1.0 } }
                )
        } else {
            Image(systemName: "photo")
                .font(.system(size: 64))
                .foregroundColor(.gray)
        }
    }
}

private struct MediaInfoSheet: View {
    var item: FileItem
    
    var body: some View {
        NavigationView {
            ZStack {
                Color.darkSurface.ignoresSafeArea()
                
                List {
                    Section(header: Text("Details").foregroundColor(.neonCyan)) {
                        InfoRow(label: "Name", value: item.name)
                        InfoRow(label: "Type", value: item.fileExtension.uppercased())
                        InfoRow(label: "Size", value: item.formattedSize)
                        InfoRow(label: "Modified", value: item.formattedDate)
                        InfoRow(label: "Path", value: item.path)
                    }
                    .listRowBackground(Color.darkSurfaceVariant)
                }
                .hideScrollContentBackground()
            }
            .navigationTitle("File Info")
            .navigationBarTitleDisplayMode(.inline)
        }
    }
}

private struct InfoRow: View {
    var label: String
    var value: String
    
    var body: some View {
        VStack(alignment: .leading, spacing: 4) {
            Text(label)
                .font(.caption)
                .foregroundColor(.gray)
            Text(value)
                .font(.subheadline)
                .foregroundColor(.white)
        }
        .padding(.vertical, 2)
    }
}

private struct ShareItem: Identifiable {
    let id = UUID()
    let url: URL
}
