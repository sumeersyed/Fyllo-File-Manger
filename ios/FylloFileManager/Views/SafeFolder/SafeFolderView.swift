import SwiftUI

public struct SafeFolderView: View {
    @StateObject private var viewModel = SafeFolderViewModel()
    @EnvironmentObject private var settingsVM: SettingsViewModel
    public var onDismiss: () -> Void
    
    @State private var showingDocPicker = false
    
    public var body: some View {
        NavigationView {
            ZStack {
                Color.backgroundColor(
                    for: settingsVM.settings.colorTheme,
                    isDark: settingsVM.settings.themeMode != .light,
                    amoled: settingsVM.settings.amoledBlack
                )
                .ignoresSafeArea()
                
                if !viewModel.isUnlocked {
                    // Lock Screen Prompt
                    VStack(spacing: 20) {
                        Spacer()
                        
                        ZStack {
                            Circle()
                                .fill(Color.neonPurple.opacity(0.15))
                                .frame(width: 100, height: 100)
                            Image(systemName: "lock.shield.fill")
                                .font(.system(size: 48))
                                .foregroundColor(.neonPurple)
                        }
                        
                        VStack(spacing: 6) {
                            Text("Safe Vault Locked")
                                .font(.title2).bold()
                                .foregroundColor(.white)
                            Text("AES-256 Encrypted Private Storage")
                                .font(.subheadline)
                                .foregroundColor(.gray)
                        }
                        
                        if let error = viewModel.errorMessage {
                            Text(error)
                                .font(.caption)
                                .foregroundColor(.red)
                                .padding(.horizontal)
                        }
                        
                        Button(action: { viewModel.authenticateAndUnlock() }) {
                            HStack(spacing: 8) {
                                Image(systemName: "faceid")
                                    .font(.headline)
                                Text("Unlock with Biometrics")
                                    .font(.headline)
                            }
                            .foregroundColor(.white)
                            .padding(.horizontal, 24)
                            .padding(.vertical, 14)
                            .background(Color.neonPurple)
                            .clipShape(Capsule())
                            .shadow(color: Color.neonPurple.opacity(0.4), radius: 12)
                        }
                        
                        Spacer()
                    }
                } else {
                    // Unlocked Vault Files List
                    VStack {
                        if viewModel.vaultRecords.isEmpty {
                            VStack(spacing: 16) {
                                Spacer()
                                Image(systemName: "shield.slash")
                                    .font(.system(size: 48))
                                    .foregroundColor(.gray)
                                Text("No files in Safe Vault")
                                    .font(.headline)
                                    .foregroundColor(.gray)
                                Button(action: { showingDocPicker = true }) {
                                    Text("Add Files to Vault")
                                        .font(.subheadline).bold()
                                        .foregroundColor(.white)
                                        .padding(.horizontal, 20)
                                        .padding(.vertical, 10)
                                        .background(Color.neonPurple)
                                        .clipShape(Capsule())
                                }
                                Spacer()
                            }
                        } else {
                            List {
                                ForEach(viewModel.vaultRecords) { record in
                                    HStack(spacing: 12) {
                                        Image(systemName: "lock.fill")
                                            .foregroundColor(.neonPurple)
                                        
                                        VStack(alignment: .leading, spacing: 2) {
                                            Text(record.originalName)
                                                .font(.subheadline).bold()
                                                .foregroundColor(.white)
                                            Text(ByteCountFormatter.string(fromByteCount: record.sizeBytes, countStyle: .file))
                                                .font(.caption2)
                                                .foregroundColor(.gray)
                                        }
                                        
                                        Spacer()
                                        
                                        Button(action: { viewModel.restoreFromVault(record: record) }) {
                                            Image(systemName: "lock.open.fill")
                                                .foregroundColor(.neonCyan)
                                        }
                                    }
                                    .listRowBackground(Color.darkSurfaceVariant)
                                }
                            }
                            .scrollContentBackground(.hidden)
                        }
                    }
                }
            }
            .navigationTitle("Safe Vault")
            .navigationBarTitleDisplayMode(.inline)
            .toolbar {
                ToolbarItem(placement: .navigationBarLeading) {
                    Button("Done", action: onDismiss)
                        .foregroundColor(.white)
                }
                
                if viewModel.isUnlocked {
                    ToolbarItemGroup(placement: .navigationBarTrailing) {
                        Button(action: { showingDocPicker = true }) {
                            Image(systemName: "plus")
                                .foregroundColor(.neonPurple)
                        }
                        
                        Button(action: { viewModel.lockVault() }) {
                            Image(systemName: "lock.fill")
                                .foregroundColor(.gray)
                        }
                    }
                }
            }
        }
        .onAppear {
            if !viewModel.isUnlocked {
                viewModel.authenticateAndUnlock()
            }
        }
        .sheet(isPresented: $showingDocPicker) {
            DocumentPickerView { urls in
                for url in urls {
                    let tempItem = FileItem(name: url.lastPathComponent, url: url, sizeBytes: 0, fileExtension: url.pathExtension)
                    viewModel.addToVault(file: tempItem)
                }
            }
        }
    }
}
