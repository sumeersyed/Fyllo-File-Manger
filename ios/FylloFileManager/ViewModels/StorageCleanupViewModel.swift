import Foundation
import SwiftUI

@MainActor
public class StorageCleanupViewModel: ObservableObject {
    @Published public var junkFiles: [FileItem] = []
    @Published public var largeFiles: [FileItem] = []
    @Published public var duplicateGroups: [DuplicateGroup] = []
    @Published public var isScanning: Bool = false
    @Published public var totalJunkBytes: Int64 = 0
    @Published public var cleanupCompletedMessage: String? = nil
    
    public init() {
        startScan()
    }
    
    public func startScan() {
        Task {
            await StorageCleanupService.shared.scanAll()
            await MainActor.run {
                self.junkFiles = StorageCleanupService.shared.junkFiles
                self.largeFiles = StorageCleanupService.shared.largeFiles
                self.duplicateGroups = StorageCleanupService.shared.duplicateGroups
                self.totalJunkBytes = StorageCleanupService.shared.totalJunkBytes
            }
        }
    }
    
    public func cleanJunk() {
        Task {
            let cleaned = await StorageCleanupService.shared.cleanAllJunk()
            let formatted = ByteCountFormatter.string(fromByteCount: cleaned, countStyle: .file)
            await MainActor.run {
                self.cleanupCompletedMessage = "Cleaned \(formatted) of junk files!"
                self.startScan()
            }
        }
    }
    
    public func deleteLargeFile(_ item: FileItem) {
        try? FileManager.default.removeItem(at: item.url)
        startScan()
    }
    
    public func deleteDuplicate(_ item: FileItem) {
        try? FileManager.default.removeItem(at: item.url)
        startScan()
    }
}
