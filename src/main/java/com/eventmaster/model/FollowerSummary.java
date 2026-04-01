package com.eventmaster.model;

import java.time.LocalDate;

public class FollowerSummary {
    private String username;
    private String name;
    private LocalDate dateJoined;

    public FollowerSummary(String username, String name, LocalDate dateJoined) {
        this.username = username;
        this.name = name;
        this.dateJoined = dateJoined;
    }

    public String getUsername() { return username; }
    public String getName() { return name; }
    public LocalDate getDateJoined() { return dateJoined; }
}
