package com.ueadmission.exam;

import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.ueadmission.auth.state.AuthState;
import com.ueadmission.auth.state.AuthStateManager;
import com.ueadmission.components.ProfileButton;
import com.ueadmission.navigation.NavigationUtil;

import io.github.palexdev.materialfx.controls.MFXButton;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.layout.HBox;

/**
 * Controller for the Exam page
 * Handles user interactions and navigation for the Exam screen
 */
public class ExamController {

    private static final Logger LOGGER = Logger.getLogger(ExamController.class.getName());
    private Consumer<AuthState> authStateListener;

    // UI Elements - Navigation
    @FXML private MFXButton homeButton;
    @FXML private MFXButton aboutButton;
    @FXML private MFXButton admissionButton;
    @FXML private MFXButton examPortalButton;
    @FXML private MFXButton contactButton;
    @FXML private MFXButton loginButton;

    // Authentication UI elements
    @FXML private HBox loginButtonContainer;
    @FXML private HBox profileButtonContainer;
    @FXML private ProfileButton profileButton;

    @FXML
    public void initialize() {
        // Configure navigation buttons
        homeButton.setOnAction(event -> navigateToHome(event));
        aboutButton.setOnAction(event -> navigateToAbout(event));
        examPortalButton.setOnAction(event -> navigateToExamPortal(event));
        contactButton.setOnAction(event -> navigateToContact(event));

        // Configure login button if it exists
        if (loginButton != null) {
            loginButton.setOnAction(event -> navigateToLogin(event));
        }

        // Initialize containers by looking them up in the scene
        javafx.application.Platform.runLater(() -> {
            try {
                initializeContainersIfNull();
                LOGGER.info("Initialized containers on startup");

                // After initialization, immediately update UI with current auth state
                refreshUI();
            } catch (Exception e) {
                LOGGER.log(Level.SEVERE, "Error initializing containers during startup: {0}", e.getMessage());
            }
        });

        // Configure admission button with authentication check
        if (admissionButton != null) {
            admissionButton.setOnAction(event -> {
                if (AuthStateManager.getInstance().isAuthenticated()) {
                    navigateToAdmission(event);
                } else {
                    navigateToLogin(event);
                }
            });
        }

        // Subscribe to auth state changes
        subscribeToAuthStateChanges();

        // Refresh UI with current auth state
        refreshUI();

        // Call onSceneActive to ensure UI is updated when scene is shown
        javafx.application.Platform.runLater(this::onSceneActive);
    }

    /**
     * Cleanup method to ensure any transitions are completed and opacity is reset
     * Called before navigating away from the Exam screen
     */
    void cleanup() {
        LOGGER.info("Cleaning up ExamController before navigation");

        // Reset opacity on the scene root if available
        if (homeButton != null && homeButton.getScene() != null && 
                homeButton.getScene().getRoot() != null) {
            homeButton.getScene().getRoot().setOpacity(1.0);
            LOGGER.info("Reset opacity to 1.0 during cleanup");
        }

        // Unsubscribe from auth state changes
        if (authStateListener != null) {
            AuthStateManager.getInstance().unsubscribe(authStateListener);
            LOGGER.info("Unsubscribed from auth state changes during cleanup");
        }
    }

    /**
     * Subscribe to authentication state changes to update UI
     */
    private void subscribeToAuthStateChanges() {
        // Create auth state listener
        authStateListener = newState -> {
            LOGGER.info("Auth state change detected in ExamController");

            boolean isAuthenticated = (newState != null && newState.isAuthenticated() && newState.getUser() != null);
            String userEmail = "none";
            if (isAuthenticated && newState != null && newState.getUser() != null) {
                userEmail = newState.getUser().getEmail();
            }
            LOGGER.log(Level.INFO, "Auth state changed - authenticated: {0}, user: {1}", 
                new Object[]{isAuthenticated, userEmail});

            // Ensure we're on the JavaFX Application Thread for UI updates
            if (!javafx.application.Platform.isFxApplicationThread()) {
                javafx.application.Platform.runLater(() -> {
                    // Update UI based on new state
                    updateContainersVisibility(isAuthenticated);

                    // Update profile button if authenticated
                    if (profileButton != null) {
                        profileButton.updateUIFromAuthState(newState);
                        LOGGER.info("Updated profile button from auth state listener");
                    }
                });
            } else {
                // Update UI based on new state
                updateContainersVisibility(isAuthenticated);

                // Update profile button if authenticated
                if (profileButton != null) {
                    profileButton.updateUIFromAuthState(newState);
                    LOGGER.info("Updated profile button from auth state listener");
                }
            }
        };

        // Subscribe to auth state changes
        AuthStateManager.getInstance().subscribe(authStateListener);
        LOGGER.info("Subscribed to auth state changes in ExamController");

        // Force an initial update
        AuthState currentState = AuthStateManager.getInstance().getState();
        if (currentState != null) {
            authStateListener.accept(currentState);
        }
    }

