import Foundation
import SwiftUI

public enum ThemeMode: String, CaseIterable, Codable {
    case system = "System Default"
    case dark = "Dark"
    case light = "Light"
}

public enum ColorTheme: String, CaseIterable, Codable {
    case `default` = "Default"
    case purple = "Purple"
    case pink = "Pink"
    case oceanBlue = "Ocean Blue"
    case forestGreen = "Forest Green"
    case eInk = "E-ink"
}

public struct SettingsState: Codable {
    public var themeMode: ThemeMode
    public var colorTheme: ColorTheme
    public var amoledBlack: Bool
    public var eInkMode: Bool
    public var enableHaptics: Bool
    public var enableSoundEffects: Bool
    public var autoCleanJunk: Bool
    public var junkCleanIntervalDays: Int
    public var isOnboardingCompleted: Bool
    public var isAppLockEnabled: Bool
    public var isBiometricEnabled: Bool
    public var showHiddenFiles: Bool
    public var defaultSortOption: String
    public var isGridView: Bool
    public var selectedLanguage: String
    
    public init(
        themeMode: ThemeMode = .system,
        colorTheme: ColorTheme = .default,
        amoledBlack: Bool = true,
        eInkMode: Bool = false,
        enableHaptics: Bool = true,
        enableSoundEffects: Bool = true,
        autoCleanJunk: Bool = false,
        junkCleanIntervalDays: Int = 7,
        isOnboardingCompleted: Bool = false,
        isAppLockEnabled: Bool = false,
        isBiometricEnabled: Bool = true,
        showHiddenFiles: Bool = false,
        defaultSortOption: String = "Date (Newest)",
        isGridView: Bool = false,
        selectedLanguage: String = "System"
    ) {
        self.themeMode = themeMode
        self.colorTheme = colorTheme
        self.amoledBlack = amoledBlack
        self.eInkMode = eInkMode
        self.enableHaptics = enableHaptics
        self.enableSoundEffects = enableSoundEffects
        self.autoCleanJunk = autoCleanJunk
        self.junkCleanIntervalDays = junkCleanIntervalDays
        self.isOnboardingCompleted = isOnboardingCompleted
        self.isAppLockEnabled = isAppLockEnabled
        self.isBiometricEnabled = isBiometricEnabled
        self.showHiddenFiles = showHiddenFiles
        self.defaultSortOption = defaultSortOption
        self.isGridView = isGridView
        self.selectedLanguage = selectedLanguage
    }
}
