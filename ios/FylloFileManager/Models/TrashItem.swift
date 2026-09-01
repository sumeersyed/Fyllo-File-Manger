import Foundation

public struct TrashItem: Identifiable, Codable, Hashable {
    public let id: String
    public let originalPath: String
    public let trashPath: String
    public let name: String
    public let isDirectory: Bool
    public let sizeBytes: Int64
    public let deletedDate: Date
    public let mimeType: String?
    
    public init(
        id: String = UUID().uuidString,
        originalPath: String,
        trashPath: String,
        name: String,
        isDirectory: Bool,
        sizeBytes: Int64,
        deletedDate: Date = Date(),
        mimeType: String? = nil
    ) {
        self.id = id
        self.originalPath = originalPath
        self.trashPath = trashPath
        self.name = name
        self.isDirectory = isDirectory
        self.sizeBytes = sizeBytes
        self.deletedDate = deletedDate
        self.mimeType = mimeType
    }
    
    public var formattedSize: String {
        ByteCountFormatter.string(fromByteCount: sizeBytes, countStyle: .file)
    }
    
    public var formattedDeletedDate: String {
        let formatter = DateFormatter()
        formatter.dateStyle = .medium
        formatter.timeStyle = .short
        return formatter.string(from: deletedDate)
    }
}
