import SwiftUI
import PDFKit

public struct PdfViewer: View {
    public var item: FileItem
    public var onDismiss: () -> Void
    
    @State private var shareURL: URL? = nil
    
    public var body: some View {
        NavigationView {
            ZStack {
                Color.black.ignoresSafeArea()
                
                PDFKitRepresentedView(url: item.url)
                    .ignoresSafeArea(edges: .bottom)
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
                
                ToolbarItem(placement: .navigationBarTrailing) {
                    Button(action: { shareURL = item.url }) {
                        Image(systemName: "square.and.arrow.up")
                            .foregroundColor(.neonCyan)
                    }
                }
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

private struct PDFKitRepresentedView: UIViewRepresentable {
    let url: URL
    
    func makeUIView(context: Context) -> PDFView {
        let pdfView = PDFView()
        pdfView.autoScales = true
        pdfView.displayMode = .singlePageContinuous
        pdfView.displayDirection = .vertical
        pdfView.document = PDFDocument(url: url)
        return pdfView
    }
    
    func updateUIView(_ uiView: PDFView, context: Context) {}
}

private struct ShareItem: Identifiable {
    let id = UUID()
    let url: URL
}
