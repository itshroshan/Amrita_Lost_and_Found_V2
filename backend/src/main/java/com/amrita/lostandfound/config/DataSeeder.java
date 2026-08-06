package com.amrita.lostandfound.config;

import com.amrita.lostandfound.model.User;
import com.amrita.lostandfound.repository.UserRepository;
import org.mindrot.jbcrypt.BCrypt;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class DataSeeder implements CommandLineRunner {

    @Autowired
    private UserRepository userRepository;

    @Value("${app.admin.email}")
    private String adminEmail;

    @Value("${app.admin.password}")
    private String adminPassword;

    @Value("${app.admin.name}")
    private String adminName;

    @Override
    public void run(String... args) throws Exception {
        // Seed the admin user if it does not exist
        if (adminEmail != null && !adminEmail.isEmpty() && userRepository.findByEmail(adminEmail).isEmpty()) {
            User admin = new User();
            admin.setName(adminName);
            admin.setEmail(adminEmail);
            admin.setPassword(BCrypt.hashpw(adminPassword, BCrypt.gensalt()));
            admin.setRole("admin");
            admin.setIsVerified(1); // Auto-verify admin
            
            userRepository.save(admin);
            System.out.println("Admin user seeded successfully: " + adminEmail);
        }
    }
}
