package com.hackathon.spotify.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "play_history")
public class PlayHistory {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String userId;
    private Long songId;
    private LocalDateTime playedAt = LocalDateTime.now();

    public PlayHistory() {}
    public PlayHistory(String userId, Long songId) {
        this.userId = userId;
        this.songId = songId;
        this.playedAt = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public String getUserId() { return userId; }
    public Long getSongId() { return songId; }
    public LocalDateTime getPlayedAt() { return playedAt; }
}
