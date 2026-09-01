import Foundation

public enum ConflictStrategy: String, CaseIterable, Codable {
    case overwrite = "Overwrite"
    case skip = "Skip"
    case keepBoth = "Keep Both"
}

public enum OperationType: String, Codable {
    case copy = "Copying"
    case move = "Moving"
    case delete = "Deleting"
    case compress = "Compressing"
    case extract = "Extracting"
    case encrypt = "Encrypting"
    case decrypt = "Decrypting"
}

public struct FileOperationState: Codable {
    public var isRunning: Bool
    public var type: OperationType?
    public var totalItems: Int
    public var processedItems: Int
    public var currentFileName: String
    public var progress: Double
    
    public init(
        isRunning: Bool = false,
        type: OperationType? = nil,
        totalItems: Int = 0,
        processedItems: Int = 0,
        currentFileName: String = "",
        progress: Double = 0.0
    ) {
        self.isRunning = isRunning
        self.type = type
        self.totalItems = totalItems
        self.processedItems = processedItems
        self.currentFileName = currentFileName
        self.progress = progress
    }
}
