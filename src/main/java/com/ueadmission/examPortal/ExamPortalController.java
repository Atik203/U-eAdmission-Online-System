package com.ueadmission.examPortal;

import java.util.logging.Level;
import java.util.logging.Logger;

import com.ueadmission.BaseController;
import com.ueadmission.auth.state.AuthState;
import com.ueadmission.auth.state.AuthStateManager;
import com.ueadmission.components.ProfileButton;
import com.ueadmission.utils.MFXNotifications;

import io.github.palexdev.materialfx.controls.MFXButton;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

/**
 * Controller for the Exam Portal page
 * Handles user interactions and navigation for the Exam Portal screen
 */
public class ExamPortalController extends BaseController {
    
    private static final Logger LOGGER = Logger.getLogger(ExamPortalController.class.getName());
    
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
    @FXML private MFXButton viewSyllabusButton;
    
    /**
     * Initialize the controller.
     * This method is called automatically after the FXML file has been loaded.
     */
    @FXML
    public void initialize() {
        // Check authentication first when page loads directly
        if (!AuthStateManager.getInstance().isAuthenticated()) {
            LOGGER.info("User not authenticated, redirecting to login");
            javafx.application.Platform.runLater(() -> {
                // Create a dummy ActionEvent with the homeButton as the source
                ActionEvent event = new ActionEvent(homeButton, ActionEvent.NULL_SOURCE_TARGET);
                navigateToLogin(event);
            });
            return;
        }
        
        // Set up navigation buttons
        homeButton.setOnAction(event -> navigateToHome(event));
        aboutButton.setOnAction(event -> navigateToAbout(event));
        
        // Improved: Exam Portal button acts as private route and does nothing if already on Exam Portal
        if (examPortalButton != null) {
            examPortalButton.setOnAction(event -> {
                // If already on Exam Portal, do nothing
                if (examPortalButton.getScene() != null &&
                    examPortalButton.getScene().getRoot().getId() != null &&
                    examPortalButton.getScene().getRoot().getId().equals("examPortalRoot")) {
                    return;
                }
                if (AuthStateManager.getInstance().isAuthenticated()) {
                    navigateToExamPortal(event);
                } else {
                    // Only show error if not already on login screen
                    if (examPortalButton.getScene() != null &&
                        examPortalButton.getScene().getRoot().getId() != null &&
                        examPortalButton.getScene().getRoot().getId().equals("loginRoot")) {
                        return;
                    }
                    MFXNotifications.showWarning("Authentication Required", "Please log in to access the Exam Portal.");
                }
            });
        }
        
        contactButton.setOnAction(event -> navigateToContact(event));
        
        // Configure login button if it exists
        if (loginButton != null) {
            loginButton.setOnAction(event -> navigateToLogin(event));
        }
        
        // Configure admission button if authenticated
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
        
        // TODO: Implement actual exam start logic
        LOGGER.info("User starting exam: " + examTitle);
    }
    
    /**
     * View exam or test syllabus
     * @param event The event that triggered this action
     */
    @FXML
    public void viewSyllabus(ActionEvent event) {
        // Open a URL to the syllabus page
        try {
            // For now, just show a notification
            MFXNotifications.showInfo("Syllabus", 
                    "The syllabus information will be displayed in a future update. For now, please visit our website.");
            
            // Get more information about which syllabus was requested
            MFXButton button = (MFXButton) event.getSource();
            
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
    @Override
    public void cleanup() {
        LOGGER.info("Cleaning up ExamPortalController before navigation");
        
        // Unsubscribe from auth state changes
        if (authStateListener != null) {
            AuthStateManager.getInstance().unsubscribe(authStateListener);
            LOGGER.info("Unsubscribed from auth state changes during cleanup");
        }
        
        super.cleanup();
    }
    
    /**
     * Subscribe to authentication state changes to update UI
     */
    private void subscribeToAuthStateChanges() {
        // Create auth state listener
        authStateListener = newState -> {
            LOGGER.info("Auth state change detected in ExamPortalController");
            
            boolean isAuthenticated = (newState != null && newState.isAuthenticated() && newState.getUser() != null);
            
            // Ensure we're on the JavaFX Application Thread for UI updates
            if (!javafx.application.Platform.isFxApplicationThread()) {
                javafx.application.Platform.runLater(() -> {
                    updateContainersVisibility(isAuthenticated);
                    
                    // Update profile button if authenticated
                    if (isAuthenticated && profileButton != null) {
                        profileButton.updateUIFromAuthState(newState);
                        LOGGER.info("Updated profile button from auth state listener");
                    }
                });
            } else {
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
     */
    public void onSceneActive() {
        LOGGER.info("Exam Portal scene became active, refreshing auth UI");
        refreshUI();
    }
    
    /**
     * Manually refresh the UI state based on current auth state
     */
    public void refreshUI() {
        // Get the current auth state
        AuthState currentState = AuthStateManager.getInstance().getState();
        boolean isAuthenticated = (currentState != null && currentState.isAuthenticated()
                && currentState.getUser() != null);
        
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
            }
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error updating UI components in ExamPortalController", e);
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
                }
                
                // Update profile container visibility
                if (profileButtonContainer != null) {
                    profileButtonContainer.setVisible(isAuthenticated);
                    profileButtonContainer.setManaged(isAuthenticated);
                }
            } catch (Exception e) {
                LOGGER.log(Level.SEVERE, "Error updating containers in ExamPortalController", e);
            }
        });
    }
      /**
     * Navigates to the Home screen
     * @param event The event that triggered this action
     */
    @FXML
    public void navigateToHome(ActionEvent event) {
        cleanup();
        super.navigateToHome(event);
    }
    
    /**
     * Navigates to the About screen
     * @param event The event that triggered this action
     */
    @FXML
    public void navigateToAbout(ActionEvent event) {
        cleanup();
        super.navigateToAbout(event);
    }
    
    /**
     * Navigates to the Admission screen
     * @param event The event that triggered this action
     */
    @FXML
    public void navigateToAdmission(ActionEvent event) {
        cleanup();
        super.navigateToAdmission(event);
    }
    
    /**
     * Navigates to the Exam Portal screen (refresh)
     * @param event The event that triggered this action
     */
    @FXML
    public void navigateToExamPortal(ActionEvent event) {
        cleanup();
        super.navigateToExamPortal(event);
    }
    
    /**
     * Navigates to the Contact screen
     * @param event The event that triggered this action
     */
    @FXML
    public void navigateToContact(ActionEvent event) {
        cleanup();
        super.navigateToContact(event);
    }

    
    /**
     * Navigates to the Login screen
     * @param event The event that triggered this action
     */
    @FXML
    public void navigateToLogin(ActionEvent event) {
        cleanup();
        super.navigateToLogin(event);
    }
    
    /**
     * Navigates to the Profile screen
     * @param event The event that triggered this action
     */
    @FXML
    public void navigateToProfile(ActionEvent event) {
        cleanup();
        super.navigateToProfile(event);
    }

    /**
     * Navigates to the Application screen
     * @param event The event that triggered this action
     */
    @FXML
    public void navigateToApplication(ActionEvent event) {
        cleanup();
        super.navigateToApplications(event);
    }
}
