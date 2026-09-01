import Foundation
import SwiftUI

public enum SortOption: String, CaseIterable {
    case dateDesc = "Date (Newest)"
    case dateAsc = "Date (Oldest)"
    case nameAsc = "Name (A-Z)"
    case nameDesc = "Name (Z-A)"
    case sizeDesc = "Size (Largest)"
    case sizeAsc = "Size (Smallest)"
}

@MainActor
public class FilesViewModel: ObservableObject {
    @Published public var currentDirectory: URL
    @Published public var pathHistory: [URL] = []
    @Published public var files: [FileItem] = []
    @Published public var isLoading: Bool = false
    @Published public var isGridView: Bool = false
    @Published public var sortOption: SortOption = .dateDesc
    @Published public var selectedItems: Set<FileItem> = []
    @Published public var isSelectionMode: Bool = false
    @Published public var clipboard: (items: [FileItem], isCut: Bool)? = nil
    @Published public var operationState = FileOperationState()
    @Published public var errorMessage: String? = nil
    
    public init(initialDirectory: URL? = nil) {
        let initial = initialDirectory ?? FileManagerService.shared.documentsDirectory
        self.currentDirectory = initial
        loadFiles()
    }
    
    public func loadFiles() {
        isLoading = true
        let items = FileManagerService.shared.contentsOfDirectory(at: currentDirectory)
        self.files = sortItemList(items)
        isLoading = false
    }
    
    public func navigateTo(directory: URL) {
        pathHistory.append(currentDirectory)
        currentDirectory = directory
        selectedItems.removeAll()
        isSelectionMode = false
        loadFiles()
    }
    
    public func navigateUp() {
        guard !pathHistory.isEmpty else {
            if currentDirectory != FileManagerService.shared.documentsDirectory {
                currentDirectory = currentDirectory.deletingLastPathComponent()
                loadFiles()
            }
            return
        }
        currentDirectory = pathHistory.removeLast()
        selectedItems.removeAll()
        isSelectionMode = false
        loadFiles()
    }
    
    public func setSortOption(_ option: SortOption) {
        self.sortOption = option
        self.files = sortItemList(self.files)
    }
    
    private func sortItemList(_ list: [FileItem]) -> [FileItem] {
        let sortedFolders = list.filter { $0.isDirectory }
        let sortedFiles = list.filter { !$0.isDirectory }
        
        let sortBlock: (FileItem, FileItem) -> Bool = { a, b in
            switch self.sortOption {
            case .nameAsc: return a.name.localizedStandardCompare(b.name) == .orderedAscending
            case .nameDesc: return a.name.localizedStandardCompare(b.name) == .orderedDescending
            case .dateDesc: return a.lastModified > b.lastModified
            case .dateAsc: return a.lastModified < b.lastModified
            case .sizeDesc: return a.sizeBytes > b.sizeBytes
            case .sizeAsc: return a.sizeBytes < b.sizeBytes
            }
        }
        
        return sortedFolders.sorted(by: sortBlock) + sortedFiles.sorted(by: sortBlock)
    }
    
    // MARK: - Actions
    public func createFolder(named name: String) {
        do {
            _ = try FileManagerService.shared.createDirectory(named: name, in: currentDirectory)
            loadFiles()
        } catch {
            errorMessage = error.localizedDescription
        }
    }
    
    public func rename(item: FileItem, to newName: String) {
        do {
            _ = try FileManagerService.shared.renameItem(at: item.url, to: newName)
            loadFiles()
        } catch {
            errorMessage = error.localizedDescription
        }
    }
    
    public func delete(items: [FileItem], permanent: Bool = false) {
        for item in items {
            do {
                if permanent {
                    try FileManagerService.shared.deleteItemPermanently(at: item.url)
                } else {
                    _ = try FileManagerService.shared.moveToTrash(item: item)
                }
            } catch {
                errorMessage = error.localizedDescription
            }
        }
        selectedItems.removeAll()
        isSelectionMode = false
        loadFiles()
    }
    
    public func copyToClipboard(items: [FileItem], isCut: Bool) {
        self.clipboard = (items, isCut)
        self.selectedItems.removeAll()
        self.isSelectionMode = false
    }
    
    public func pasteFromClipboard() {
        guard let clip = clipboard else { return }
        
        for item in clip.items {
            let destinationURL = currentDirectory.appendingPathComponent(item.name)
            do {
                if clip.isCut {
                    _ = try FileManagerService.shared.moveItem(at: item.url, to: destinationURL)
                } else {
                    _ = try FileManagerService.shared.copyItem(at: item.url, to: destinationURL)
                }
            } catch {
                errorMessage = error.localizedDescription
            }
        }
        
        if clip.isCut {
            self.clipboard = nil
        }
        loadFiles()
    }
    
    public func clearClipboard() {
        self.clipboard = nil
    }
    
    public func compress(items: [FileItem], archiveName: String) {
        let urls = items.map { $0.url }
        do {
            _ = try FileManagerService.shared.compressFiles(urls: urls, to: archiveName)
            loadFiles()
        } catch {
            errorMessage = error.localizedDescription
        }
    }
    
    public func toggleSelection(for item: FileItem) {
        if selectedItems.contains(item) {
            selectedItems.remove(item)
            if selectedItems.isEmpty {
                isSelectionMode = false
            }
        } else {
            selectedItems.insert(item)
            isSelectionMode = true
        }
    }
    
    public func selectAll() {
        selectedItems = Set(files)
        isSelectionMode = true
    }
    
    public func deselectAll() {
        selectedItems.removeAll()
        isSelectionMode = false
    }
}
