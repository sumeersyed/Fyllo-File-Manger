import Foundation
import SwiftUI
import UniformTypeIdentifiers
import CryptoKit

public class FileManagerService: ObservableObject {
    public static let shared = FileManagerService()
    
    private let fileManager = FileManager.default
    
    public var documentsDirectory: URL {
        fileManager.urls(for: .documentDirectory, in: .userDomainMask)[0]
    }
    
    public var cachesDirectory: URL {
        fileManager.urls(for: .cachesDirectory, in: .userDomainMask)[0]
    }
    
    public var temporaryDirectory: URL {
        fileManager.temporaryDirectory
    }
    
    public var safeVaultDirectory: URL {
        let url = documentsDirectory.appendingPathComponent(".safe_vault", isDirectory: true)
        if !fileManager.fileExists(atPath: url.path) {
            try? fileManager.createDirectory(at: url, withIntermediateDirectories: true)
        }
        return url
    }
    
    public var trashDirectory: URL {
        let url = documentsDirectory.appendingPathComponent(".trash", isDirectory: true)
        if !fileManager.fileExists(atPath: url.path) {
            try? fileManager.createDirectory(at: url, withIntermediateDirectories: true)
        }
        return url
    }
    
    public init() {
        createDefaultDirectoriesIfNeeded()
    }
    
    private func createDefaultDirectoriesIfNeeded() {
        let sampleFolders = ["Downloads", "Documents", "Images", "Music", "Videos"]
        for folder in sampleFolders {
            let url = documentsDirectory.appendingPathComponent(folder, isDirectory: true)
            if !fileManager.fileExists(atPath: url.path) {
                try? fileManager.createDirectory(at: url, withIntermediateDirectories: true)
            }
        }
    }
    
    // MARK: - List Files & Folders
    public func contentsOfDirectory(at url: URL, showHidden: Bool = false) -> [FileItem] {
        var results: [FileItem] = []
        
        let resourceKeys: Set<URLResourceKey> = [
            .nameKey, .isDirectoryKey, .fileSizeKey,
            .contentModificationDateKey, .typeIdentifierKey,
            .isHiddenKey, .totalFileSizeKey
        ]
        
        guard let fileURLs = try? fileManager.contentsOfDirectory(
            at: url,
            includingPropertiesForKeys: Array(resourceKeys),
            options: showHidden ? [] : [.skipsHiddenFiles]
        ) else {
            return []
        }
        
        for fileURL in fileURLs {
            // Ignore system safe folder & trash from normal listing
            let lastComponent = fileURL.lastPathComponent
            if !showHidden && (lastComponent == ".safe_vault" || lastComponent == ".trash" || lastComponent.hasPrefix(".")) {
                continue
            }
            
            guard let resourceValues = try? fileURL.resourceValues(forKeys: resourceKeys) else { continue }
            
            let isDir = resourceValues.isDirectory ?? false
            let name = resourceValues.name ?? fileURL.lastPathComponent
            let size = Int64(resourceValues.fileSize ?? resourceValues.totalFileSize ?? 0)
            let date = resourceValues.contentModificationDate ?? Date()
            let typeId = resourceValues.typeIdentifier
            let ext = fileURL.pathExtension
            
            var itemCount: Int? = nil
            if isDir {
                let subitems = try? fileManager.contentsOfDirectory(at: fileURL, includingPropertiesForKeys: nil, options: [.skipsHiddenFiles])
                itemCount = subitems?.count
            }
            
            let item = FileItem(
                id: fileURL.path,
                name: name,
                url: fileURL,
                path: fileURL.path,
                isDirectory: isDir,
                sizeBytes: size,
                lastModified: date,
                mimeType: mimeType(for: fileURL, typeId: typeId),
                fileExtension: ext,
                itemCount: itemCount,
                isHidden: resourceValues.isHidden ?? false
            )
            results.append(item)
        }
        
        return results
    }
    
    // MARK: - CRUD Operations
    public func createDirectory(named name: String, in parentURL: URL) throws -> URL {
        let targetURL = parentURL.appendingPathComponent(name, isDirectory: true)
        try fileManager.createDirectory(at: targetURL, withIntermediateDirectories: true)
        return targetURL
    }
    
