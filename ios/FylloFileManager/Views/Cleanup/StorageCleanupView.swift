import SwiftUI

public struct StorageCleanupView: View {
    @StateObject private var viewModel = StorageCleanupViewModel()
    @EnvironmentObject private var settingsVM: SettingsViewModel
    public var onDismiss: () -> Void
    
    @State private var selectedTab = 0
    
    public var body: some View {
        NavigationView {
            ZStack {
                Color.backgroundColor(
                    for: settingsVM.settings.colorTheme,
                    isDark: settingsVM.settings.themeMode != .light,
                    amoled: settingsVM.settings.amoledBlack
                )
                .ignoresSafeArea()
                
                VStack(spacing: 16) {
                    // Header Hero Card with Clean Button
                    VStack(spacing: 12) {
                        ZStack {
                            Circle()
                                .fill(Color.neonGreen.opacity(0.15))
                                .frame(width: 70, height: 70)
                            Image(systemName: "sparkles")
                                .font(.system(size: 32))
                                .foregroundColor(.neonGreen)
                        }
                        
                        Text(ByteCountFormatter.string(fromByteCount: viewModel.totalJunkBytes, countStyle: .file))
                            .font(.title).bold()
                            .foregroundColor(.white)
                        
                        Text("Junk & Temporary Files Found")
                            .font(.subheadline)
                            .foregroundColor(.gray)
                        
                        if let msg = viewModel.cleanupCompletedMessage {
                            Text(msg)
                                .font(.caption).bold()
                                .foregroundColor(.neonGreen)
                        }
                        
                        Button(action: { viewModel.cleanJunk() }) {
                            HStack(spacing: 8) {
                                Image(systemName: "trash.fill")
                                Text("Clean Junk Now")
                                    .font(.headline)
                            }
                            .foregroundColor(.black)
                            .padding(.horizontal, 28)
                            .padding(.vertical, 12)
                            .background(Color.neonGreen)
                            .clipShape(Capsule())
                            .shadow(color: Color.neonGreen.opacity(0.3), radius: 10)
                        }
                        .disabled(viewModel.totalJunkBytes == 0)
                    }
                    .padding(20)
                    .frame(maxWidth: .infinity)
                    .glassCard(cornerRadius: 24, strokeColor: Color.neonGreen.opacity(0.2), backgroundColor: Color.darkSurfaceVariant)
                    .padding(.horizontal)
                    
                    // Segments (Junk, Large Files, Duplicates)
                    Picker("Category", selection: $selectedTab) {
                        Text("Junk (\(viewModel.junkFiles.count))").tag(0)
                        Text("Large Files (\(viewModel.largeFiles.count))").tag(1)
                        Text("Duplicates (\(viewModel.duplicateGroups.count))").tag(2)
                    }
                    .pickerStyle(SegmentedPickerStyle())
                    .padding(.horizontal)
                    
                    // Content List
                    if selectedTab == 0 {
                        List {
                            ForEach(viewModel.junkFiles) { item in
                                HStack {
                                    Image(systemName: "doc.badge.gearshape")
                                        .foregroundColor(.neonGreen)
                                    VStack(alignment: .leading, spacing: 2) {
                                        Text(item.name)
                                            .font(.subheadline).bold()
                                            .foregroundColor(.white)
                                        Text(item.formattedSize)
                                            .font(.caption2)
                                            .foregroundColor(.gray)
                                    }
                                }
                                .listRowBackground(Color.darkSurfaceVariant)
                            }
                        }
                        .hideScrollContentBackground()
                    } else if selectedTab == 1 {
                        List {
                            ForEach(viewModel.largeFiles) { item in
                                HStack {
                                    Image(systemName: item.systemIconName)
                                        .foregroundColor(item.categoryColor)
                                    VStack(alignment: .leading, spacing: 2) {
                                        Text(item.name)
                                            .font(.subheadline).bold()
                                            .foregroundColor(.white)
                                        Text(item.formattedSize)
                                            .font(.caption2)
                                            .foregroundColor(.neonPink)
                                    }
                                    Spacer()
                                    Button(action: { viewModel.deleteLargeFile(item) }) {
                                        Image(systemName: "trash")
                                            .foregroundColor(.red)
                                    }
                                }
                                .listRowBackground(Color.darkSurfaceVariant)
                            }
                        }
                        .hideScrollContentBackground()
                    } else {
                        List {
                            ForEach(viewModel.duplicateGroups) { group in
                                Section(header: Text("Hash: \(group.hash.prefix(8))... (\(group.files.count) copies)").foregroundColor(.neonCyan)) {
                                    ForEach(group.files) { item in
                                        HStack {
                                            VStack(alignment: .leading, spacing: 2) {
                                                Text(item.name)
                                                    .font(.subheadline).bold()
                                                    .foregroundColor(.white)
                                                Text(item.path)
                                                    .font(.caption2)
                                                    .foregroundColor(.gray)
                                                    .lineLimit(1)
                                            }
                                            Spacer()
                                            Button(action: { viewModel.deleteDuplicate(item) }) {
                                                Image(systemName: "trash")
                                                    .foregroundColor(.red)
                                            }
                                        }
                                        .listRowBackground(Color.darkSurfaceVariant)
                                    }
                                }
                            }
                        }
                        .hideScrollContentBackground()
                    }
                }
            }
            .navigationTitle("Storage Cleanup")
            .navigationBarTitleDisplayMode(.inline)
            .toolbar {
                ToolbarItem(placement: .navigationBarLeading) {
                    Button("Done", action: onDismiss)
                        .foregroundColor(.white)
                }
                
                ToolbarItem(placement: .navigationBarTrailing) {
                    Button(action: { viewModel.startScan() }) {
                        Image(systemName: "arrow.clockwise")
                            .foregroundColor(.neonCyan)
                    }
                }
            }
        }
    }
}
