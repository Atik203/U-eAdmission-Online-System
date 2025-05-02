package com.ueadmission;

import java.util.function.Consumer;
import java.util.logging.Logger;

import com.ueadmission.auth.state.AuthState;
import com.ueadmission.auth.state.AuthStateManager;
import com.ueadmission.components.ProfileButton;
import com.ueadmission.navigation.NavigationUtil;

import javafx.event.ActionEvent;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.layout.HBox;

/**
 * Base controller class that provides common functionality for all controllers
 */
public abstract class BaseController {
    protected static final Logger LOGGER = Logger.getLogger(BaseController.class.getName());
    protected Consumer<AuthState> authStateListener;

    /**
     * Navigates to the Home screen with cleanup
     * @param event The event that triggered this action
     */
    protected void navigateToHome(ActionEvent event) {
        cleanup();
        NavigationUtil.navigateToHome(event);
    }
    
    /**
     * Navigates to the About screen with cleanup
     * @param event The event that triggered this action
     */
    protected void navigateToAbout(ActionEvent event) {
        cleanup();
        NavigationUtil.navigateToAbout(event);
    }
    
    /**
     * Navigates to the Admission screen with cleanup
     * @param event The event that triggered this action
     */
    protected void navigateToAdmission(ActionEvent event) {
        cleanup();
        NavigationUtil.navigateToAdmission(event);
    }
    
    /**
     * Navigates to the Login screen with cleanup
     * @param event The event that triggered this action
     */
    protected void navigateToLogin(ActionEvent event) {
        cleanup();
        NavigationUtil.navigateToLogin(event);
    }
    
    /**
     * Navigates to the Registration screen with cleanup
     * @param event The event that triggered this action
     */
    protected void navigateToRegistration(ActionEvent event) {
        cleanup();
        NavigationUtil.navigateToRegistration(event);
    }
    
    /**
     * Navigates to the Profile screen with cleanup
     * @param event The event that triggered this action
     */
    protected void navigateToProfile(ActionEvent event) {
        cleanup();
        NavigationUtil.navigateToProfile(event);
    }
    
    /**
     * Opens a URL in the default browser
     * @param url The URL to open
     */
    protected void openWebsite(String url) {
        try {
            java.awt.Desktop.getDesktop().browse(new java.net.URI(url));
        } catch (Exception e) {
            LOGGER.warning("Failed to open URL: " + e.getMessage());
        }
    }
    
    /**
     * Update containers visibility based on authentication status
     * @param loginButtonContainer The login button container
     * @param profileButtonContainer The profile button container
     * @param isAuthenticated Whether the user is authenticated
     */
    protected void updateContainersVisibility(HBox loginButtonContainer, HBox profileButtonContainer, boolean isAuthenticated) {
        javafx.application.Platform.runLater(() -> {
            try {
                // Update login container visibility
                if (loginButtonContainer != null) {
                    loginButtonContainer.setVisible(!isAuthenticated);
                    loginButtonContainer.setManaged(!isAuthenticated);
                    LOGGER.fine("Updated login container visibility: " + !isAuthenticated);
                }

                // Update profile container visibility
                if (profileButtonContainer != null) {
                    profileButtonContainer.setVisible(isAuthenticated);
                    profileButtonContainer.setManaged(isAuthenticated);
                    LOGGER.fine("Updated profile container visibility: " + isAuthenticated);
                }
            } catch (Exception e) {
                LOGGER.warning("Error updating containers: " + e.getMessage());
            }
        });
    }
    
    /**
     * Find a node in the scene by ID and class type
     * @param scene The scene to search in
     * @param id The ID of the node
     * @param type The class type of the node
     * @return The node if found, null otherwise
     */
    protected <T extends Node> T findNodeById(Scene scene, String id, Class<T> type) {
        if (scene == null) return null;
        
        Node node = scene.lookup("#" + id);
        if (node != null && type.isInstance(node)) {
            return type.cast(node);
        }
        return null;
    }
    
    /**
     * Update the profile button with the current auth state
     * @param profileButton The profile button to update
     * @param currentState The current auth state
     */
    protected void updateProfileButton(ProfileButton profileButton, AuthState currentState) {
        if (profileButton != null && currentState != null) {
            javafx.application.Platform.runLater(() -> {
                profileButton.updateUIFromAuthState(currentState);
            });
        }
    }
    
    /**
     * Cleanup resources before navigating away
     * Override this method in subclasses to provide specific cleanup behavior
     */
    protected void cleanup() {
        // Reset opacity on the scene root if available
        try {
            Scene scene = getScene();
            if (scene != null && scene.getRoot() != null) {
                scene.getRoot().setOpacity(1.0);
            }
        } catch (Exception e) {
            LOGGER.warning("Error during cleanup: " + e.getMessage());
        }
        
        // Unsubscribe from auth state changes
        if (authStateListener != null) {
            AuthStateManager.getInstance().unsubscribe(authStateListener);
            LOGGER.info("Unsubscribed from auth state changes during cleanup");
        }
    }
    
    /**
     * Get the scene associated with this controller
     * Override this method in subclasses to provide the correct scene
     * @return The scene or null if not available
     */
    protected Scene getScene() {
        return null;
    }
    
    /**
     * Subscribe to auth state changes
     * @param profileButton The profile button to update
     * @param loginButtonContainer The login button container
     * @param profileButtonContainer The profile button container
     */
    protected void subscribeToAuthStateChanges(ProfileButton profileButton, 
                                              HBox loginButtonContainer, 
                                              HBox profileButtonContainer) {
        // Create auth state listener
        authStateListener = newState -> {
            LOGGER.fine("Auth state change detected");

            boolean isAuthenticated = (newState != null && newState.isAuthenticated() && newState.getUser() != null);

            // Ensure we're on the JavaFX Application Thread for UI updates
            if (!javafx.application.Platform.isFxApplicationThread()) {
                javafx.application.Platform.runLater(() -> {
                    // Update UI based on new state
                    updateContainersVisibility(loginButtonContainer, profileButtonContainer, isAuthenticated);

                    // Update profile button if authenticated
                    if (isAuthenticated && profileButton != null) {
                        profileButton.updateUIFromAuthState(newState);
                        LOGGER.fine("Updated profile button from auth state listener");
                    }
                });
            } else {
                // Update UI based on new state
                updateContainersVisibility(loginButtonContainer, profileButtonContainer, isAuthenticated);

                // Update profile button if authenticated
                if (isAuthenticated && profileButton != null) {
                    profileButton.updateUIFromAuthState(newState);
                    LOGGER.fine("Updated profile button from auth state listener");
                }
            }
        };

        // Subscribe to auth state changes
        AuthStateManager.getInstance().subscribe(authStateListener);
        LOGGER.info("Subscribed to auth state changes");

        // Force an initial update
        AuthState currentState = AuthStateManager.getInstance().getState();
        if (currentState != null) {
            authStateListener.accept(currentState);
        }
    }
}