package com.ueadmission.profile;

import java.io.IOException;

import com.ueadmission.auth.state.AuthStateManager;
import com.ueadmission.auth.state.User;
import com.ueadmission.components.ProfileButton;

import io.github.palexdev.materialfx.controls.MFXButton;
import javafx.animation.FadeTransition;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.util.Duration;

/**
 * Controller for the profile page
 */
public class ProfileController {
    
    @FXML
    private Text initialsText;
    
    @FXML
    private Label nameLabel;
    
    @FXML
    private Label roleLabel;
    
    @FXML
    private Label emailLabel;
    
    @FXML
    private Label firstNameLabel;
    
    @FXML
    private Label lastNameLabel;
    
    @FXML
    private Label profileEmailLabel;
    
    @FXML
    private Label phoneLabel;
    
    @FXML
    private Label profileRoleLabel;
    
    @FXML
    private MFXButton homeButton;
    
    @FXML
    private MFXButton aboutButton;
    
    @FXML
    public MFXButton admissionButton;
    
    @FXML
    private MFXButton mockTestButton;
    
    @FXML
    private MFXButton contactButton;
    
    @FXML
    private ProfileButton profileButton;
    
    /**
     * Initialize the controller
     */
    @FXML
    public void initialize() {
        // Set up navigation button handlers using NavigationUtil
        homeButton.setOnAction(e -> com.ueadmission.navigation.NavigationUtil.navigateToHome(e));
        aboutButton.setOnAction(e -> com.ueadmission.navigation.NavigationUtil.navigateToAbout(e));
        admissionButton.setOnAction(e -> com.ueadmission.navigation.NavigationUtil.navigateToAdmission(e));
        mockTestButton.setOnAction(e -> System.out.println("Navigate to Mock Test page"));
        contactButton.setOnAction(e -> System.out.println("Navigate to Contact page"));
        
        // Load user data
        loadUserData();
    }
    
    /**
     * Loads the current user's data into the profile page
     */
    private void loadUserData() {
        User user = AuthStateManager.getInstance().getState().getUser();
        
        if (user != null) {
            // Set user info in the header
            String fullName = user.getFirstName() + " " + user.getLastName();
            nameLabel.setText(fullName);
            roleLabel.setText(user.getRole());
            emailLabel.setText(user.getEmail());
            
            // Set user details in the personal information tab
            firstNameLabel.setText(user.getFirstName());
            lastNameLabel.setText(user.getLastName());
            profileEmailLabel.setText(user.getEmail());
            phoneLabel.setText(user.getPhoneNumber());
            profileRoleLabel.setText(user.getRole());
            
            // Set initials for the avatar
            String initials = "";
            if (user.getFirstName() != null && !user.getFirstName().isEmpty()) {
                initials += user.getFirstName().charAt(0);
            }
            if (user.getLastName() != null && !user.getLastName().isEmpty()) {
                initials += user.getLastName().charAt(0);
            }
            initialsText.setText(initials.toUpperCase());
        }
    }
    
    /**
     * Navigate to a different page
     */
    private void navigateTo(String fxmlFile, String title) {
        try {
            // Get the current stage
            Stage stage = (Stage) homeButton.getScene().getWindow();
            
            // Load the target screen
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com.ueadmission/" + fxmlFile));
            Parent root = loader.load();
            
            // Create a new scene
            Scene scene = new Scene(root, stage.getWidth(), stage.getHeight());
            
            // Set the scene to the stage
            stage.setScene(scene);
            stage.setTitle(title);
            
            // Apply fade-in transition
            FadeTransition fadeIn = new FadeTransition(Duration.millis(300), root);
            fadeIn.setFromValue(0.0);
            fadeIn.setToValue(1.0);
            fadeIn.play();
            
        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("Failed to navigate to " + fxmlFile + ": " + e.getMessage());
        }
    }
}
