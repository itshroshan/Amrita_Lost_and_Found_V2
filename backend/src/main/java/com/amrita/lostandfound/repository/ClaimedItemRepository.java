package com.amrita.lostandfound.repository;

import com.amrita.lostandfound.model.ClaimedItem;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.time.LocalDateTime;

public interface ClaimedItemRepository extends JpaRepository<ClaimedItem, Long> {
    // Automatically sorts the claimed items from newest to oldest
//    List<ClaimedItem> findAllByOrderByClaimedAtDesc(); (if its is in list)
    Page<ClaimedItem> findAllByOrderByClaimedAtDesc(Pageable pageable);
    Page<ClaimedItem> findAllByClaimedAtAfterOrderByClaimedAtDesc(LocalDateTime date, Pageable pageable);

    @org.springframework.transaction.annotation.Transactional
    void deleteByClaimedAtBefore(LocalDateTime date);
}