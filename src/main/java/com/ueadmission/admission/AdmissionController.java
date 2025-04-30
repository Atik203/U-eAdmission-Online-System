package com.ueadmission.admission;

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
import io.github.palexdev.materialfx.controls.MFXComboBox;
import io.github.palexdev.materialfx.controls.MFXDatePicker;
import io.github.palexdev.materialfx.controls.MFXTextField;
import javafx.animation.FadeTransition;
import javafx.application.HostServices;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Duration;

public class AdmissionController {
    private HostServices hostServices;

    private static final Logger LOGGER = Logger.getLogger(AdmissionController.class.getName());
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
    private MFXButton aboutButton;

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
    private VBox root;

    @FXML
    private Button applyNowBtn;

    @FXML
    private Label titleLabel;

    @FXML
    private Button btnSoBE;

    @FXML
    private Button btnSoSE;

    @FXML
    private Button btnSoHSS;

    @FXML
    private Button btnSoLS;

    @FXML
    private VBox infoBox;

    @FXML
    private VBox applicationFormContainer;

    @FXML
    private MFXTextField firstNameField;

    @FXML
    private MFXTextField lastNameField;

    @FXML
    private MFXTextField emailField;

    @FXML
    private MFXTextField phoneField;

    @FXML
    private MFXDatePicker dobPicker;

    @FXML
    private MFXComboBox<String> programComboBox;

    @FXML
    private MFXTextField institutionField;

    @FXML
    private MFXTextField sscGpaField;

    @FXML
    private MFXTextField hscGpaField;

    @FXML
    private MFXComboBox<String> referenceComboBox;

    @FXML
    private MFXTextField commentsField;

