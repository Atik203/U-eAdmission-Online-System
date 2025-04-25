package com.ueadmission.auth;

import com.ueadmission.about.About;
import javafx.animation.FadeTransition;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.stage.Stage;
import javafx.util.Duration;
import io.github.palexdev.materialfx.controls.MFXButton;
import io.github.palexdev.materialfx.controls.MFXPasswordField;
import io.github.palexdev.materialfx.controls.MFXTextField;

public class LoginController {

    @FXML private MFXTextField emailField;
    @FXML private MFXPasswordField passwordField;
    @FXML private MFXButton loginButton;
    @FXML private MFXButton logoutButton; // ✅ ADDED
    @FXML private MFXButton homeButton;
    @FXML private MFXButton aboutButton;
    @FXML private MFXButton admissionButton;
    @FXML private MFXButton mockTestButton;
    @FXML private MFXButton contactButton;
    @FXML private Hyperlink forgotPasswordLink;
    @FXML private Hyperlink createAccountLink;
    @FXML private Label errorLabel;
    @FXML private Label toastLabel;

    @FXML
    public void initialize() {
        errorLabel.setVisible(false);
        logoutButton.setVisible(false);
        toastLabel.setVisible(false);

        admissionButton.setOnAction(this::navigateToAdmission);
        mockTestButton.setOnAction(this::navigateToMockTest);
        contactButton.setOnAction(this::navigateToContact);
        createAccountLink.setOnAction(this::navigateToRegistration);
        logoutButton.setOnAction(event -> handleLogout());
    }

    @FXML
    public void handleLogin(ActionEvent event) {
        errorLabel.setVisible(false);

        String email = emailField.getText();
        String password = passwordField.getText();

        if (isEmpty(email) || isEmpty(password)) {
            errorLabel.setText("Please fill all required fields!");
            errorLabel.setVisible(true);
            return;
        }

        if (!isValidEmail(email)) {
            errorLabel.setText("Please enter a valid email address!");
            errorLabel.setVisible(true);
            return;
        }


        if (MyJDBC.authenticateUser(email, password)) {
            loginButton.setVisible(false);
            logoutButton.setVisible(true);
            showToast("Login successful!");
            navigateToHome(event);
        } else {
            errorLabel.setText("Invalid email or password!");
            errorLabel.setVisible(true);
        }
    }

    private void handleLogout() {
        loginButton.setVisible(true);
        logoutButton.setVisible(false);
        emailField.clear();
        passwordField.clear();
        errorLabel.setVisible(false);
        showToast("You have been logged out.");
    }

    private void showToast(String message) {
        toastLabel.setText(message);
        toastLabel.setVisible(true);

        FadeTransition fadeIn = new FadeTransition(Duration.seconds(0.5), toastLabel);
        fadeIn.setFromValue(0.0);
        fadeIn.setToValue(1.0);

        FadeTransition fadeOut = new FadeTransition(Duration.seconds(2), toastLabel);
        fadeOut.setFromValue(1.0);
        fadeOut.setToValue(0.0);
        fadeOut.setDelay(Duration.seconds(2));

        fadeIn.setOnFinished(e -> fadeOut.play());
        fadeOut.setOnFinished(e -> toastLabel.setVisible(false));

        fadeIn.play();
    }

    private boolean isEmpty(String str) {
        return str == null || str.trim().isEmpty();
    }

    private boolean isValidEmail(String email) {
        String emailRegex = "^[a-zA-Z0-9_+&*-]+(?:\\.[a-zA-Z0-9_+&*-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,7}$";
        return email.matches(emailRegex);
    }
     @FXML
    private void navigateToHome(ActionEvent event) {
        try {
            Stage currentStage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            double width = currentStage.getWidth();
            double height = currentStage.getHeight();
            double x = currentStage.getX();
            double y = currentStage.getY();
            boolean maximized = currentStage.isMaximized();

            Stage mainStage = Auth.prepareMainWindow(width, height, x, y, maximized);
            applyTransition(currentStage, mainStage);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Navigation methods (unchanged)
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
            Stage aboutWindow = About.prepareAboutWindow(width, height, x, y, maximized);
            if (aboutWindow != null) {
                // Apply smooth transition
                applyTransition(currentStage, aboutWindow);
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Failed to navigate to about: " + e.getMessage());
        }
    }
    @FXML
    public void navigateToRegistration(ActionEvent event) {
        try {
            Stage currentStage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            double width = currentStage.getWidth();
            double height = currentStage.getHeight();
            double x = currentStage.getX();
            double y = currentStage.getY();
            boolean maximized = currentStage.isMaximized();

            // Assuming Auth.prepareRegistrationWindow() loads the registration scene
            Stage registrationStage = Auth.prepareRegistrationWindow(width, height, x, y, maximized);

            applyTransition(currentStage, registrationStage);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void navigateToAdmission(ActionEvent event) { /* ... */ }
    public void navigateToMockTest(ActionEvent event) { /* ... */ }
    public void navigateToContact(ActionEvent event) { /* ... */ }

    private void applyTransition(Stage currentStage, Stage newStage) {
        try {
            newStage.setOpacity(0.0);
            newStage.show();

            FadeTransition fadeIn = new FadeTransition(Duration.millis(300), newStage.getScene().getRoot());
            fadeIn.setFromValue(0.0);
            fadeIn.setToValue(1.0);

            FadeTransition fadeOut = new FadeTransition(Duration.millis(200), currentStage.getScene().getRoot());
            fadeOut.setFromValue(1.0);
            fadeOut.setToValue(0.0);

            fadeOut.setOnFinished(e -> {
                currentStage.hide();
                newStage.setOpacity(1.0);
                fadeIn.play();
                fadeIn.setOnFinished(f -> currentStage.close());
            });

            fadeOut.play();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
