package com.hackathon.spotify.config;

import com.hackathon.spotify.model.Song;
import com.hackathon.spotify.repository.SongRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.*;

import java.util.List;

@Configuration
public class DataSeeder {
    @Bean
    CommandLineRunner seed(SongRepository repo) {
        return args -> {
            if (repo.count() == 0) {
                repo.saveAll(List.of(
                    new Song("Blinding Lights", "The Weeknd", "After Hours",
                            "https://picsum.photos/seed/blinding/300", "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-1.mp3", "Pop"),
                    new Song("Shape of You", "Ed Sheeran", "Divide",
                            "https://picsum.photos/seed/shape/300", "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-2.mp3", "Pop"),
                    new Song("Believer", "Imagine Dragons", "Evolve",
                            "https://picsum.photos/seed/believer/300", "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-3.mp3", "Rock"),
                    new Song("Levitating", "Dua Lipa", "Future Nostalgia",
                            "https://picsum.photos/seed/levitating/300", "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-4.mp3", "Dance"),
                    new Song("Perfect", "Ed Sheeran", "Divide",
                            "https://picsum.photos/seed/perfect/300", "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-5.mp3", "Romance")
                ));
            }
        };
    }
}
