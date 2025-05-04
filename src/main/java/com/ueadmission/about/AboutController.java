package com.ueadmission.about;
import java.awt.Desktop;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.function.Consumer;
import java.util.logging.Logger;

import com.ueadmission.auth.state.AuthState;
import com.ueadmission.auth.state.AuthStateManager;
import com.ueadmission.components.ProfileButton;

import io.github.palexdev.materialfx.controls.MFXButton;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;

public class AboutController {
    private static final Logger LOGGER = Logger.getLogger(AboutController.class.getName());
    private Consumer<AuthState> authStateListener;

    @FXML
    private ImageView campusImage;

    @FXML
    private MFXButton visitWebsiteBtn;

    @FXML
    private MFXButton homeButton;

    @FXML
    private MFXButton loginButton;

    @FXML
    private MFXButton admissionButton;

    @FXML
    private MFXButton mockTestButton;

    @FXML
    private MFXButton contactButton;

    @FXML
    private HBox loginButtonContainer;

    @FXML
    private HBox profileButtonContainer;

    @FXML
    private ProfileButton profileButton;

    @FXML
    private void initialize() {
        // Set up the button action to open the UIU website
        visitWebsiteBtn.setOnAction(event -> openWebsite("https://www.uiu.ac.bd/"));

        // Configure navigation buttons
        homeButton.setOnAction(event -> navigateToHome(event));

        // Configure login button if it exists
        if (loginButton != null) {
            loginButton.setOnAction(event -> navigateToLogin(event));
        }

        // Configure other navigation buttons if they exist
        if (admissionButton != null) {
            admissionButton.setOnAction(event -> {
                if (AuthStateManager.getInstance().isAuthenticated()) {
                    navigateToAdmission(event);
                } else {
                    // Redirect to login page if not authenticated
                    navigateToLogin(event);
                }
            });
        }

        if (mockTestButton != null) {
            mockTestButton.setOnAction(event -> System.out.println("Mock Test button clicked"));
        }

        if (contactButton != null) {
            contactButton.setOnAction(event -> navigateToContact(event));
        }

        // Subscribe to auth state changes
        subscribeToAuthStateChanges();

        // Refresh UI with current auth state
        refreshUI();

        // Call onSceneActive to ensure UI is updated when scene is shown
        javafx.application.Platform.runLater(this::onSceneActive);
    }

    /**
     * Setup profile button click action
     */
    private void setupProfileButtonAction() {
        // The ProfileButton already has its internal click handlers
        // No need to set an action explicitly, as it's handled internally
        // in the ProfileButton.handleProfileClick() method
        
        // Call this method in initialize() to ensure the ProfileButton is properly set up
    }

    /**
     * Cleanup method to ensure any transitions are completed and opacity is reset
     * Called before navigating away from the About screen
     */
    private void cleanup() {
        LOGGER.info("Cleaning up AboutController before navigation");

        // Reset opacity on the scene root if available
        if (visitWebsiteBtn != null && visitWebsiteBtn.getScene() != null &&
                visitWebsiteBtn.getScene().getRoot() != null) {
            visitWebsiteBtn.getScene().getRoot().setOpacity(1.0);
            LOGGER.info("Reset opacity to 1.0 during cleanup");
        }

        // Unsubscribe from auth state changes
        if (authStateListener != null) {
            AuthStateManager.getInstance().unsubscribe(authStateListener);
            LOGGER.info("Unsubscribed from auth state changes during cleanup");
        }
    }

    private void subscribeToAuthStateChanges() {
        // Create auth state listener
        authStateListener = newState -> {
            LOGGER.info("Auth state change detected in AboutController");

            boolean isAuthenticated = (newState != null && newState.isAuthenticated() && newState.getUser() != null);

            // Ensure we're on the JavaFX Application Thread for UI updates
            if (!javafx.application.Platform.isFxApplicationThread()) {
                javafx.application.Platform.runLater(() -> {
                    // Update UI based on new state
                    updateContainersVisibility(isAuthenticated);

                    // Update profile button if authenticated
                    if (isAuthenticated && profileButton != null) {
                        profileButton.updateUIFromAuthState(newState);
                        LOGGER.info("Updated profile button from auth state listener");
                    }
                });
            } else {
                // Update UI based on new state
                updateContainersVisibility(isAuthenticated);

                // Update profile button if authenticated
                if (isAuthenticated && profileButton != null) {
                    profileButton.updateUIFromAuthState(newState);
                    LOGGER.info("Updated profile button from auth state listener");
                }
            }
        };

        // Subscribe to auth state changes
        AuthStateManager.getInstance().subscribe(authStateListener);
        LOGGER.info("Subscribed to auth state changes in AboutController");

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
        LOGGER.info("About scene became active, refreshing auth UI");
        refreshUI();
    }

