import SwiftUI

public struct CategoryCard: View {
    public var category: FileCategory
    public var count: Int
    public var onTap: () -> Void
    
    public var body: some View {
        Button(action: onTap) {
            VStack(alignment: .leading, spacing: 12) {
                HStack {
                    ZStack {
                        Circle()
                            .fill(categoryColor.opacity(0.18))
                            .frame(width: 44, height: 44)
                        Image(systemName: category.iconName)
                            .font(.system(size: 20, weight: .semibold))
                            .foregroundColor(categoryColor)
                    }
                    Spacer()
                    Text("\(count)")
                        .font(.caption).bold()
                        .foregroundColor(.gray)
                        .padding(.horizontal, 8)
                        .padding(.vertical, 4)
                        .background(Color.white.opacity(0.08))
                        .clipShape(Capsule())
                }
                
                VStack(alignment: .leading, spacing: 2) {
                    Text(category.rawValue)
                        .font(.subheadline).bold()
                        .foregroundColor(.white)
                    Text(categorySubtitle)
                        .font(.caption2)
                        .foregroundColor(.gray)
                }
            }
            .padding(14)
            .frame(maxWidth: .infinity, alignment: .leading)
            .glassCard(cornerRadius: 18, strokeColor: categoryColor.opacity(0.2), backgroundColor: Color.darkSurface)
        }
        .buttonStyle(PlainButtonStyle())
    }
    
    private var categoryColor: Color {
        switch category {
        case .image: return .neonPink
        case .video: return .neonPurple
        case .audio: return .neonGreen
        case .document, .pdf: return .neonCyan
        case .archive: return .neonYellow
        case .folder: return .neonBlue
        default: return .gray
        }
    }
    
    private var categorySubtitle: String {
        switch category {
        case .image: return "Photos, Wallpapers"
        case .video: return "Movies, Clips"
        case .audio: return "Tracks, Voice"
        case .document: return "Docs, Sheets, Notes"
        case .pdf: return "PDF Reader"
        case .archive: return "Zip, Rar files"
        case .folder: return "Directories"
        default: return "Files"
        }
    }
}
