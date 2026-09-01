import Foundation
import CryptoKit
import Security

public struct SafeVaultRecord: Codable, Identifiable {
    public let id: String
    public let originalName: String
    public let encryptedFileName: String
    public let originalExtension: String
    public let mimeType: String?
    public let sizeBytes: Int64
    public let encryptedDate: Date
}

public class SafeFolderService: ObservableObject {
    public static let shared = SafeFolderService()
    
    @Published public var vaultRecords: [SafeVaultRecord] = []
    
    private let keychainService = "com.fyllo.filemanager.vaultkey"
    private let keychainAccount = "VaultMasterKey"
    
    private var vaultDirectory: URL {
        FileManagerService.shared.safeVaultDirectory
    }
    
    private var manifestURL: URL {
        vaultDirectory.appendingPathComponent("vault_manifest.enc")
    }
    
    public init() {
        loadVaultRecords()
    }
    
    // MARK: - Key Management (Keychain)
    private func getOrCreateSymmetricKey() -> SymmetricKey {
        if let existingKeyData = loadKeyFromKeychain() {
            return SymmetricKey(data: existingKeyData)
        }
        let newKey = SymmetricKey(size: .bits256)
        let keyData = newKey.withUnsafeBytes { Data($0) }
        saveKeyToKeychain(data: keyData)
        return newKey
    }
    
    private func saveKeyToKeychain(data: Data) {
        let query: [String: Any] = [
            kSecClass as String: kSecClassGenericPassword,
            kSecAttrService as String: keychainService,
            kSecAttrAccount as String: keychainAccount,
            kSecValueData as String: data,
            kSecAttrAccessible as String: kSecAttrAccessibleAfterFirstUnlock
        ]
        SecItemDelete(query as CFDictionary)
        SecItemAdd(query as CFDictionary, nil)
    }
    
    private func loadKeyFromKeychain() -> Data? {
        let query: [String: Any] = [
            kSecClass as String: kSecClassGenericPassword,
            kSecAttrService as String: keychainService,
            kSecAttrAccount as String: keychainAccount,
            kSecReturnData as String: true,
            kSecMatchLimit as String: kSecMatchLimitOne
        ]
        var dataTypeRef: AnyObject?
        let status = SecItemCopyMatching(query as CFDictionary, &dataTypeRef)
        if status == errSecSuccess {
            return dataTypeRef as? Data
        }
        return nil
    }
    
    // MARK: - Encrypt / Move to Safe Folder
    public func encryptFile(item: FileItem) throws {
        let key = getOrCreateSymmetricKey()
        let fileData = try Data(contentsOf: item.url)
        
        let sealedBox = try AES.GCM.seal(fileData, using: key)
        guard let encryptedData = sealedBox.combined else {
            throw NSError(domain: "SafeFolderService", code: 1, userInfo: [NSLocalizedDescriptionKey: "Encryption failed"])
        }
        
        let encryptedID = UUID().uuidString
        let encryptedFileName = "\(encryptedID).fyllo"
        let targetURL = vaultDirectory.appendingPathComponent(encryptedFileName)
        
        try encryptedData.write(to: targetURL)
        
        // Remove original file
        try FileManager.default.removeItem(at: item.url)
        
        let record = SafeVaultRecord(
            id: encryptedID,
            originalName: item.name,
            encryptedFileName: encryptedFileName,
            originalExtension: item.fileExtension,
            mimeType: item.mimeType,
            sizeBytes: item.sizeBytes,
            encryptedDate: Date()
        )
        
        vaultRecords.append(record)
        saveVaultManifest()
    }
    
    // MARK: - Decrypt / Restore
    public func decryptFile(record: SafeVaultRecord, to destinationDirectory: URL? = nil) throws -> URL {
        let key = getOrCreateSymmetricKey()
        let encryptedURL = vaultDirectory.appendingPathComponent(record.encryptedFileName)
        let encryptedData = try Data(contentsOf: encryptedURL)
        
        let sealedBox = try AES.GCM.SealedBox(combined: encryptedData)
        let decryptedData = try AES.GCM.open(sealedBox, using: key)
        
        let targetDir = destinationDirectory ?? FileManagerService.shared.documentsDirectory
        let destinationURL = targetDir.appendingPathComponent(record.originalName)
        
        try decryptedData.write(to: destinationURL)
        try FileManager.default.removeItem(at: encryptedURL)
        
        vaultRecords.removeAll { $0.id == record.id }
        saveVaultManifest()
        
        return destinationURL
    }
    
    // MARK: - Manifest
    private func saveVaultManifest() {
        let key = getOrCreateSymmetricKey()
        guard let jsonData = try? JSONEncoder().encode(vaultRecords) else { return }
        if let sealedBox = try? AES.GCM.seal(jsonData, using: key),
           let combined = sealedBox.combined {
            try? combined.write(to: manifestURL)
        }
    }
    
    public func loadVaultRecords() {
        guard FileManager.default.fileExists(atPath: manifestURL.path),
              let encryptedData = try? Data(contentsOf: manifestURL) else {
            self.vaultRecords = []
            return
        }
        let key = getOrCreateSymmetricKey()
        if let sealedBox = try? AES.GCM.SealedBox(combined: encryptedData),
           let decryptedData = try? AES.GCM.open(sealedBox, using: key),
           let list = try? JSONDecoder().decode([SafeVaultRecord].self, from: decryptedData) {
            self.vaultRecords = list
        }
    }
}
