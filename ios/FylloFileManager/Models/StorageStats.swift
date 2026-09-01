import Foundation

public struct StorageStats: Codable, Hashable {
    public var totalBytes: Int64
    public var usedBytes: Int64
    public var freeBytes: Int64
    
    public var imagesBytes: Int64
    public var videosBytes: Int64
    public var audioBytes: Int64
    public var docsBytes: Int64
    public var appsBytes: Int64
    public var archivesBytes: Int64
    public var otherBytes: Int64
    public var systemBytes: Int64
    
    public init(
        totalBytes: Int64 = 0,
        usedBytes: Int64 = 0,
        freeBytes: Int64 = 0,
        imagesBytes: Int64 = 0,
        videosBytes: Int64 = 0,
        audioBytes: Int64 = 0,
        docsBytes: Int64 = 0,
        appsBytes: Int64 = 0,
        archivesBytes: Int64 = 0,
        otherBytes: Int64 = 0,
        systemBytes: Int64 = 0
    ) {
        self.totalBytes = totalBytes
        self.usedBytes = usedBytes
        self.freeBytes = freeBytes
        self.imagesBytes = imagesBytes
        self.videosBytes = videosBytes
        self.audioBytes = audioBytes
        self.docsBytes = docsBytes
        self.appsBytes = appsBytes
        self.archivesBytes = archivesBytes
        self.otherBytes = otherBytes
        self.systemBytes = systemBytes
    }
    
    public var usedRatio: Double {
        guard totalBytes > 0 else { return 0.0 }
        return Double(usedBytes) / Double(totalBytes)
    }
    
    public var formattedTotal: String {
        ByteCountFormatter.string(fromByteCount: totalBytes, countStyle: .file)
    }
    
    public var formattedUsed: String {
        ByteCountFormatter.string(fromByteCount: usedBytes, countStyle: .file)
    }
    
    public var formattedFree: String {
        ByteCountFormatter.string(fromByteCount: freeBytes, countStyle: .file)
    }
}
