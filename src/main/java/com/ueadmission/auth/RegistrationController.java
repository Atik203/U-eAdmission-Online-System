package com.ueadmission.auth;

import com.ueadmission.auth.state.AuthStateManager;
import com.ueadmission.auth.state.User;
import com.ueadmission.utils.MFXNotifications;

import io.github.palexdev.materialfx.controls.MFXButton;
import io.github.palexdev.materialfx.controls.MFXCheckbox;
import io.github.palexdev.materialfx.controls.MFXPasswordField;
import io.github.palexdev.materialfx.controls.MFXTextField;
import javafx.animation.FadeTransition;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.stage.Stage;
import javafx.util.Duration;


public class RegistrationController {
    
    @FXML
    private MFXTextField firstNameField;
    
    @FXML
    private MFXTextField lastNameField;
    
    @FXML
    private MFXTextField emailField;
    
    @FXML
    private MFXTextField phoneField;
    
    @FXML
    private MFXTextField addressField;
    
    @FXML
    private MFXTextField cityField;
    
    // Remove the country combo box and create a field to store the country value
    private final String country = "Bangladesh";
    
    @FXML
    private MFXPasswordField passwordField;
    
    @FXML
    private MFXPasswordField confirmPasswordField;
    
    @FXML
    private MFXCheckbox termsCheckbox;
    
    @FXML
    private Label errorLabel;
    
    @FXML
    private MFXButton homeButton;
    
    @FXML
    private MFXButton aboutButton;
    
    @FXML
    private MFXButton admissionButton;
    
    @FXML
    private MFXButton examPortalButton;
    
    @FXML
    private MFXButton contactButton;
    
    @FXML
    private MFXButton loginButton;
    
    // Role field removed from the controller
    
    @FXML
    public void initialize() {
        // Remove country dropdown initialization
        
        // Set up button actions for navigation
        admissionButton.setOnAction(event -> {
            if (AuthStateManager.getInstance().isAuthenticated()) {
                navigateToAdmission(event);
            } else {
                // Show error message or redirect to login
                errorLabel.setText("Please complete registration or login to access Admission!");
                errorLabel.setVisible(true);
            }
        });
        examPortalButton.setOnAction(event -> navigateToExamPortal(event));
        contactButton.setOnAction(event -> navigateToContact(event));
        
        // Style the terms checkbox
        termsCheckbox.setSelected(false);
    }

    /**
     * Navigates to the Home screen
     */
    @FXML
    public void navigateToHome(ActionEvent event) {
        com.ueadmission.navigation.NavigationUtil.navigateToHome(event);
    }
    
    /**
     * Navigates to the About screen
     */
    @FXML
    public void navigateToAbout(ActionEvent event) {
        com.ueadmission.navigation.NavigationUtil.navigateToAbout(event);
    }
    
    /**
     * Navigates to the Login screen
     */
    @FXML
    public void navigateToLogin(ActionEvent event) {
        com.ueadmission.navigation.NavigationUtil.navigateToLogin(event);
    }
    
    /**
     * Navigates to the Admission screen
     */
    @FXML
    public void navigateToAdmission(ActionEvent event) {
        com.ueadmission.navigation.NavigationUtil.navigateToAdmission(event);
    }
      /**
     * Navigates to the Exam Portal screen
     */
    @FXML
    public void navigateToExamPortal(ActionEvent event) {
        try {
            // Get current stage and its properties
            Stage currentStage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            double width = currentStage.getWidth();
            double height = currentStage.getHeight();
            double x = currentStage.getX();
            double y = currentStage.getY();
            boolean maximized = currentStage.isMaximized();
            
            // In a real app, this would navigate to the exam portal screen
            // For now, we'll just show an alert or log a message
            System.out.println("Navigate to Exam Portal (not implemented yet)");
            
            // Placeholder for future implementation
            // Stage examPortalWindow = ExamPortal.prepareExamPortalWindow(width, height, x, y, maximized);
            // if (examPortalWindow != null) {
            //     applyTransition(currentStage, examPortalWindow);
            // }
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Failed to navigate to exam portal: " + e.getMessage());
        }
    }
    
    /**
     * Navigates to the Contact screen
     */
    @FXML
    public void navigateToContact(ActionEvent event) {
        com.ueadmission.navigation.NavigationUtil.navigateToContact(event);
    }

