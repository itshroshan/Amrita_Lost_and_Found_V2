package com.amrita.lostandfound.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "match_history")
public class MatchHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long lostItemId;
    private Long foundItemId;
    private double matchScore;
    private LocalDateTime matchedAt = LocalDateTime.now();

    // Generate Constructors, Getters, and Setters below!
    public MatchHistory() {}
    public MatchHistory(Long lostItemId, Long foundItemId, double matchScore) {
        this.lostItemId = lostItemId;
        this.foundItemId = foundItemId;
        this.matchScore = matchScore;
    }
    // ... add getters/setters ...
}