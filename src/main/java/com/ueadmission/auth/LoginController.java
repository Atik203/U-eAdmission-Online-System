package com.ueadmission.auth;

import com.ueadmission.about.About;
import javafx.animation.FadeTransition;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.stage.Stage;
import javafx.util.Duration;
import io.github.palexdev.materialfx.controls.MFXButton;
import io.github.palexdev.materialfx.controls.MFXPasswordField;
import io.github.palexdev.materialfx.controls.MFXTextField;

public class LoginController {

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

        // Set up button actions for navigation
        admissionButton.setOnAction(event -> navigateToAdmission(event));
        mockTestButton.setOnAction(event -> navigateToMockTest(event));
        contactButton.setOnAction(event -> navigateToContact(event));
        createAccountLink.setOnAction(event -> navigateToRegistration(event));
    }

    /**
     * Navigates to the Home screen
     */
    @FXML
    public void navigateToHome(ActionEvent event) {
        try {
            // Get current stage and its properties
            Stage currentStage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            double width = currentStage.getWidth();
            double height = currentStage.getHeight();
            double x = currentStage.getX();
            double y = currentStage.getY();
            boolean maximized = currentStage.isMaximized();

            // Prepare the Main window before closing current one
            Stage mainStage = Auth.prepareMainWindow(width, height, x, y, maximized);

            // Apply smooth transition
            applyTransition(currentStage, mainStage);
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Failed to navigate to home: " + e.getMessage());
        }
    }

    /**
     * Navigates to the About screen
     */
    @FXML
    public void navigateToAbout(ActionEvent event) {
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

            // Apply smooth transition
            applyTransition(currentStage, aboutStage);
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Failed to navigate to about: " + e.getMessage());
        }
    }

    /**
     * Navigates to the Registration screen
     */
    @FXML
    public void navigateToRegistration(ActionEvent event) {
        try {
            // Get current stage and its properties
            Stage currentStage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            double width = currentStage.getWidth();
            double height = currentStage.getHeight();
            double x = currentStage.getX();
            double y = currentStage.getY();
            boolean maximized = currentStage.isMaximized();

            // Prepare the Registration window before closing current one
            Stage registrationStage = Auth.prepareRegistrationWindow(width, height, x, y, maximized);

            // Apply smooth transition
            applyTransition(currentStage, registrationStage);
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Failed to navigate to registration: " + e.getMessage());
        }
    }

    /**
     * Navigates to the Admission screen
     */
    @FXML
    public void navigateToAdmission(ActionEvent event) {
        try {
            // Get current stage and its properties
            Stage currentStage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            double width = currentStage.getWidth();
            double height = currentStage.getHeight();
            double x = currentStage.getX();
            double y = currentStage.getY();
            boolean maximized = currentStage.isMaximized();

            // Placeholder for future implementation
            System.out.println("Navigate to Admission (not implemented yet)");
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Failed to navigate to admission: " + e.getMessage());
        }
    }

    /**
     * Navigates to the Mock Test screen
     */
    @FXML
    public void navigateToMockTest(ActionEvent event) {
        try {
            // Get current stage and its properties
            Stage currentStage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            double width = currentStage.getWidth();
            double height = currentStage.getHeight();
            double x = currentStage.getX();
            double y = currentStage.getY();
            boolean maximized = currentStage.isMaximized();

            // Placeholder for future implementation
            System.out.println("Navigate to Mock Test (not implemented yet)");
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Failed to navigate to mock test: " + e.getMessage());
        }
    }

    /**
     * Navigates to the Contact screen
     */
    @FXML
    public void navigateToContact(ActionEvent event) {
        try {
            // Get current stage and its properties
            Stage currentStage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            double width = currentStage.getWidth();
            double height = currentStage.getHeight();
            double x = currentStage.getX();
            double y = currentStage.getY();
            boolean maximized = currentStage.isMaximized();

            // Placeholder for future implementation
            System.out.println("Navigate to Contact (not implemented yet)");
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Failed to navigate to contact: " + e.getMessage());
        }
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