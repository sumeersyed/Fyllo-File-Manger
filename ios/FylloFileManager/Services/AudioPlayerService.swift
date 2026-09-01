import Foundation
import AVFoundation
import MediaPlayer
import SwiftUI

public enum RepeatMode: String, CaseIterable {
    case off = "Off"
    case all = "Repeat All"
    case one = "Repeat One"
}

public class AudioPlayerService: NSObject, ObservableObject, AVAudioPlayerDelegate {
    public static let shared = AudioPlayerService()
    
    @Published public var playlist: [FileItem] = []
    @Published public var currentIndex: Int = 0
    @Published public var isPlaying: Bool = false
    @Published public var currentTime: Double = 0
    @Published public var duration: Double = 0
    @Published public var isShuffle: Bool = false
    @Published public var repeatMode: RepeatMode = .off
    
    private var audioPlayer: AVAudioPlayer?
    private var timer: Timer?
    
    public var currentTrack: FileItem? {
        guard !playlist.isEmpty, currentIndex >= 0, currentIndex < playlist.count else { return nil }
        return playlist[currentIndex]
    }
    
    override private init() {
        super.init()
        setupAudioSession()
        setupRemoteCommandCenter()
    }
    
    private func setupAudioSession() {
        do {
            try AVAudioSession.sharedInstance().setCategory(.playback, mode: .`default`, options: [])
            try AVAudioSession.sharedInstance().setActive(true)
        } catch {
            print("Failed to set audio session category: \(error)")
        }
    }
    
    public func loadPlaylist(_ items: [FileItem], startIndex: Int = 0) {
        self.playlist = items
        self.currentIndex = min(max(0, startIndex), max(0, items.count - 1))
        if !items.isEmpty {
            playTrack(at: self.currentIndex)
        }
    }
    
    public func playTrack(at index: Int) {
        guard index >= 0 && index < playlist.count else { return }
        self.currentIndex = index
        let track = playlist[index]
        
        do {
            audioPlayer?.stop()
            audioPlayer = try AVAudioPlayer(contentsOf: track.url)
            audioPlayer?.delegate = self
            audioPlayer?.prepareToPlay()
            audioPlayer?.play()
            
            self.isPlaying = true
            self.duration = audioPlayer?.duration ?? 0
            self.currentTime = 0
            
            startProgressTimer()
            updateNowPlayingInfo()
        } catch {
            print("Error playing audio: \(error)")
        }
    }
    
    public func togglePlayPause() {
        guard let player = audioPlayer else {
            if let current = currentTrack {
                playTrack(at: currentIndex)
            }
            return
        }
        if player.isPlaying {
            player.pause()
            isPlaying = false
            stopProgressTimer()
        } else {
            player.play()
            isPlaying = true
            startProgressTimer()
        }
        updateNowPlayingInfo()
    }
    
    public func nextTrack() {
        guard !playlist.isEmpty else { return }
        if isShuffle {
            let randomIndex = Int.random(in: 0..<playlist.count)
            playTrack(at: randomIndex)
        } else {
            let nextIndex = (currentIndex + 1) % playlist.count
            playTrack(at: nextIndex)
        }
    }
    
    public func previousTrack() {
        guard !playlist.isEmpty else { return }
        if currentTime > 3.0 {
            seek(to: 0)
        } else {
            let prevIndex = (currentIndex - 1 + playlist.count) % playlist.count
            playTrack(at: prevIndex)
        }
    }
    
    public func seek(to time: Double) {
        audioPlayer?.currentTime = time
        currentTime = time
        updateNowPlayingInfo()
    }
    
    public func toggleShuffle() {
        isShuffle.toggle()
    }
    
    public func toggleRepeat() {
        switch repeatMode {
        case .off: repeatMode = .all
        case .all: repeatMode = .one
        case .one: repeatMode = .off
        }
    }
    
    // MARK: - Progress Timer
    private func startProgressTimer() {
        stopProgressTimer()
        timer = Timer.scheduledTimer(withTimeInterval: 0.5, repeats: true) { [weak self] _ in
            guard let self = self, let player = self.audioPlayer else { return }
            self.currentTime = player.currentTime
        }
    }
    
    private func stopProgressTimer() {
        timer?.invalidate()
        timer = nil
    }
    
    // MARK: - AVAudioPlayerDelegate
    public func audioPlayerDidFinishPlaying(_ player: AVAudioPlayer, successfully flag: Bool) {
        if repeatMode == .one {
            seek(to: 0)
            player.play()
        } else if repeatMode == .all || currentIndex < playlist.count - 1 {
            nextTrack()
        } else {
            isPlaying = false
            stopProgressTimer()
        }
    }
    
    // MARK: - Lock Screen & Control Center Controls
    private func setupRemoteCommandCenter() {
        let commandCenter = MPRemoteCommandCenter.shared()
        
        commandCenter.playCommand.addTarget { [weak self] _ in
            self?.togglePlayPause()
            return .success
        }
        
        commandCenter.pauseCommand.addTarget { [weak self] _ in
            self?.togglePlayPause()
            return .success
        }
        
        commandCenter.nextTrackCommand.addTarget { [weak self] _ in
            self?.nextTrack()
            return .success
        }
        
        commandCenter.previousTrackCommand.addTarget { [weak self] _ in
            self?.previousTrack()
            return .success
        }
        
        commandCenter.changePlaybackPositionCommand.addTarget { [weak self] event in
            guard let self = self, let posEvent = event as? MPChangePlaybackPositionCommandEvent else {
                return .commandFailed
            }
            self.seek(to: posEvent.positionTime)
            return .success
        }
    }
    
    private func updateNowPlayingInfo() {
        guard let track = currentTrack else {
            MPNowPlayingInfoCenter.default().nowPlayingInfo = nil
            return
        }
        
        var nowPlayingInfo = [String: Any]()
        nowPlayingInfo[MPMediaItemPropertyTitle] = track.name
        nowPlayingInfo[MPMediaItemPropertyArtist] = "Fyllo File Manager"
        nowPlayingInfo[MPMediaItemPropertyPlaybackDuration] = duration
        nowPlayingInfo[MPNowPlayingInfoPropertyElapsedPlaybackTime] = currentTime
        nowPlayingInfo[MPNowPlayingInfoPropertyPlaybackRate] = isPlaying ? 1.0 : 0.0
        
        MPNowPlayingInfoCenter.default().nowPlayingInfo = nowPlayingInfo
    }
}
