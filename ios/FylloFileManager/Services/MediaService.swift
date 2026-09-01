import Foundation
import Photos
import AVFoundation
import UIKit

public class MediaService: ObservableObject {
    public static let shared = MediaService()
    
    @Published public var photosMedia: [FileItem] = []
    @Published public var videosMedia: [FileItem] = []
    @Published public var audioMedia: [FileItem] = []
    @Published public var documentMedia: [FileItem] = []
    @Published public var isLoading: Bool = false
    
    public init() {}
    
    // MARK: - Scan Local Media & PhotoKit
    public func fetchAllMedia(filter: String = "all") async {
        await MainActor.run { isLoading = true }
        
        let allItems = FileManagerService.shared.recursivelyScanFiles(at: FileManagerService.shared.documentsDirectory)
        
        var images: [FileItem] = []
        var videos: [FileItem] = []
        var audios: [FileItem] = []
        var docs: [FileItem] = []
        
        for item in allItems {
            switch item.category {
            case .image:
                images.append(item)
            case .video:
                var videoItem = item
                if let duration = getVideoDuration(for: item.url) {
                    videoItem.durationSeconds = duration
                }
                videos.append(videoItem)
            case .audio:
                var audioItem = item
                if let duration = getAudioDuration(for: item.url) {
                    audioItem.durationSeconds = duration
                }
                audios.append(audioItem)
            case .document, .pdf:
                docs.append(item)
            default:
                break
            }
        }
        
        await MainActor.run {
            self.photosMedia = images.sorted(by: { $0.lastModified > $1.lastModified })
            self.videosMedia = videos.sorted(by: { $0.lastModified > $1.lastModified })
            self.audioMedia = audios.sorted(by: { $0.lastModified > $1.lastModified })
            self.documentMedia = docs.sorted(by: { $0.lastModified > $1.lastModified })
            self.isLoading = false
        }
    }
    
    private func getVideoDuration(for url: URL) -> Double? {
        let asset = AVURLAsset(url: url)
        let duration = asset.duration
        let seconds = CMTimeGetSeconds(duration)
        return seconds.isNaN ? nil : seconds
    }
    
    private func getAudioDuration(for url: URL) -> Double? {
        let asset = AVURLAsset(url: url)
        let duration = asset.duration
        let seconds = CMTimeGetSeconds(duration)
        return seconds.isNaN ? nil : seconds
    }
    
    public func generateThumbnail(for file: FileItem, size: CGSize = CGSize(width: 200, height: 200)) async -> UIImage? {
        if file.category == .image {
            guard let data = try? Data(contentsOf: file.url), let image = UIImage(data: data) else { return nil }
            return image
        } else if file.category == .video {
            let asset = AVAsset(url: file.url)
            let generator = AVAssetImageGenerator(asset: asset)
            generator.appliesPreferredTrackTransform = true
            let time = CMTime(seconds: 1, preferredTimescale: 60)
            if let cgImage = try? generator.copyCGImage(at: time, actualTime: nil) {
                return UIImage(cgImage: cgImage)
            }
        }
        return nil
    }
}
