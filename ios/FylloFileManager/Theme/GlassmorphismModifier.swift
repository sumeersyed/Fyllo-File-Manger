import SwiftUI

public struct GlassCardModifier: ViewModifier {
    var cornerRadius: CGFloat = 16
    var strokeColor: Color = Color.white.opacity(0.12)
    var backgroundColor: Color = Color.white.opacity(0.06)
    
    public func body(content: Content) -> some View {
        content
            .background(
                RoundedRectangle(cornerRadius: cornerRadius, style: .continuous)
                    .fill(backgroundColor)
                    .background(
                        RoundedRectangle(cornerRadius: cornerRadius, style: .continuous)
                            .fill(.ultraThinMaterial)
                    )
            )
            .overlay(
                RoundedRectangle(cornerRadius: cornerRadius, style: .continuous)
                    .strokeBorder(strokeColor, lineWidth: 1)
            )
            .clipShape(RoundedRectangle(cornerRadius: cornerRadius, style: .continuous))
    }
}

public struct NeonGlowModifier: ViewModifier {
    var color: Color
    var radius: CGFloat = 8
    
    public func body(content: Content) -> some View {
        content
            .shadow(color: color.opacity(0.4), radius: radius, x: 0, y: 0)
    }
}

public extension View {
    func glassCard(cornerRadius: CGFloat = 16, strokeColor: Color = Color.white.opacity(0.12), backgroundColor: Color = Color.white.opacity(0.06)) -> some View {
        self.modifier(GlassCardModifier(cornerRadius: cornerRadius, strokeColor: strokeColor, backgroundColor: backgroundColor))
    }
    
    func neonGlow(color: Color, radius: CGFloat = 8) -> some View {
        self.modifier(NeonGlowModifier(color: color, radius: radius))
    }
}
