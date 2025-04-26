package com.ueadmission;

import com.ueadmission.about.About;
import com.ueadmission.auth.Auth;
import com.ueadmission.auth.state.AuthState;
import com.ueadmission.auth.state.AuthStateManager;
import io.github.palexdev.materialfx.controls.MFXButton;
import javafx.animation.FadeTransition;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Duration;

public class MainController {
    @FXML
    private Label welcomeText;
    
    @FXML
    private MFXButton aboutButton;
    
    @FXML
    private MFXButton learnMoreBtn;
    
    @FXML
    private MFXButton loginButton;
    
    @FXML
    private MFXButton applyNowBtn; // "Apply Now" button in the hero section
    
    @FXML
    private MFXButton learnMoreAboutUIUBtn; // "Learn More About UIU" button in the about section
    
    @FXML
    private javafx.scene.layout.HBox loginButtonContainer; // Container for login button
    
    @FXML
    private javafx.scene.layout.HBox profileButtonContainer; // Container for profile button
    
    @FXML
    private com.ueadmission.components.ProfileButton profileButton; // Profile button component
    
    @FXML
    private VBox mainContainer; // Main container VBox defined in main.fxml
    
    @FXML
    private void initialize() {
        // Set up the About button click action
        aboutButton.setOnAction(event -> openAboutPage(event));
        
        // Set up the Learn More button to also open the About window
        learnMoreBtn.setOnAction(event -> openAboutPage(event));
        
        // Set up the Login button click action
        loginButton.setOnAction(event -> openLoginPage(event));
        
        // Set up the Apply Now button to open login page
        if (applyNowBtn != null) {
            applyNowBtn.setOnAction(event -> openLoginPage(event));
        }
        
        // Set up the Learn More About UIU button to open about page
        if (learnMoreAboutUIUBtn != null) {
            learnMoreAboutUIUBtn.setOnAction(event -> openAboutPage(event));
        }
        
        // Check and update UI based on authentication state
        javafx.application.Platform.runLater(() -> {
            AuthState state = AuthStateManager.getInstance().getState();
            boolean isAuthenticated = AuthStateManager.getInstance().isAuthenticated();
            
            System.out.println("Initial auth state in MainController: " + isAuthenticated);
            
            // Update UI based on auth state
            updateContainersVisibility(isAuthenticated);
            
            if (isAuthenticated && profileButton != null) {
                profileButton.updateUIFromAuthState(state);
            }
        });
        
        // Listen for auth state changes
        AuthStateManager.getInstance().subscribe(newState -> {
            boolean isAuth = (newState != null && newState.isAuthenticated());
            System.out.println("Auth state changed in MainController: " + isAuth);
            updateContainersVisibility(isAuth);
            
            if (isAuth && profileButton != null) {
                profileButton.updateUIFromAuthState(newState);
            }
        });
    }
    
    /**
     * Opens the About page
     * @param event The event that triggered this action
     */
    private void openAboutPage(javafx.event.ActionEvent event) {
        try {
            // Get current stage and its properties
            Stage currentStage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            double width = currentStage.getWidth();
            double height = currentStage.getHeight();
            double x = currentStage.getX();
            double y = currentStage.getY();
            boolean maximized = currentStage.isMaximized();
            
            // Prepare the About window before closing current one
            Stage aboutStage = About.prepareAboutWindow(width, height, x, y, maximized);
            
            // Make the new stage ready but not visible yet
            aboutStage.setOpacity(0.0);
            aboutStage.show();
            
            // Use a fade transition for the new window
            javafx.animation.FadeTransition fadeIn = new javafx.animation.FadeTransition(
                    javafx.util.Duration.millis(300), aboutStage.getScene().getRoot());
            fadeIn.setFromValue(0.0);
            fadeIn.setToValue(1.0);
            
            // Add a fade out transition for the current window
            javafx.animation.FadeTransition fadeOut = new javafx.animation.FadeTransition(
                    javafx.util.Duration.millis(200), currentStage.getScene().getRoot());
            fadeOut.setFromValue(1.0);
            fadeOut.setToValue(0.0);
            
            // Start the fade out, then hide current stage when done
            fadeOut.setOnFinished(e -> {
                currentStage.hide();
                aboutStage.setOpacity(1.0);
                fadeIn.play();
                
                // Finally close the original stage after transition completes
                fadeIn.setOnFinished(f -> currentStage.close());
            });
            
            fadeOut.play();
            
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Failed to navigate to about: " + e.getMessage());
        }
    }
    
