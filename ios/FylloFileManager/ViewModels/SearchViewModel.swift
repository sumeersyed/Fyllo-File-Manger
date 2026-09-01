import Foundation
import SwiftUI

@MainActor
public class SearchViewModel: ObservableObject {
    @Published public var searchQuery: String = ""
    @Published public var searchFilter: FileCategory = .all
    @Published public var searchResults: [FileItem] = []
    @Published public var isSearching: Bool = false
    
    public init() {}
    
    public func performSearch() {
        guard !searchQuery.trimmingCharacters(in: .whitespacesAndNewlines).isEmpty else {
            searchResults = []
            return
        }
        
        isSearching = true
        let allItems = FileManagerService.shared.recursivelyScanFiles(at: FileManagerService.shared.documentsDirectory)
        
        let query = searchQuery.lowercased()
        searchResults = allItems.filter { item in
            let matchesName = item.name.lowercased().contains(query)
            let matchesCategory = (searchFilter == .all || item.category == searchFilter)
            return matchesName && matchesCategory
        }
        isSearching = false
    }
}
