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

    public String getEmail() { return email; }
    public String getName() { return name; }
    public String getLocation() { return location; }
}
