package com.ueadmission;

import com.ueadmission.about.About;
import com.ueadmission.auth.Auth;
import io.github.palexdev.materialfx.controls.MFXButton;
import javafx.animation.FadeTransition;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.stage.Stage;
import javafx.util.Duration;

public class MainController {
    @FXML
    private Label welcomeText;
    
    @FXML
    private MFXButton aboutButton;
    
    @FXML
    private MFXButton learnMoreBtn;
    
    @FXML
    private MFXButton loginButton;
    
    @FXML
    private MFXButton applyNowBtn; // "Apply Now" button in the hero section
    
    @FXML
    private MFXButton learnMoreAboutUIUBtn; // "Learn More About UIU" button in the about section
    
    @FXML
    private void initialize() {
        // Set up the About button click action
        aboutButton.setOnAction(event -> openAboutPage(event));
        
        // Set up the Learn More button to also open the About window
        learnMoreBtn.setOnAction(event -> openAboutPage(event));
        
        // Set up the Login button click action
        loginButton.setOnAction(event -> openLoginPage(event));
        
        // Set up the Apply Now button to open login page
        if (applyNowBtn != null) {
            applyNowBtn.setOnAction(event -> openLoginPage(event));
        }
        
        // Set up the Learn More About UIU button to open about page
        if (learnMoreAboutUIUBtn != null) {
            learnMoreAboutUIUBtn.setOnAction(event -> openAboutPage(event));
        }
    }
    
    /**
     * Opens the About page
     * @param event The event that triggered this action
     */
    private void openAboutPage(javafx.event.ActionEvent event) {
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
            
            // Make the new stage ready but not visible yet
            aboutStage.setOpacity(0.0);
            aboutStage.show();
            
            // Use a fade transition for the new window
            javafx.animation.FadeTransition fadeIn = new javafx.animation.FadeTransition(
                    javafx.util.Duration.millis(300), aboutStage.getScene().getRoot());
            fadeIn.setFromValue(0.0);
            fadeIn.setToValue(1.0);
            
            // Add a fade out transition for the current window
            javafx.animation.FadeTransition fadeOut = new javafx.animation.FadeTransition(
                    javafx.util.Duration.millis(200), currentStage.getScene().getRoot());
            fadeOut.setFromValue(1.0);
            fadeOut.setToValue(0.0);
            
            // Start the fade out, then hide current stage when done
            fadeOut.setOnFinished(e -> {
                currentStage.hide();
                aboutStage.setOpacity(1.0);
                fadeIn.play();
                
                // Finally close the original stage after transition completes
                fadeIn.setOnFinished(f -> currentStage.close());
            });
            
            fadeOut.play();
            
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Failed to navigate to about: " + e.getMessage());
        }
    }
    
    /**
     * Opens the Login page
     * @param event The event that triggered this action
     */
    private void openLoginPage(javafx.event.ActionEvent event) {
        try {
            // Get current stage and its properties
            Stage currentStage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            double width = currentStage.getWidth();
            double height = currentStage.getHeight();
            double x = currentStage.getX();
            double y = currentStage.getY();
            boolean maximized = currentStage.isMaximized();
            
            // Prepare the Login window before closing current one
            Stage loginStage = Auth.prepareLoginWindow(width, height, x, y, maximized);
            
            // Check if loginStage is null before proceeding
            if (loginStage == null) {
                System.err.println("Failed to create login stage. Login window couldn't be prepared.");
                return;
            }
            
            // Make the new stage ready but not visible yet
            loginStage.setOpacity(0.0);
            loginStage.show();
            
            // Use a fade transition for the new window
            javafx.animation.FadeTransition fadeIn = new javafx.animation.FadeTransition(
                    javafx.util.Duration.millis(300), loginStage.getScene().getRoot());
            fadeIn.setFromValue(0.0);
            fadeIn.setToValue(1.0);
            
            // Add a fade out transition for the current window
            javafx.animation.FadeTransition fadeOut = new javafx.animation.FadeTransition(
                    javafx.util.Duration.millis(200), currentStage.getScene().getRoot());
            fadeOut.setFromValue(1.0);
            fadeOut.setToValue(0.0);
            
            // Start the fade out, then hide current stage when done
            fadeOut.setOnFinished(e -> {
                currentStage.hide();
                loginStage.setOpacity(1.0);
                fadeIn.play();
                
                // Finally close the original stage after transition completes
                fadeIn.setOnFinished(f -> currentStage.close());
            });
            
            fadeOut.play();
            
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Failed to navigate to login: " + e.getMessage());
        }
    }

    @FXML
    protected void onHelloButtonClick() {
        welcomeText.setText("Welcome to JavaFX Application!");
    }
    
    /**
     * Helper method to find a button by its ID in the scene graph
     * @param id The ID of the button to find
     * @return The found button or null if not found
     */
    private MFXButton findButtonById(String id) {
        try {
            return (MFXButton) welcomeText.getScene().lookup("#" + id);
        } catch (Exception e) {
            System.err.println("Could not find button with ID: " + id);
            return null;
        }
    }
    
    /**
     * Helper method to find a button by its text in the scene graph
     * @param text The text of the button to find
     * @return The found button or null if not found
     */
    private MFXButton findButtonByText(String text) {
        try {
            // Find all MFXButtons in the scene
            for (Node node : welcomeText.getScene().getRoot().lookupAll(".mfx-button, .mfx-button-outline")) {
                if (node instanceof MFXButton button && text.equals(button.getText())) {
                    return button;
                }
            }
            return null;
        } catch (Exception e) {
            System.err.println("Could not find button with text: " + text);
            return null;
        }
    }
}
