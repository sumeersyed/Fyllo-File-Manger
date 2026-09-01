import Foundation
import LocalAuthentication

public class BiometricAuthService {
    public static let shared = BiometricAuthService()
    
    public enum BiometricType {
        case none
        case touchID
        case faceID
    }
    
    public func biometricType() -> BiometricType {
        let context = LAContext()
        var error: NSError?
        guard context.canEvaluatePolicy(.deviceOwnerAuthenticationWithBiometrics, error: &error) else {
            return .none
        }
        if #available(iOS 11.0, *) {
            switch context.biometryType {
            case .faceID: return .faceID
            case .touchID: return .touchID
            case .none: return .none
            @unknown default: return .none
            }
        }
        return .touchID
    }
    
    public func authenticate(reason: String = "Unlock Fyllo File Manager") async -> (success: Bool, error: Error?) {
        let context = LAContext()
        var error: NSError?
        
        guard context.canEvaluatePolicy(.deviceOwnerAuthentication, error: &error) else {
            return (false, error)
        }
        
        return await withCheckedContinuation { continuation in
            context.evaluatePolicy(.deviceOwnerAuthentication, localizedReason: reason) { success, evalError in
                continuation.resume(returning: (success, evalError))
            }
        }
    }
}
