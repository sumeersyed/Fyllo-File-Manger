import SwiftUI

public struct FilesView: View {
    @StateObject private var viewModel: FilesViewModel
    @EnvironmentObject private var settingsVM: SettingsViewModel
    
    public var onOpenFile: (FileItem) -> Void
    
    @State private var showingNewFolderAlert = false
    @State private var newFolderName = ""
    @State private var showingRenameAlert = false
    @State private var itemToRename: FileItem? = nil
    @State private var renameText = ""
    @State private var shareURL: URL? = nil
    @State private var showingDocPicker = false
    
    public init(initialDirectory: URL? = nil, onOpenFile: @escaping (FileItem) -> Void) {
        _viewModel = StateObject(wrappedValue: FilesViewModel(initialDirectory: initialDirectory))
        self.onOpenFile = onOpenFile
    }
    
    public var body: some View {
        ZStack {
            Color.backgroundColor(
                for: settingsVM.settings.colorTheme,
                isDark: settingsVM.settings.themeMode != .light,
                amoled: settingsVM.settings.amoledBlack
            )
            .ignoresSafeArea()
            
            VStack(spacing: 0) {
                // Header & Action Bar
                HStack {
                    if !viewModel.pathHistory.isEmpty {
                        Button(action: { viewModel.navigateUp() }) {
                            Image(systemName: "chevron.left")
                                .font(.headline)
                                .foregroundColor(.white)
                                .padding(8)
                        }
                    }
                    
                    Text("Files")
                        .font(.title2).bold()
                        .foregroundColor(.white)
                    
                    Spacer()
                    
                    // Grid / List Toggle
                    Button(action: { viewModel.isGridView.toggle() }) {
                        Image(systemName: viewModel.isGridView ? "list.bullet" : "square.grid.2x2")
                            .font(.system(size: 16))
                            .foregroundColor(.white)
                            .padding(8)
                            .background(Color.white.opacity(0.1))
                            .clipShape(Circle())
                    }
                    
                    // Sort Menu
                    Menu {
                        ForEach(SortOption.allCases, id: \.self) { option in
                            Button(action: { viewModel.setSortOption(option) }) {
                                HStack {
                                    Text(option.rawValue)
                                    if viewModel.sortOption == option {
                                        Image(systemName: "checkmark")
                                    }
                                }
                            }
                        }
                    } label: {
                        Image(systemName: "arrow.up.arrow.down")
                            .font(.system(size: 16))
                            .foregroundColor(.white)
                            .padding(8)
                            .background(Color.white.opacity(0.1))
                            .clipShape(Circle())
                    }
                    
                    // Create Folder Button
                    Button(action: {
                        newFolderName = ""
                        showingNewFolderAlert = true
                    }) {
                        Image(systemName: "folder.badge.plus")
                            .font(.system(size: 16))
                            .foregroundColor(.neonCyan)
                            .padding(8)
                            .background(Color.neonCyan.opacity(0.15))
                            .clipShape(Circle())
                    }
                }
                .padding(.horizontal)
                .padding(.vertical, 8)
                
                // Breadcrumbs
                BreadcrumbView(
                    currentDirectory: viewModel.currentDirectory,
                    rootDirectory: FileManagerService.shared.documentsDirectory,
                    onSelectURL: { viewModel.navigateTo(directory: $0) }
                )
                
                // File List / Grid
                if viewModel.files.isEmpty {
                    VStack(spacing: 16) {
                        Spacer()
                        Image(systemName: "folder.badge.questionmark")
                            .font(.system(size: 48))
                            .foregroundColor(.gray)
                        Text("No files in this folder")
                            .font(.headline)
                            .foregroundColor(.gray)
                        Button(action: { showingDocPicker = true }) {
                            Text("Import Files")
                                .font(.subheadline).bold()
                                .foregroundColor(.white)
                                .padding(.horizontal, 20)
                                .padding(.vertical, 10)
                                .background(Color.neonPurple)
                                .clipShape(Capsule())
                        }
                        Spacer()
                    }
                } else if viewModel.isGridView {
                    ScrollView(showsIndicators: false) {
                        LazyVGrid(columns: [GridItem(.flexible()), GridItem(.flexible())], spacing: 12) {
                            ForEach(viewModel.files) { file in
                                FileGridItemView(
                                    file: file,
                                    isSelected: viewModel.selectedItems.contains(file),
                                    isSelectionMode: viewModel.isSelectionMode,
                                    onTap: {
                                        if viewModel.isSelectionMode {
                                            viewModel.toggleSelection(for: file)
                                        } else if file.isDirectory {
                                            viewModel.navigateTo(directory: file.url)
                                        } else {
                                            onOpenFile(file)
                                        }
                                    },
                                    onSelectToggle: {
                                        viewModel.toggleSelection(for: file)
                                    }
                                )
                            }
                        }
                        .padding(.horizontal)
                        .padding(.top, 8)
                        .padding(.bottom, 120)
                    }
                } else {
                    ScrollView(showsIndicators: false) {
                        LazyVStack(spacing: 4) {
                            ForEach(viewModel.files) { file in
                                FileRowView(
                                    file: file,
                                    isSelected: viewModel.selectedItems.contains(file),
                                    isSelectionMode: viewModel.isSelectionMode,
                                    onTap: {
                                        if viewModel.isSelectionMode {
                                            viewModel.toggleSelection(for: file)
                                        } else if file.isDirectory {
                                            viewModel.navigateTo(directory: file.url)
                                        } else {
                                            onOpenFile(file)
                                        }
                                    },
                                    onSelectToggle: {
                                        viewModel.toggleSelection(for: file)
                                    },
                                    onRename: {
                                        itemToRename = file
                                        renameText = file.name
                                        showingRenameAlert = true
                                    },
                                    onCopy: {
                                        viewModel.copyToClipboard(items: [file], isCut: false)
                                    },
                                    onCut: {
                                        viewModel.copyToClipboard(items: [file], isCut: true)
                                    },
                                    onDelete: {
                                        viewModel.delete(items: [file])
                                    },
                                    onShare: {
                                        shareURL = file.url
                                    },
                                    onCompress: {
                                        viewModel.compress(items: [file], archiveName: "\(file.name).zip")
                                    }
                                )
                            }
                        }
                        .padding(.top, 8)
                        .padding(.bottom, 120)
                    }
                }
            }
            
            // Clipboard Paste Bar
            if let clip = viewModel.clipboard {
                VStack {
                    Spacer()
                    HStack {
                        VStack(alignment: .leading, spacing: 2) {
                            Text("\(clip.items.count) item(s) to \(clip.isCut ? "Move" : "Copy")")
                                .font(.caption).bold()
                                .foregroundColor(.white)
                        }
                        
                        Spacer()
                        
                        Button(action: { viewModel.clearClipboard() }) {
                            Text("Cancel")
                                .font(.caption).bold()
                                .foregroundColor(.gray)
                                .padding(.horizontal, 12)
                                .padding(.vertical, 6)
                        }
                        
                        Button(action: { viewModel.pasteFromClipboard() }) {
                            Text("Paste Here")
                                .font(.caption).bold()
                                .foregroundColor(.black)
                                .padding(.horizontal, 14)
                                .padding(.vertical, 8)
                                .background(Color.neonCyan)
                                .clipShape(Capsule())
                        }
                    }
                    .padding(.horizontal, 16)
                    .padding(.vertical, 12)
                    .glassCard(cornerRadius: 20, strokeColor: Color.neonCyan.opacity(0.3), backgroundColor: Color.darkSurfaceVariant)
                    .padding(.horizontal)
                    .padding(.bottom, 90)
                }
            }
            // Custom Dialog Overlays (iOS 14+ Compatible)
            if showingNewFolderAlert {
                Color.black.opacity(0.6).ignoresSafeArea()
                    .onTapGesture { showingNewFolderAlert = false }
                
                VStack(spacing: 16) {
                    Text("New Folder")
                        .font(.headline).bold()
                        .foregroundColor(.white)
                    
                    TextField("Folder name", text: $newFolderName)
                        .padding(10)
                        .background(Color.white.opacity(0.1))
                        .cornerRadius(8)
                        .foregroundColor(.white)
                    
                    HStack(spacing: 16) {
                        Button("Cancel") {
                            showingNewFolderAlert = false
                            newFolderName = ""
                        }
                        .foregroundColor(.gray)
                        
                        Spacer()
                        
                        Button("Create") {
                            if !newFolderName.isEmpty {
                                viewModel.createFolder(named: newFolderName)
                                newFolderName = ""
                                showingNewFolderAlert = false
                            }
                        }
                        .font(.headline)
                        .foregroundColor(.neonCyan)
                    }
                }
                .padding(20)
                .glassCard(cornerRadius: 20, strokeColor: Color.neonCyan.opacity(0.4), backgroundColor: Color.darkSurfaceVariant)
                .padding(.horizontal, 36)
            }
            
            if showingRenameAlert {
                Color.black.opacity(0.6).ignoresSafeArea()
                    .onTapGesture { showingRenameAlert = false }
                
                VStack(spacing: 16) {
                    Text("Rename Item")
                        .font(.headline).bold()
                        .foregroundColor(.white)
                    
                    TextField("New name", text: $renameText)
                        .padding(10)
                        .background(Color.white.opacity(0.1))
                        .cornerRadius(8)
                        .foregroundColor(.white)
                    
                    HStack(spacing: 16) {
                        Button("Cancel") {
                            showingRenameAlert = false
                            renameText = ""
                        }
                        .foregroundColor(.gray)
                        
                        Spacer()
                        
                        Button("Rename") {
                            if let item = itemToRename, !renameText.isEmpty {
                                viewModel.rename(item: item, to: renameText)
                                renameText = ""
                                showingRenameAlert = false
                            }
                        }
                        .font(.headline)
                        .foregroundColor(.neonCyan)
                    }
                }
                .padding(20)
                .glassCard(cornerRadius: 20, strokeColor: Color.neonCyan.opacity(0.4), backgroundColor: Color.darkSurfaceVariant)
                .padding(.horizontal, 36)
            }
        }
        .sheet(isPresented: $showingDocPicker) {
            DocumentPickerView { urls in
                for url in urls {
                    let dest = viewModel.currentDirectory.appendingPathComponent(url.lastPathComponent)
                    try? FileManagerService.shared.copyItem(at: url, to: dest)
                }
                viewModel.loadFiles()
            }
        }
        .sheet(item: Binding(
            get: { shareURL != nil ? ShareItem(url: shareURL!) : nil },
            set: { _ in shareURL = nil }
        )) { item in
            ShareSheet(items: [item.url])
        }
    }
}

private struct ShareItem: Identifiable {
    let id = UUID()
    let url: URL
}
