import Foundation
import SwiftUI

@MainActor
public class SettingsViewModel: ObservableObject {
    @Published public var settings: SettingsState {
        didSet {
            saveSettings()
        }
    }
    
    private let settingsKey = "fyllo_file_manager_settings"
    
    public init() {
        if let data = UserDefaults.standard.data(forKey: settingsKey),
           let saved = try? JSONDecoder().decode(SettingsState.self, from: data) {
            self.settings = saved
        } else {
            self.settings = SettingsState()
        }
    }
    
    private func saveSettings() {
        if let data = try? JSONEncoder().encode(settings) {
            UserDefaults.standard.set(data, forKey: settingsKey)
        }
    }
    
    public func setThemeMode(_ mode: ThemeMode) {
        settings.themeMode = mode
    }
    
    public func setColorTheme(_ theme: ColorTheme) {
        settings.colorTheme = theme
    }
    
    public func toggleAmoledBlack() {
        settings.amoledBlack.toggle()
    }
    
    public func toggleAppLock() {
        settings.isAppLockEnabled.toggle()
    }
}
