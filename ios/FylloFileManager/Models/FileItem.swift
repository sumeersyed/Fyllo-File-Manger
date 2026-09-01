import Foundation
import SwiftUI
import UniformTypeIdentifiers

public struct FileItem: Identifiable, Hashable, Codable {
    public let id: String
    public var name: String
    public var url: URL
    public var path: String
    public var isDirectory: Bool
    public var sizeBytes: Int64
    public var lastModified: Date
    public var mimeType: String?
    public var fileExtension: String
    public var itemCount: Int?
    public var durationSeconds: Double?
    public var dimensions: String?
    public var isFavorite: Bool
    public var isHidden: Bool
    
    public init(
        id: String = UUID().uuidString,
        name: String,
        url: URL,
        path: String? = nil,
        isDirectory: Bool = false,
        sizeBytes: Int64 = 0,
        lastModified: Date = Date(),
        mimeType: String? = nil,
        fileExtension: String? = nil,
        itemCount: Int? = nil,
        durationSeconds: Double? = nil,
        dimensions: String? = nil,
        isFavorite: Bool = false,
        isHidden: Bool = false
    ) {
        self.id = id
        self.name = name
        self.url = url
        self.path = path ?? url.path
        self.isDirectory = isDirectory
        self.sizeBytes = sizeBytes
        self.lastModified = lastModified
        self.mimeType = mimeType
        self.fileExtension = fileExtension ?? url.pathExtension.lowercased()
        self.itemCount = itemCount
        self.durationSeconds = durationSeconds
        self.dimensions = dimensions
        self.isFavorite = isFavorite
        self.isHidden = isHidden
    }
    
    public var formattedSize: String {
        if isDirectory {
            if let count = itemCount {
                return "\(count) item\(count == 1 ? "" : "s")"
            }
            return "Folder"
        }
        return ByteCountFormatter.string(fromByteCount: sizeBytes, countStyle: .file)
    }
    
    public var formattedDate: String {
        let formatter = DateFormatter()
        formatter.dateStyle = .medium
        formatter.timeStyle = .short
        return formatter.string(from: lastModified)
    }
    
    public var category: FileCategory {
        if isDirectory { return .folder }
        let ext = fileExtension.lowercased()
        
        switch ext {
        case "jpg", "jpeg", "png", "gif", "heic", "webp", "bmp", "tiff", "svg":
            return .image
        case "mp4", "mov", "m4v", "mkv", "avi", "3gp", "webm":
            return .video
        case "mp3", "m4a", "wav", "aac", "flac", "ogg", "aiff", "wma":
            return .audio
        case "pdf":
            return .pdf
        case "doc", "docx", "txt", "rtf", "pages", "xls", "xlsx", "numbers", "ppt", "pptx", "key", "csv", "md", "json", "xml", "html":
            return .document
        case "zip", "rar", "7z", "tar", "gz", "bz2", "xz":
            return .archive
        case "ipa", "apk":
            return .appPackage
        default:
            return .other
        }
    }
    
    public var systemIconName: String {
        if isDirectory { return "folder.fill" }
        switch category {
        case .folder: return "folder.fill"
        case .image: return "photo.fill"
        case .video: return "film.fill"
        case .audio: return "music.note"
        case .pdf: return "doc.richtext.fill"
        case .document: return "doc.text.fill"
        case .archive: return "doc.zipper"
        case .appPackage: return "shippingbox.fill"
        case .other: return "doc.fill"
        }
    }
    
    public var categoryColor: Color {
        switch category {
        case .folder: return Color(hex: "00E5FF") // Neon Cyan
        case .image: return Color(hex: "FF4081") // Neon Pink
        case .video: return Color(hex: "D500F9") // Neon Purple
        case .audio: return Color(hex: "38F570") // Neon Green
        case .pdf: return Color(hex: "FF5252") // Red
        case .document: return Color(hex: "448AFF") // Blue
        case .archive: return Color(hex: "FFD600") // Yellow
        case .appPackage: return Color(hex: "7C4DFF") // Violet
        case .other: return Color.gray
        }
    }
}

public enum FileCategory: String, CaseIterable, Codable, Identifiable {
    case all = "All"
    case folder = "Folders"
    case image = "Images"
    case video = "Videos"
    case audio = "Audio"
    case document = "Documents"
    case pdf = "PDFs"
    case archive = "Archives"
    case appPackage = "Packages"
    case other = "Other"
    
    public var id: String { rawValue }
    
    public var iconName: String {
        switch self {
        case .all: return "square.grid.2x2.fill"
        case .folder: return "folder.fill"
        case .image: return "photo.fill"
        case .video: return "film.fill"
        case .audio: return "music.note"
        case .document: return "doc.text.fill"
        case .pdf: return "doc.richtext.fill"
        case .archive: return "doc.zipper"
        case .appPackage: return "shippingbox.fill"
        case .other: return "ellipsis.circle.fill"
        }
    }
}
