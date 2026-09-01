import SwiftUI

public struct RecentFileCard: View {
    public var file: FileItem
    public var onTap: () -> Void
    
    public var body: some View {
        Button(action: onTap) {
            VStack(alignment: .leading, spacing: 8) {
                ZStack {
                    RoundedRectangle(cornerRadius: 12)
                        .fill(file.categoryColor.opacity(0.15))
                        .frame(width: 110, height: 80)
                    
                    Image(systemName: file.systemIconName)
                        .font(.system(size: 28))
                        .foregroundColor(file.categoryColor)
                }
                
                VStack(alignment: .leading, spacing: 2) {
                    Text(file.name)
                        .font(.caption).bold()
                        .foregroundColor(.white)
                        .lineLimit(1)
                    
                    Text(file.formattedSize)
                        .font(.caption2)
                        .foregroundColor(.gray)
                }
                .frame(width: 110, alignment: .leading)
            }
            .padding(10)
            .glassCard(cornerRadius: 16, strokeColor: Color.white.opacity(0.1), backgroundColor: Color.darkSurface)
        }
        .buttonStyle(PlainButtonStyle())
    }
}
