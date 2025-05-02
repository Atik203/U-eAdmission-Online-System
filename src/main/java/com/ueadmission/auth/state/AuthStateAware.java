package com.ueadmission.auth.state;

/**
 * Interface for controllers that can handle authentication state changes
 */
public interface AuthStateAware {
    /**
     * Refresh the UI based on the current authentication state
     */
    void refreshUI();
    
    /**
     * Update UI components when authentication state changes
     * @param authState The new authentication state
     */
    void updateAuthUI(AuthState authState);
    
    /**
     * Called when the controller's scene becomes active
     */
    void onSceneActive();
}