    /**
     * Applies a smooth transition between stages
     */
    private void applyTransition(Stage currentStage, Stage newStage) {
        try {
            // Make the new stage ready but not visible yet
            newStage.setOpacity(0.0);
            newStage.show();
            
            // Use a fade transition for the new window
            FadeTransition fadeIn = new FadeTransition(Duration.millis(300), newStage.getScene().getRoot());
            fadeIn.setFromValue(0.0);
            fadeIn.setToValue(1.0);
            
            // Add a fade out transition for the current window
            FadeTransition fadeOut = new FadeTransition(Duration.millis(200), currentStage.getScene().getRoot());
            fadeOut.setFromValue(1.0);
            fadeOut.setToValue(0.0);
            
            // Start the fade out, then hide current stage when done
            fadeOut.setOnFinished(e -> {
                currentStage.hide();
                newStage.setOpacity(1.0);
                fadeIn.play();
                
                // Finally close the original stage after transition completes
                fadeIn.setOnFinished(f -> currentStage.close());
            });
            
            fadeOut.play();
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Failed to apply transition: " + e.getMessage());
            
            // Fallback to direct transition without animation
            currentStage.close();
            newStage.show();
        }
    }

    /**
     * Handles the form submission for registration
     */
    @FXML
    public void handleRegistration(ActionEvent event) {
        // Reset error state
        errorLabel.setVisible(false);
        
        // Validate form inputs
        if (isFormValid()) {
            // Check if email already exists
            if (UserDAO.emailExists(emailField.getText())) {
                errorLabel.setText("Email already exists. Please use a different email address.");
                errorLabel.setVisible(true);
                return;
            }
            
            // Create registration object with role hardcoded as "student"
            Registration registration = new Registration(
                firstNameField.getText(),
                lastNameField.getText(),
                emailField.getText(),
                phoneField.getText(),
                addressField.getText(),
                cityField.getText(),
                country, // Use the hardcoded country value
                passwordField.getText(),
                "student" // Role is always student for registrations
            );
            

            
            // Save to database
            boolean success = UserDAO.registerUser(registration);
            
            if (success) {
                // Get the user ID (it should be set by registerUser)
                // If ID is not available in the Registration object, use 0 as a default
                int userId = 0;
                try {
                    userId = registration.getId();
                } catch (Exception e) {
                    System.err.println("Warning: Could not get user ID, using default: " + e.getMessage());
                }
                
                // Create User object for auth state
                User user = new User(
                    userId,
                    firstNameField.getText(),
                    lastNameField.getText(),
                    emailField.getText(),
                    phoneField.getText(),
                    "student" // Role is always student for new registrations
                );
                
                // Auto login the user (without remember me)
                AuthStateManager.getInstance().login(user, false);
                
                // Show success notification
                MFXNotifications.showSuccess("Registration Successful",
                    "Welcome, " + firstNameField.getText() + "! Your account has been created successfully.");
                
                System.out.println("Registration successful for " + firstNameField.getText() + " " + 
                                  lastNameField.getText() + " (" + emailField.getText() + ")");
                
                // Redirect to home page directly since user is now logged in
                navigateToHome(event);
            } else {
                // Show error message
                errorLabel.setText("Registration failed. Please try again later.");
                errorLabel.setVisible(true);
                
                // Show error notification
                MFXNotifications.showError("Registration Failed",
                    "There was a problem creating your account. Please try again later.");
            }
        } else {
            // Show error message
            errorLabel.setVisible(true);
        }
    }
    
    /**
     * Validates all form inputs
     * @return true if all required fields are filled and valid
     */
    private boolean isFormValid() {
        // Check required fields
        if (isEmpty(firstNameField.getText()) || 
            isEmpty(lastNameField.getText()) ||
            isEmpty(emailField.getText()) ||
            isEmpty(phoneField.getText()) ||
            isEmpty(addressField.getText()) ||
            isEmpty(cityField.getText()) ||
            isEmpty(passwordField.getText()) ||
            isEmpty(confirmPasswordField.getText())) {
            
            errorLabel.setText("Please fill all required fields!");
            return false;
        }
        
        // Check email format
        if (!isValidEmail(emailField.getText())) {
            errorLabel.setText("Please enter a valid email address!");
            return false;
        }
        
        // Check password match
        if (!passwordField.getText().equals(confirmPasswordField.getText())) {
            errorLabel.setText("Passwords do not match!");
            return false;
        }
        
        // Check terms agreement
        if (!termsCheckbox.isSelected()) {
            errorLabel.setText("You must agree to the Terms and Conditions!");
            return false;
        }
        
        return true;
    }
    
    /**
     * Checks if a string is empty or null
     */
    private boolean isEmpty(String str) {
        return str == null || str.trim().isEmpty();
    }
    
    /**
     * Validates email format
     */
    private boolean isValidEmail(String email) {
        String emailRegex = "^[a-zA-Z0-9_+&*-]+(?:\\.[a-zA-Z0-9_+&*-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,7}$";
        return email.matches(emailRegex);
    }
}
