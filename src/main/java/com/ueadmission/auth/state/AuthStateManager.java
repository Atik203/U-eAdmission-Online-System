package com.ueadmission.auth.state;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.logging.Logger;

import com.ueadmission.auth.session.SessionManager;
import com.ueadmission.context.ApplicationContext;

/**
 * A global authentication state manager that works like Redux/Zustand
 * Singleton pattern ensures state is maintained across different windows
 */
public class AuthStateManager {
    private static final Logger LOGGER = Logger.getLogger(AuthStateManager.class.getName());
    private static AuthStateManager instance;
    
    // Current auth state
    private AuthState state;
    
    // List of listeners for state changes
    private final List<Consumer<AuthState>> listeners = new ArrayList<>();
    
    // We no longer need the Preferences field as we're using SessionManager
    

    
    // Private constructor (singleton pattern)
    private AuthStateManager() {
        // Get state from the ApplicationContext first if available
        if (ApplicationContext.getInstance().isInitialized() && 
            ApplicationContext.getInstance().getAuthState() != null) {
            this.state = ApplicationContext.getInstance().getAuthState();
            LOGGER.info("Restored auth state from ApplicationContext");
        } else {
            // Otherwise, try to load from persistent session
            AuthState loadedState = SessionManager.loadSession();
            if (loadedState != null && loadedState.isAuthenticated() && !loadedState.isExpired()) {
                this.state = loadedState;
                // Also update the ApplicationContext
                ApplicationContext.getInstance().setAuthState(loadedState);
                LOGGER.info("Restored auth state from persistent session");
            } else {
                // If no valid session, initialize with default state
                this.state = new AuthState();
                ApplicationContext.getInstance().setAuthState(this.state);
                LOGGER.info("Initialized default unauthenticated state");
            }
            
            // Mark context as initialized
            ApplicationContext.getInstance().setInitialized(true);
        }
    }
    
    /**
     * Get the singleton instance of AuthStateManager
     * @return The singleton instance
     */
    public static synchronized AuthStateManager getInstance() {
        if (instance == null) {
            instance = new AuthStateManager();
        }
        return instance;
    }
    
    /**
     * Get the current authentication state
     * @return The current state
     */
    public AuthState getState() {
        return state;
    }
    
    /**
     * Check if user is authenticated
     * @return True if authenticated, false otherwise
     */
    public boolean isAuthenticated() {
        // Check if state is valid and not expired
        if (state.isAuthenticated() && !state.isExpired()) {
            return true;
        }
        
        // If expired, log out and return false
        if (state.isExpired()) {
            logout();
        }
        
        return false;
    }
    
    /**
     * Login a user
     * @param user The user to login
     * @param rememberMe Whether to remember the login
     */
    public void login(User user, boolean rememberMe) {
        // Create auth token (in real app, would be from server)
        String authToken = generateAuthToken();
        
        // Calculate expiry (30 days if remember me, 2 hours otherwise)
        long expiresAt = System.currentTimeMillis() + 
                (rememberMe ? 30L * 24 * 60 * 60 * 1000 : 2 * 60 * 60 * 1000);
        
        // Create new state
        AuthState newState = new AuthState(true, user, authToken, expiresAt);
        
        // Update state in memory
        updateState(newState);
        
        // Always update the ApplicationContext
        ApplicationContext.getInstance().setAuthState(newState);
        
        // Save to persistent storage if remember me
        if (rememberMe) {
            SessionManager.saveSession(newState);
        }
        
            // Extra diagnostic logging
            LOGGER.info("User logged in: " + user.getEmail() + 
                       (rememberMe ? " (with persistent session)" : " (session will expire on app close)"));
            System.out.println("Login completed. Authentication state: " + isAuthenticated());
            System.out.println("User info: " + user.getFirstName() + " " + user.getLastName() + 
                             " (Email: " + user.getEmail() + ", Role: " + user.getRole() + ")");
            
            // Force notification of all subscribers
            notifySubscribers();
        }
        
