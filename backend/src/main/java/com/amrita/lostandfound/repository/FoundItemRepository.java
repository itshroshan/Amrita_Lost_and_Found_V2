package com.amrita.lostandfound.repository;

import com.amrita.lostandfound.model.FoundItem;
import org.springframework.data.jpa.repository.JpaRepository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.time.LocalDateTime;
import java.util.List;
import java.util.List;

public interface FoundItemRepository extends JpaRepository<FoundItem, Long> {
    Page<FoundItem> findByIsApprovedTrue(Pageable pageable);
    Page<FoundItem> findByIsApprovedTrueAndCreatedAtAfter(LocalDateTime date, Pageable pageable);
    
    Page<FoundItem> findByIsApprovedFalse(Pageable pageable);
    Page<FoundItem> findByIsApprovedFalseAndCreatedAtAfter(LocalDateTime date, Pageable pageable);

    @org.springframework.data.jpa.repository.Query("SELECT f FROM FoundItem f WHERE f.isApproved = true AND (LOWER(f.itemName) LIKE LOWER(CONCAT('%', :query, '%')) OR LOWER(f.location) LIKE LOWER(CONCAT('%', :query, '%')))")
    Page<FoundItem> findApprovedByQuery(@org.springframework.data.repository.query.Param("query") String query, Pageable pageable);

    @org.springframework.data.jpa.repository.Query("SELECT f FROM FoundItem f WHERE f.isApproved = true AND f.createdAt >= :date AND (LOWER(f.itemName) LIKE LOWER(CONCAT('%', :query, '%')) OR LOWER(f.location) LIKE LOWER(CONCAT('%', :query, '%')))")
    Page<FoundItem> findApprovedByQueryAndDate(@org.springframework.data.repository.query.Param("query") String query, @org.springframework.data.repository.query.Param("date") LocalDateTime date, Pageable pageable);

    Page<FoundItem> findByReportedBy(String reportedBy, Pageable pageable);
    Page<FoundItem> findByReportedByAndCreatedAtAfter(String reportedBy, LocalDateTime date, Pageable pageable);

    @org.springframework.transaction.annotation.Transactional
    void deleteByCreatedAtBefore(LocalDateTime date);
}