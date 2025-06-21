package com.ueadmission.examPortal;

import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.ueadmission.auth.state.AuthState;
import com.ueadmission.auth.state.AuthStateManager;
import com.ueadmission.components.ProfileButton;
import com.ueadmission.utils.MFXNotifications;

import io.github.palexdev.materialfx.controls.MFXButton;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

/**
 * Controller for the Exam Portal page
 * Handles user interactions and navigation for the Exam Portal screen
 */
public class ExamPortalController {

    private static final Logger LOGGER = Logger.getLogger(ExamPortalController.class.getName());
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

    // Exam Portal specific elements
    @FXML private MFXButton viewSyllabusButton;    @FXML
    public void initialize() {        
        // Configure navigation buttons
        homeButton.setOnAction(event -> navigateToHome(event));
        aboutButton.setOnAction(event -> navigateToAbout(event));

        // Exam Portal button with authentication check
        examPortalButton.setOnAction(event -> {
            // If already on Exam Portal, do nothing to avoid unnecessary refreshes
            if (examPortalButton.getScene() != null &&
                examPortalButton.getScene().getRoot().getId() != null &&
                examPortalButton.getScene().getRoot().getId().equals("examPortalRoot")) {
                LOGGER.info("Already on Exam Portal, no navigation needed");
                return;
            }

            // Check authentication before navigating
            if (AuthStateManager.getInstance().isAuthenticated()) {
                navigateToExamPortal(event);
            } else {
                LOGGER.info("User not authenticated, redirecting to login");
                MFXNotifications.showWarning("Authentication Required", 
                        "Please log in to access the Exam Portal.");
                navigateToLogin(event);
            }
        });

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

        // Setup profile button action
        setupProfileButtonAction();

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
     * Setup profile button click action
     */
    private void setupProfileButtonAction() {
        // The ProfileButton already has its internal click handlers
        // No need to set an action explicitly, as it's handled internally
        // in the ProfileButton.handleProfileClick() method

        // Call this method in initialize() to ensure the ProfileButton is properly set up
    }

    /**
     * Start an exam or test
     * @param event The event that triggered this action
     */
    @FXML
    public void startExam(ActionEvent event) {
        // Check if user is authenticated
        if (!AuthStateManager.getInstance().isAuthenticated()) {
            MFXNotifications.showWarning("Authentication Required", 
                    "You need to log in before you can take an exam.");
            navigateToLogin(event);
            return;
        }

        // Get the button that was clicked
        MFXButton button = (MFXButton) event.getSource();

        // Get the exam card container
        VBox examCard = (VBox) button.getParent().getParent();

        // Get the exam title
        Label titleLabel = (Label) examCard.getChildren().get(0);
        String examTitle = titleLabel.getText();

        // Show confirmation dialog
        MFXNotifications.showInfo("Starting Exam", 
                "You are about to start the " + examTitle + " exam. Make sure you have a stable internet connection and a quiet environment.");

        // Get the button text to determine which screen to navigate to
        String buttonText = button.getText();
        LOGGER.log(Level.INFO, "User starting exam: {0} with button text: {1}", new Object[]{examTitle, buttonText});

        cleanup();

        // Navigate to the appropriate screen based on the button text
        if (buttonText.equals("Start Test")) {
            // Set the selected school in MockTestController
            com.ueadmission.mockTest.MockTestController.setSelectedSchoolStatic(examTitle);
            LOGGER.log(Level.INFO, "Set selected school for mock test: {0}", examTitle);

            // Navigate to the mock test screen for practice tests
            LOGGER.log(Level.INFO, "Navigating to mock test screen");
            com.ueadmission.navigation.NavigationUtil.navigateToMockTest(event);
        } else if (buttonText.equals("Start Exam")) {
            // Set the selected school in ExamController
            com.ueadmission.exam.ExamController.setSelectedSchoolStatic(examTitle);
            LOGGER.log(Level.INFO, "Set selected school for exam: {0}", examTitle);

            // Navigate to the actual exam screen for official exams
            LOGGER.log(Level.INFO, "Navigating to exam screen");
            com.ueadmission.navigation.NavigationUtil.navigateToExam(event);
        } else {
            // Default to mock test if button text is unknown
            LOGGER.log(Level.WARNING, "Unknown button text: {0}, defaulting to mock test", buttonText);
            com.ueadmission.navigation.NavigationUtil.navigateToMockTest(event);
        }
    }    /**
     * View exam or test syllabus
     * @param event The event that triggered this action
     */
    @FXML
    public void viewSyllabus(ActionEvent event) {
        // Open a URL to the syllabus page
        try {            
            // Check if viewSyllabusButton exists and was clicked
            if (viewSyllabusButton != null && event.getSource() == viewSyllabusButton) {
                // Future implementation: Different handling for the main syllabus button
            }

            // For now, just show a notification
            MFXNotifications.showInfo("Syllabus", 
                    "The syllabus information will be displayed in a future update. For now, please visit our website.");

            LOGGER.info("User requested to view syllabus.");
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error viewing syllabus", e);
            MFXNotifications.showError("Error", "Could not open syllabus information. Please try again later.");
        }
    }

    /**
     * Register for an actual admission exam
     * @param event The event that triggered this action
     */
    @FXML
    public void registerForExam(ActionEvent event) {
        // Check if user is authenticated
        if (!AuthStateManager.getInstance().isAuthenticated()) {
            MFXNotifications.showWarning("Authentication Required", 
                    "You need to log in before you can register for an exam.");
            navigateToLogin(event);
            return;
        }

        // For now, navigate to the application page
        try {
            MFXNotifications.showInfo("Application Process", 
                    "To register for an exam, you need to complete the application process first.");
            navigateToApplication(event);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error navigating to application page", e);
            MFXNotifications.showError("Error", "Could not navigate to the application page. Please try again later.");
        }
    }

    /**
     * View more details about an exam
     * @param event The event that triggered this action
     */
    @FXML
    public void viewExamDetails(ActionEvent event) {
        // For now, just show a notification
        MFXNotifications.showInfo("Exam Details", 
                "Detailed information about the exam will be available in a future update.");

        LOGGER.info("User requested to view exam details.");
    }

    /**
     * Cleanup method to ensure any transitions are completed and opacity is reset
     * Called before navigating away from the Exam Portal screen
     */
    void cleanup() {
        LOGGER.info("Cleaning up ExamPortalController before navigation");

        // Reset opacity on the scene root if available
        if (examPortalButton != null && examPortalButton.getScene() != null && 
                examPortalButton.getScene().getRoot() != null) {
            examPortalButton.getScene().getRoot().setOpacity(1.0);
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
            LOGGER.info("Auth state change detected in ExamPortalController");

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
        LOGGER.info("Subscribed to auth state changes in ExamPortalController");

        // Force an initial update
        AuthState currentState = AuthStateManager.getInstance().getState();
        if (currentState != null) {
            authStateListener.accept(currentState);
        }
    }
      /**
     * Called when scene becomes visible or active
     * This ensures the UI is updated with current auth state
     */    public void onSceneActive() {
        LOGGER.info("Exam Portal scene became active, refreshing auth UI");

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
        } else {
            // Force a manual update to the ProfileButton since we're authenticated
            if (profileButton != null) {
                profileButton.refreshAuthState();
                LOGGER.info("Explicitly refreshed profile button on scene activation");
            } else {
                LOGGER.warning("Profile button is null on scene activation, trying to find it");

                // Try one more time to find it using multiple approaches
                javafx.scene.Scene scene = getScene();
                if (scene != null) {
                    // Try by class first
                    Node foundButton = scene.lookup(".profile-button-container");
                    if (foundButton instanceof ProfileButton) {
                        profileButton = (ProfileButton) foundButton;
                        profileButton.refreshAuthState();
                        LOGGER.info("Found profile button by class and refreshed on scene activation");
                    } else {
                        // Try by container children
                        if (profileButtonContainer != null && !profileButtonContainer.getChildren().isEmpty()) {
                            for (Node child : profileButtonContainer.getChildren()) {
                                if (child instanceof ProfileButton) {
                                    profileButton = (ProfileButton) child;
                                    profileButton.refreshAuthState();
                                    LOGGER.info("Found profile button in container children and refreshed on scene activation");
                                    break;
                                }
                            }
                        } else {
                            LOGGER.warning("Could not find profile button by any method during scene activation");
                        }
                    }
                }
            }
        }
    }

    /**
     * Manually refresh the UI state based on current auth state
     */
    public void refreshUI() {
        LOGGER.info("Refreshing ExamPortalController UI");

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
                LOGGER.info("Updated profile button in Exam Portal page");
            } else {
                LOGGER.warning("Profile button is null in ExamPortalController");

                // Try to find profile button by lookup
                if (examPortalButton != null && examPortalButton.getScene() != null) {
                    Node foundProfileButton = examPortalButton.getScene().lookup(".profile-button-container");
                    if (foundProfileButton instanceof ProfileButton) {
                        LOGGER.info("Found profile button by lookup in scene");
                        ((ProfileButton) foundProfileButton).updateUIFromAuthState(currentState);
                    }
                }
            }
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error updating UI components in ExamPortalController: {0}", e.getMessage());
            e.printStackTrace();
        }
    }
      /**
     * Update containers visibility based on authentication status
     */    private void updateContainersVisibility(boolean isAuthenticated) {
        javafx.application.Platform.runLater(() -> {
            try {
                // Try to initialize containers if they are null first to ensure we have the containers
                initializeContainersIfNull();

                // Update login container visibility
                if (loginButtonContainer != null) {
                    loginButtonContainer.setVisible(!isAuthenticated);
                    loginButtonContainer.setManaged(!isAuthenticated);
                    LOGGER.log(Level.INFO, "Login button container visibility set to: {0}", !isAuthenticated);
                } else {
                    LOGGER.warning("Login button container is null, attempting to find it again");
                    javafx.scene.Scene scene = getScene();
                    if (scene != null) {
                        // Try multiple approaches to find the login container
                        Node loginContainer = scene.lookup("#loginButtonContainer");
                        if (loginContainer instanceof HBox) {
                            loginContainer.setVisible(!isAuthenticated);
                            loginContainer.setManaged(!isAuthenticated);
                            loginButtonContainer = (HBox) loginContainer;
                            LOGGER.info("Found and updated login container by ID lookup");
                        } else {
                            // If we still can't find it, look for it using a different approach
                            LOGGER.warning("Could not find loginButtonContainer by ID, searching for login button directly");
                            Node loginBtn = scene.lookup("#login-button");
                            if (loginBtn != null && loginBtn.getParent() instanceof HBox) {
                                HBox container = (HBox) loginBtn.getParent();
                                container.setVisible(!isAuthenticated);
                                container.setManaged(!isAuthenticated);
                                loginButtonContainer = container;
                                LOGGER.info("Found login button and updated its parent container: " + container.getId());
                            } else {
                                // Last resort: look through all HBoxes in the navbar
                                LOGGER.warning("Could not find login button, trying to find HBox in navbar");
                                Node navbar = scene.lookup("#navbar");
                                if (navbar instanceof javafx.scene.Parent) {
                                    for (Node child : ((javafx.scene.Parent) navbar).getChildrenUnmodifiable()) {
                                        if (child instanceof HBox && child.getId() != null && 
                                            child.getId().equals("loginButtonContainer")) {
                                            child.setVisible(!isAuthenticated);
                                            child.setManaged(!isAuthenticated);
                                            loginButtonContainer = (HBox) child;
                                            LOGGER.info("Found login container in navbar children");
                                            break;
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // Update profile container visibility with similar approach
                if (profileButtonContainer != null) {
                    profileButtonContainer.setVisible(isAuthenticated);
                    profileButtonContainer.setManaged(isAuthenticated);
                    LOGGER.log(Level.INFO, "Profile button container visibility set to: {0}", isAuthenticated);
                } else {
                    LOGGER.warning("Profile button container is null, attempting to find it again");
                    javafx.scene.Scene scene = getScene();
                    if (scene != null) {
                        // Try multiple approaches to find the profile container
                        Node profileContainer = scene.lookup("#profileButtonContainer");
                        if (profileContainer instanceof HBox) {
                            profileContainer.setVisible(isAuthenticated);
                            profileContainer.setManaged(isAuthenticated);
                            profileButtonContainer = (HBox) profileContainer;
                            LOGGER.info("Found and updated profile container by ID lookup");
                        } else {
                            // Try finding the ProfileButton and get its parent
                            LOGGER.warning("Could not find profileButtonContainer by ID, searching for profile button directly");
                            Node profileBtn = scene.lookup(".profile-button-container");
                            if (profileBtn != null && profileBtn.getParent() instanceof HBox) {
                                HBox container = (HBox) profileBtn.getParent();
                                container.setVisible(isAuthenticated);
                                container.setManaged(isAuthenticated);
                                profileButtonContainer = container;
                                LOGGER.info("Found profile button and updated its parent container: " + container.getId());

                                // Also update the profile button reference if needed
                                if (profileButton == null && profileBtn instanceof ProfileButton) {
                                    profileButton = (ProfileButton) profileBtn;
                                    LOGGER.info("Updated profile button reference while finding container");
                                }
                            } else {
                                // Last resort: look through all HBoxes in the navbar
                                LOGGER.warning("Could not find profile button, trying to find HBox in navbar");
                                Node navbar = scene.lookup("#navbar");
                                if (navbar instanceof javafx.scene.Parent) {
                                    for (Node child : ((javafx.scene.Parent) navbar).getChildrenUnmodifiable()) {
                                        if (child instanceof HBox && child.getId() != null && 
                                            child.getId().equals("profileButtonContainer")) {
                                            child.setVisible(isAuthenticated);
                                            child.setManaged(isAuthenticated);
                                            profileButtonContainer = (HBox) child;
                                            LOGGER.info("Found profile container in navbar children");
                                            break;
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // Update profile button if we have it
                if (profileButton != null && isAuthenticated) {
                    profileButton.refreshAuthState();
                    LOGGER.info("Refreshed profile button during container update");
                }

                // Verify the containers after updating
                if (loginButtonContainer != null) {
                    LOGGER.info("Login container visibility after update: " + loginButtonContainer.isVisible());
                }
                if (profileButtonContainer != null) {
                    LOGGER.info("Profile container visibility after update: " + profileButtonContainer.isVisible());
                }
            } catch (Exception e) {
                LOGGER.log(Level.SEVERE, "Error updating containers in ExamPortalController: {0}", e.getMessage());
                e.printStackTrace();
            }
        });
    }
      /**
     * Try to initialize the containers if they are null
     */    private void initializeContainersIfNull() {
        try {
            javafx.scene.Scene scene = getScene();
            if (scene == null) {
                LOGGER.warning("Scene is null, cannot initialize containers");
                return;
            }

            // Find loginButtonContainer if it's null
            if (loginButtonContainer == null) {
                // First try with fx:id (FXML injection should already have handled this)
                Node node = scene.lookup("#loginButtonContainer"); // Use CSS ID selector with # prefix
                if (node instanceof HBox) {
                    loginButtonContainer = (HBox) node;
                    LOGGER.info("Initialized loginButtonContainer via scene lookup: " + node.getId());
                } else {
                    // Try looking for the login button and get its parent
                    LOGGER.warning("Could not find loginButtonContainer directly, trying to find login button");
                    Node loginBtn = scene.lookup("#login-button");
                    if (loginBtn != null && loginBtn.getParent() instanceof HBox) {
                        loginButtonContainer = (HBox) loginBtn.getParent();
                        LOGGER.info("Found loginButtonContainer via login button parent: " + loginButtonContainer.getId());
                    } else {
                        LOGGER.warning("Could not find loginButtonContainer in scene or element is not an HBox");
                    }
                }
            }

            // Find profileButtonContainer if it's null
            if (profileButtonContainer == null) {
                // First try with fx:id (FXML injection should already have handled this)
                Node node = scene.lookup("#profileButtonContainer"); // Use CSS ID selector with # prefix
                if (node instanceof HBox) {
                    profileButtonContainer = (HBox) node;
                    LOGGER.info("Initialized profileButtonContainer via scene lookup: " + node.getId());
                } else {
                    // Try looking for the profile button and get its parent
                    LOGGER.warning("Could not find profileButtonContainer directly, trying to find profile button");
                    Node profileBtn = scene.lookup(".profile-button-container");
                    if (profileBtn != null && profileBtn.getParent() instanceof HBox) {
                        profileButtonContainer = (HBox) profileBtn.getParent();
                        LOGGER.info("Found profileButtonContainer via profile button parent: " + profileButtonContainer.getId());
                    } else {
                        LOGGER.warning("Could not find profileButtonContainer in scene or element is not an HBox");
                    }
                }
            }

            // Try to find profile button if it's null
            if (profileButton == null) {
                Node node = scene.lookup(".profile-button-container"); // Use CSS class selector with . prefix
                if (node instanceof ProfileButton) {
                    profileButton = (ProfileButton) node;
                    LOGGER.info("Initialized profileButton via scene lookup with class");

                    // Initialize the profile button with current auth state
                    profileButton.refreshAuthState();
                } else {
                    // Try a different approach
                    LOGGER.warning("Could not find profileButton by class, trying to find it by ID");
                    if (profileButtonContainer != null && !profileButtonContainer.getChildren().isEmpty()) {
                        for (Node child : profileButtonContainer.getChildren()) {
                            if (child instanceof ProfileButton) {
                                profileButton = (ProfileButton) child;
                                LOGGER.info("Found profileButton as child of profileButtonContainer");

                                // Initialize the profile button with current auth state
                                profileButton.refreshAuthState();
                                break;
                            }
                        }
                    }
                }
            }

            // Output debugging info about the found elements
            if (loginButtonContainer != null) {
                LOGGER.info("loginButtonContainer found: " + loginButtonContainer.getId() + ", visible: " + loginButtonContainer.isVisible());
            }
            if (profileButtonContainer != null) {
                LOGGER.info("profileButtonContainer found: " + profileButtonContainer.getId() + ", visible: " + profileButtonContainer.isVisible());
            }
            if (profileButton != null) {
                LOGGER.info("profileButton found with classes: " + profileButton.getStyleClass());
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
        if (examPortalButton != null && examPortalButton.getScene() != null) {
            return examPortalButton.getScene();
        } else if (homeButton != null && homeButton.getScene() != null) {
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
        com.ueadmission.navigation.NavigationUtil.navigateToHome(event);
    }

    /**
     * Navigates to the About screen
     * @param event The event that triggered this action
     */
    private void navigateToAbout(ActionEvent event) {
        cleanup();
        com.ueadmission.navigation.NavigationUtil.navigateToAbout(event);
    }

    /**
     * Navigates to the Admission screen
     * @param event The event that triggered this action
     */
    private void navigateToAdmission(ActionEvent event) {
        cleanup();
        com.ueadmission.navigation.NavigationUtil.navigateToAdmission(event);
    }

    /**
     * Navigates to the Exam Portal screen (refresh)
     * @param event The event that triggered this action
     */
    private void navigateToExamPortal(ActionEvent event) {
        // Check if we're already on the exam portal screen
        if (examPortalButton != null && 
            examPortalButton.getScene() != null &&
            examPortalButton.getScene().getRoot().getId() != null &&
            examPortalButton.getScene().getRoot().getId().equals("examPortalRoot")) {

            LOGGER.info("Already on Exam Portal, refreshing instead of navigating");
            // Just refresh the UI instead of full navigation
            refreshUI();
            return;
        }
          // Check authentication before navigating
        if (!AuthStateManager.getInstance().isAuthenticated()) {
            LOGGER.info("User not authenticated, redirecting to login");
            navigateToLogin(event);
            return;
        }

        // Normal navigation flow
        cleanup();
        com.ueadmission.navigation.NavigationUtil.navigateToExamPortal(event);
    }

    /**
     * Navigates to the Contact screen
     * @param event The event that triggered this action
     */
    private void navigateToContact(ActionEvent event) {
        cleanup();
        com.ueadmission.navigation.NavigationUtil.navigateToContact(event);
    }

    /**
     * Navigates to the Login screen
     * @param event The event that triggered this action
     */
    private void navigateToLogin(ActionEvent event) {
        cleanup();
        com.ueadmission.navigation.NavigationUtil.navigateToLogin(event);
    }    /**
     * Handle mouse click navigation to Login in the footer or elsewhere
     * @param event The mouse event that triggered this action
     */
    @FXML
    public void navigateToLogin(javafx.scene.input.MouseEvent event) {
        cleanup();
        com.ueadmission.navigation.NavigationUtil.navigateToLogin(event);
    }

    /**
     * Navigates to the Application screen
     * @param event The event that triggered this action
     */
    @FXML
    public void navigateToApplication(ActionEvent event) {
        cleanup();
        com.ueadmission.navigation.NavigationUtil.navigateToApplications(event);
    }

    /**
     * Handle mouse click navigation to Home in the footer
     * @param event The mouse event that triggered this action
     */
    @FXML
    public void navigateToHome(javafx.scene.input.MouseEvent event) {
        cleanup();
        com.ueadmission.navigation.NavigationUtil.navigateToHome(event);
    }

    /**
     * Handle mouse click navigation to About in the footer
     * @param event The mouse event that triggered this action
     */
    @FXML
    public void navigateToAbout(javafx.scene.input.MouseEvent event) {
        cleanup();
        com.ueadmission.navigation.NavigationUtil.navigateToAbout(event);
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
            com.ueadmission.navigation.NavigationUtil.navigateToLogin(event);
            return;
        }

        cleanup();
        com.ueadmission.navigation.NavigationUtil.navigateToAdmission(event);
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
            com.ueadmission.navigation.NavigationUtil.navigateToLogin(event);
            return;
        }

        cleanup();
        com.ueadmission.navigation.NavigationUtil.navigateToExamPortal(event);
    }

    /**
     * Handle mouse click navigation to Contact in the footer
     * @param event The mouse event that triggered this action
     */
    @FXML
    public void navigateToContact(javafx.scene.input.MouseEvent event) {
        cleanup();
        com.ueadmission.navigation.NavigationUtil.navigateToContact(event);
    }
}
