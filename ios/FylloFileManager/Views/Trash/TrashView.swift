import SwiftUI

public struct TrashView: View {
    @StateObject private var viewModel = TrashViewModel()
    @EnvironmentObject private var settingsVM: SettingsViewModel
    public var onDismiss: () -> Void
    
    public var body: some View {
        NavigationView {
            ZStack {
                Color.backgroundColor(
                    for: settingsVM.settings.colorTheme,
                    isDark: settingsVM.settings.themeMode != .light,
                    amoled: settingsVM.settings.amoledBlack
                )
                .ignoresSafeArea()
                
                if viewModel.trashItems.isEmpty {
                    VStack(spacing: 16) {
                        Spacer()
                        Image(systemName: "trash.slash")
                            .font(.system(size: 48))
                            .foregroundColor(.gray)
                        Text("Trash is Empty")
                            .font(.headline)
                            .foregroundColor(.gray)
                        Spacer()
                    }
                } else {
                    List {
                        Section(header: Text("Items in Trash (\(viewModel.trashItems.count))").foregroundColor(.neonPink)) {
                            ForEach(viewModel.trashItems) { item in
                                HStack {
                                    VStack(alignment: .leading, spacing: 2) {
                                        Text(item.name)
                                            .font(.subheadline).bold()
                                            .foregroundColor(.white)
                                        HStack(spacing: 6) {
                                            Text(item.formattedSize)
                                                .font(.caption2)
                                                .foregroundColor(.gray)
                                            Text("•")
                                                .font(.caption2)
                                                .foregroundColor(.gray)
                                            Text("Deleted \(item.formattedDeletedDate)")
                                                .font(.caption2)
                                                .foregroundColor(.gray)
                                        }
                                    }
                                    Spacer()
                                    Button(action: { viewModel.restoreItem(item) }) {
                                        Image(systemName: "arrow.uturn.backward.circle.fill")
                                            .font(.system(size: 20))
                                            .foregroundColor(.neonCyan)
                                    }
                                }
                                .listRowBackground(Color.darkSurfaceVariant)
                            }
                        }
                    }
                    .hideScrollContentBackground()
                }
            }
            .navigationTitle("Trash")
            .navigationBarTitleDisplayMode(.inline)
            .toolbar {
                ToolbarItem(placement: .navigationBarLeading) {
                    Button("Done", action: onDismiss)
                        .foregroundColor(.white)
                }
                
                if !viewModel.trashItems.isEmpty {
                    ToolbarItem(placement: .navigationBarTrailing) {
                        Button("Empty", role: .destructive) {
                            viewModel.emptyTrash()
                        }
                        .foregroundColor(.red)
                    }
                }
            }
        }
    }
}
