import SwiftUI

public struct FileRowView: View {
    public var file: FileItem
    public var isSelected: Bool
    public var isSelectionMode: Bool
    public var onTap: () -> Void
    public var onSelectToggle: () -> Void
    public var onRename: () -> Void
    public var onCopy: () -> Void
    public var onCut: () -> Void
    public var onDelete: () -> Void
    public var onShare: () -> Void
    public var onCompress: () -> Void
    
    public var body: some View {
        HStack(spacing: 12) {
            if isSelectionMode {
                Button(action: onSelectToggle) {
                    Image(systemName: isSelected ? "checkmark.circle.fill" : "circle")
                        .font(.system(size: 20))
                        .foregroundColor(isSelected ? .neonCyan : .gray)
                }
            }
            
            Button(action: onTap) {
                HStack(spacing: 12) {
                    ZStack {
                        RoundedRectangle(cornerRadius: 10)
                            .fill(file.categoryColor.opacity(0.15))
                            .frame(width: 44, height: 44)
                        Image(systemName: file.systemIconName)
                            .font(.system(size: 20))
                            .foregroundColor(file.categoryColor)
                    }
                    
                    VStack(alignment: .leading, spacing: 4) {
                        Text(file.name)
                            .font(.subheadline).bold()
                            .foregroundColor(.white)
                            .lineLimit(1)
                        
                        HStack(spacing: 8) {
                            Text(file.formattedSize)
                                .font(.caption2)
                                .foregroundColor(.gray)
                            Text("•")
                                .font(.caption2)
                                .foregroundColor(.gray)
                            Text(file.formattedDate)
                                .font(.caption2)
                                .foregroundColor(.gray)
                        }
                    }
                    
                    Spacer()
                    
                    if file.isDirectory {
                        Image(systemName: "chevron.right")
                            .font(.system(size: 14))
                            .foregroundColor(.gray)
                    }
                }
            }
            .buttonStyle(PlainButtonStyle())
        }
        .padding(.horizontal, 16)
        .padding(.vertical, 10)
        .background(isSelected ? Color.neonCyan.opacity(0.08) : Color.clear)
        .contextMenu {
            Button(action: onRename) {
                Label("Rename", systemImage: "pencil")
            }
            Button(action: onCopy) {
                Label("Copy", systemImage: "doc.on.doc")
            }
            Button(action: onCut) {
                Label("Move / Cut", systemImage: "arrow.right.doc.on.clipboard")
            }
            Button(action: onCompress) {
                Label("Compress (ZIP)", systemImage: "doc.zipper")
            }
            Button(action: onShare) {
                Label("Share", systemImage: "square.and.arrow.up")
            }
            Divider()
            Button(role: .destructive, action: onDelete) {
                Label("Delete", systemImage: "trash")
            }
        }
    }
}
