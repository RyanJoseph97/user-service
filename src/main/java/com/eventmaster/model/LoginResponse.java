package com.eventmaster.model;

/**
 * Response object for successful login containing JWT token and user information.
 */
public class LoginResponse {
    private String token;
    private String username;
    private String email;
    private String name;
    private String location;
    private String dateJoined;
    private String accountStatus;

    public LoginResponse() {}

    public LoginResponse(String token, String username, String email, String name,
                         String location, String dateJoined, String accountStatus) {
        this.token = token;
        this.username = username;
        this.email = email;
        this.name = name;
        this.location = location;
        this.dateJoined = dateJoined;
        this.accountStatus = accountStatus;
    }

    // Getters and setters
    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
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

    public String getDateJoined() {
        return dateJoined;
    }

    public void setDateJoined(String dateJoined) {
        this.dateJoined = dateJoined;
    }

    public String getAccountStatus() {
        return accountStatus;
    }

    public void setAccountStatus(String accountStatus) {
        this.accountStatus = accountStatus;
    }
}