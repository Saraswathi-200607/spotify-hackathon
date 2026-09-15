package com.hackathon.spotify.service;

import com.hackathon.spotify.model.*;
import com.hackathon.spotify.repository.*;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SpotifyService {
    private final SongRepository songs;
    private final PlaylistRepository playlists;
    private final PlayHistoryRepository history;

    public SpotifyService(SongRepository songs, PlaylistRepository playlists,
                          PlayHistoryRepository history) {
        this.songs = songs;
        this.playlists = playlists;
        this.history = history;
    }

    public List<Song> songs() { return songs.findAll(); }

    public List<Song> search(String q) {
        return songs.findByTitleContainingIgnoreCaseOrArtistContainingIgnoreCase(q, q);
    }

    public void play(String userId, Long songId) {
        if (!songs.existsById(songId)) throw new IllegalArgumentException("Song not found");
        history.save(new PlayHistory(userId, songId));
    }

    public List<Playlist> playlists(String userId) {
        return playlists.findByUserId(userId);
    }

    public Playlist createPlaylist(String userId, String name) {
        Playlist p = new Playlist();
        p.setName(name);
        p.setUserId(userId);
        return playlists.save(p);
    }

    public List<PlayHistory> history(String userId) {
        return history.findByUserIdOrderByPlayedAtDesc(userId);
    }
}
