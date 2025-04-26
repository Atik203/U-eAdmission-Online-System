package com.ueadmission.auth.session;

import com.ueadmission.auth.state.AuthState;
import com.ueadmission.auth.state.User;
import com.ueadmission.context.ApplicationContext;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.Preferences;

/**
 * SessionManager handles persistence of user session across application restarts
 * It uses both Java Preferences API and file-based storage for redundancy
 */
public class SessionManager {
    private static final Logger LOGGER = Logger.getLogger(SessionManager.class.getName());
    private static final String SESSION_FILE_NAME = "session.dat";
    private static final String SESSION_DIR = System.getProperty("user.home") + File.separator + ".ueadmission";
    
    // Preferences store
    private static final Preferences prefs = Preferences.userNodeForPackage(SessionManager.class);
    
    /**
     * Save the current session to both preferences and file
     * @param authState The auth state to save
     */
    public static void saveSession(AuthState authState) {
        if (authState == null || !authState.isAuthenticated() || authState.getUser() == null) {
            clearSession();
            return;
        }
        
        // Save to preferences
        saveToPreferences(authState);
        
        // Save to file
        saveToFile(authState);
        
        LOGGER.info("Session saved successfully");
    }
    
    /**
     * Load session from either preferences or file (whichever is valid)
     * @return The loaded auth state, or null if no valid session exists
     */
    public static AuthState loadSession() {
        // Try to load from preferences first
        AuthState state = loadFromPreferences();
        
        // If preferences failed, try file
        if (state == null || !state.isAuthenticated() || state.getUser() == null) {
            state = loadFromFile();
        }
        
        // If we have a valid state, check if it's expired
        if (state != null && state.isAuthenticated() && state.isExpired()) {
            LOGGER.info("Session expired, clearing session data");
            clearSession();
            return null;
        }
        
        return state;
    }
    
    /**
     * Check if there's an active session
     * @return true if there is an active session, false otherwise
     */
    public static boolean hasActiveSession() {
        AuthState state = loadSession();
        return state != null && state.isAuthenticated() && !state.isExpired();
    }
    
    /**
     * Clear all session data
     */
    public static void clearSession() {
        // Clear preferences
        try {
            prefs.clear();
            prefs.flush();
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Error clearing preferences: " + e.getMessage(), e);
        }
        
        // Delete session file
        try {
            Path sessionFile = Paths.get(SESSION_DIR, SESSION_FILE_NAME);
            Files.deleteIfExists(sessionFile);
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Error deleting session file: " + e.getMessage(), e);
        }
        
        LOGGER.info("Session cleared");
    }
    
    /**
     * Save auth state to preferences
     */
    private static void saveToPreferences(AuthState authState) {
        try {
            prefs.putBoolean("authenticated", authState.isAuthenticated());
            
            if (authState.isAuthenticated() && authState.getUser() != null) {
                User user = authState.getUser();
                prefs.putInt("user_id", user.getId());
                prefs.put("user_firstName", user.getFirstName());
                prefs.put("user_lastName", user.getLastName());
                prefs.put("user_email", user.getEmail());
                prefs.put("user_phone", user.getPhoneNumber());
                prefs.put("user_role", user.getRole());
                prefs.put("authToken", authState.getAuthToken());
                prefs.putLong("expiresAt", authState.getExpiresAt());
                
                prefs.flush();
                LOGGER.info("Auth state saved to preferences");
            }
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Failed to save auth state to preferences: " + e.getMessage(), e);
        }
    }
    
    /**
     * Load auth state from preferences
     */
    private static AuthState loadFromPreferences() {
        try {
            boolean authenticated = prefs.getBoolean("authenticated", false);
            
            if (authenticated) {
                int id = prefs.getInt("user_id", 0);
                String firstName = prefs.get("user_firstName", "");
                String lastName = prefs.get("user_lastName", "");
                String email = prefs.get("user_email", "");
                String phone = prefs.get("user_phone", "");
                String role = prefs.get("user_role", "");
                String authToken = prefs.get("authToken", "");
                long expiresAt = prefs.getLong("expiresAt", 0);
                
                User user = new User(id, firstName, lastName, email, phone, role);
                AuthState authState = new AuthState(true, user, authToken, expiresAt);
                
                LOGGER.info("Auth state loaded from preferences for user: " + email);
                return authState;
            }
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Failed to load auth state from preferences: " + e.getMessage(), e);
        }
        
        return null;
    }
    
    /**
     * Save auth state to file
     */
    private static void saveToFile(AuthState authState) {
        try {
            // Ensure directory exists
            Path sessionDir = Paths.get(SESSION_DIR);
            Files.createDirectories(sessionDir);
            
            // Create session file
            Path sessionFile = Paths.get(SESSION_DIR, SESSION_FILE_NAME);
            
            // Serialize auth state
            try (ObjectOutputStream oos = new ObjectOutputStream(
                    new FileOutputStream(sessionFile.toFile()))) {
                oos.writeObject(authState);
                LOGGER.info("Auth state saved to file: " + sessionFile);
            }
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Failed to save auth state to file: " + e.getMessage(), e);
        }
    }
    
    /**
     * Load auth state from file
     */
    private static AuthState loadFromFile() {
        Path sessionFile = Paths.get(SESSION_DIR, SESSION_FILE_NAME);
        
        if (Files.exists(sessionFile)) {
            try (ObjectInputStream ois = new ObjectInputStream(
                    new FileInputStream(sessionFile.toFile()))) {
                AuthState authState = (AuthState) ois.readObject();
                LOGGER.info("Auth state loaded from file for user: " + 
                           (authState.getUser() != null ? authState.getUser().getEmail() : "unknown"));
                return authState;
            } catch (Exception e) {
                LOGGER.log(Level.WARNING, "Failed to load auth state from file: " + e.getMessage(), e);
            }
        }
        
        return null;
    }
}
