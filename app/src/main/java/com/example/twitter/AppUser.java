package com.example.twitter;

public class AppUser {
    private String username;
    private String email;
    private String birthday;
    private String profilePictureUrl;
    private long createdAt;

    // Constructor
    public AppUser(String username, String email, String birthday, String profilePictureUrl, long createdAt) {
        this.username = username;
        this.email = email;
        this.birthday = birthday;
        this.profilePictureUrl = profilePictureUrl;
        this.createdAt = createdAt;
    }

    // Getters and setters (optional)
    public String getUsername() {
        return username;
    }

    public String getEmail() {
        return email;
    }

    public String getBirthday() {
        return birthday;
    }

    public String getProfilePictureUrl() {
        return profilePictureUrl;
    }

    public long getCreatedAt() {
        return createdAt;
    }
}
