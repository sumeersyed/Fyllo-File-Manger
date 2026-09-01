import SwiftUI

public struct SettingsView: View {
    @EnvironmentObject private var viewModel: SettingsViewModel
    public var onDismiss: () -> Void
    
    public var body: some View {
        NavigationView {
            ZStack {
                Color.backgroundColor(
                    for: viewModel.settings.colorTheme,
                    isDark: viewModel.settings.themeMode != .light,
                    amoled: viewModel.settings.amoledBlack
                )
                .ignoresSafeArea()
                
                List {
                    // Appearance Section
                    Section(header: Text("Appearance").foregroundColor(.neonPurple)) {
                        Picker("Theme Mode", selection: $viewModel.settings.themeMode) {
                            ForEach(ThemeMode.allCases, id: \.self) { mode in
                                Text(mode.rawValue).tag(mode)
                            }
                        }
                        
                        Picker("Color Accent", selection: $viewModel.settings.colorTheme) {
                            ForEach(ColorTheme.allCases, id: \.self) { theme in
                                Text(theme.rawValue).tag(theme)
                            }
                        }
                        
                        Toggle("AMOLED Pure Black", isOn: $viewModel.settings.amoledBlack)
                        Toggle("E-Ink Minimal Mode", isOn: $viewModel.settings.eInkMode)
                    }
                    .listRowBackground(Color.darkSurfaceVariant)
                    
                    // Security Section
                    Section(header: Text("Security & Privacy").foregroundColor(.neonCyan)) {
                        Toggle("App Lock (Biometric/PIN)", isOn: $viewModel.settings.isAppLockEnabled)
                        Toggle("Show Hidden Files", isOn: $viewModel.settings.showHiddenFiles)
                    }
                    .listRowBackground(Color.darkSurfaceVariant)
                    
                    // Performance & Cleanup
                    Section(header: Text("Storage & Optimization").foregroundColor(.neonGreen)) {
                        Toggle("Auto Clean Junk", isOn: $viewModel.settings.autoCleanJunk)
                        Stepper("Clean Interval: Every \(viewModel.settings.junkCleanIntervalDays) days", value: $viewModel.settings.junkCleanIntervalDays, in: 1...30)
                    }
                    .listRowBackground(Color.darkSurfaceVariant)
                    
                    // Feedback & Haptics
                    Section(header: Text("Interactions").foregroundColor(.neonYellow)) {
                        Toggle("Haptic Feedback", isOn: $viewModel.settings.enableHaptics)
                        Toggle("Sound Effects", isOn: $viewModel.settings.enableSoundEffects)
                    }
                    .listRowBackground(Color.darkSurfaceVariant)
                    
                    // About Section
                    Section(header: Text("About").foregroundColor(.gray)) {
                        HStack {
                            Text("Version")
                            Spacer()
                            Text("v1.0.0 (iOS Native)")
                                .foregroundColor(.gray)
                        }
                        HStack {
                            Text("Developer")
                            Spacer()
                            Text("Fyllo Team")
                                .foregroundColor(.gray)
                        }
                    }
                    .listRowBackground(Color.darkSurfaceVariant)
                }
                .scrollContentBackground(.hidden)
            }
            .navigationTitle("Settings")
            .navigationBarTitleDisplayMode(.inline)
            .toolbar {
                ToolbarItem(placement: .navigationBarTrailing) {
                    Button("Done", action: onDismiss)
                        .foregroundColor(.neonCyan)
                }
            }
        }
    }
}
