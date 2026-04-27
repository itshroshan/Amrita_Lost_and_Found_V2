package com.amrita.lostandfound.repository;

import com.amrita.lostandfound.model.ClaimedItem;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ClaimedItemRepository extends JpaRepository<ClaimedItem, Long> {
    // Automatically sorts the claimed items from newest to oldest
    List<ClaimedItem> findAllByOrderByClaimedAtDesc();
}