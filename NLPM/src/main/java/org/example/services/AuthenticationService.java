package org.example.services;

import org.example.models.User;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.prefs.Preferences;

public class AuthenticationService {
    private static AuthenticationService instance;
    private Map<String, User> users;
    private User currentUser;
    private Preferences prefs;

    private static final String PREF_USERNAME = "remembered_username";
    private static final String PREF_PASSWORD_HASH = "remembered_password_hash";
    private static final String PREF_REMEMBER_ME = "remember_me";

    private AuthenticationService() {
        users = new HashMap<>();
        prefs = Preferences.userNodeForPackage(AuthenticationService.class);
        initializeDefaultUsers();
    }

    public static AuthenticationService getInstance() {
        if (instance == null) {
            instance = new AuthenticationService();
        }
        return instance;
    }

    private void initializeDefaultUsers() {
        // Default admin user - password: "admin123"
        users.put("admin", new User("admin", hashPassword("admin123"), "ADMIN"));

        // Default regular user - password: "user123"
        users.put("user", new User("user", hashPassword("user123"), "USER"));
    }

    /**
     * Hash password using SHA-256
     */
    private String hashPassword(String password) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(password.getBytes());
            return Base64.getEncoder().encodeToString(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Error hashing password", e);
        }
    }

    /**
     * Authenticate user with username and password
     */
    public boolean authenticate(String username, String password, boolean rememberMe) {
        if (username == null || password == null || username.trim().isEmpty() || password.trim().isEmpty()) {
            return false;
        }

        User user = users.get(username.toLowerCase());
        if (user != null) {
            String hashedPassword = hashPassword(password);
            if (user.getPassword().equals(hashedPassword)) {
                currentUser = user;
                currentUser.setAuthenticated(true);

                // Handle remember me
                if (rememberMe) {
                    saveCredentials(username, hashedPassword);
                } else {
                    clearSavedCredentials();
                }

                return true;
            }
        }
        return false;
    }

    /**
     * Try to auto-login using saved credentials
     */
    public boolean autoLogin() {
        boolean rememberMe = prefs.getBoolean(PREF_REMEMBER_ME, false);
        if (!rememberMe) {
            return false;
        }

        String savedUsername = prefs.get(PREF_USERNAME, null);
        String savedPasswordHash = prefs.get(PREF_PASSWORD_HASH, null);

        if (savedUsername != null && savedPasswordHash != null) {
            User user = users.get(savedUsername.toLowerCase());
            if (user != null && user.getPassword().equals(savedPasswordHash)) {
                currentUser = user;
                currentUser.setAuthenticated(true);
                return true;
            }
        }
        return false;
    }

    /**
     * Save credentials to preferences
     */
    private void saveCredentials(String username, String passwordHash) {
        prefs.put(PREF_USERNAME, username);
        prefs.put(PREF_PASSWORD_HASH, passwordHash);
        prefs.putBoolean(PREF_REMEMBER_ME, true);
    }

    /**
     * Clear saved credentials
     */
    public void clearSavedCredentials() {
        prefs.remove(PREF_USERNAME);
        prefs.remove(PREF_PASSWORD_HASH);
        prefs.putBoolean(PREF_REMEMBER_ME, false);
    }

    /**
     * Get saved username for auto-fill
     */
    public String getSavedUsername() {
        return prefs.get(PREF_USERNAME, "");
    }

    /**
     * Check if remember me was enabled
     */
    public boolean isRememberMeEnabled() {
        return prefs.getBoolean(PREF_REMEMBER_ME, false);
    }

    /**
     * Logout current user
     */
    public void logout() {
        if (currentUser != null) {
            currentUser.setAuthenticated(false);
            currentUser = null;
        }
    }

    /**
     * Get current logged-in user
     */
    public User getCurrentUser() {
        return currentUser;
    }

    /**
     * Check if user is authenticated
     */
    public boolean isAuthenticated() {
        return currentUser != null && currentUser.isAuthenticated();
    }

    /**
     * Register a new user
     */
    public boolean registerUser(String username, String password, String role) {
        if (users.containsKey(username.toLowerCase())) {
            return false;
        }
        User newUser = new User(username, hashPassword(password), role);
        users.put(username.toLowerCase(), newUser);
        return true;
    }
}