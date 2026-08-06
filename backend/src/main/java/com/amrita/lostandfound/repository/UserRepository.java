package com.amrita.lostandfound.repository;

import com.amrita.lostandfound.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    // Automatically generates SQL to find a user by their email
    Optional<User> findByEmail(String email);
}