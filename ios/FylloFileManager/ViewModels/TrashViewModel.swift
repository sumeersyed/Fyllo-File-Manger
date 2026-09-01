import Foundation
import SwiftUI

@MainActor
public class TrashViewModel: ObservableObject {
    @Published public var trashItems: [TrashItem] = []
    @Published public var totalTrashBytes: Int64 = 0
    @Published public var isLoading: Bool = false
    
    public init() {
        loadTrash()
    }
    
    public func loadTrash() {
        isLoading = true
        self.trashItems = FileManagerService.shared.loadTrashRecords()
        self.totalTrashBytes = self.trashItems.reduce(0) { $0 + $1.sizeBytes }
        isLoading = false
    }
    
    public func restoreItem(_ item: TrashItem) {
        try? FileManagerService.shared.restoreFromTrash(item: item)
        loadTrash()
    }
    
    public func emptyTrash() {
        try? FileManagerService.shared.emptyTrash()
        loadTrash()
    }
}
