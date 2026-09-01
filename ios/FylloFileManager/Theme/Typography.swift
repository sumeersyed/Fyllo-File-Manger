import SwiftUI

public struct AppTypography {
    public static func titleFont(isEInk: Bool = false) -> Font {
        isEInk ? .system(.title2, design: .monospaced).bold() : .system(.title2, design: .rounded).bold()
    }
    
    public static func largeTitleFont(isEInk: Bool = false) -> Font {
        isEInk ? .system(.largeTitle, design: .monospaced).bold() : .system(.largeTitle, design: .rounded).bold()
    }
    
    public static func headlineFont(isEInk: Bool = false) -> Font {
        isEInk ? .system(.headline, design: .monospaced).weight(.semibold) : .system(.headline, design: .rounded).weight(.semibold)
    }
    
    public static func subheadlineFont(isEInk: Bool = false) -> Font {
        isEInk ? .system(.subheadline, design: .monospaced) : .system(.subheadline, design: .`default`)
    }
    
    public static func bodyFont(isEInk: Bool = false) -> Font {
        isEInk ? .system(.body, design: .monospaced) : .system(.body, design: .`default`)
    }
    
    public static func captionFont(isEInk: Bool = false) -> Font {
        isEInk ? .system(.caption, design: .monospaced) : .system(.caption, design: .`default`)
    }
}
