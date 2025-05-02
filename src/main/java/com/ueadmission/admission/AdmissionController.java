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
import javafx.application.HostServices;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

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
    void openBBA(ActionEvent event) {
        try {
            Desktop.getDesktop().browse(new URI("https://sobe.uiu.ac.bd/bba/"));
        } catch (IOException | URISyntaxException e) {
            e.printStackTrace();
        }
    }

    @FXML
    void openBBAAIS(ActionEvent event) {
        try {
            Desktop.getDesktop().browse(new URI("https://sobe.uiu.ac.bd/bba-in-ais/"));
        } catch (IOException | URISyntaxException e) {
            e.printStackTrace();
        }
    }

    @FXML
    void openEconomics(ActionEvent event) {
        try {
            Desktop.getDesktop().browse(new URI("https://sobe.uiu.ac.bd/economics/"));
        } catch (IOException | URISyntaxException e) {
            e.printStackTrace();
        }
    }

    @FXML
    void openCivil(ActionEvent event) {
        try {
            Desktop.getDesktop().browse(new URI("https://ce.uiu.ac.bd/"));
        } catch (IOException | URISyntaxException e) {
            e.printStackTrace();
        }
    }

    @FXML
    void openCSE(ActionEvent event) {
        try {
            Desktop.getDesktop().browse(new URI("https://cse.uiu.ac.bd/"));
        } catch (IOException | URISyntaxException e) {
            e.printStackTrace();
        }
    }

    @FXML
    void openDS(ActionEvent event) {
        try {
            Desktop.getDesktop().browse(new URI("https://datascience.uiu.ac.bd/"));
        } catch (IOException | URISyntaxException e) {
            e.printStackTrace();
        }
    }

    @FXML
    void openEEE(ActionEvent event) {
        try {
            Desktop.getDesktop().browse(new URI("https://eee.uiu.ac.bd/"));
        } catch (IOException | URISyntaxException e) {
            e.printStackTrace();
        }
    }

    @FXML
    void openEDS(ActionEvent event) {
        try {
            Desktop.getDesktop().browse(new URI("https://eds.uiu.ac.bd/"));
        } catch (IOException | URISyntaxException e) {
            e.printStackTrace();
        }
    }

    @FXML
    void openMSJ(ActionEvent event) {
        try {
            Desktop.getDesktop().browse(new URI("https://msj.uiu.ac.bd/"));
        } catch (IOException | URISyntaxException e) {
            e.printStackTrace();
        }
    }

    @FXML
    void openEnglish(ActionEvent event) {
        try {
            Desktop.getDesktop().browse(new URI("https://english.uiu.ac.bd/"));
        } catch (IOException | URISyntaxException e) {
            e.printStackTrace();
        }
    }

    @FXML
    void openPharmacy(ActionEvent event) {
        try {
            Desktop.getDesktop().browse(new URI("https://pharmacy.uiu.ac.bd/"));
        } catch (IOException | URISyntaxException e) {
            e.printStackTrace();
        }
    }

    @FXML
    void openBiotech(ActionEvent event) {
        try {
            Desktop.getDesktop().browse(new URI("https://bge.uiu.ac.bd/"));
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
        // Call cleanup first to ensure proper resource disposal
        cleanup();
        com.ueadmission.navigation.NavigationUtil.navigateToHome(event);
    }

    /**
     * Navigates to the About page with transition effects
     * @param event The event that triggered this action
     */
    private void navigateToAbout(javafx.event.ActionEvent event) {
        // Call cleanup first to ensure proper resource disposal
        cleanup();
        com.ueadmission.navigation.NavigationUtil.navigateToAbout(event);
    }

    /**
     * Navigates to the Contact page with transition effects
     * @param event The event that triggered this action
     */
    private void navigateToContact(javafx.event.ActionEvent event) {
        // Call cleanup first to ensure proper resource disposal
        cleanup();
        com.ueadmission.navigation.NavigationUtil.navigateToContact(event);
    }

    /**
     * Navigates to the Login page with transition effects
     * @param event The event that triggered this action
     */
    private void navigateToLogin(javafx.event.ActionEvent event) {
        // Call cleanup first to ensure proper resource disposal
        cleanup();
        com.ueadmission.navigation.NavigationUtil.navigateToLogin(event);
    }
}








