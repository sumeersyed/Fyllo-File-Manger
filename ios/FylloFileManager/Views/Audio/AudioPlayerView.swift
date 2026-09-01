import SwiftUI

public struct AudioPlayerView: View {
    @ObservedObject private var player = AudioPlayerService.shared
    public var onDismiss: () -> Void
    
    @State private var showingPlaylist = false
    @State private var rotationAngle: Double = 0
    
    public var body: some View {
        ZStack {
            Color.darkBackground.ignoresSafeArea()
            
            // Neon Glow Atmosphere
            Circle()
                .fill(Color.neonGreen.opacity(0.12))
                .blur(radius: 80)
                .frame(width: 300, height: 300)
                .offset(y: -50)
            
            VStack(spacing: 24) {
                // Header
                HStack {
                    Button(action: onDismiss) {
                        Image(systemName: "chevron.down")
                            .font(.system(size: 18, weight: .bold))
                            .foregroundColor(.white)
                            .frame(width: 40, height: 40)
                            .background(Color.white.opacity(0.1))
                            .clipShape(Circle())
                    }
                    
                    Spacer()
                    
                    VStack(spacing: 2) {
                        Text("PLAYING FROM FILE")
                            .font(.system(size: 10, weight: .bold))
                            .foregroundColor(.neonGreen)
                        Text("Fyllo Audio Player")
                            .font(.caption).bold()
                            .foregroundColor(.gray)
                    }
                    
                    Spacer()
                    
                    Button(action: { showingPlaylist.toggle() }) {
                        Image(systemName: "music.note.list")
                            .font(.system(size: 18, weight: .bold))
                            .foregroundColor(.white)
                            .frame(width: 40, height: 40)
                            .background(Color.white.opacity(0.1))
                            .clipShape(Circle())
                    }
                }
                .padding(.horizontal)
                .padding(.top, 20)
                
                Spacer()
                
                // Vinyl / Album Art Animation
                ZStack {
                    Circle()
                        .fill(
                            LinearGradient(
                                colors: [Color.darkSurfaceVariant, Color.black],
                                startPoint: .topLeading,
                                endPoint: .bottomTrailing
                            )
                        )
                        .frame(width: 260, height: 260)
                        .overlay(
                            Circle()
                                .strokeBorder(Color.neonGreen.opacity(0.3), lineWidth: 2)
                        )
                        .shadow(color: Color.neonGreen.opacity(player.isPlaying ? 0.35 : 0.0), radius: 24)
                    
                    // Grooves
                    Circle()
                        .strokeBorder(Color.white.opacity(0.06), lineWidth: 1)
                        .frame(width: 210, height: 210)
                    Circle()
                        .strokeBorder(Color.white.opacity(0.06), lineWidth: 1)
                        .frame(width: 160, height: 160)
                    
                    // Center Disc
                    Circle()
                        .fill(Color.neonGreen)
                        .frame(width: 70, height: 70)
                        .overlay(
                            Image(systemName: "music.quarternote.3")
                                .font(.system(size: 24, weight: .bold))
                                .foregroundColor(.black)
                        )
                }
                .rotationEffect(.degrees(rotationAngle))
                .animation(player.isPlaying ? Animation.linear(duration: 8).repeatForever(autoreverses: false) : .default, value: rotationAngle)
                .onAppear {
                    if player.isPlaying {
                        rotationAngle = 360
                    }
                }
                .onChange(of: player.isPlaying) { isPlaying in
                    if isPlaying {
                        rotationAngle += 360
                    }
                }
                
                Spacer()
                
                // Track Info
                VStack(spacing: 6) {
                    Text(player.currentTrack?.name ?? "No Track Loaded")
                        .font(.title3).bold()
                        .foregroundColor(.white)
                        .lineLimit(1)
                        .multilineTextAlignment(.center)
                        .padding(.horizontal, 24)
                    
                    Text(player.currentTrack?.formattedSize ?? "")
                        .font(.caption)
                        .foregroundColor(.gray)
                }
                
                // Progress Scrubber
                VStack(spacing: 8) {
                    Slider(
                        value: Binding(
                            get: { player.currentTime },
                            set: { newVal in player.seek(to: newVal) }
                        ),
                        in: 0...max(player.duration, 1.0)
                    )
                    .accentColor(.neonGreen)
                    
                    HStack {
                        Text(formatTime(player.currentTime))
                            .font(.caption2)
                            .foregroundColor(.gray)
                        Spacer()
                        Text(formatTime(player.duration))
                            .font(.caption2)
                            .foregroundColor(.gray)
                    }
                }
                .padding(.horizontal, 28)
                
                // Playback Controls
                HStack(spacing: 28) {
                    Button(action: { player.toggleShuffle() }) {
                        Image(systemName: "shuffle")
                            .font(.system(size: 18))
                            .foregroundColor(player.isShuffle ? .neonGreen : .gray)
                    }
                    
                    Button(action: { player.previousTrack() }) {
                        Image(systemName: "backward.end.fill")
                            .font(.system(size: 24))
                            .foregroundColor(.white)
                    }
                    
                    Button(action: { player.togglePlayPause() }) {
                        ZStack {
                            Circle()
                                .fill(Color.neonGreen)
                                .frame(width: 68, height: 68)
                                .shadow(color: Color.neonGreen.opacity(0.4), radius: 12)
                            Image(systemName: player.isPlaying ? "pause.fill" : "play.fill")
                                .font(.system(size: 28))
                                .foregroundColor(.black)
                        }
                    }
                    
                    Button(action: { player.nextTrack() }) {
                        Image(systemName: "forward.end.fill")
                            .font(.system(size: 24))
                            .foregroundColor(.white)
                    }
                    
                    Button(action: { player.toggleRepeat() }) {
                        Image(systemName: player.repeatMode == .one ? "repeat.1" : "repeat")
                            .font(.system(size: 18))
                            .foregroundColor(player.repeatMode != .off ? .neonGreen : .gray)
                    }
                }
                .padding(.bottom, 40)
            }
        }
        .sheet(isPresented: $showingPlaylist) {
            AudioPlaylistSheet()
        }
    }
    
