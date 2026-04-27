package com.amrita.lostandfound.repository;

import com.amrita.lostandfound.model.FoundItem;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface FoundItemRepository extends JpaRepository<FoundItem, Long> {
    // Automatically generates: SELECT * FROM found_items WHERE item_name LIKE %?% OR location LIKE %?%
    List<FoundItem> findByItemNameContainingIgnoreCaseOrLocationContainingIgnoreCase(String itemName, String location);
}