    /**
     * Opens the Login page
     * @param event The event that triggered this action
     */
    private void openLoginPage(javafx.event.ActionEvent event) {
        try {
            // Get current stage and its properties
            Stage currentStage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            double width = currentStage.getWidth();
            double height = currentStage.getHeight();
            double x = currentStage.getX();
            double y = currentStage.getY();
            boolean maximized = currentStage.isMaximized();
            
            // Prepare the Login window before closing current one
            Stage loginStage = Auth.prepareLoginWindow(width, height, x, y, maximized);
            
            // Check if loginStage is null before proceeding
            if (loginStage == null) {
                System.err.println("Failed to create login stage. Login window couldn't be prepared.");
                return;
            }
            
            // Make the new stage ready but not visible yet
            loginStage.setOpacity(0.0);
            loginStage.show();
            
            // Use a fade transition for the new window
            javafx.animation.FadeTransition fadeIn = new javafx.animation.FadeTransition(
                    javafx.util.Duration.millis(300), loginStage.getScene().getRoot());
            fadeIn.setFromValue(0.0);
            fadeIn.setToValue(1.0);
            
            // Add a fade out transition for the current window
            javafx.animation.FadeTransition fadeOut = new javafx.animation.FadeTransition(
                    javafx.util.Duration.millis(200), currentStage.getScene().getRoot());
            fadeOut.setFromValue(1.0);
            fadeOut.setToValue(0.0);
            
            // Start the fade out, then hide current stage when done
            fadeOut.setOnFinished(e -> {
                currentStage.hide();
                loginStage.setOpacity(1.0);
                fadeIn.play();
                
                // Finally close the original stage after transition completes
                fadeIn.setOnFinished(f -> currentStage.close());
            });
            
            fadeOut.play();
            
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Failed to navigate to login: " + e.getMessage());
        }
    }

    @FXML
    protected void onHelloButtonClick() {
        welcomeText.setText("Welcome to JavaFX Application!");
    }
    
    /**
     * Helper method to find a button by its ID in the scene graph
     * @param id The ID of the button to find
     * @return The found button or null if not found
     */
    private MFXButton findButtonById(String id) {
        try {
            return (MFXButton) welcomeText.getScene().lookup("#" + id);
        } catch (Exception e) {
            System.err.println("Could not find button with ID: " + id);
            return null;
        }
    }
    
    /**
     * Manually refresh the UI state
     * This can be called from anywhere to force UI to update based on current auth state
     */
    public void refreshUI() {
        System.out.println("Manually refreshing MainController UI");
        
        // Get the current auth state
        AuthState currentState = AuthStateManager.getInstance().getState();
        boolean isAuthenticated = (currentState != null && currentState.isAuthenticated() 
                                  && currentState.getUser() != null);
        
        System.out.println("Current auth state in refreshUI: " + isAuthenticated);
        
        // Initialize containers first to ensure they're not null
        javafx.application.Platform.runLater(this::initializeContainersIfNull);
        
        // We need to use JavaFX Platform thread for UI updates
        javafx.application.Platform.runLater(() -> {
            try {
                // Update container visibility
                updateContainersVisibility(isAuthenticated);
                
                // Update profile button if it exists
                if (isAuthenticated && profileButton != null) {
                    profileButton.updateUIFromAuthState(currentState);
                    System.out.println("Updated profile button in MainController refreshUI");
                } else if (isAuthenticated) {
                    // Try to find and update profile button by lookup
                    updateProfileButton(currentState);
                    System.out.println("Tried to update profile button via lookup in refreshUI");
                }
                
            } catch (Exception e) {
                System.err.println("Error refreshing UI in MainController: " + e.getMessage());
                e.printStackTrace();
            }
        });
        
        System.out.println("UI refresh completed based on auth state: " + isAuthenticated);
    }
    
