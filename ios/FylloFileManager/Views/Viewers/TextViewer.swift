import SwiftUI

public struct TextViewer: View {
    public var item: FileItem
    public var onDismiss: () -> Void
    
    @State private var fileContent: String = ""
    @State private var isCopied = false
    @State private var shareURL: URL? = nil
    
    public var body: some View {
        NavigationView {
            ZStack {
                Color.darkSurface.ignoresSafeArea()
                
                ScrollView {
                    Text(fileContent)
                        .font(.system(.subheadline, design: .monospaced))
                        .foregroundColor(.white)
                        .padding()
                        .frame(maxWidth: .infinity, alignment: .leading)
                }
            }
            .navigationTitle(item.name)
            .navigationBarTitleDisplayMode(.inline)
            .toolbar {
                ToolbarItem(placement: .navigationBarLeading) {
                    Button(action: onDismiss) {
                        Image(systemName: "xmark")
                            .foregroundColor(.white)
                    }
                }
                
                ToolbarItemGroup(placement: .navigationBarTrailing) {
                    Button(action: {
                        UIPasteboard.general.string = fileContent
                        isCopied = true
                        DispatchQueue.main.asyncAfter(deadline: .now() + 2) {
                            isCopied = false
                        }
                    }) {
                        Image(systemName: isCopied ? "checkmark" : "doc.on.doc")
                            .foregroundColor(isCopied ? .neonGreen : .white)
                    }
                    
                    Button(action: { shareURL = item.url }) {
                        Image(systemName: "square.and.arrow.up")
                            .foregroundColor(.neonCyan)
                    }
                }
            }
        }
        .onAppear {
            if let content = try? String(contentsOf: item.url, encoding: .utf8) {
                fileContent = content
            } else if let content = try? String(contentsOf: item.url, encoding: .ascii) {
                fileContent = content
            } else {
                fileContent = "Unable to preview binary or unsupported encoded text file."
            }
        }
        .sheet(item: Binding(
            get: { shareURL != nil ? ShareItem(url: shareURL!) : nil },
            set: { _ in shareURL = nil }
        )) { share in
            ShareSheet(items: [share.url])
        }
    }
}

private struct ShareItem: Identifiable {
    let id = UUID()
    let url: URL
}
