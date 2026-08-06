package com.amrita.lostandfound.repository;

import com.amrita.lostandfound.model.LostItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.time.LocalDateTime;

public interface LostItemRepository extends JpaRepository<LostItem, Long> {
    Page<LostItem> findByStudentEmail(String studentEmail, Pageable pageable);
    Page<LostItem> findByStudentEmailAndCreatedAtAfter(String studentEmail, LocalDateTime date, Pageable pageable);
    Page<LostItem> findAllByCreatedAtAfter(LocalDateTime date, Pageable pageable);
    
    @org.springframework.transaction.annotation.Transactional
    void deleteByCreatedAtBefore(LocalDateTime date);
}