    /**
     * Update containers visibility based on authentication status
     */
    private void updateContainersVisibility(boolean isAuthenticated) {
        javafx.application.Platform.runLater(() -> {
            try {
                // Initialize containers before updating visibility
                initializeContainersIfNull();
                
                // Update login container visibility
                if (loginButtonContainer != null) {
                    loginButtonContainer.setVisible(!isAuthenticated);
                    loginButtonContainer.setManaged(!isAuthenticated);
                    LOGGER.info("Updated login container visibility: " + !isAuthenticated);
                } else {
                    // Try direct scene lookup as a last resort
                    try {
                        Node loginContainer = null;
                        if (welcomeText != null && welcomeText.getScene() != null) {
                            loginContainer = welcomeText.getScene().lookup("#loginButtonContainer");
                        } else if (aboutButton != null && aboutButton.getScene() != null) {
                            loginContainer = aboutButton.getScene().lookup("#loginButtonContainer");
                        } else if (mainContainer != null) {
                            loginContainer = mainContainer.lookup("#loginButtonContainer");
                        }
                        
                        if (loginContainer != null) {
                            loginContainer.setVisible(!isAuthenticated);
                            loginContainer.setManaged(!isAuthenticated);
                            LOGGER.info("Updated login container visibility via direct lookup: " + !isAuthenticated);
                        } else {
                            LOGGER.warning("loginButtonContainer is still null, can't update visibility");
                        }
                    } catch (Exception e) {
                        LOGGER.warning("Failed to find loginButtonContainer: " + e.getMessage());
                    }
                }
                
                // Update profile container visibility
                if (profileButtonContainer != null) {
                    profileButtonContainer.setVisible(isAuthenticated);
                    profileButtonContainer.setManaged(isAuthenticated);
                    LOGGER.info("Updated profile container visibility: " + isAuthenticated);
                } else {
                    // Try direct scene lookup as a last resort
                    try {
                        Node profileContainer = null;
                        if (welcomeText != null && welcomeText.getScene() != null) {
                            profileContainer = welcomeText.getScene().lookup("#profileButtonContainer");
                        } else if (aboutButton != null && aboutButton.getScene() != null) {
                            profileContainer = aboutButton.getScene().lookup("#profileButtonContainer");
                        } else if (mainContainer != null) {
                            profileContainer = mainContainer.lookup("#profileButtonContainer");
                        }
                        
                        if (profileContainer != null) {
                            profileContainer.setVisible(isAuthenticated);
                            profileContainer.setManaged(isAuthenticated);
                            LOGGER.info("Updated profile container visibility via direct lookup: " + isAuthenticated);
                        } else {
                            LOGGER.warning("profileButtonContainer is still null, can't update visibility");
                        }
                    } catch (Exception e) {
                        LOGGER.warning("Failed to find profileButtonContainer: " + e.getMessage());
                    }
                }
            } catch (Exception e) {
                LOGGER.severe("Error updating containers: " + e.getMessage());
                e.printStackTrace();
            }
        });
    }
    