    private func formatTime(_ seconds: Double) -> String {
        let mins = Int(seconds) / 60
        let secs = Int(seconds) % 60
        return String(format: "%d:%02d", mins, secs)
    }
}

private struct AudioPlaylistSheet: View {
    @ObservedObject private var player = AudioPlayerService.shared
    
    var body: some View {
        NavigationView {
            ZStack {
                Color.darkSurface.ignoresSafeArea()
                
                List {
                    ForEach(0..<player.playlist.count, id: \.self) { idx in
                        let track = player.playlist[idx]
                        let isCurrent = idx == player.currentIndex
                        
                        Button(action: { player.playTrack(at: idx) }) {
                            HStack(spacing: 12) {
                                Text("\(idx + 1)")
                                    .font(.caption).bold()
                                    .foregroundColor(isCurrent ? .neonGreen : .gray)
                                    .frame(width: 24)
                                
                                VStack(alignment: .leading, spacing: 2) {
                                    Text(track.name)
                                        .font(.subheadline).bold()
                                        .foregroundColor(isCurrent ? .neonGreen : .white)
                                        .lineLimit(1)
                                    Text(track.formattedSize)
                                        .font(.caption2)
                                        .foregroundColor(.gray)
                                }
                                
                                Spacer()
                                
                                if isCurrent && player.isPlaying {
                                    Image(systemName: "waveform")
                                        .font(.system(size: 14))
                                        .foregroundColor(.neonGreen)
                                }
                            }
                            .padding(.vertical, 4)
                        }
                    }
                    .listRowBackground(Color.darkSurfaceVariant)
                }
                .scrollContentBackground(.hidden)
            }
            .navigationTitle("Queue")
            .navigationBarTitleDisplayMode(.inline)
        }
    }
}
