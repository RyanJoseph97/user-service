package com.eventmaster.model;

import java.time.LocalDate;

public class FollowerSummary {
    private String username;
    private String name;
    private LocalDate dateJoined;
    private String profilePictureUrl;

    public FollowerSummary(String username, String name, LocalDate dateJoined, String profilePictureUrl) {
        this.username = username;
        this.name = name;
        this.dateJoined = dateJoined;
        this.profilePictureUrl = profilePictureUrl;
    }

    public String getUsername() { return username; }
    public String getName() { return name; }
    public LocalDate getDateJoined() { return dateJoined; }
    public String getProfilePictureUrl() { return profilePictureUrl; }
}