    /**
     * Initialize containers if they are null by looking them up in the scene
     */
    private void initializeContainersIfNull() {
        try {
            if (loginButtonContainer == null || profileButtonContainer == null) {
                LOGGER.info("One or both containers are null, trying to find them");
                
                if (welcomeText != null && welcomeText.getScene() != null) {
                    // Try to find containers using scene lookup
                    if (loginButtonContainer == null) {
                        loginButtonContainer = (HBox) welcomeText.getScene().lookup("#loginButtonContainer");
                        if (loginButtonContainer != null) {
                            LOGGER.info("Found loginButtonContainer by scene lookup");
                        } else {
                            LOGGER.warning("Could not find loginButtonContainer by scene lookup");
                        }
                    }
                    
                    if (profileButtonContainer == null) {
                        profileButtonContainer = (HBox) welcomeText.getScene().lookup("#profileButtonContainer");
                        if (profileButtonContainer != null) {
                            LOGGER.info("Found profileButtonContainer by scene lookup");
                        } else {
                            LOGGER.warning("Could not find profileButtonContainer by scene lookup");
                        }
                    }
                } else if (aboutButton != null && aboutButton.getScene() != null) {
                    // Try using another control as a reference
                    if (loginButtonContainer == null) {
                        loginButtonContainer = (HBox) aboutButton.getScene().lookup("#loginButtonContainer");
                        if (loginButtonContainer != null) {
                            LOGGER.info("Found loginButtonContainer via aboutButton lookup");
                        }
                    }
                    
                    if (profileButtonContainer == null) {
                        profileButtonContainer = (HBox) aboutButton.getScene().lookup("#profileButtonContainer");
                        if (profileButtonContainer != null) {
                            LOGGER.info("Found profileButtonContainer via aboutButton lookup");
                        }
                    }
                } else {
                    LOGGER.severe("Cannot find scene to look up containers");
                }
            }
        } catch (Exception e) {
            LOGGER.severe("Error initializing containers: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Find and update the ProfileButton
     */
    private void updateProfileButton(AuthState authState) {
        javafx.application.Platform.runLater(() -> {
            try {
                // Try to find ProfileButton in scene
                com.ueadmission.components.ProfileButton profileBtn = findProfileButtonInScene();
                
                if (profileBtn != null && authState != null) {
                    profileBtn.updateUIFromAuthState(authState);
                    System.out.println("Updated profile button during refresh");
                } else {
                    System.out.println("ProfileButton not found or auth state is null");
                }
            } catch (Exception e) {
                System.err.println("Error updating ProfileButton: " + e.getMessage());
            }
        });
    }
    
    /**
     * Try to find ProfileButton in scene
     */
    public com.ueadmission.components.ProfileButton findProfileButtonInScene() {
        if (welcomeText == null || welcomeText.getScene() == null) return null;
        
        // Look for ProfileButton instance in the scene
        for (javafx.scene.Node node : welcomeText.getScene().getRoot().lookupAll(".profile-button-container")) {
            if (node instanceof com.ueadmission.components.ProfileButton) {
                return (com.ueadmission.components.ProfileButton) node;
            }
            
            // Check if it's a container that might contain a ProfileButton
            if (node instanceof javafx.scene.Parent) {
                javafx.scene.Parent parent = (javafx.scene.Parent) node;
                for (javafx.scene.Node child : parent.getChildrenUnmodifiable()) {
                    if (child instanceof com.ueadmission.components.ProfileButton) {
                        return (com.ueadmission.components.ProfileButton) child;
                    }
                }
            }
        }
        
        return null;
    }
    
    // Add a logger for this class
    private static final java.util.logging.Logger LOGGER = java.util.logging.Logger.getLogger(MainController.class.getName());
    
    /**
     * Called when scene becomes visible or active
     * This method is called by NavigationManager when scene changes
     */
    public void onSceneActive() {
        LOGGER.info("Main scene became active, refreshing auth UI");
        
        javafx.application.Platform.runLater(() -> {
            // Force immediate refresh of auth state
            AuthState currentState = AuthStateManager.getInstance().getState();
            
            // Ensure we update UI with current state
            if (currentState != null && currentState.isAuthenticated()) {
                LOGGER.info("Authenticated user detected, refreshing profile display");
                if (profileButton != null) {
                    profileButton.updateUIFromAuthState(currentState);
                    LOGGER.info("Updated profile button UI for user: " + currentState.getUser().getFullName());
                } else {
                    LOGGER.warning("Profile button is null, trying lookup");
                    updateProfileButton(currentState);
                }
                
                // Initialize containers if needed
                initializeContainersIfNull();
                
                // Update container visibility
                updateContainersVisibility(true);
            } else {
                LOGGER.info("No authenticated user detected or auth state is null");
                
                // Initialize containers if needed
                initializeContainersIfNull();
                
                // Update container visibility
                updateContainersVisibility(false);
            }
        });
    }
    
    /**
     * Helper method to find a button by its text in the scene graph
     * @param text The text of the button to find
     * @return The found button or null if not found
     */
    private MFXButton findButtonByText(String text) {
        try {
            // Find all MFXButtons in the scene
            for (Node node : welcomeText.getScene().getRoot().lookupAll(".mfx-button, .mfx-button-outline")) {
                if (node instanceof MFXButton button && text.equals(button.getText())) {
                    return button;
                }
            }
            return null;
        } catch (Exception e) {
            System.err.println("Could not find button with text: " + text);
            return null;
        }
    }
}
