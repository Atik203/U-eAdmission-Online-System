package com.ueadmission.auth;

import com.ueadmission.auth.state.AuthState;
import com.ueadmission.auth.state.AuthStateManager;
import com.ueadmission.auth.state.User;
import com.ueadmission.context.ApplicationContext;

import java.util.function.Consumer;
import java.util.logging.Logger;

/**
 * Base controller class that handles auth state management
 * All controllers that need access to auth state should extend this class
 */
public abstract class BaseController {
    protected static final Logger LOGGER = Logger.getLogger(BaseController.class.getName());
    
    // Auth state listener
    protected Consumer<AuthState> authStateListener;
    
    /**
     * Initialize the controller with auth state subscription
     */
    public void initialize() {
        // Subscribe to auth state changes
        subscribeToAuthState();
        
        // Call controller-specific initialization
        onInitialize();
    }
    
    /**
     * Controller-specific initialization, to be implemented by subclasses
     */
    protected abstract void onInitialize();
    
    /**
     * Subscribe to auth state changes
     */
    protected void subscribeToAuthState() {
        authStateListener = this::handleAuthStateChange;
        AuthStateManager.getInstance().subscribe(authStateListener);
    }
    
    /**
     * Handle auth state changes
     * Default implementation does nothing, subclasses should override as needed
     * @param state The new auth state
     */
    protected void handleAuthStateChange(AuthState state) {
        // Default implementation does nothing
    }
    
    /**
     * Check if the user is authenticated
     * @return true if authenticated, false otherwise
     */
    protected boolean isAuthenticated() {
        return AuthStateManager.getInstance().isAuthenticated();
    }
    
    /**
     * Get the current authenticated user
     * @return The current user, or null if not authenticated
     */
    protected User getCurrentUser() {
        return ApplicationContext.getInstance().getCurrentUser();
    }
    
    /**
     * Clean up resources when this controller is no longer needed
     * This should be called when switching scenes
     */
    public void cleanup() {
        // Unsubscribe from auth state changes
        if (authStateListener != null) {
            AuthStateManager.getInstance().unsubscribe(authStateListener);
        }
    }
}
