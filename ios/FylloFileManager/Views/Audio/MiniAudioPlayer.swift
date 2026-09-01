import SwiftUI

public struct MiniAudioPlayer: View {
    @ObservedObject private var player = AudioPlayerService.shared
    public var onExpand: () -> Void
    
    public var body: some View {
        if let track = player.currentTrack {
            Button(action: onExpand) {
                HStack(spacing: 12) {
                    ZStack {
                        Circle()
                            .fill(Color.neonGreen.opacity(0.2))
                            .frame(width: 40, height: 40)
                        Image(systemName: "music.note")
                            .font(.system(size: 16))
                            .foregroundColor(.neonGreen)
                    }
                    
                    VStack(alignment: .leading, spacing: 2) {
                        Text(track.name)
                            .font(.caption).bold()
                            .foregroundColor(.white)
                            .lineLimit(1)
                        
                        Text(formatTime(player.currentTime) + " / " + formatTime(player.duration))
                            .font(.caption2)
                            .foregroundColor(.gray)
                    }
                    
                    Spacer()
                    
                    Button(action: { player.previousTrack() }) {
                        Image(systemName: "backward.fill")
                            .font(.system(size: 14))
                            .foregroundColor(.white)
                    }
                    
                    Button(action: { player.togglePlayPause() }) {
                        Image(systemName: player.isPlaying ? "pause.circle.fill" : "play.circle.fill")
                            .font(.system(size: 28))
                            .foregroundColor(.neonGreen)
                    }
                    
                    Button(action: { player.nextTrack() }) {
                        Image(systemName: "forward.fill")
                            .font(.system(size: 14))
                            .foregroundColor(.white)
                    }
                }
                .padding(.horizontal, 14)
                .padding(.vertical, 8)
                .glassCard(cornerRadius: 22, strokeColor: Color.neonGreen.opacity(0.3), backgroundColor: Color.darkSurfaceVariant)
                .padding(.horizontal)
            }
            .buttonStyle(PlainButtonStyle())
        }
    }
    
    private func formatTime(_ seconds: Double) -> String {
        let mins = Int(seconds) / 60
        let secs = Int(seconds) % 60
        return String(format: "%d:%02d", mins, secs)
    }
}
