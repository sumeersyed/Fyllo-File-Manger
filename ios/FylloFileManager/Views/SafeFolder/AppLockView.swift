import SwiftUI

public struct AppLockView: View {
    public var onUnlockSuccess: () -> Void
    @State private var errorMessage: String? = nil
    
    public var body: some View {
        ZStack {
            Color.black.ignoresSafeArea()
            
            VStack(spacing: 24) {
                Spacer()
                
                ZStack {
                    Circle()
                        .fill(Color.neonPurple.opacity(0.18))
                        .frame(width: 120, height: 120)
                    
                    Image(systemName: "lock.shield.fill")
                        .font(.system(size: 56))
                        .foregroundColor(.neonPurple)
                }
                
                VStack(spacing: 6) {
                    Text("Fyllo File Manager")
                        .font(.title).bold()
                        .foregroundColor(.white)
                    Text("Protected with App Lock")
                        .font(.subheadline)
                        .foregroundColor(.gray)
                }
                
                if let error = errorMessage {
                    Text(error)
                        .font(.caption)
                        .foregroundColor(.red)
                        .padding(.horizontal)
                }
                
                Button(action: { authenticate() }) {
                    HStack(spacing: 8) {
                        Image(systemName: "faceid")
                            .font(.headline)
                        Text("Unlock with Face ID / Touch ID")
                            .font(.headline)
                    }
                    .foregroundColor(.white)
                    .padding(.horizontal, 24)
                    .padding(.vertical, 14)
                    .background(Color.neonPurple)
                    .clipShape(Capsule())
                    .shadow(color: Color.neonPurple.opacity(0.4), radius: 12)
                }
                
                Spacer()
            }
        }
        .onAppear {
            authenticate()
        }
    }
    
    private func authenticate() {
        Task {
            let res = await BiometricAuthService.shared.authenticate(reason: "Unlock Fyllo File Manager")
            await MainActor.run {
                if res.success {
                    onUnlockSuccess()
                } else {
                    errorMessage = res.error?.localizedDescription ?? "Authentication failed"
                }
            }
        }
    }
}
