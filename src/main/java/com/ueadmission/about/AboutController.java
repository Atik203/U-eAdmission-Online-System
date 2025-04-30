package com.ueadmission.about;
import java.awt.Desktop;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.function.Consumer;
import java.util.logging.Logger;

import com.ueadmission.MainController;
import com.ueadmission.auth.state.AuthState;
import com.ueadmission.auth.state.AuthStateManager;
import com.ueadmission.components.ProfileButton;

import io.github.palexdev.materialfx.controls.MFXButton;
import javafx.animation.FadeTransition;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import javafx.util.Duration;

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
            admissionButton.setOnAction(event ->navigateToAdmission(event));//System.out.println("Admission button clicked"));
        }

        if (mockTestButton != null) {
            mockTestButton.setOnAction(event -> System.out.println("Mock Test button clicked"));
        }

        if (contactButton != null) {
            contactButton.setOnAction(event -> System.out.println("Contact button clicked"));
        }

        // Subscribe to auth state changes
        subscribeToAuthStateChanges();

        // Refresh UI with current auth state
        refreshUI();

        // Call onSceneActive to ensure UI is updated when scene is shown
        javafx.application.Platform.runLater(this::onSceneActive);
    }

    /**
     * Subscribe to authentication state changes
     */
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
        try {
            // Get current stage and its properties
            Stage currentStage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            double width = currentStage.getWidth();
            double height = currentStage.getHeight();
            double x = currentStage.getX();
            double y = currentStage.getY();
            boolean maximized = currentStage.isMaximized();
            
            // Store current auth state
            AuthState currentAuthState = AuthStateManager.getInstance().getState();
            boolean isAuthenticated = (currentAuthState != null && currentAuthState.isAuthenticated());
            LOGGER.info("Navigating to home with auth state: " + (isAuthenticated ? "authenticated" : "not authenticated"));
            
            // Prepare the Main window before closing current one
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com.ueadmission/main.fxml"));
            javafx.scene.Parent root = loader.load();
            
            // Get the controller and pass auth state data
            MainController mainController = loader.getController();
            
            // Create new stage
            Stage mainStage = new Stage();
            javafx.scene.Scene scene = new javafx.scene.Scene(root, width, height);
            mainStage.setScene(scene);
            mainStage.setTitle("UeAdmission - Home");
            
            // Set the position and size
            mainStage.setX(x);
            mainStage.setY(y);
            mainStage.setWidth(width);
            mainStage.setHeight(height);
            mainStage.setMaximized(maximized);
            
            // Make the new stage ready but not visible yet
            mainStage.setOpacity(0.0);
            mainStage.show();
            
            // Important: Force the scene to layout all nodes before accessing containers
            scene.getRoot().applyCss();
            scene.getRoot().layout();
            
            // Ensure UI is updated with authentication state before displaying
            mainController.refreshUI();
            
            // Set up JavaFX logging to help troubleshoot UI issues
            java.util.logging.Logger jfxLogger = java.util.logging.Logger.getLogger("javafx");
            jfxLogger.setLevel(java.util.logging.Level.FINE);
            
            // Use a fade transition for the new window
            FadeTransition fadeIn = new FadeTransition(Duration.millis(300), mainStage.getScene().getRoot());
            fadeIn.setFromValue(0.0);
            fadeIn.setToValue(1.0);
            
            // Add a fade out transition for the current window
            FadeTransition fadeOut = new FadeTransition(Duration.millis(200), currentStage.getScene().getRoot());
            fadeOut.setFromValue(1.0);
            fadeOut.setToValue(0.0);
            
            // Clean up before transition
            cleanup();
            
            // Start the fade out, then hide current stage when done
            fadeOut.setOnFinished(e -> {
                currentStage.hide();
                mainStage.setOpacity(1.0);
                fadeIn.play();
                
                // Execute this after fade in to ensure UI is fully ready
                fadeIn.setOnFinished(f -> {
                    try {
                        // Ensure profile is updated with current auth state after navigation
                        LOGGER.info("Fade in complete, calling onSceneActive on MainController");
                        mainController.onSceneActive();
                        
                        // Log the status of important controls
                        LOGGER.info("Profile button in MainController: " + 
                            (mainController.findProfileButtonInScene() != null ? "found" : "not found"));
                    } catch (Exception ex) {
                        LOGGER.severe("Error in fade in completion: " + ex.getMessage());
                        ex.printStackTrace();
                    } finally {
                        currentStage.close();
                    }
                });
            });
            
            fadeOut.play();
            
            LOGGER.info("Navigating to home screen with transition");
            
        } catch (Exception e) {
            LOGGER.severe("Failed to navigate to home: " + e.getMessage());
            e.printStackTrace();
            
            // Clean up even if there's an error
            cleanup();
            
            // Try direct navigation as fallback (much simpler approach)
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/com.ueadmission/main.fxml"));
                javafx.scene.Parent root = loader.load();
                MainController mainController = loader.getController();
                
                Stage stage = (Stage) homeButton.getScene().getWindow();
                stage.setScene(new javafx.scene.Scene(root));
                stage.setTitle("UeAdmission - Home");
                
                // Force layout before accessing UI elements
                stage.getScene().getRoot().applyCss();
                stage.getScene().getRoot().layout();
                
                // Call refresh UI on the controller
                mainController.refreshUI();
                
                // Then call onSceneActive to ensure everything is properly updated
                mainController.onSceneActive();
                
                LOGGER.info("Navigated to home using fallback method");
            } catch (Exception ex) {
                LOGGER.severe("Complete navigation failure: " + ex.getMessage());
            }
        }
    }


    /**
     * Navigates to the Admission page with transition effects
     * @param event The event that triggered this action
     */
    private void navigateToAdmission(javafx.event.ActionEvent event) {
        try {
            // Get current stage and its properties
            Stage currentStage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            double width = currentStage.getWidth();
            double height = currentStage.getHeight();
            double x = currentStage.getX();
            double y = currentStage.getY();
            boolean maximized = currentStage.isMaximized();
            
            // Store current auth state
            AuthState currentAuthState = AuthStateManager.getInstance().getState();
            boolean isAuthenticated = (currentAuthState != null && currentAuthState.isAuthenticated());
            LOGGER.info("Navigating to admission with auth state: " + (isAuthenticated ? "authenticated" : "not authenticated"));
            
            // Prepare the Admission window before closing current one
            Stage admissionStage = com.ueadmission.admission.Admission.prepareAdmissionWindow(width, height, x, y, maximized);
            
            if (admissionStage == null) {
                LOGGER.severe("Failed to create Admission window.");
                return;
            }
            
            // Make the new stage ready but not visible yet
            admissionStage.setOpacity(0.0);
            admissionStage.show();
            
            // Force layout before accessing UI elements
            admissionStage.getScene().getRoot().applyCss();
            admissionStage.getScene().getRoot().layout();
            
            // Try to get the controller
            javafx.fxml.FXMLLoader loader = (javafx.fxml.FXMLLoader) admissionStage.getScene().getUserData();
            if (loader != null) {
                Object controller = loader.getController();
                if (controller != null && controller instanceof com.ueadmission.admission.AdmissionController) {
                    // Call refresh method if available
                    try {
                        ((com.ueadmission.admission.AdmissionController) controller).refreshUI();
                        LOGGER.info("Refreshed UI in AdmissionController");
                    } catch (Exception e) {
                        LOGGER.warning("Could not call refreshUI: " + e.getMessage());
                    }
                    
                    // Try to call onSceneActive
                    try {
                        java.lang.reflect.Method method = controller.getClass().getMethod("onSceneActive");
                        method.invoke(controller);
                        LOGGER.info("Called onSceneActive on AdmissionController");
                    } catch (Exception e) {
                        LOGGER.warning("Could not call onSceneActive: " + e.getMessage());
                    }
                }
            }
            
            // Use a fade transition for the new window
            FadeTransition fadeIn = new FadeTransition(Duration.millis(300), admissionStage.getScene().getRoot());
            fadeIn.setFromValue(0.0);
            fadeIn.setToValue(1.0);
            
            // Add a fade out transition for the current window
            FadeTransition fadeOut = new FadeTransition(Duration.millis(200), currentStage.getScene().getRoot());
            fadeOut.setFromValue(1.0);
            fadeOut.setToValue(0.0);
            
            // Clean up before transition
            cleanup();
            
            // Start the fade out, then hide current stage when done
            fadeOut.setOnFinished(e -> {
                currentStage.hide();
                admissionStage.setOpacity(1.0);
                fadeIn.play();
                
                // Finally close the original stage after transition completes
                fadeIn.setOnFinished(f -> currentStage.close());
            });
            
            fadeOut.play();
            
            LOGGER.info("Navigating to admission screen with transition");
            
        } catch (Exception e) {
            LOGGER.severe("Failed to navigate to admission: " + e.getMessage());
            e.printStackTrace();
            
            // Clean up even if there's an error
            cleanup();
            
            // Try direct navigation as fallback
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/com.ueadmission/admission/admission.fxml"));
                javafx.scene.Parent root = loader.load();
                
                Stage stage = (Stage) admissionButton.getScene().getWindow();
                stage.setScene(new javafx.scene.Scene(root));
                stage.setTitle("Admission - UeAdmission");
                
                // Try to call refreshUI on the controller
                Object controller = loader.getController();
                if (controller != null && controller instanceof com.ueadmission.admission.AdmissionController) {
                    ((com.ueadmission.admission.AdmissionController) controller).refreshUI();
                }
                
                LOGGER.info("Navigated to admission using fallback method");
            } catch (Exception ex) {
                LOGGER.severe("Complete navigation failure: " + ex.getMessage());
            }
        }
    }


    /**
     * Navigates to the login page
     * @param event The event that triggered this action
     */
    private void navigateToLogin(javafx.event.ActionEvent event) {
        try {
            // Get current stage and its properties
            Stage currentStage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            double width = currentStage.getWidth();
            double height = currentStage.getHeight();
            double x = currentStage.getX();
            double y = currentStage.getY();
            boolean maximized = currentStage.isMaximized();
            
            // Prepare the Login window before closing current one
            Stage loginStage = com.ueadmission.auth.Auth.prepareLoginWindow(width, height, x, y, maximized);
            
            // Check if loginStage is null before proceeding
            if (loginStage == null) {
                LOGGER.severe("Failed to create login stage. Login window couldn't be prepared.");
                return;
            }
            
            // Make the new stage ready but not visible yet
            loginStage.setOpacity(0.0);
            loginStage.show();
            
            // Use a fade transition for the new window
            FadeTransition fadeIn = new FadeTransition(Duration.millis(300), loginStage.getScene().getRoot());
            fadeIn.setFromValue(0.0);
            fadeIn.setToValue(1.0);
            
            // Add a fade out transition for the current window
            FadeTransition fadeOut = new FadeTransition(Duration.millis(200), currentStage.getScene().getRoot());
            fadeOut.setFromValue(1.0);
            fadeOut.setToValue(0.0);
            
            // Clean up before transition
            cleanup();
            
            // Start the fade out, then hide current stage when done
            fadeOut.setOnFinished(e -> {
                currentStage.hide();
                loginStage.setOpacity(1.0);
                fadeIn.play();
                
                // Finally close the original stage after transition completes
                fadeIn.setOnFinished(f -> currentStage.close());
            });
            
            fadeOut.play();
            
            LOGGER.info("Navigating to login screen with transition");
            
        } catch (Exception e) {
            LOGGER.severe("Failed to navigate to login: " + e.getMessage());
            e.printStackTrace();
            
            // Clean up even if there's an error
            cleanup();
            
            // Try direct navigation as fallback
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/com.ueadmission/auth/login.fxml"));
                javafx.scene.Parent root = loader.load();
                Stage stage = (Stage) loginButton.getScene().getWindow();
                stage.setScene(new javafx.scene.Scene(root));
                stage.setTitle("Login - UeAdmission");
                LOGGER.info("Navigated to login using fallback method");
            } catch (Exception ex) {
                LOGGER.severe("Complete navigation failure: " + ex.getMessage());
            }
        }
    }
}
