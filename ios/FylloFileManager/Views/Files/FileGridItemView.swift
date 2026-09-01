import SwiftUI

public struct FileGridItemView: View {
    public var file: FileItem
    public var isSelected: Bool
    public var isSelectionMode: Bool
    public var onTap: () -> Void
    public var onSelectToggle: () -> Void
    
    public var body: some View {
        Button(action: onTap) {
            VStack(spacing: 8) {
                ZStack(alignment: .topTrailing) {
                    RoundedRectangle(cornerRadius: 14)
                        .fill(file.categoryColor.opacity(0.12))
                        .frame(height: 100)
                    
                    Image(systemName: file.systemIconName)
                        .font(.system(size: 36))
                        .foregroundColor(file.categoryColor)
                        .frame(maxWidth: .infinity, maxHeight: .infinity)
                    
                    if isSelectionMode {
                        Image(systemName: isSelected ? "checkmark.circle.fill" : "circle")
                            .font(.system(size: 18))
                            .foregroundColor(isSelected ? .neonCyan : .gray)
                            .padding(8)
                    }
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
                .frame(maxWidth: .infinity, alignment: .leading)
            }
            .padding(10)
            .glassCard(cornerRadius: 16, strokeColor: isSelected ? Color.neonCyan : Color.white.opacity(0.1), backgroundColor: Color.darkSurface)
        }
        .buttonStyle(PlainButtonStyle())
    }
}
