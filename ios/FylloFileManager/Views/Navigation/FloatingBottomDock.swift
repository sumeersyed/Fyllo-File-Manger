import SwiftUI

public enum MainTab: String, CaseIterable {
    case home = "Home"
    case files = "Files"
    case media = "Media"
    case cleanup = "Clean"
    case settings = "Settings"
    
    public var iconName: String {
        switch self {
        case .home: return "house.fill"
        case .files: return "folder.fill"
        case .media: return "photo.fill"
        case .cleanup: return "sparkles"
        case .settings: return "gearshape.fill"
        }
    }
}

public struct FloatingBottomDock: View {
    @Binding public var selectedTab: MainTab
    
    public var body: some View {
        HStack(spacing: 0) {
            ForEach(MainTab.allCases, id: \.self) { tab in
                let isSelected = selectedTab == tab
                Button(action: {
                    withAnimation(.spring(response: 0.35, dampingFraction: 0.7)) {
                        selectedTab = tab
                    }
                }) {
                    VStack(spacing: 4) {
                        Image(systemName: tab.iconName)
                            .font(.system(size: isSelected ? 20 : 18, weight: isSelected ? .bold : .regular))
                            .foregroundColor(isSelected ? .neonCyan : .gray)
                        
                        Text(tab.rawValue)
                            .font(.system(size: 10, weight: isSelected ? .bold : .medium))
                            .foregroundColor(isSelected ? .neonCyan : .gray)
                    }
                    .frame(maxWidth: .infinity)
                    .padding(.vertical, 8)
                }
            }
        }
        .padding(.horizontal, 8)
        .padding(.vertical, 6)
        .glassCard(cornerRadius: 32, strokeColor: Color.white.opacity(0.15), backgroundColor: Color.darkSurfaceVariant)
        .shadow(color: Color.black.opacity(0.4), radius: 16, y: 8)
        .padding(.horizontal, 24)
    }
}
