package com.amrita.lostandfound.config;

import com.amrita.lostandfound.model.User;
import com.amrita.lostandfound.repository.UserRepository;
import org.mindrot.jbcrypt.BCrypt;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DatabaseSeeder {

    @Bean
    CommandLineRunner initDatabase(UserRepository userRepository) {
        return args -> {
            // Change this to whatever email you want your Admin to use
            String adminEmail = "roshansah393@gmail.com";

            // If the database is empty and doesn't have this admin yet...
            if (userRepository.findByEmail(adminEmail).isEmpty()) {

                User admin = new User();
                admin.setName("System Admin");
                admin.setEmail(adminEmail);

                // Set your desired admin password here (it will be safely hashed!)
                admin.setPassword(BCrypt.hashpw("admin123", BCrypt.gensalt()));

                admin.setRole("admin");
                admin.setIsVerified(1); // Auto-verify the admin

                userRepository.save(admin);
                System.out.println("✅ FRESH ADMIN ACCOUNT GENERATED SUCCESSFULLY!");
            }
        };
    }
}