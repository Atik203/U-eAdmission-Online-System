package com.ueadmission.utils;

import java.util.logging.Logger;

import com.ueadmission.auth.state.AuthState;
import com.ueadmission.components.ProfileButton;

import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.layout.HBox;

/**
 * Utility class for handling UI updates related to authentication state
 */
public class AuthUIUpdater {
    private static final Logger LOGGER = Logger.getLogger(AuthUIUpdater.class.getName());
    
    /**
     * Update containers visibility based on authentication status
     * @param loginContainer Login button container
     * @param profileContainer Profile button container
     * @param isAuthenticated Authentication status
     */
    public static void updateContainersVisibility(HBox loginContainer, HBox profileContainer, boolean isAuthenticated) {
        javafx.application.Platform.runLater(() -> {
            try {
                // Update login container visibility
                if (loginContainer != null) {
                    loginContainer.setVisible(!isAuthenticated);
                    loginContainer.setManaged(!isAuthenticated);
                    LOGGER.fine("Updated login container visibility: " + !isAuthenticated);
                }

                // Update profile container visibility
                if (profileContainer != null) {
                    profileContainer.setVisible(isAuthenticated);
                    profileContainer.setManaged(isAuthenticated);
                    LOGGER.fine("Updated profile container visibility: " + isAuthenticated);
                }
            } catch (Exception e) {
                LOGGER.warning("Error updating containers: " + e.getMessage());
            }
        });
    }
    
    /**
     * Find and update containers by lookup if direct references are null
     * @param scene The scene to search in
     * @param isAuthenticated Authentication status
     */
    public static void findAndUpdateContainers(Scene scene, boolean isAuthenticated) {
        if (scene == null) return;
        
        javafx.application.Platform.runLater(() -> {
            try {
                // Try to find containers by ID
                Node loginContainer = scene.lookup("#loginButtonContainer");
                Node profileContainer = scene.lookup("#profileButtonContainer");
                
                // Update login container
                if (loginContainer != null) {
                    loginContainer.setVisible(!isAuthenticated);
                    loginContainer.setManaged(!isAuthenticated);
                    LOGGER.fine("Found and updated login container by lookup");
                }
                
                // Update profile container
                if (profileContainer != null) {
                    profileContainer.setVisible(isAuthenticated);
                    profileContainer.setManaged(isAuthenticated);
                    LOGGER.fine("Found and updated profile container by lookup");
                    
                    // Look for ProfileButton inside the container
                    if (profileContainer instanceof javafx.scene.Parent) {
                        for (Node child : ((javafx.scene.Parent)profileContainer).getChildrenUnmodifiable()) {
                            if (child instanceof ProfileButton) {
                                ProfileButton profileBtn = (ProfileButton) child;
                                AuthState currentState = com.ueadmission.auth.state.AuthStateManager.getInstance().getState();
                                if (currentState != null) {
                                    profileBtn.updateUIFromAuthState(currentState);
                                    LOGGER.fine("Updated ProfileButton found in container");
                                }
                                break;
                            }
                        }
                    }
                }
                
                // If containers weren't found by ID, try class selectors
                if (loginContainer == null) {
                    for (Node node : scene.getRoot().lookupAll(".mfx-button-login")) {
                        node.setVisible(!isAuthenticated);
                        node.setManaged(!isAuthenticated);
                        LOGGER.fine("Found and updated login button by class selector");
                    }
                }
                
                if (profileContainer == null) {
                    for (Node node : scene.getRoot().lookupAll(".profile-button-container")) {
                        node.setVisible(isAuthenticated);
                        node.setManaged(isAuthenticated);
                        LOGGER.fine("Found and updated profile component by class selector");
                    }
                }
            } catch (Exception e) {
                LOGGER.warning("Error finding and updating containers: " + e.getMessage());
            }
        });
    }
    
    /**
     * Update profile button with current auth state
     * @param profileButton The profile button to update
     * @param state The current auth state
     */
    public static void updateProfileButton(ProfileButton profileButton, AuthState state) {
        if (profileButton == null || state == null) return;
        
        javafx.application.Platform.runLater(() -> {
            try {
                profileButton.updateUIFromAuthState(state);
                LOGGER.fine("Updated profile button with auth state");
            } catch (Exception e) {
                LOGGER.warning("Error updating profile button: " + e.getMessage());
            }
        });
    }
    
    /**
     * Refresh all UI components based on current auth state
     * @param scene The scene containing the UI components
     */
    public static void refreshUI(Scene scene) {
        if (scene == null) return;
        
        // Get current auth state
        AuthState currentState = com.ueadmission.auth.state.AuthStateManager.getInstance().getState();
        boolean isAuthenticated = (currentState != null && currentState.isAuthenticated() 
                                  && currentState.getUser() != null);
        
        // Update containers visibility
        findAndUpdateContainers(scene, isAuthenticated);
    }
}