    @FXML
    void handleApplyNow(ActionEvent event) {
        try {
            java.awt.Desktop.getDesktop().browse(new java.net.URI("https://admission.uiu.ac.bd/"));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    void showSoBE(ActionEvent event) {
        displayInfo("School of Business and Economics (SoBE)", new String[]{
                "BBA - https://www.uiu.ac.bd/program/bba/",
                "BBA in AIS - https://www.uiu.ac.bd/program/bba-in-ais/",
                "Economics - https://www.uiu.ac.bd/program/bsc-in-economics/"
        });
    }

    @FXML
    void showSoSE(ActionEvent event) {
        displayInfo("School of Science and Engineering (SoSE)", new String[]{
                "Civil Engineering - https://www.uiu.ac.bd/program/bsc-in-civil-engineering/",
                "CSE - https://www.uiu.ac.bd/program/bsc-in-cse/",
                "Data Science - https://www.uiu.ac.bd/program/bsc-in-data-science/",
                "EEE - https://www.uiu.ac.bd/program/bsc-in-eee/"
        });
    }

    @FXML
    void showSoHSS(ActionEvent event) {
        displayInfo("School of Humanities and Social Sciences (SoHSS)", new String[]{
                "EDS - https://www.uiu.ac.bd/program/bss-in-environment-and-development-studies/",
                "MSJ - https://www.uiu.ac.bd/program/bss-in-msj/",
                "English - https://www.uiu.ac.bd/program/ba-in-english/"
        });
    }

    @FXML
    void showSoLS(ActionEvent event) {
        displayInfo("School of Life Sciences (SoLS)", new String[]{
                "Pharmacy - https://www.uiu.ac.bd/program/bachelor-of-pharmacy/",
                "Biotech & Genetic Engineering - https://www.uiu.ac.bd/program/biotechnology-genetic-engineering/"
        });
    }

    @FXML
    void openBBA(ActionEvent event) {
        try {
            Desktop.getDesktop().browse(new URI("https://www.uiu.ac.bd/program/bba/"));
        } catch (IOException | URISyntaxException e) {
            e.printStackTrace();
        }
    }

    @FXML
    void openBBAAIS(ActionEvent event) {
        try {
            Desktop.getDesktop().browse(new URI("https://www.uiu.ac.bd/program/bba-in-ais/"));
        } catch (IOException | URISyntaxException e) {
            e.printStackTrace();
        }
    }

    @FXML
    void openEconomics(ActionEvent event) {
        try {
            Desktop.getDesktop().browse(new URI("https://www.uiu.ac.bd/program/bsc-in-economics/"));
        } catch (IOException | URISyntaxException e) {
            e.printStackTrace();
        }
    }

    @FXML
    void openCivil(ActionEvent event) {
        try {
            Desktop.getDesktop().browse(new URI("https://www.uiu.ac.bd/program/bsc-in-civil-engineering/"));
        } catch (IOException | URISyntaxException e) {
            e.printStackTrace();
        }
    }

    @FXML
    void openCSE(ActionEvent event) {
        try {
            Desktop.getDesktop().browse(new URI("https://www.uiu.ac.bd/program/bsc-in-cse/"));
        } catch (IOException | URISyntaxException e) {
            e.printStackTrace();
        }
    }

    @FXML
    void openDS(ActionEvent event) {
        try {
            Desktop.getDesktop().browse(new URI("https://www.uiu.ac.bd/program/bsc-in-data-science/"));
        } catch (IOException | URISyntaxException e) {
            e.printStackTrace();
        }
    }

    @FXML
    void openEEE(ActionEvent event) {
        try {
            Desktop.getDesktop().browse(new URI("https://www.uiu.ac.bd/program/bsc-in-eee/"));
        } catch (IOException | URISyntaxException e) {
            e.printStackTrace();
        }
    }

    @FXML
    void openEDS(ActionEvent event) {
        try {
            Desktop.getDesktop().browse(new URI("https://www.uiu.ac.bd/program/bss-in-environment-and-development-studies/"));
        } catch (IOException | URISyntaxException e) {
            e.printStackTrace();
        }
    }

    @FXML
    void openMSJ(ActionEvent event) {
        try {
            Desktop.getDesktop().browse(new URI("https://www.uiu.ac.bd/program/bss-in-msj/"));
        } catch (IOException | URISyntaxException e) {
            e.printStackTrace();
        }
    }

    @FXML
    void openEnglish(ActionEvent event) {
        try {
            Desktop.getDesktop().browse(new URI("https://www.uiu.ac.bd/program/ba-in-english/"));
        } catch (IOException | URISyntaxException e) {
            e.printStackTrace();
        }
    }

    @FXML
    void openPharmacy(ActionEvent event) {
        try {
            Desktop.getDesktop().browse(new URI("https://www.uiu.ac.bd/program/bachelor-of-pharmacy/"));
        } catch (IOException | URISyntaxException e) {
            e.printStackTrace();
        }
    }

    @FXML
    void openBiotech(ActionEvent event) {
        try {
            Desktop.getDesktop().browse(new URI("https://www.uiu.ac.bd/program/biotechnology-genetic-engineering/"));
        } catch (IOException | URISyntaxException e) {
            e.printStackTrace();
        }
    }

    @FXML
    void showApplicationForm(ActionEvent event) {
        applicationFormContainer.setVisible(true);
        applicationFormContainer.setManaged(true);
    }

    @FXML
    void hideApplicationForm(ActionEvent event) {
        applicationFormContainer.setVisible(false);
        applicationFormContainer.setManaged(false);
    }

    @FXML
    void resetForm(ActionEvent event) {
        // Clear all form fields
        firstNameField.clear();
        lastNameField.clear();
        emailField.clear();
        phoneField.clear();
        dobPicker.clear();
        programComboBox.clear();
        institutionField.clear();
        sscGpaField.clear();
        hscGpaField.clear();
        referenceComboBox.clear();
        commentsField.clear();
    }

    @FXML
    void submitForm(ActionEvent event) {
        // Basic validation
        if (firstNameField.getText().isEmpty() || 
            lastNameField.getText().isEmpty() || 
            emailField.getText().isEmpty()) {
            
            // Show error message or alert
            javafx.scene.control.Alert alert = new javafx.scene.control.Alert(
                javafx.scene.control.Alert.AlertType.ERROR,
                "Please fill in all required fields.",
                javafx.scene.control.ButtonType.OK
            );
            alert.setHeaderText("Form Validation Error");
            alert.show();
            return;
        }
        
        // If validation passes, show success message
        javafx.scene.control.Alert alert = new javafx.scene.control.Alert(
            javafx.scene.control.Alert.AlertType.INFORMATION,
            "Your application has been successfully submitted. We will contact you soon.",
            javafx.scene.control.ButtonType.OK
        );
        alert.setHeaderText("Application Submitted");
        alert.showAndWait();
        
        // Hide form after submission
        hideApplicationForm(event);
        
        // Reset form for next use
        resetForm(event);
    }

    private void displayInfo(String schoolTitle, String[] items) {
        infoBox.getChildren().clear();

        Label header = new Label(schoolTitle);
        header.getStyleClass().add("section-title");
        infoBox.getChildren().add(header);

        for (String item : items) {
            Label label = new Label(item);
            label.getStyleClass().add("info-label");
            infoBox.getChildren().add(label);
        }
    }

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
        if (aboutButton != null) {
            aboutButton.setOnAction(event -> navigateToAbout(event));
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

        // Initialize the program combo box
        if (programComboBox != null) {
            programComboBox.getItems().addAll(
                "BBA", "BBA in AIS", "Economics", 
                "Civil Engineering", "CSE", "Data Science", "EEE",
                "EDS", "MSJ", "English",
                "Pharmacy", "Biotech and Genetic Engineering"
            );
        }
        
        // Initialize the reference combo box
        if (referenceComboBox != null) {
            referenceComboBox.getItems().addAll(
                "Friends/Family", "School Counselor", "Social Media",
                "UIU Website", "Newspaper", "Education Fair", "Other"
            );
        }
    }

    /**
     * Cleanup method to ensure any transitions are completed and opacity is reset
     * Called before navigating away from the Admission screen
     */
    private void cleanup() {
        LOGGER.info("Cleaning up AdmissionController before navigation");

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
                System.out.println("Updated profile button in Admission page");
            } else {
                System.out.println("Profile button is null in AdmissionController");

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
            
            // Try to get the controller
            Object controller = loader.getController();
            if (controller != null) {
                // Try to call refreshUI method if available
                try {
                    java.lang.reflect.Method refreshMethod = controller.getClass().getMethod("refreshUI");
                    refreshMethod.invoke(controller);
                    LOGGER.info("Called refreshUI on MainController");
                } catch (Exception e) {
                    LOGGER.warning("Could not call refreshUI: " + e.getMessage());
                }
                
                // Try to call onSceneActive method if available
                try {
                    java.lang.reflect.Method method = controller.getClass().getMethod("onSceneActive");
                    method.invoke(controller);
                    LOGGER.info("Called onSceneActive on MainController");
                } catch (Exception e) {
                    LOGGER.warning("Could not call onSceneActive: " + e.getMessage());
                }
            }
            
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
                
                // Finally close the original stage after transition completes
                fadeIn.setOnFinished(f -> currentStage.close());
            });
            
            fadeOut.play();
            
            LOGGER.info("Navigating to home screen with transition");
            
        } catch (Exception e) {
            LOGGER.severe("Failed to navigate to Home: " + e.getMessage());
            e.printStackTrace();
            
            // Clean up even if there's an error
            cleanup();
            
            // Try direct navigation as fallback
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/com.ueadmission/main.fxml"));
                javafx.scene.Parent root = loader.load();
                
                Stage stage = (Stage) homeButton.getScene().getWindow();
                stage.setScene(new javafx.scene.Scene(root));
                stage.setTitle("UeAdmission - Home");
                
                LOGGER.info("Navigated to home using fallback method");
            } catch (Exception ex) {
                LOGGER.severe("Complete navigation failure: " + ex.getMessage());
            }
        }
    }

    /**
     * Navigates to the About page with transition effects
     * @param event The event that triggered this action
     */
    private void navigateToAbout(javafx.event.ActionEvent event) {
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
            LOGGER.info("Navigating to about with auth state: " + (isAuthenticated ? "authenticated" : "not authenticated"));
            
            // Prepare the About window before closing current one
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/ueadmission/about/About.fxml"));
            javafx.scene.Parent root = loader.load();
            
            // Create new stage
            Stage aboutStage = new Stage();
            javafx.scene.Scene scene = new javafx.scene.Scene(root, width, height);
            aboutStage.setScene(scene);
            aboutStage.setTitle("About - UeAdmission");
            
            // Set the position and size
            aboutStage.setX(x);
            aboutStage.setY(y);
            aboutStage.setWidth(width);
            aboutStage.setHeight(height);
            aboutStage.setMaximized(maximized);
            
            // Make the new stage ready but not visible yet
            aboutStage.setOpacity(0.0);
            aboutStage.show();
            
            // Important: Force the scene to layout all nodes before accessing containers
            scene.getRoot().applyCss();
            scene.getRoot().layout();
            
            // Try to get the controller
            Object controller = loader.getController();
            if (controller != null) {
                // Try to call refreshUI method if available
                try {
                    java.lang.reflect.Method refreshMethod = controller.getClass().getMethod("refreshUI");
                    refreshMethod.invoke(controller);
                    LOGGER.info("Called refreshUI on AboutController");
                } catch (Exception e) {
                    LOGGER.warning("Could not call refreshUI: " + e.getMessage());
                }
                
                // Try to call onSceneActive method if available
                try {
                    java.lang.reflect.Method method = controller.getClass().getMethod("onSceneActive");
                    method.invoke(controller);
                    LOGGER.info("Called onSceneActive on AboutController");
                } catch (Exception e) {
                    LOGGER.warning("Could not call onSceneActive: " + e.getMessage());
                }
            }
            
            // Use a fade transition for the new window
            FadeTransition fadeIn = new FadeTransition(Duration.millis(300), aboutStage.getScene().getRoot());
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
                aboutStage.setOpacity(1.0);
                fadeIn.play();
                
                // Finally close the original stage after transition completes
                fadeIn.setOnFinished(f -> currentStage.close());
            });
            
            fadeOut.play();
            
            LOGGER.info("Navigating to about screen with transition");
            
        } catch (Exception e) {
            LOGGER.severe("Failed to navigate to About: " + e.getMessage());
            e.printStackTrace();
            
            // Clean up even if there's an error
            cleanup();
            
            // Try direct navigation as fallback
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/ueadmission/about/About.fxml"));
                javafx.scene.Parent root = loader.load();
                
                Stage stage = (Stage) aboutButton.getScene().getWindow();
                stage.setScene(new javafx.scene.Scene(root));
                stage.setTitle("About - UeAdmission");
                
                LOGGER.info("Navigated to about using fallback method");
            } catch (Exception ex) {
                LOGGER.severe("Complete navigation failure: " + ex.getMessage());
            }
        }
    }

    /**
     * Navigates to the Contact page with transition effects
     * @param event The event that triggered this action
     */
    private void navigateToContact(javafx.event.ActionEvent event) {
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
            LOGGER.info("Navigating to contact with auth state: " + (isAuthenticated ? "authenticated" : "not authenticated"));
            
            // Prepare the Contact window before closing current one
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/ueadmission/contact/Contact.fxml"));
            javafx.scene.Parent root = loader.load();
            
            // Create new stage
            Stage contactStage = new Stage();
            javafx.scene.Scene scene = new javafx.scene.Scene(root, width, height);
            contactStage.setScene(scene);
            contactStage.setTitle("Contact - UeAdmission");
            
            // Set the position and size
            contactStage.setX(x);
            contactStage.setY(y);
            contactStage.setWidth(width);
            contactStage.setHeight(height);
            contactStage.setMaximized(maximized);
            
            // Make the new stage ready but not visible yet
            contactStage.setOpacity(0.0);
            contactStage.show();
            
            // Important: Force the scene to layout all nodes before accessing containers
            scene.getRoot().applyCss();
            scene.getRoot().layout();
            
            // Try to get the controller
            Object controller = loader.getController();
            if (controller != null) {
                // Try to call refreshUI method if available
                try {
                    java.lang.reflect.Method refreshMethod = controller.getClass().getMethod("refreshUI");
                    refreshMethod.invoke(controller);
                    LOGGER.info("Called refreshUI on ContactController");
                } catch (Exception e) {
                    LOGGER.warning("Could not call refreshUI: " + e.getMessage());
                }
                
                // Try to call onSceneActive method if available
                try {
                    java.lang.reflect.Method method = controller.getClass().getMethod("onSceneActive");
                    method.invoke(controller);
                    LOGGER.info("Called onSceneActive on ContactController");
                } catch (Exception e) {
                    LOGGER.warning("Could not call onSceneActive: " + e.getMessage());
                }
            }
            
            // Use a fade transition for the new window
            FadeTransition fadeIn = new FadeTransition(Duration.millis(300), contactStage.getScene().getRoot());
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
                contactStage.setOpacity(1.0);
                fadeIn.play();
                
                // Finally close the original stage after transition completes
                fadeIn.setOnFinished(f -> currentStage.close());
            });
            
            fadeOut.play();
            
            LOGGER.info("Navigating to contact screen with transition");
            
        } catch (Exception e) {
            LOGGER.severe("Failed to navigate to Contact: " + e.getMessage());
            e.printStackTrace();
            
            // Clean up even if there's an error
            cleanup();
            
            // Try direct navigation as fallback
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/ueadmission/contact/Contact.fxml"));
                javafx.scene.Parent root = loader.load();
                
                Stage stage = (Stage) contactButton.getScene().getWindow();
                stage.setScene(new javafx.scene.Scene(root));
                stage.setTitle("Contact - UeAdmission");
                
                LOGGER.info("Navigated to contact using fallback method");
            } catch (Exception ex) {
                LOGGER.severe("Complete navigation failure: " + ex.getMessage());
            }
        }
    }

    /**
     * Navigates to the Login page with transition effects
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
            
            // Store current auth state
            AuthState currentAuthState = AuthStateManager.getInstance().getState();
            boolean isAuthenticated = (currentAuthState != null && currentAuthState.isAuthenticated());
            LOGGER.info("Navigating to login with auth state: " + (isAuthenticated ? "authenticated" : "not authenticated"));
            
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
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/com.ueadmission/auth/Auth.fxml"));
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








