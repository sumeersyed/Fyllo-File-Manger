import SwiftUI

public extension Color {
    init(hex: String) {
        let hex = hex.trimmingCharacters(in: CharacterSet.alphanumerics.inverted)
        var int: UInt64 = 0
        Scanner(string: hex).scanHexInt64(&int)
        let a, r, g, b: UInt64
        switch hex.count {
        case 3: // RGB (12-bit)
            (a, r, g, b) = (255, (int >> 8) * 17, (int >> 4 & 0xF) * 17, (int & 0xF) * 17)
        case 6: // RGB (24-bit)
            (a, r, g, b) = (255, int >> 16, int >> 8 & 0xFF, int & 0xFF)
        case 8: // ARGB (32-bit)
            (a, r, g, b) = (int >> 24, int >> 16 & 0xFF, int >> 8 & 0xFF, int & 0xFF)
        default:
            (a, r, g, b) = (255, 0, 0, 0)
        }
        self.init(
            .sRGB,
            red: Double(r) / 255,
            green: Double(g) / 255,
            blue: Double(b) / 255,
            opacity: Double(a) / 255
        )
    }
    
    // Core Neon Brand Colors
    static let neonGreen = Color(hex: "38F570")
    static let neonCyan = Color(hex: "00E5FF")
    static let neonPurple = Color(hex: "D500F9")
    static let neonPink = Color(hex: "FF4081")
    static let neonYellow = Color(hex: "FFD600")
    static let neonBlue = Color(hex: "0091EA")
    
    // Backgrounds & Dark Surfaces
    static let darkBackground = Color(hex: "0A0C0E")
    static let amoledBackground = Color(hex: "000000")
    static let darkSurface = Color(hex: "13161A")
    static let darkSurfaceVariant = Color(hex: "1D2127")
    static let darkBorder = Color(hex: "2A2F37")
    
    // Theme Palette Provider
    static func primaryColor(for theme: ColorTheme) -> Color {
        switch theme {
        case .`default`: return .neonPurple
        case .purple: return Color(hex: "9C27B0")
        case .pink: return Color(hex: "E91E63")
        case .oceanBlue: return Color(hex: "0288D1")
        case .forestGreen: return Color(hex: "2E7D32")
        case .eInk: return Color(hex: "E7EBE0")
        }
    }
    
    static func secondaryColor(for theme: ColorTheme) -> Color {
        switch theme {
        case .`default`: return .neonCyan
        case .purple: return Color(hex: "E1BEE7")
        case .pink: return Color(hex: "F8BBD0")
        case .oceanBlue: return Color(hex: "80DEEA")
        case .forestGreen: return Color(hex: "A5D6A7")
        case .eInk: return Color(hex: "6B6B6B")
        }
    }
    
    static func tertiaryColor(for theme: ColorTheme) -> Color {
        switch theme {
        case .`default`: return .neonGreen
        case .purple: return Color(hex: "CE93D8")
        case .pink: return Color(hex: "F48FB1")
        case .oceanBlue: return Color(hex: "4DD0E1")
        case .forestGreen: return Color(hex: "81C784")
        case .eInk: return Color(hex: "444444")
        }
    }
    
    static func backgroundColor(for theme: ColorTheme, isDark: Bool, amoled: Bool) -> Color {
        if theme == .eInk {
            return isDark ? Color(hex: "1C1C1E") : Color(hex: "E7EBE0")
        }
        if !isDark {
            return Color(hex: "F8F9FA")
        }
        if amoled {
            return .amoledBackground
        }
        switch theme {
        case .purple: return Color(hex: "12001A")
        case .pink: return Color(hex: "2A0010")
        case .oceanBlue: return Color(hex: "001F2D")
        case .forestGreen: return Color(hex: "0A1F0C")
        case .`default`: return .darkBackground
        case .eInk: return Color(hex: "1C1C1E")
        }
    }
    
    static func surfaceColor(for theme: ColorTheme, isDark: Bool, amoled: Bool) -> Color {
        if theme == .eInk {
            return isDark ? Color(hex: "2C2C2E") : Color(hex: "D7DBD0")
        }
        if !isDark {
            return Color.white
        }
        if amoled {
            return Color(hex: "0D0D0E")
        }
        switch theme {
        case .purple: return Color(hex: "2A003D")
        case .pink: return Color(hex: "3F0018")
        case .oceanBlue: return Color(hex: "00334E")
        case .forestGreen: return Color(hex: "143818")
        case .`default`: return .darkSurface
        case .eInk: return Color(hex: "2C2C2E")
        }
    }
}
