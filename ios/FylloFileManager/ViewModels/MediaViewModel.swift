import Foundation
import SwiftUI

@MainActor
public class MediaViewModel: ObservableObject {
    @Published public var mediaItems: [FileItem] = []
    @Published public var currentCategory: FileCategory = .image
    @Published public var isLoading: Bool = false
    @Published public var selectedItem: FileItem? = nil
    
    public init() {
        loadMedia()
    }
    
    public func setCategory(_ category: FileCategory) {
        self.currentCategory = category
        loadMedia()
    }
    
    public func loadMedia() {
        isLoading = true
        Task {
            await MediaService.shared.fetchAllMedia()
            await MainActor.run {
                switch self.currentCategory {
                case .image:
                    self.mediaItems = MediaService.shared.photosMedia
                case .video:
                    self.mediaItems = MediaService.shared.videosMedia
                case .audio:
                    self.mediaItems = MediaService.shared.audioMedia
                case .document, .pdf:
                    self.mediaItems = MediaService.shared.documentMedia
                default:
                    self.mediaItems = MediaService.shared.photosMedia + MediaService.shared.videosMedia
                }
                self.isLoading = false
            }
        }
    }
    
    public func deleteMedia(_ item: FileItem) {
        _ = try? FileManagerService.shared.moveToTrash(item: item)
        loadMedia()
    }
}
