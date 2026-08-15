package com.amrita.lostandfound.config;

import com.amrita.lostandfound.model.User;
import com.amrita.lostandfound.repository.UserRepository;
import org.mindrot.jbcrypt.BCrypt;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Optional;

@Configuration
public class DatabaseSeeder {

    @Bean
    CommandLineRunner initDatabase(UserRepository userRepository) {
        return args -> {
            // Change this to whatever email you want your Admin to use
            String adminEmail = "itshroshan@gmail.com";

            // Check if admin exists
            Optional<User> existingAdmin = userRepository.findByEmail(adminEmail);
            User admin;

            if (existingAdmin.isEmpty()) {
                admin = new User();
                admin.setName("System Admin");
                admin.setEmail(adminEmail);
                admin.setRole("admin");
                admin.setIsVerified(1);
            } else {
                admin = existingAdmin.get();
            }

            // FORCE update the password to admin123 so the user can always log in
            admin.setPassword(BCrypt.hashpw("admin123", BCrypt.gensalt()));
            userRepository.save(admin);
            System.out.println("✅ ADMIN ACCOUNT PASSWORD FORCED TO: admin123");
        };
    }
}