    public func renameItem(at sourceURL: URL, to newName: String) throws -> URL {
        let destinationURL = sourceURL.deletingLastPathComponent().appendingPathComponent(newName)
        try fileManager.moveItem(at: sourceURL, to: destinationURL)
        return destinationURL
    }
    
    public func copyItem(at sourceURL: URL, to destinationURL: URL, strategy: ConflictStrategy = .keepBoth) throws -> URL {
        var finalDestination = destinationURL
        if fileManager.fileExists(atPath: destinationURL.path) {
            switch strategy {
            case .overwrite:
                try fileManager.removeItem(at: destinationURL)
            case .skip:
                return destinationURL
            case .keepBoth:
                finalDestination = makeUniqueDestinationURL(for: destinationURL)
            }
        }
        try fileManager.copyItem(at: sourceURL, to: finalDestination)
        return finalDestination
    }
    
    public func moveItem(at sourceURL: URL, to destinationURL: URL, strategy: ConflictStrategy = .keepBoth) throws -> URL {
        var finalDestination = destinationURL
        if fileManager.fileExists(atPath: destinationURL.path) {
            switch strategy {
            case .overwrite:
                try fileManager.removeItem(at: destinationURL)
            case .skip:
                return destinationURL
            case .keepBoth:
                finalDestination = makeUniqueDestinationURL(for: destinationURL)
            }
        }
        try fileManager.moveItem(at: sourceURL, to: finalDestination)
        return finalDestination
    }
    
    public func deleteItemPermanently(at url: URL) throws {
        try fileManager.removeItem(at: url)
    }
    
    public func moveToTrash(item: FileItem) throws -> TrashItem {
        let trashID = UUID().uuidString
        let trashTargetURL = trashDirectory.appendingPathComponent("\(trashID)_\(item.name)")
        
        try fileManager.moveItem(at: item.url, to: trashTargetURL)
        
        let trashItem = TrashItem(
            id: trashID,
            originalPath: item.path,
            trashPath: trashTargetURL.path,
            name: item.name,
            isDirectory: item.isDirectory,
            sizeBytes: item.sizeBytes,
            deletedDate: Date(),
            mimeType: item.mimeType
        )
        saveTrashRecord(trashItem)
        return trashItem
    }
    
    public func restoreFromTrash(item: TrashItem) throws {
        let trashURL = URL(fileURLWithPath: item.trashPath)
        let originalURL = URL(fileURLWithPath: item.originalPath)
        
        let finalURL = makeUniqueDestinationURL(for: originalURL)
        try fileManager.moveItem(at: trashURL, to: finalURL)
        removeTrashRecord(item)
    }
    
    // MARK: - Trash Storage Records
    private var trashRecordsURL: URL {
        trashDirectory.appendingPathComponent("trash_manifest.json")
    }
    
    public func loadTrashRecords() -> [TrashItem] {
        guard let data = try? Data(contentsOf: trashRecordsURL),
              let list = try? JSONDecoder().decode([TrashItem].self, from: data) else {
            return []
        }
        return list
    }
    
    private func saveTrashRecord(_ item: TrashItem) {
        var records = loadTrashRecords()
        records.append(item)
        if let data = try? JSONEncoder().encode(records) {
            try? data.write(to: trashRecordsURL)
        }
    }
    
    private func removeTrashRecord(_ item: TrashItem) {
        var records = loadTrashRecords()
        records.removeAll { $0.id == item.id }
        if let data = try? JSONEncoder().encode(records) {
            try? data.write(to: trashRecordsURL)
        }
    }
    
    public func emptyTrash() throws {
        let records = loadTrashRecords()
        for item in records {
            let url = URL(fileURLWithPath: item.trashPath)
            if fileManager.fileExists(atPath: url.path) {
                try? fileManager.removeItem(at: url)
            }
        }
        try? fileManager.removeItem(at: trashRecordsURL)
    }
    
