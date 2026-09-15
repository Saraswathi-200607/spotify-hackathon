package com.hackathon.spotify.repository;

import com.hackathon.spotify.model.PlayHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface PlayHistoryRepository extends JpaRepository<PlayHistory, Long> {
    List<PlayHistory> findByUserIdOrderByPlayedAtDesc(String userId);
}
