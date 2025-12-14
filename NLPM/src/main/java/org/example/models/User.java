package org.example.models;

public class User {
    private String username;
    private String password; // This will be hashed
    private String role;
    private boolean isAuthenticated;

    public User(String username, String password, String role) {
        this.username = username;
        this.password = password;
        this.role = role;
        this.isAuthenticated = false;
    }

    // Getters and Setters
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

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public boolean isAuthenticated() {
        return isAuthenticated;
    }

    public void setAuthenticated(boolean authenticated) {
        isAuthenticated = authenticated;
    }

    @Override
    public String toString() {
        return "User{" +
                "username='" + username + '\'' +
                ", role='" + role + '\'' +
                ", isAuthenticated=" + isAuthenticated +
                '}';
    }
}