import SwiftUI

public struct BreadcrumbView: View {
    public var currentDirectory: URL
    public var rootDirectory: URL
    public var onSelectURL: (URL) -> Void
    
    public var body: some View {
        ScrollView(.horizontal, showsIndicators: false) {
            HStack(spacing: 6) {
                Button(action: { onSelectURL(rootDirectory) }) {
                    HStack(spacing: 4) {
                        Image(systemName: "house.fill")
                            .font(.system(size: 12))
                        Text("Root")
                            .font(.caption).bold()
                    }
                    .foregroundColor(isRoot ? .neonCyan : .gray)
                    .padding(.horizontal, 10)
                    .padding(.vertical, 6)
                    .background(isRoot ? Color.neonCyan.opacity(0.15) : Color.white.opacity(0.06))
                    .clipShape(Capsule())
                }
                
                let trail = generatePathTrail()
                ForEach(trail, id: \.self) { pathURL in
                    Image(systemName: "chevron.right")
                        .font(.system(size: 10))
                        .foregroundColor(.gray)
                    
                    let isCurrent = pathURL == currentDirectory
                    Button(action: { onSelectURL(pathURL) }) {
                        Text(pathURL.lastPathComponent)
                            .font(.caption).bold()
                            .foregroundColor(isCurrent ? .neonCyan : .gray)
                            .padding(.horizontal, 10)
                            .padding(.vertical, 6)
                            .background(isCurrent ? Color.neonCyan.opacity(0.15) : Color.white.opacity(0.06))
                            .clipShape(Capsule())
                    }
                }
            }
            .padding(.horizontal)
            .padding(.vertical, 4)
        }
    }
    
    private var isRoot: Bool {
        currentDirectory == rootDirectory
    }
    
    private func generatePathTrail() -> [URL] {
        var trail: [URL] = []
        var cursor = currentDirectory
        while cursor != rootDirectory && cursor.path != "/" && cursor.pathComponents.count > rootDirectory.pathComponents.count {
            trail.insert(cursor, at: 0)
            cursor = cursor.deletingLastPathComponent()
        }
        return trail
    }
}
