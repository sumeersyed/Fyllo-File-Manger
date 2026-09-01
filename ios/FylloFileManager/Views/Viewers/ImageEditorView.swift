import SwiftUI
import CoreImage
import CoreImage.CIFilterBuiltins

public enum FilterEffect: String, CaseIterable {
    case original = "Original"
    case mono = "Mono"
    case sepia = "Sepia"
    case noir = "Noir"
    case chrome = "Chrome"
    case instant = "Instant"
    case fade = "Fade"
}

public struct ImageEditorView: View {
    public var item: FileItem
    public var onDismiss: () -> Void
    public var onSaved: () -> Void
    
    @State private var originalImage: UIImage? = nil
    @State private var editedImage: UIImage? = nil
    @State private var selectedFilter: FilterEffect = .original
    @State private var rotationAngle: Double = 0
    @State private var isSaving = false
    
    private let context = CIContext()
    
    public var body: some View {
        NavigationView {
            ZStack {
                Color.black.ignoresSafeArea()
                
                VStack {
                    Spacer()
                    
                    if let img = editedImage {
                        Image(uiImage: img)
                            .resizable()
                            .scaledToFit()
                            .rotationEffect(.degrees(rotationAngle))
                            .padding()
                    } else {
                        ProgressView().accentColor(.white)
                    }
                    
                    Spacer()
                    
                    // Filter Carousel
                    ScrollView(.horizontal, showsIndicators: false) {
                        HStack(spacing: 12) {
                            ForEach(FilterEffect.allCases, id: \.self) { filter in
                                Button(action: {
                                    selectedFilter = filter
                                    applyCurrentFilter()
                                }) {
                                    VStack(spacing: 4) {
                                        Text(filter.rawValue)
                                            .font(.caption).bold()
                                            .foregroundColor(selectedFilter == filter ? .neonCyan : .gray)
                                            .padding(.horizontal, 12)
                                            .padding(.vertical, 6)
                                            .background(selectedFilter == filter ? Color.neonCyan.opacity(0.15) : Color.white.opacity(0.08))
                                            .clipShape(Capsule())
                                    }
                                }
                            }
                        }
                        .padding(.horizontal)
                    }
                    
                    // Tools bar
                    HStack(spacing: 24) {
                        Button(action: {
                            rotationAngle = (rotationAngle + 90).truncatingRemainder(dividingBy: 360)
                        }) {
                            VStack(spacing: 4) {
                                Image(systemName: "rotate.right")
                                    .font(.system(size: 20))
                                Text("Rotate")
                                    .font(.caption2)
                            }
                            .foregroundColor(.white)
                        }
                        
                        Button(action: {
                            selectedFilter = .original
                            rotationAngle = 0
                            editedImage = originalImage
                        }) {
                            VStack(spacing: 4) {
                                Image(systemName: "arrow.counterclockwise")
                                    .font(.system(size: 20))
                                Text("Reset")
                                    .font(.caption2)
                            }
                            .foregroundColor(.white)
                        }
                    }
                    .padding(.vertical, 16)
                }
            }
            .navigationTitle("Edit Image")
            .navigationBarTitleDisplayMode(.inline)
            .toolbar {
                ToolbarItem(placement: .navigationBarLeading) {
                    Button("Cancel", action: onDismiss)
                        .foregroundColor(.gray)
                }
                
                ToolbarItem(placement: .navigationBarTrailing) {
                    Button("Save") {
                        saveImage()
                    }
                    .font(.headline)
                    .foregroundColor(.neonCyan)
                }
            }
        }
        .onAppear {
            if let data = try? Data(contentsOf: item.url), let ui = UIImage(data: data) {
                originalImage = ui
                editedImage = ui
            }
        }
    }
    
    private func applyCurrentFilter() {
        guard let original = originalImage else { return }
        guard selectedFilter != .original else {
            editedImage = original
            return
        }
        
        guard let ciInput = CIImage(image: original) else { return }
        var outputCI: CIImage?
        
        switch selectedFilter {
        case .mono:
            outputCI = ciInput.applyingFilter("CIPhotoEffectMono")
        case .sepia:
            outputCI = ciInput.applyingFilter("CISepiaTone", parameters: [kCIInputIntensityKey: 0.8])
        case .noir:
            outputCI = ciInput.applyingFilter("CIPhotoEffectNoir")
        case .chrome:
            outputCI = ciInput.applyingFilter("CIPhotoEffectChrome")
        case .instant:
            outputCI = ciInput.applyingFilter("CIPhotoEffectInstant")
        case .fade:
            outputCI = ciInput.applyingFilter("CIPhotoEffectFade")
        case .original:
            break
        }
        
        if let out = outputCI, let cg = context.createCGImage(out, from: out.extent) {
            editedImage = UIImage(cgImage: cg)
        }
    }
    
    private func saveImage() {
        guard let finalImg = editedImage else { return }
        let newURL = item.url.deletingPathExtension().appendingPathExtension("edited.jpg")
        if let data = finalImg.jpegData(compressionQuality: 0.9) {
            try? data.write(to: newURL)
            onSaved()
            onDismiss()
        }
    }
}