        /**
         * Force notification of all subscribers
         * This helps ensure UI components are updated when auth state changes
         */
        public void notifySubscribers() {
            // Check if we have listeners registered
            if (listeners != null && !listeners.isEmpty()) {
                // Create a copy to avoid concurrent modification issues
                List<Consumer<AuthState>> currentListeners = new ArrayList<>(listeners);
                for (Consumer<AuthState> listener : currentListeners) {
                    if (listener != null) {
                        try {
                            listener.accept(state);
                        } catch (Exception e) {
                            LOGGER.warning("Error notifying listener: " + e.getMessage());
                        }
                    }
                }
                LOGGER.info("Notified " + currentListeners.size() + " listeners of auth state change");
            } else {
                LOGGER.info("No listeners to notify of auth state change");
            }
    }
    
    /**
     * Logout the current user
     */
    public void logout() {
        // Get the current user ID before resetting state
        int userId = 0;
        if (state != null && state.isAuthenticated() && state.getUser() != null) {
            userId = state.getUser().getId();
        }
        
        // Reset to unauthenticated state
        AuthState newState = new AuthState();
        updateState(newState);
        
        // Update login status in database if we have a valid user ID
        if (userId > 0) {
            try {
                // Update database to mark user as logged out
                com.ueadmission.auth.UserDAO.logoutUser(userId);
                LOGGER.info("Updated user ID " + userId + " login status to logged out in database");
            } catch (Exception e) {
                LOGGER.warning("Failed to update login status in database: " + e.getMessage());
            }
        }
        
        // Update ApplicationContext
        ApplicationContext.getInstance().setAuthState(newState);
        
        // Clear persistent session
        SessionManager.clearSession();
        
        LOGGER.info("User logged out");
    }
    
    /**
     * Subscribe to state changes
     * @param listener The listener to add
     * @return The listener for chaining
     */
    public Consumer<AuthState> subscribe(Consumer<AuthState> listener) {
        listeners.add(listener);
        
        // Immediately notify with current state
        listener.accept(state);
        
        return listener;
    }
    
    /**
     * Unsubscribe from state changes
     * @param listener The listener to remove
     */
    public void unsubscribe(Consumer<AuthState> listener) {
        listeners.remove(listener);
    }
    
    /**
     * Update the authentication state and notify listeners
     * @param newState The new state
     */
    private void updateState(AuthState newState) {
        this.state = newState;
        
        // Notify all listeners
        for (Consumer<AuthState> listener : listeners) {
            try {
                listener.accept(newState);
            } catch (Exception e) {
                LOGGER.warning("Error in auth state listener: " + e.getMessage());
            }
        }
    }
    
    /**
     * Generate a simple auth token
     * In a real app, this would be from your server
     * @return A simple auth token
     */
    private String generateAuthToken() {
        return "auth_" + System.currentTimeMillis() + "_" + Math.random();
    }
    
    /**
     * Check if there's a persistent session available
     * @return true if a session is available, false otherwise
     */
    public boolean hasPersistentSession() {
        return SessionManager.hasActiveSession();
    }
    
    /**
     * Restore session from persistent storage
     * @return true if session was restored successfully, false otherwise
     */
    public boolean restoreSession() {
        AuthState loadedState = SessionManager.loadSession();
        if (loadedState != null && loadedState.isAuthenticated() && !loadedState.isExpired()) {
            // Update state
            updateState(loadedState);
            
            // Also update ApplicationContext
            ApplicationContext.getInstance().setAuthState(loadedState);
            
            LOGGER.info("Session restored successfully for user: " + 
                       loadedState.getUser().getEmail());
            return true;
        }
        return false;
    }
    
    /**
     * Save current session to persistent storage
     * This can be used to force save the session even if not initially requested
     */
    public void saveCurrentSession() {
        if (state.isAuthenticated() && state.getUser() != null) {
            SessionManager.saveSession(state);
            LOGGER.info("Current session saved to persistent storage");
        }
    }
    
    /**
     * Clear the current session from persistent storage
     */
    public void clearPersistentSession() {
        SessionManager.clearSession();
        LOGGER.info("Persistent session cleared");
    }
}
