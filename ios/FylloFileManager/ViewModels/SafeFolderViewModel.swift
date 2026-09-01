import Foundation
import SwiftUI

@MainActor
public class SafeFolderViewModel: ObservableObject {
    @Published public var isUnlocked: Bool = false
    @Published public var vaultRecords: [SafeVaultRecord] = []
    @Published public var errorMessage: String? = nil
    @Published public var isAuthenticating: Bool = false
    
    public init() {}
    
    public func authenticateAndUnlock() {
        isAuthenticating = true
        Task {
            let result = await BiometricAuthService.shared.authenticate(reason: "Authenticate to access your Safe Vault")
            await MainActor.run {
                self.isAuthenticating = false
                if result.success {
                    self.isUnlocked = true
                    self.loadVault()
                } else {
                    self.errorMessage = result.error?.localizedDescription ?? "Authentication failed"
                }
            }
        }
    }
    
    public func lockVault() {
        self.isUnlocked = false
    }
    
    public func loadVault() {
        SafeFolderService.shared.loadVaultRecords()
        self.vaultRecords = SafeFolderService.shared.vaultRecords
    }
    
    public func addToVault(file: FileItem) {
        do {
            try SafeFolderService.shared.encryptFile(item: file)
            loadVault()
        } catch {
            errorMessage = error.localizedDescription
        }
    }
    
    public func restoreFromVault(record: SafeVaultRecord) {
        do {
            _ = try SafeFolderService.shared.decryptFile(record: record)
            loadVault()
        } catch {
            errorMessage = error.localizedDescription
        }
    }
}
