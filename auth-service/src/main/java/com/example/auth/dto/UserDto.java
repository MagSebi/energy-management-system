package com.example.auth.dto;

import com.example.auth.entities.User;
import java.util.UUID;

public class UserDto {
    private UUID id;
    private String username;
    private String role;

    public UserDto() {}

    public UserDto(UUID id, String username, String role) {
        this.id = id;
        this.username = username;
        this.role = role;
    }

    public static UserDto from(User u) {
        return new UserDto(u.getId(), u.getUsername(), u.getRole().name());
    }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }
}
