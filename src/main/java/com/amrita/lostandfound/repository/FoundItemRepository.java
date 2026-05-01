package com.amrita.lostandfound.repository;

import com.amrita.lostandfound.model.FoundItem;
import org.springframework.data.jpa.repository.JpaRepository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.List;

public interface FoundItemRepository extends JpaRepository<FoundItem, Long> {
    // Automatically generates: SELECT * FROM found_items WHERE item_name LIKE %?% OR location LIKE %?%
    Page<FoundItem> findByItemNameContainingIgnoreCaseOrLocationContainingIgnoreCase(String name, String location, Pageable pageable);}