    /**
     * Called when scene becomes visible or active
     * This ensures the UI is updated with current auth state
     */
    public void onSceneActive() {
        LOGGER.info("Exam scene became active, refreshing auth UI");

        // Check authentication status first
        boolean isAuthenticated = AuthStateManager.getInstance().isAuthenticated();
        LOGGER.log(Level.INFO, "Authentication status on scene activation: {0}", isAuthenticated);

        // Try to initialize containers if they are null - do this first to ensure we have the containers
        try {
            initializeContainersIfNull();
            LOGGER.info("Containers initialized during scene activation");
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Error initializing containers on scene activation: {0}", e.getMessage());
        }

        // Refresh UI with current auth state
        refreshUI();

        // Check authentication as this is a private route
        if (!isAuthenticated) {
            LOGGER.info("User not authenticated on scene activation, redirecting to login");
            javafx.application.Platform.runLater(() -> {
                // Create a dummy ActionEvent with the homeButton as the source
                ActionEvent event = new ActionEvent(homeButton, ActionEvent.NULL_SOURCE_TARGET);
                navigateToLogin(event);
            });
        }
    }

    /**
     * Manually refresh the UI state based on current auth state
     */
    public void refreshUI() {
        LOGGER.info("Refreshing ExamController UI");

        // Get the current auth state
        AuthState currentState = AuthStateManager.getInstance().getState();
        boolean isAuthenticated = (currentState != null && currentState.isAuthenticated()
                && currentState.getUser() != null);

        LOGGER.log(Level.INFO, "Current auth state in refreshUI: {0}", isAuthenticated);

        // Ensure we're on the JavaFX Application Thread for UI updates
        if (!javafx.application.Platform.isFxApplicationThread()) {
            javafx.application.Platform.runLater(() -> {
                updateUIComponents(currentState, isAuthenticated);
            });
        } else {
            updateUIComponents(currentState, isAuthenticated);
        }
    }

