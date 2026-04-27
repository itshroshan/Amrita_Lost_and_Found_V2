package com.amrita.lostandfound.repository;

import com.amrita.lostandfound.model.LostItem;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LostItemRepository extends JpaRepository<LostItem, Long> {
    // JpaRepository already gives us save(), findAll(), deleteById(), etc. for free!
}