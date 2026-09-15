package com.hackathon.spotify.model;

import jakarta.persistence.*;

@Entity
@Table(name = "songs")
public class Song {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false)
    private String artist;

    private String album;
    private String coverUrl;
    private String audioUrl;
    private String genre;

    public Song() {}

    public Song(String title, String artist, String album, String coverUrl, String audioUrl, String genre) {
        this.title = title;
        this.artist = artist;
        this.album = album;
        this.coverUrl = coverUrl;
        this.audioUrl = audioUrl;
        this.genre = genre;
    }

    public Long getId() { return id; }
    public String getTitle() { return title; }
    public String getArtist() { return artist; }
    public String getAlbum() { return album; }
    public String getCoverUrl() { return coverUrl; }
    public String getAudioUrl() { return audioUrl; }
    public String getGenre() { return genre; }
}
