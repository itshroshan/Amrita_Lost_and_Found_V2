package com.amrita.lostandfound.repository;

import com.amrita.lostandfound.model.MatchHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MatchHistoryRepository extends JpaRepository<MatchHistory, Long> {

    // This custom method checks if we already emailed the student about this specific pair!
    boolean existsByLostItemIdAndFoundItemId(Long lostItemId, Long foundItemId);
}