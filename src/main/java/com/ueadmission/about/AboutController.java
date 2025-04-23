package com.ueadmission.about;

import com.ueadmission.auth.Auth;
import io.github.palexdev.materialfx.controls.MFXButton;
import javafx.animation.FadeTransition;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.awt.Desktop;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

public class AboutController {
    
    @FXML
    private ImageView campusImage;
    
    @FXML
    private MFXButton visitWebsiteBtn;
    
    @FXML
    private MFXButton homeButton;
    
    @FXML
    private MFXButton loginButton;
    
    @FXML
    private MFXButton admissionButton;
    
    @FXML
    private MFXButton mockTestButton;
    
    @FXML
    private MFXButton contactButton;
    
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
        if (admissionButton != null) {
            admissionButton.setOnAction(event -> System.out.println("Admission button clicked"));
        }
        
        if (mockTestButton != null) {
            mockTestButton.setOnAction(event -> System.out.println("Mock Test button clicked"));
        }
        
        if (contactButton != null) {
            contactButton.setOnAction(event -> System.out.println("Contact button clicked"));
        }
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
        try {
            // Get current stage and its properties
            Stage currentStage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            double width = currentStage.getWidth();
            double height = currentStage.getHeight();
            double x = currentStage.getX();
            double y = currentStage.getY();
            boolean maximized = currentStage.isMaximized();
            
            // Prepare the Main window before closing current one
            Stage mainStage = About.prepareMainWindow(width, height, x, y, maximized);
            
            // Make the new stage ready but not visible yet
            mainStage.setOpacity(0.0);
            mainStage.show();
            
            // Use a fade transition for the new window
            FadeTransition fadeIn = new FadeTransition(Duration.millis(300), mainStage.getScene().getRoot());
            fadeIn.setFromValue(0.0);
            fadeIn.setToValue(1.0);
            
            // Add a fade out transition for the current window
            FadeTransition fadeOut = new FadeTransition(Duration.millis(200), currentStage.getScene().getRoot());
            fadeOut.setFromValue(1.0);
            fadeOut.setToValue(0.0);
            
            // Start the fade out, then hide current stage when done
            fadeOut.setOnFinished(e -> {
                currentStage.hide();
                mainStage.setOpacity(1.0);
                fadeIn.play();
                
                // Finally close the original stage after transition completes
                fadeIn.setOnFinished(f -> currentStage.close());
            });
            
            fadeOut.play();
            
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Failed to navigate to home: " + e.getMessage());
        }
    }
    
    /**
     * Navigates to the login page with transition effects
     * @param event The event that triggered this action
     */
    private void navigateToLogin(javafx.event.ActionEvent event) {
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
            
            // Make the new stage ready but not visible yet
            loginStage.setOpacity(0.0);
            loginStage.show();
            
            // Use a fade transition for the new window
            FadeTransition fadeIn = new FadeTransition(Duration.millis(300), loginStage.getScene().getRoot());
            fadeIn.setFromValue(0.0);
            fadeIn.setToValue(1.0);
            
            // Add a fade out transition for the current window
            FadeTransition fadeOut = new FadeTransition(Duration.millis(200), currentStage.getScene().getRoot());
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
}
