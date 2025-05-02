package com.ueadmission.contact;

import com.ueadmission.auth.state.AuthState;
import com.ueadmission.auth.state.AuthStateManager;
import com.ueadmission.components.ProfileButton;
import com.ueadmission.navigation.NavigationUtil;
import com.ueadmission.utils.MFXNotifications;

import io.github.palexdev.materialfx.controls.MFXButton;
import io.github.palexdev.materialfx.controls.MFXTextField;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.TextArea;
import javafx.scene.layout.HBox;

/**
 * Controller for the Contact page
 * Handles user interactions and navigation for the Contact screen
 */
public class ContactController {
    
    // UI Elements - Navigation
    @FXML private MFXButton homeButton;
    @FXML private MFXButton aboutButton;
    @FXML private MFXButton admissionButton;
    @FXML private MFXButton mockTestButton;
    @FXML private MFXButton contactButton;
    @FXML private MFXButton loginButton;
    
    // Authentication UI elements
    @FXML private HBox loginButtonContainer;
    @FXML private HBox profileButtonContainer;
    @FXML private ProfileButton profileButton;
    
    // Contact Form Elements
    @FXML private MFXTextField nameField;
    @FXML private MFXTextField emailField;
    @FXML private MFXTextField phoneField;
    @FXML private MFXTextField subjectField;
    @FXML private TextArea messageField;
    @FXML private MFXButton submitButton;
    
    /**
     * Initialize the controller.
     * This method is called automatically after the FXML file has been loaded.
     */
    @FXML
    public void initialize() {
        // Set up event handlers for navigation buttons
        homeButton.setOnAction(this::navigateToHome);
        aboutButton.setOnAction(this::navigateToAbout);
        admissionButton.setOnAction(this::navigateToAdmission);
        mockTestButton.setOnAction(this::navigateToMockTest);
        contactButton.setOnAction(event -> {}); // Already on contact page
        loginButton.setOnAction(this::navigateToLogin);
        
        // Set up contact form submission
        submitButton.setOnAction(this::handleContactFormSubmission);
        
        // Update auth UI based on current state
        refreshUI();
    }
    
    /**
     * Handles the contact form submission
     * @param event The action event
     */
    private void handleContactFormSubmission(ActionEvent event) {
        // Validate form fields
        if (validateContactForm()) {
            // In a real application, this would send the form data to a server
            // For now, we'll just show a success notification
            MFXNotifications.show("Message Sent", 
                    "Thank you for your message! We will get back to you soon.",
                    MFXNotifications.NotificationType.SUCCESS);
            
            // Clear the form fields
            clearContactForm();
        }
    }
    
    /**
     * Validates the contact form fields
     * @return true if all required fields are filled correctly, false otherwise
     */
    private boolean validateContactForm() {
        boolean isValid = true;
        
        // Check required fields: name, email, subject, message
        if (isEmpty(nameField.getText())) {
            MFXNotifications.show("Form Error", 
                    "Please enter your name.",
                    MFXNotifications.NotificationType.ERROR);
            nameField.requestFocus();
            isValid = false;
        } else if (isEmpty(emailField.getText()) || !isValidEmail(emailField.getText())) {
            MFXNotifications.show("Form Error", 
                    "Please enter a valid email address.",
                    MFXNotifications.NotificationType.ERROR);
            emailField.requestFocus();
            isValid = false;
        } else if (isEmpty(subjectField.getText())) {
            MFXNotifications.show("Form Error", 
                    "Please enter a subject.",
                    MFXNotifications.NotificationType.ERROR);
            subjectField.requestFocus();
            isValid = false;
        } else if (isEmpty(messageField.getText())) {
            MFXNotifications.show("Form Error", 
                    "Please enter your message.",
                    MFXNotifications.NotificationType.ERROR);
            messageField.requestFocus();
            isValid = false;
        }
        
        return isValid;
    }
    
    /**
     * Clears all form fields
     */
    private void clearContactForm() {
        nameField.clear();
        emailField.clear();
        phoneField.clear();
        subjectField.clear();
        messageField.clear();
    }
    
    /**
     * Refresh the UI based on authentication state
     * Called when the scene becomes active or when auth state changes
     */
    public void refreshUI() {
        AuthState authState = AuthStateManager.getInstance().getState();
        boolean isAuthenticated = (authState != null && authState.isAuthenticated());
        
        // Check if UI elements are initialized before accessing them
        if (loginButtonContainer != null) {
            loginButtonContainer.setVisible(!isAuthenticated);
            loginButtonContainer.setManaged(!isAuthenticated);
        }
        
        if (profileButtonContainer != null) {
            profileButtonContainer.setVisible(isAuthenticated);
            profileButtonContainer.setManaged(isAuthenticated);
        }
        
        // If authenticated, update the profile button
        if (isAuthenticated && profileButton != null) {
            profileButton.updateUIFromAuthState(authState);
        }
    }
    
    /**
     * Called when scene becomes visible or active
     * This ensures the UI is updated with current auth state
     */
    public void onSceneActive() {
        refreshUI();
    }
    
    /**
     * Navigates to the Home page
     * @param event The event that triggered this action
     */
    @FXML
    private void navigateToHome(ActionEvent event) {
        // Call cleanup first to ensure proper resource disposal
        cleanup();
        NavigationUtil.navigateToHome(event);
    }
    
    /**
     * Navigates to the About page
     * @param event The event that triggered this action
     */
    @FXML
    private void navigateToAbout(ActionEvent event) {
        // Call cleanup first to ensure proper resource disposal
        cleanup();
        NavigationUtil.navigateToAbout(event);
    }
    
    /**
     * Navigates to the Admission page
     * @param event The event that triggered this action
     */
    @FXML
    private void navigateToAdmission(ActionEvent event) {
        // Call cleanup first to ensure proper resource disposal
        cleanup();
        NavigationUtil.navigateToAdmission(event);
    }
    
    /**
     * Navigates to the Mock Test page
     * @param event The event that triggered this action
     */
    @FXML
    private void navigateToMockTest(ActionEvent event) {
        // Call cleanup first to ensure proper resource disposal
        cleanup();
        // Mock Test page navigation is not yet fully implemented
        System.out.println("Navigate to Mock Test (not implemented yet)");
    }
    
    /**
     * Navigates to the Login page
     * @param event The event that triggered this action
     */
    @FXML
    private void navigateToLogin(ActionEvent event) {
        // Call cleanup first to ensure proper resource disposal
        cleanup();
        NavigationUtil.navigateToLogin(event);
    }
    
    /**
     * Perform cleanup before navigation
     * Ensures resources are properly disposed
     */
    private void cleanup() {
        // Any cleanup needed before leaving the page
        // For example, cancelling any pending operations or freeing resources
    }
    
    /**
     * Checks if a string is empty or null
     * @param str The string to check
     * @return true if the string is null or empty, false otherwise
     */
    private boolean isEmpty(String str) {
        return str == null || str.trim().isEmpty();
    }
    
    /**
     * Validates email format
     * @param email The email to validate
     * @return true if the email is valid, false otherwise
     */
    private boolean isValidEmail(String email) {
        // Basic email validation using regex
        String emailRegex = "^[A-Za-z0-9+_.-]+@(.+)$";
        return email.matches(emailRegex);
    }
}
