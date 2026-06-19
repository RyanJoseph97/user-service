package com.eventmaster.model;

import javax.validation.constraints.Email;
import javax.validation.constraints.Size;

public class UpdateUserRequest {

    @Email(message = "Invalid email format")
    private String email;

    @Size(max = 100, message = "Name must not exceed 100 characters")
    private String name;

    @Size(max = 100, message = "Location must not exceed 100 characters")
    private String location;

    private Boolean privateProfile;

    private String profilePictureUrl;

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }

    public Boolean getPrivateProfile() { return privateProfile; }
    public void setPrivateProfile(Boolean privateProfile) { this.privateProfile = privateProfile; }

    public String getProfilePictureUrl() { return profilePictureUrl; }
    public void setProfilePictureUrl(String profilePictureUrl) { this.profilePictureUrl = profilePictureUrl; }

    private Double latitude;
    private Double longitude;

    public Double getLatitude() { return latitude; }
    public void setLatitude(Double latitude) { this.latitude = latitude; }

    public Double getLongitude() { return longitude; }
    public void setLongitude(Double longitude) { this.longitude = longitude; }
}
