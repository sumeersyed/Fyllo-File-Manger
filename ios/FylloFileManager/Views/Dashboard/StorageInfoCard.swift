import SwiftUI

public struct StorageInfoCard: View {
    public var stats: StorageStats
    public var onManageTap: () -> Void
    
    public var body: some View {
        VStack(alignment: .leading, spacing: 16) {
            HStack {
                VStack(alignment: .leading, spacing: 4) {
                    Text("Device Storage")
                        .font(.headline)
                        .foregroundColor(.white)
                    Text("\(stats.formattedUsed) used of \(stats.formattedTotal)")
                        .font(.subheadline)
                        .foregroundColor(.gray)
                }
                
                Spacer()
                
                Button(action: onManageTap) {
                    Text("Manage")
                        .font(.caption).bold()
                        .foregroundColor(.neonCyan)
                        .padding(.horizontal, 12)
                        .padding(.vertical, 6)
                        .background(Color.neonCyan.opacity(0.15))
                        .clipShape(Capsule())
                }
            }
            
            // Custom Multi-Segment Progress Bar
            GeometryReader { geo in
                let totalWidth = geo.size.width
                let usedRatio = stats.totalBytes > 0 ? Double(stats.usedBytes) / Double(stats.totalBytes) : 0.0
                
                ZStack(alignment: .leading) {
                    RoundedRectangle(cornerRadius: 6)
                        .fill(Color.white.opacity(0.1))
                        .frame(height: 10)
                    
                    RoundedRectangle(cornerRadius: 6)
                        .fill(
                            LinearGradient(
                                gradient: Gradient(colors: [.neonPurple, .neonPink, .neonCyan]),
                                startPoint: .leading,
                                endPoint: .trailing
                            )
                        )
                        .frame(width: max(0, totalWidth * CGFloat(usedRatio)), height: 10)
                }
            }
            .frame(height: 10)
            
            // Legend
            HStack(spacing: 16) {
                LegendItem(color: .neonPurple, title: "Media", size: ByteCountFormatter.string(fromByteCount: stats.imagesBytes + stats.videosBytes + stats.audioBytes, countStyle: .file))
                LegendItem(color: .neonCyan, title: "Docs", size: ByteCountFormatter.string(fromByteCount: stats.docsBytes, countStyle: .file))
                LegendItem(color: .neonGreen, title: "Free", size: stats.formattedFree)
            }
        }
        .padding(18)
        .glassCard(cornerRadius: 20, strokeColor: Color.white.opacity(0.15), backgroundColor: Color.darkSurfaceVariant)
    }
}

private struct LegendItem: View {
    var color: Color
    var title: String
    var size: String
    
    var body: some View {
        HStack(spacing: 6) {
            Circle()
                .fill(color)
                .frame(width: 8, height: 8)
            VStack(alignment: .leading, spacing: 2) {
                Text(title)
                    .font(.caption2)
                    .foregroundColor(.gray)
                Text(size)
                    .font(.caption).bold()
                    .foregroundColor(.white)
            }
        }
    }
}
