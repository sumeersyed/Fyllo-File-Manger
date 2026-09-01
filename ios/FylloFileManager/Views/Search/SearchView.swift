import SwiftUI

public struct SearchView: View {
    @StateObject private var viewModel = SearchViewModel()
    @EnvironmentObject private var settingsVM: SettingsViewModel
    public var onDismiss: () -> Void
    public var onOpenFile: (FileItem) -> Void
    
    public var body: some View {
        NavigationView {
            ZStack {
                Color.backgroundColor(
                    for: settingsVM.settings.colorTheme,
                    isDark: settingsVM.settings.themeMode != .light,
                    amoled: settingsVM.settings.amoledBlack
                )
                .ignoresSafeArea()
                
                VStack(spacing: 12) {
                    // Search Text Field
                    HStack {
                        Image(systemName: "magnifyingglass")
                            .foregroundColor(.gray)
                        TextField("Search files, folders, extensions...", text: $viewModel.searchQuery)
                            .foregroundColor(.white)
                            .onChange(of: viewModel.searchQuery) { _ in
                                viewModel.performSearch()
                            }
                        if !viewModel.searchQuery.isEmpty {
                            Button(action: {
                                viewModel.searchQuery = ""
                                viewModel.performSearch()
                            }) {
                                Image(systemName: "xmark.circle.fill")
                                    .foregroundColor(.gray)
                            }
                        }
                    }
                    .padding(12)
                    .glassCard(cornerRadius: 14, strokeColor: Color.white.opacity(0.15), backgroundColor: Color.darkSurfaceVariant)
                    .padding(.horizontal)
                    
                    // Filter Chips
                    ScrollView(.horizontal, showsIndicators: false) {
                        HStack(spacing: 8) {
                            ForEach(FileCategory.allCases) { cat in
                                Button(action: {
                                    viewModel.searchFilter = cat
                                    viewModel.performSearch()
                                }) {
                                    Text(cat.rawValue)
                                        .font(.caption).bold()
                                        .foregroundColor(viewModel.searchFilter == cat ? .black : .gray)
                                        .padding(.horizontal, 12)
                                        .padding(.vertical, 6)
                                        .background(viewModel.searchFilter == cat ? Color.neonCyan : Color.white.opacity(0.08))
                                        .clipShape(Capsule())
                                }
                            }
                        }
                        .padding(.horizontal)
                    }
                    
                    // Results
                    if viewModel.searchResults.isEmpty && !viewModel.searchQuery.isEmpty {
                        Spacer()
                        Text("No matching files found")
                            .font(.headline)
                            .foregroundColor(.gray)
                        Spacer()
                    } else {
                        List {
                            ForEach(viewModel.searchResults) { item in
                                Button(action: {
                                    onOpenFile(item)
                                    onDismiss()
                                }) {
                                    HStack(spacing: 12) {
                                        Image(systemName: item.systemIconName)
                                            .foregroundColor(item.categoryColor)
                                        VStack(alignment: .leading, spacing: 2) {
                                            Text(item.name)
                                                .font(.subheadline).bold()
                                                .foregroundColor(.white)
                                            Text(item.path)
                                                .font(.caption2)
                                                .foregroundColor(.gray)
                                                .lineLimit(1)
                                        }
                                    }
                                }
                                .listRowBackground(Color.darkSurfaceVariant)
                            }
                        }
                        .scrollContentBackground(.hidden)
                    }
                }
            }
            .navigationTitle("Search")
            .navigationBarTitleDisplayMode(.inline)
            .toolbar {
                ToolbarItem(placement: .navigationBarLeading) {
                    Button("Cancel", action: onDismiss)
                        .foregroundColor(.gray)
                }
            }
        }
    }
}
