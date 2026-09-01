import Foundation
import SwiftUI

@MainActor
public class HomeViewModel: ObservableObject {
    @Published public var storageStats: StorageStats = StorageStats()
    @Published public var recentFiles: [FileItem] = []
    @Published public var categoryCounts: [FileCategory: Int] = [:]
    @Published public var isLoading: Bool = false
    
    public init() {
        refreshDashboard()
    }
    
    public func refreshDashboard() {
        isLoading = true
        
        let stats = FileManagerService.shared.calculateStorageStats()
        self.storageStats = stats
        
        let allFiles = FileManagerService.shared.recursivelyScanFiles(at: FileManagerService.shared.documentsDirectory)
        
        // Sort by last modified for recent files
        self.recentFiles = Array(allFiles.sorted(by: { $0.lastModified > $1.lastModified }).prefix(15))
        
        // Count categories
        var counts: [FileCategory: Int] = [:]
        for cat in FileCategory.allCases {
            counts[cat] = 0
        }
        for file in allFiles {
            counts[file.category, default: 0] += 1
        }
        self.categoryCounts = counts
        
        self.isLoading = false
    }
}
