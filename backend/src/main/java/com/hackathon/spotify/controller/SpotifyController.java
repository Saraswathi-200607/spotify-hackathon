package com.hackathon.spotify.controller;

import com.hackathon.spotify.model.*;
import com.hackathon.spotify.repository.UserRepository;
import com.hackathon.spotify.service.SpotifyService;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class SpotifyController {
    private final SpotifyService service;
    private final UserRepository users;

    public SpotifyController(SpotifyService service, UserRepository users) {
        this.service = service;
        this.users = users;
    }

    private String userId() {
        return SecurityContextHolder.getContext().getAuthentication().getName();
    }

    @GetMapping("/songs")
    public List<Song> songs() {
        return service.songs();
    }

    @GetMapping("/search")
    public List<Song> search(@RequestParam(defaultValue = "") String q) {
        return service.search(q);
    }

    @PostMapping("/play")
    public Map<String,String> play(@RequestBody Map<String,Long> body) {
        service.play(userId(), body.get("songId"));
        return Map.of("message", "Play recorded");
    }

    @GetMapping("/playlists")
    public List<Playlist> playlists() {
        return service.playlists(userId());
    }

    @PostMapping("/playlists")
    public Playlist createPlaylist(@RequestBody Map<String,String> body) {
        return service.createPlaylist(userId(), body.get("name"));
    }

    @GetMapping("/profile")
    public Map<String,String> profile() {
        User u = users.findByUserId(userId()).orElseThrow();
        return Map.of("userId", u.getUserId(), "email", u.getEmail(), "plan", u.getPlan().name());
    }

    @GetMapping("/history")
    public List<PlayHistory> history() {
        return service.history(userId());
    }

    @GetMapping("/health")
    public Map<String,String> health() {
        return Map.of("status", "UP");
    }
}