    // MARK: - Storage Stats & Calculations
    public func calculateStorageStats() -> StorageStats {
        let fileURL = documentsDirectory
        do {
            let values = try fileURL.resourceValues(forKeys: [.volumeTotalCapacityKey, .volumeAvailableCapacityForImportantUsageKey])
            let total = Int64(values.volumeTotalCapacity ?? 0)
            let free = values.volumeAvailableCapacityForImportantUsage ?? 0
            let used = total - free
            
            // Breakdown Documents
            var images: Int64 = 0
            var videos: Int64 = 0
            var audio: Int64 = 0
            var docs: Int64 = 0
            var archives: Int64 = 0
            var other: Int64 = 0
            
            let allItems = recursivelyScanFiles(at: documentsDirectory)
            for item in allItems {
                switch item.category {
                case .image: images += item.sizeBytes
                case .video: videos += item.sizeBytes
                case .audio: audio += item.sizeBytes
                case .document, .pdf: docs += item.sizeBytes
                case .archive: archives += item.sizeBytes
                default: other += item.sizeBytes
                }
            }
            
            return StorageStats(
                totalBytes: total,
                usedBytes: used,
                freeBytes: free,
                imagesBytes: images,
                videosBytes: videos,
                audioBytes: audio,
                docsBytes: docs,
                appsBytes: 0,
                archivesBytes: archives,
                otherBytes: other,
                systemBytes: max(0, used - (images + videos + audio + docs + archives + other))
            )
        } catch {
            return StorageStats()
        }
    }
    
    public func recursivelyScanFiles(at rootURL: URL) -> [FileItem] {
        var items: [FileItem] = []
        guard let enumerator = fileManager.enumerator(
            at: rootURL,
            includingPropertiesForKeys: [.fileSizeKey, .isDirectoryKey, .contentModificationDateKey],
            options: [.skipsHiddenFiles]
        ) else { return [] }
        
        for case let url as URL in enumerator {
            guard let values = try? url.resourceValues(forKeys: [.fileSizeKey, .isDirectoryKey, .contentModificationDateKey]) else { continue }
            let isDir = values.isDirectory ?? false
            let size = Int64(values.fileSize ?? 0)
            let date = values.contentModificationDate ?? Date()
            
            if !isDir {
                items.append(FileItem(
                    name: url.lastPathComponent,
                    url: url,
                    isDirectory: false,
                    sizeBytes: size,
                    lastModified: date,
                    fileExtension: url.pathExtension
                ))
            }
        }
        return items
    }
    
    // MARK: - ZIP Compression / Archive
    public func compressFiles(urls: [URL], to archiveName: String) throws -> URL {
        var name = archiveName
        if !name.lowercased().hasSuffix(".zip") {
            name += ".zip"
        }
        let destURL = documentsDirectory.appendingPathComponent(name)
        let coordinator = NSFileCoordinator()
        var error: NSError?
        var outputURL: URL?
        
        coordinator.coordinate(readingItemAt: urls[0], options: .forUploading, error: &error) { zipURL in
            do {
                if self.fileManager.fileExists(atPath: destURL.path) {
                    try self.fileManager.removeItem(at: destURL)
                }
                try self.fileManager.copyItem(at: zipURL, to: destURL)
                outputURL = destURL
            } catch {
                print("Error copying zip: \(error)")
            }
        }
        
        if let err = error { throw err }
        return outputURL ?? destURL
    }
    
    // MARK: - Helpers
    private func makeUniqueDestinationURL(for url: URL) -> URL {
        guard fileManager.fileExists(atPath: url.path) else { return url }
        let directory = url.deletingLastPathComponent()
        let filename = url.deletingPathExtension().lastPathComponent
        let ext = url.pathExtension
        
        var counter = 1
        var candidateURL: URL
        repeat {
            let newName = ext.isEmpty ? "\(filename) (\(counter))" : "\(filename) (\(counter)).\(ext)"
            candidateURL = directory.appendingPathComponent(newName)
            counter += 1
        } while fileManager.fileExists(atPath: candidateURL.path)
        
        return candidateURL
    }
    
    private func mimeType(for url: URL, typeId: String?) -> String? {
        if let typeId = typeId, let utType = UTType(typeId) {
            return utType.preferredMIMEType
        }
        if let utType = UTType(filenameExtension: url.pathExtension) {
            return utType.preferredMIMEType
        }
        return nil
    }
}
