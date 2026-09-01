import Foundation
import CryptoKit

public struct DuplicateGroup: Identifiable {
    public let id: String
    public let hash: String
    public let sizeBytes: Int64
    public var files: [FileItem]
}

public class StorageCleanupService: ObservableObject {
    public static let shared = StorageCleanupService()
    
    @Published public var junkFiles: [FileItem] = []
    @Published public var largeFiles: [FileItem] = []
    @Published public var duplicateGroups: [DuplicateGroup] = []
    @Published public var isScanning: Bool = false
    @Published public var totalJunkBytes: Int64 = 0
    
    public init() {}
    
    public func scanAll() async {
        await MainActor.run { isScanning = true }
        
        let allFiles = FileManagerService.shared.recursivelyScanFiles(at: FileManagerService.shared.documentsDirectory)
        let cacheFiles = FileManagerService.shared.recursivelyScanFiles(at: FileManagerService.shared.cachesDirectory)
        let tempFiles = FileManagerService.shared.recursivelyScanFiles(at: FileManagerService.shared.temporaryDirectory)
        
        // 1. Junk & Cache Files (.tmp, .log, .cache, orphan caches)
        var junk: [FileItem] = cacheFiles + tempFiles
        for file in allFiles {
            let ext = file.fileExtension.lowercased()
            if ["tmp", "log", "bak", "cache", "dmp", "temp"].contains(ext) {
                junk.append(file)
            }
        }
        
        // 2. Large Files (> 25MB)
        let large = allFiles.filter { $0.sizeBytes > 25 * 1024 * 1024 }.sorted(by: { $0.sizeBytes > $1.sizeBytes })
        
        // 3. Duplicate Detection via SHA-256 (for files > 10KB)
        var hashMap: [String: [FileItem]] = [:]
        for file in allFiles where file.sizeBytes > 10 * 1024 {
            if let hash = computeFileHash(url: file.url) {
                hashMap[hash, default: []].append(file)
            }
        }
        
        let duplicates = hashMap
            .filter { $0.value.count > 1 }
            .map { DuplicateGroup(id: $0.key, hash: $0.key, sizeBytes: $0.value.first?.sizeBytes ?? 0, files: $0.value) }
            .sorted(by: { ($0.sizeBytes * Int64($0.files.count)) > ($1.sizeBytes * Int64($1.files.count)) })
        
        let junkSum = junk.reduce(0) { $0 + $1.sizeBytes }
        
        await MainActor.run {
            self.junkFiles = junk
            self.largeFiles = large
            self.duplicateGroups = duplicates
            self.totalJunkBytes = junkSum
            self.isScanning = false
        }
    }
    
    public func cleanAllJunk() async -> Int64 {
        var cleanedBytes: Int64 = 0
        for file in junkFiles {
            if (try? FileManager.default.removeItem(at: file.url)) != nil {
                cleanedBytes += file.sizeBytes
            }
        }
        await scanAll()
        return cleanedBytes
    }
    
    private func computeFileHash(url: URL) -> String? {
        guard let data = try? Data(contentsOf: url, options: .mappedIfSafe) else { return nil }
        let digest = SHA256.hash(data: data)
        return digest.compactMap { String(format: "%02x", $0) }.joined()
    }
}
