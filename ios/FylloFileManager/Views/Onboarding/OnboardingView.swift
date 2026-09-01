import SwiftUI

public struct OnboardingView: View {
    public var onFinish: () -> Void
    @State private var currentStep = 0
    
    private let steps = [
        OnboardingStep(
            icon: "folder.badge.gearshape",
            color: Color.neonPurple,
            title: "Manage Everything",
            description: "Fast, elegant file management with instant access to your iCloud and local documents."
        ),
        OnboardingStep(
            icon: "lock.shield.fill",
            color: Color.neonCyan,
            title: "Private Safe Vault",
            description: "Protect sensitive documents and photos with AES-256 military-grade encryption and Face ID."
        ),
        OnboardingStep(
            icon: "sparkles",
            color: Color.neonGreen,
            title: "Intelligent Cleanup",
            description: "Free up storage by identifying duplicate photos, caches, and large unused files in seconds."
        )
    ]
    
    public var body: some View {
        ZStack {
            Color.black.ignoresSafeArea()
            
            VStack(spacing: 32) {
                Spacer()
                
                TabView(selection: $currentStep) {
                    ForEach(0..<steps.count, id: \.self) { idx in
                        let step = steps[idx]
                        VStack(spacing: 24) {
                            ZStack {
                                Circle()
                                    .fill(step.color.opacity(0.18))
                                    .frame(width: 140, height: 140)
                                    .overlay(
                                        Circle()
                                            .strokeBorder(step.color.opacity(0.3), lineWidth: 2)
                                    )
                                Image(systemName: step.icon)
                                    .font(.system(size: 60))
                                    .foregroundColor(step.color)
                            }
                            
                            VStack(spacing: 12) {
                                Text(step.title)
                                    .font(.title).bold()
                                    .foregroundColor(.white)
                                
                                Text(step.description)
                                    .font(.body)
                                    .foregroundColor(.gray)
                                    .multilineTextAlignment(.center)
                                    .padding(.horizontal, 32)
                            }
                        }
                        .tag(idx)
                    }
                }
                .tabViewStyle(PageTabViewStyle(indexDisplayMode: .always))
                .frame(height: 380)
                
                Spacer()
                
                Button(action: {
                    if currentStep < steps.count - 1 {
                        withAnimation { currentStep += 1 }
                    } else {
                        onFinish()
                    }
                }) {
                    Text(currentStep == steps.count - 1 ? "Get Started" : "Continue")
                        .font(.headline)
                        .foregroundColor(.black)
                        .frame(maxWidth: .infinity)
                        .padding(.vertical, 16)
                        .background(Color.neonCyan)
                        .clipShape(Capsule())
                        .shadow(color: Color.neonCyan.opacity(0.4), radius: 12)
                }
                .padding(.horizontal, 32)
                .padding(.bottom, 24)
            }
        }
    }
}

private struct OnboardingStep {
    let icon: String
    let color: Color
    let title: String
    let description: String
}