    /**
     * Manually refresh the UI state based on current auth state
     */
    public void refreshUI() {
        System.out.println("Refreshing AboutController UI");

        // Get the current auth state
        AuthState currentState = AuthStateManager.getInstance().getState();
        boolean isAuthenticated = (currentState != null && currentState.isAuthenticated()
                && currentState.getUser() != null);

        System.out.println("Current auth state in About refreshUI: " + isAuthenticated);

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
                System.out.println("Updated profile button in About page");
            } else {
                System.out.println("Profile button is null in AboutController");

                // Try to find profile button by lookup
                if (visitWebsiteBtn != null && visitWebsiteBtn.getScene() != null) {
                    Node foundProfileButton = visitWebsiteBtn.getScene().lookup(".profile-button-container");
                    if (foundProfileButton instanceof ProfileButton) {
                        System.out.println("Found profile button by lookup in scene");
                        ((ProfileButton) foundProfileButton).updateUIFromAuthState(currentState);
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("Error updating UI components in AboutController: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Update containers visibility based on authentication status
     */
    private void updateContainersVisibility(boolean isAuthenticated) {
        javafx.application.Platform.runLater(() -> {
            try {
                // Update login container visibility
                if (loginButtonContainer != null) {
                    loginButtonContainer.setVisible(!isAuthenticated);
                    loginButtonContainer.setManaged(!isAuthenticated);
                    System.out.println("About page: Updated login container visibility: " + !isAuthenticated);
                }

                // Update profile container visibility
                if (profileButtonContainer != null) {
                    profileButtonContainer.setVisible(isAuthenticated);
                    profileButtonContainer.setManaged(isAuthenticated);
                    System.out.println("About page: Updated profile container visibility: " + isAuthenticated);
                }

                // If containers are null, try to find by ID
                if ((loginButtonContainer == null || profileButtonContainer == null) &&
                        visitWebsiteBtn != null && visitWebsiteBtn.getScene() != null) {

                    if (loginButtonContainer == null) {
                        Node loginContainer = visitWebsiteBtn.getScene().lookup("#loginButtonContainer");
                        if (loginContainer != null) {
                            loginContainer.setVisible(!isAuthenticated);
                            loginContainer.setManaged(!isAuthenticated);
                            System.out.println("Found and updated login container by lookup");
                        }
                    }

                    if (profileButtonContainer == null) {
                        Node profileContainer = visitWebsiteBtn.getScene().lookup("#profileButtonContainer");
                        if (profileContainer != null) {
                            profileContainer.setVisible(isAuthenticated);
                            profileContainer.setManaged(isAuthenticated);
                            System.out.println("Found and updated profile container by lookup");
                        }
                    }
                }
            } catch (Exception e) {
                System.err.println("Error updating containers in About page: " + e.getMessage());
            }
        });
    }

    /**
     * Opens the specified URL in the default browser
     * @param url The URL to open
     */
    private void openWebsite(String url) {
        try {
            Desktop.getDesktop().browse(new URI(url));
        } catch (IOException | URISyntaxException e) {
            e.printStackTrace();
            System.err.println("Failed to open website: " + e.getMessage());
        }
    }

    /**
     * Navigates to the home page with transition effects
     * @param event The event that triggered this action
     */
    private void navigateToHome(javafx.event.ActionEvent event) {
        // Call cleanup first to ensure proper resource disposal
        cleanup();
        com.ueadmission.navigation.NavigationUtil.navigateToHome(event);
    }

    /**
     * Navigates to the Admission page with transition effects
     * @param event The event that triggered this action
     */
    private void navigateToAdmission(javafx.event.ActionEvent event) {
        // Call cleanup first to ensure proper resource disposal
        cleanup();
        com.ueadmission.navigation.NavigationUtil.navigateToAdmission(event);
    }

    /**
     * Navigates to the login page
     * @param event The event that triggered this action
     */
    private void navigateToLogin(javafx.event.ActionEvent event) {
        // Call cleanup first to ensure proper resource disposal
        cleanup();
        com.ueadmission.navigation.NavigationUtil.navigateToLogin(event);
    }

    /**
     * Navigates to the Contact page
     * @param event The event that triggered this action
     */
    private void navigateToContact(javafx.event.ActionEvent event) {
        // Call cleanup first to ensure proper resource disposal
        cleanup();
        com.ueadmission.navigation.NavigationUtil.navigateToContact(event);
    }

    /**
     * Navigates to the Profile page with transition effects
     * @param event The event that triggered this action
     */
    private void navigateToProfile(javafx.event.ActionEvent event) {
        // Call cleanup first to ensure proper resource disposal
        cleanup();
        com.ueadmission.navigation.NavigationUtil.navigateToProfile(event);
    }
}