    /**
     * Helper method to update UI components based on auth state
     */
    private void updateUIComponents(AuthState currentState, boolean isAuthenticated) {
        try {
            // Update container visibility
            updateContainersVisibility(isAuthenticated);

            // Update profile button if it exists
            if (profileButton != null) {
                profileButton.updateUIFromAuthState(currentState);
                LOGGER.info("Updated profile button in Exam page");
            }
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error updating UI components in ExamController: {0}", e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Update containers visibility based on authentication status
     */
    private void updateContainersVisibility(boolean isAuthenticated) {
        javafx.application.Platform.runLater(() -> {
            try {
                // Try to initialize containers if they are null first to ensure we have the containers
                initializeContainersIfNull();

                // Update login container visibility
                if (loginButtonContainer != null) {
                    loginButtonContainer.setVisible(!isAuthenticated);
                    loginButtonContainer.setManaged(!isAuthenticated);
                    LOGGER.log(Level.INFO, "Login button container visibility set to: {0}", !isAuthenticated);
                }

                // Update profile container visibility
                if (profileButtonContainer != null) {
                    profileButtonContainer.setVisible(isAuthenticated);
                    profileButtonContainer.setManaged(isAuthenticated);
                    LOGGER.log(Level.INFO, "Profile button container visibility set to: {0}", isAuthenticated);
                }

                // Update profile button if we have it
                if (profileButton != null && isAuthenticated) {
                    profileButton.refreshAuthState();
                    LOGGER.info("Refreshed profile button during container update");
                }
            } catch (Exception e) {
                LOGGER.log(Level.SEVERE, "Error updating containers in ExamController: {0}", e.getMessage());
                e.printStackTrace();
            }
        });
    }

    /**
     * Try to initialize the containers if they are null
     */
    private void initializeContainersIfNull() {
        try {
            javafx.scene.Scene scene = getScene();
            if (scene == null) {
                LOGGER.warning("Scene is null, cannot initialize containers");
                return;
            }

            // Find loginButtonContainer if it's null
            if (loginButtonContainer == null) {
                Node node = scene.lookup("#loginButtonContainer");
                if (node instanceof HBox) {
                    loginButtonContainer = (HBox) node;
                    LOGGER.info("Initialized loginButtonContainer via scene lookup: " + node.getId());
                }
            }

            // Find profileButtonContainer if it's null
            if (profileButtonContainer == null) {
                Node node = scene.lookup("#profileButtonContainer");
                if (node instanceof HBox) {
                    profileButtonContainer = (HBox) node;
                    LOGGER.info("Initialized profileButtonContainer via scene lookup: " + node.getId());
                }
            }

            // Try to find profile button if it's null
            if (profileButton == null) {
                Node node = scene.lookup(".profile-button-container");
                if (node instanceof ProfileButton) {
                    profileButton = (ProfileButton) node;
                    LOGGER.info("Initialized profileButton via scene lookup with class");

                    // Initialize the profile button with current auth state
                    profileButton.refreshAuthState();
                }
            }
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error initializing containers: {0}", e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Get the current scene
     * @return The current scene or null if not available
     */
    private javafx.scene.Scene getScene() {
        // Try different UI elements to get the scene
        if (homeButton != null && homeButton.getScene() != null) {
            return homeButton.getScene();
        } else if (loginButton != null && loginButton.getScene() != null) {
            return loginButton.getScene();
        } else if (profileButton != null && profileButton.getScene() != null) {
            return profileButton.getScene();
        }

        // No scene found
        return null;
    }

    /**
     * Navigates to the Home screen
     * @param event The event that triggered this action
     */
    private void navigateToHome(ActionEvent event) {
        cleanup();
        NavigationUtil.navigateToHome(event);
    }

    /**
     * Navigates to the About screen
     * @param event The event that triggered this action
     */
    private void navigateToAbout(ActionEvent event) {
        cleanup();
        NavigationUtil.navigateToAbout(event);
    }

    /**
     * Navigates to the Admission screen
     * @param event The event that triggered this action
     */
    private void navigateToAdmission(ActionEvent event) {
        cleanup();
        NavigationUtil.navigateToAdmission(event);
    }

    /**
     * Navigates to the Exam Portal screen
     * @param event The event that triggered this action
     */
    private void navigateToExamPortal(ActionEvent event) {
        cleanup();
        NavigationUtil.navigateToExamPortal(event);
    }

    /**
     * Navigates to the Contact screen
     * @param event The event that triggered this action
     */
    private void navigateToContact(ActionEvent event) {
        cleanup();
        NavigationUtil.navigateToContact(event);
    }

    /**
     * Navigates to the Login screen
     * @param event The event that triggered this action
     */
    private void navigateToLogin(ActionEvent event) {
        cleanup();
        NavigationUtil.navigateToLogin(event);
    }

    /**
     * Handle mouse click navigation to Home in the footer
     * @param event The mouse event that triggered this action
     */
    @FXML
    public void navigateToHome(javafx.scene.input.MouseEvent event) {
        cleanup();
        NavigationUtil.navigateToHome(event);
    }

    /**
     * Handle mouse click navigation to About in the footer
     * @param event The mouse event that triggered this action
     */
    @FXML
    public void navigateToAbout(javafx.scene.input.MouseEvent event) {
        cleanup();
        NavigationUtil.navigateToAbout(event);
    }

    /**
     * Handle mouse click navigation to Admission in the footer
     * @param event The mouse event that triggered this action
     */
    @FXML
    public void navigateToAdmission(javafx.scene.input.MouseEvent event) {
        // Check authentication before navigating
        if (!AuthStateManager.getInstance().isAuthenticated()) {
            LOGGER.info("User not authenticated, redirecting to login");
            NavigationUtil.navigateToLogin(event);
            return;
        }

        cleanup();
        NavigationUtil.navigateToAdmission(event);
    }

    /**
     * Handle mouse click navigation to Exam Portal in the footer
     * @param event The mouse event that triggered this action
     */
    @FXML
    public void navigateToExamPortal(javafx.scene.input.MouseEvent event) {
        // Check authentication before navigating
        if (!AuthStateManager.getInstance().isAuthenticated()) {
            LOGGER.info("User not authenticated, redirecting to login");
            NavigationUtil.navigateToLogin(event);
            return;
        }

        cleanup();
        NavigationUtil.navigateToExamPortal(event);
    }

    /**
     * Handle mouse click navigation to Contact in the footer
     * @param event The mouse event that triggered this action
     */
    @FXML
    public void navigateToContact(javafx.scene.input.MouseEvent event) {
        cleanup();
        NavigationUtil.navigateToContact(event);
    }
}
