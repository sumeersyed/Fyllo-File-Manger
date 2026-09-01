import SwiftUI

public struct StorageRingData: Identifiable {
    public let id = UUID()
    public let name: String
    public let ratio: Double
    public let color: Color
}

public struct ActivityRingsView: View {
    public var rings: [StorageRingData]
    public var thickness: CGFloat = 12
    public var spacing: CGFloat = 6
    
    public init(rings: [StorageRingData], thickness: CGFloat = 12, spacing: CGFloat = 6) {
        self.rings = rings
        self.thickness = thickness
        self.spacing = spacing
    }
    
    public var body: some View {
        ZStack {
            ForEach(0..<rings.count, id: \.self) { index in
                let ring = rings[index]
                let currentInset = CGFloat(index) * (thickness + spacing)
                
                // Track background
                Circle()
                    .stroke(ring.color.opacity(0.2), style: StrokeStyle(lineWidth: thickness, lineCap: .round))
                    .padding(currentInset)
                
                // Progress
                Circle()
                    .trim(from: 0.0, to: CGFloat(min(max(ring.ratio, 0.0), 1.0)))
                    .stroke(
                        ring.color,
                        style: StrokeStyle(lineWidth: thickness, lineCap: .round)
                    )
                    .rotationEffect(.degrees(-90))
                    .padding(currentInset)
                    .animation(.spring(response: 0.8, dampingFraction: 0.7), value: ring.ratio)
            }
        }
    }
}
