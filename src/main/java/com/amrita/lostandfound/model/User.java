package com.amrita.lostandfound.model;

import jakarta.persistence.*;

@Entity
@Table(name = "users") // This tells Spring to link this class to your existing "users" table
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    
    @Column(unique = true)
    private String email;
    
    private String password;
    
    private String role; // "admin" or "student"
    
    private Integer isVerified; // Using Integer since SQLite stores booleans as 0 or 1

    // --- Getters and Setters ---
    // Spring Boot needs these to read and write data to the object

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public Integer getIsVerified() { return isVerified; }
    public void setIsVerified(Integer isVerified) { this.isVerified = isVerified; }
}