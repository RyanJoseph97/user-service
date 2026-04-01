package com.eventmaster.model;
import javax.persistence.*;
import com.fasterxml.jackson.annotation.JsonIgnore;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.time.LocalDate;
import com.eventmaster.service.PasswordService;

@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @JsonIgnore
    private Long id;

    @Column(nullable = false, unique = true)
    private String username;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false, unique = true)
    private String email;

    private String name;

    private String location;

    @Column(name = "date_joined")
    private LocalDate dateJoined;

    // Constructors
    public User() {
        // Default constructor
    }

    public User(String username, String password, String email, String name, String location, LocalDate dateJoined) {
        this.username = username;
        this.password = password;
        this.email = email;
        this.name = name;
        this.location = location;
        this.dateJoined = dateJoined;
    }

    public User(String username, String password, String email, String name, String location){
        this.username = username;
        this.password = password;
        this.email = email;
        this.name = name;
        this.location = location;
        this.dateJoined = LocalDate.now();
    }

    // Getters and setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    /**
     * Set password with automatic hashing using BCrypt.
     * This method should be used when setting passwords from user input.
     * 
     * @param rawPassword the plain text password to hash and store
     * @param passwordService the PasswordService to use for hashing
     */
    public void setHashedPassword(String rawPassword, PasswordService passwordService) {
        if (rawPassword != null && !rawPassword.trim().isEmpty()) {
            this.password = passwordService.hashPassword(rawPassword);
        } else {
            this.password = null;
        }
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public LocalDate getDateJoined() {
        return dateJoined;
    }

    public void setDateJoined(LocalDate dateJoined) {
        this.dateJoined = dateJoined;
    }
}