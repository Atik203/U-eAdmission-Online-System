

package com.ueadmission.auth;

import com.ueadmission.about.About;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.stage.Stage;
import io.github.palexdev.materialfx.controls.MFXButton;
import io.github.palexdev.materialfx.controls.MFXPasswordField;
import io.github.palexdev.materialfx.controls.MFXTextField;

public class Login {

    @FXML
    private MFXTextField emailField;

    @FXML
    private MFXPasswordField passwordField;

    @FXML
    private MFXButton loginButton;

    @FXML
    private MFXButton homeButton;

    @FXML
    private MFXButton aboutButton;

    @FXML
    private MFXButton admissionButton;

    @FXML
    private MFXButton mockTestButton;

    @FXML
    private MFXButton contactButton;

    @FXML
    private Hyperlink forgotPasswordLink;

    @FXML
    private Hyperlink createAccountLink;

    @FXML
    private Label errorLabel;

    @FXML
    public void initialize() {
        // Hide error label initially
        errorLabel.setVisible(false);
    }

    /**
     * Handles the login form submission
     */
    @FXML
    public void handleLogin(ActionEvent event) {
        // Reset error state
        errorLabel.setVisible(false);

        String email = emailField.getText();
        String password = passwordField.getText();

        // Check if fields are filled
        if (isEmpty(email) || isEmpty(password)) {
            errorLabel.setText("Please fill all required fields!");
            errorLabel.setVisible(true);
            return;
        }

        // Validate email format
        if (!isValidEmail(email)) {
            errorLabel.setText("Please enter a valid email address!");
            errorLabel.setVisible(true);
            return;
        }

        // Successfully logged in, navigate to main screen
        navigateToHome(event);
    }

    private void navigateToHome(ActionEvent event) {
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
