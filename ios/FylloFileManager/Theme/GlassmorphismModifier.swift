import SwiftUI
import UIKit

public struct BlurView: UIViewRepresentable {
    public var style: UIBlurEffect.Style = .systemThinMaterialDark
    
    public func makeUIView(context: Context) -> UIVisualEffectView {
        UIVisualEffectView(effect: UIBlurEffect(style: style))
    }
    
    public func updateUIView(_ uiView: UIVisualEffectView, context: Context) {
        uiView.effect = UIBlurEffect(style: style)
    }
}

public struct GlassCardModifier: ViewModifier {
    var cornerRadius: CGFloat = 16
    var strokeColor: Color = Color.white.opacity(0.12)
    var backgroundColor: Color = Color.white.opacity(0.06)
    
    public func body(content: Content) -> some View {
        content
            .background(
                ZStack {
                    BlurView(style: .systemThinMaterialDark)
                    backgroundColor
                }
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
    
    @ViewBuilder
    func hideScrollContentBackground() -> some View {
        if #available(iOS 16.0, *) {
            self.scrollContentBackground(.hidden)
        } else {
            self.onAppear {
                UITableView.appearance().backgroundColor = .clear
            }
        }
    }
}
