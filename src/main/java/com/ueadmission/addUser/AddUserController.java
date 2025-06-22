package com.ueadmission.addUser;

import java.net.URL;
import java.util.ResourceBundle;

import com.ueadmission.auth.Registration;
import com.ueadmission.auth.UserDAO;
import com.ueadmission.auth.state.AuthState;
import com.ueadmission.auth.state.AuthStateManager;
import com.ueadmission.auth.state.User;
import com.ueadmission.navigation.NavigationUtil;
import com.ueadmission.utils.MFXNotifications;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import io.github.palexdev.materialfx.controls.MFXButton;
import io.github.palexdev.materialfx.controls.MFXPasswordField;
import io.github.palexdev.materialfx.controls.MFXTextField;
import com.ueadmission.components.ProfileButton;

/**
 * Controller for the Add User page
 */
public class AddUserController implements Initializable {

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
    private HBox profileButtonContainer;

    @FXML
    private ProfileButton profileButton;

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

    // Country is hardcoded as Bangladesh
    private final String country = "Bangladesh";

    @FXML
    private MFXPasswordField passwordField;

    @FXML
    private MFXPasswordField confirmPasswordField;

    @FXML
    private Label errorLabel;

    @FXML
    private Label successLabel;

    @FXML
    private MFXButton addUserButton;

    /**
     * Initialize the controller
     */
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Set up navigation button handlers
        homeButton.setOnAction(this::navigateToHome);
        aboutButton.setOnAction(this::navigateToAbout);
        admissionButton.setOnAction(this::navigateToAdmission);
        examPortalButton.setOnAction(this::navigateToExamPortal);
        contactButton.setOnAction(this::navigateToContact);

        // Update UI based on authentication state
        refreshUI();
    }

    /**
     * Refresh the UI based on authentication state
     */
    public void refreshUI() {
        AuthState state = AuthStateManager.getInstance().getState();
        boolean isAuthenticated = (state != null && state.isAuthenticated());

        // If authenticated, refresh the profile button
        if (isAuthenticated && profileButton != null) {
            profileButton.refreshAuthState();
        }
    }

    /**
     * Navigate to home page
     */
    private void navigateToHome(ActionEvent event) {
        NavigationUtil.navigateToHome(event);
    }

    /**
     * Navigate to about page
     */
    private void navigateToAbout(ActionEvent event) {
        NavigationUtil.navigateToAbout(event);
    }

    /**
     * Navigate to admission page
     */
    private void navigateToAdmission(ActionEvent event) {
        NavigationUtil.navigateToAdmission(event);
    }

    /**
     * Navigate to exam portal page
     */
    private void navigateToExamPortal(ActionEvent event) {
        NavigationUtil.navigateToExamPortal(event);
    }

    /**
     * Navigate to contact page
     */
    private void navigateToContact(ActionEvent event) {
        NavigationUtil.navigateToContact(event);
    }

    /**
     * Handles the form submission for adding a new admin user
     */
    @FXML
    public void handleAddUser(ActionEvent event) {
        // Reset error and success states
        errorLabel.setVisible(false);
        successLabel.setVisible(false);

        // Validate form inputs
        if (isFormValid()) {
            // Check if email already exists
            if (UserDAO.emailExists(emailField.getText())) {
                errorLabel.setText("Email already exists. Please use a different email address.");
                errorLabel.setVisible(true);
                return;
            }

            // Create registration object with role hardcoded as "admin"
            Registration registration = new Registration(
                firstNameField.getText(),
                lastNameField.getText(),
                emailField.getText(),
                phoneField.getText(),
                addressField.getText(),
                cityField.getText(),
                country, // Use the hardcoded country value
                passwordField.getText(),
                "admin" // Role is always admin for users created here
            );

            // Save to database
            boolean success = UserDAO.registerUser(registration);

            if (success) {
                // Create personalized success message
                String fullName = firstNameField.getText() + " " + lastNameField.getText();
                successLabel.setText("Admin user " + fullName + " has been added successfully!");
                successLabel.setVisible(true);

                System.out.println("Admin user added successfully: " + firstNameField.getText() + " " + 
                                  lastNameField.getText() + " (" + emailField.getText() + ")");

                // Clear the form
                clearForm();
            } else {
                // Show error message
                errorLabel.setText("Failed to add user. Please try again later.");
                errorLabel.setVisible(true);

                // Show error notification
                MFXNotifications.showError("User Addition Failed",
                    "There was a problem creating the admin user. Please try again later.");
            }
        } else {
            // Show error message
            errorLabel.setVisible(true);
        }
    }

    /**
     * Clears all form fields
     */
    private void clearForm() {
        firstNameField.clear();
        lastNameField.clear();
        emailField.clear();
        phoneField.clear();
        addressField.clear();
        cityField.clear();
        passwordField.clear();
        confirmPasswordField.clear();
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
