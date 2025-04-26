package com.ueadmission.context;

import com.ueadmission.auth.state.AuthState;
import com.ueadmission.auth.state.User;

/**
 * ApplicationContext provides a central place to store application-wide state.
 * This class is a singleton and is accessible from any part of the application.
 */
public class ApplicationContext {
    private static ApplicationContext instance;
    
    // Application-wide state
    private AuthState authState;
    private boolean initialized = false;
    
    // Private constructor to enforce singleton pattern
    private ApplicationContext() {
        // Initialize with empty auth state
        authState = new AuthState();
    }
    
    /**
     * Get the singleton instance of ApplicationContext
     * @return The singleton instance
     */
    public static synchronized ApplicationContext getInstance() {
        if (instance == null) {
            instance = new ApplicationContext();
        }
        return instance;
    }
    
    /**
     * Check if the application context has been initialized
     * @return true if initialized, false otherwise
     */
    public boolean isInitialized() {
        return initialized;
    }
    
    /**
     * Mark the application context as initialized
     */
    public void setInitialized(boolean initialized) {
        this.initialized = initialized;
    }
    
    /**
     * Get the current auth state
     * @return The current auth state
     */
    public AuthState getAuthState() {
        return authState;
    }
    
    /**
     * Set the current auth state
     * @param authState The new auth state
     */
    public void setAuthState(AuthState authState) {
        this.authState = authState;
    }
    
    /**
     * Check if a user is currently authenticated
     * @return true if a user is authenticated, false otherwise
     */
    public boolean isAuthenticated() {
        return authState != null && authState.isAuthenticated() && 
               authState.getUser() != null && !authState.isExpired();
    }
    
    /**
     * Get the current authenticated user
     * @return The current user, or null if not authenticated
     */
    public User getCurrentUser() {
        return isAuthenticated() ? authState.getUser() : null;
    }
    
    /**
     * Reset the application context
     * This is useful for testing or when completely resetting the application state
     */
    public static void reset() {
        instance = null;
    }
}
