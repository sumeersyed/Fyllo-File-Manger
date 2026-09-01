import SwiftUI
import PDFKit

struct PdfViewer: View {
    let url: URL
    
    var body: some View {
        PDFViewRepresentable(url: url)
            .ignoresSafeArea()
    }
}

struct PDFViewRepresentable: UIViewRepresentable {
    let url: URL
    
    func makeUIView(context: Context) -> PDFView {
        let pdfView = PDFView()
        pdfView.document = PDFDocument(url: url)
        pdfView.autoScales = true
        return pdfView
    }
    
    func updateUIView(_ uiView: PDFView, context: Context) {}
}

#Preview {
    PdfViewer(url: URL(fileURLWithPath: "/path/to/document.pdf"))
}
