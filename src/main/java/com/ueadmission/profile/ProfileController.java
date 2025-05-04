package com.ueadmission.profile;

import java.util.concurrent.CompletableFuture;
import java.util.logging.Logger;

import com.ueadmission.auth.state.AuthState;
import com.ueadmission.auth.state.AuthStateManager;
import com.ueadmission.auth.state.User;
import com.ueadmission.components.ProfileButton;
import com.ueadmission.navigation.NavigationUtil;

import io.github.palexdev.materialfx.controls.MFXButton;
import io.github.palexdev.materialfx.controls.MFXSpinner;
import javafx.animation.FadeTransition;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;
import javafx.scene.text.Text;
import javafx.util.Duration;

/**
 * Controller for the profile page
 */
public class ProfileController {
    
    private static final Logger LOGGER = Logger.getLogger(ProfileController.class.getName());
    
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
    private Label addressLabel;
    
    @FXML
    private Label cityLabel;
    
    @FXML
    private Label countryLabel;
    
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
    
    @FXML
    private MFXButton editProfileButton;
    
    @FXML
    private MFXButton myApplicationsButton;
    
    @FXML
    private StackPane loaderContainer;
    
    @FXML
    private MFXSpinner spinner;
    
    @FXML
    private GridPane userInfoGrid;
    
    /**
     * Initialize the controller
     */
    @FXML
    public void initialize() {
        // Set up navigation button handlers using NavigationUtil
        homeButton.setOnAction(e -> navigateToHome(e));
        aboutButton.setOnAction(e -> navigateToAbout(e));
        
        // Add authentication check before navigating to admission
        admissionButton.setOnAction(e -> {
            if (AuthStateManager.getInstance().isAuthenticated()) {
                navigateToAdmission(e);
            } else {
                // Redirect to login if not authenticated
                navigateToLogin(e);
            }
        });
        
        mockTestButton.setOnAction(e -> navigateToMockTest(e));
        contactButton.setOnAction(e -> navigateToContact(e));
        
        editProfileButton.setOnAction(e -> handleEditProfile());
        
        // Set up My Applications button action - check if it exists first
        if (myApplicationsButton != null) {
            myApplicationsButton.setOnAction(e -> navigateToApplications(e));
        } else {
            LOGGER.info("myApplicationsButton is null in the FXML file - this element might have been removed");
        }
        
        // Make the user info grid initially hidden until data is loaded
        userInfoGrid.setVisible(false);
        userInfoGrid.setManaged(false);
        
        // Show loader and fetch user data
        showLoader();
        loadDataWithAnimation();
    }
    
    /**
     * Show the loader animation
     */
    private void showLoader() {
        loaderContainer.setVisible(true);
        loaderContainer.setManaged(true);
        
        FadeTransition fadeIn = new FadeTransition(Duration.millis(300), loaderContainer);
        fadeIn.setFromValue(0);
        fadeIn.setToValue(1);
        fadeIn.play();
        
        LOGGER.info("Showing loader animation");
    }
    
    /**
     * Hide the loader spinner with animation
     */
    private void hideLoader() {
        FadeTransition fadeOut = new FadeTransition(Duration.millis(200), loaderContainer);
        fadeOut.setFromValue(1);
        fadeOut.setToValue(0);
        fadeOut.setOnFinished(e -> {
            loaderContainer.setVisible(false);
            loaderContainer.setManaged(false);
        });
        fadeOut.play();
    }
    
    /**
     * Simulate loading data from database with a delay
     */
    private void simulateDataLoading() {
        CompletableFuture.runAsync(() -> {
            try {
                // Simulate a network delay
                Thread.sleep(1500);
                
                // Load data on the JavaFX Application Thread
                Platform.runLater(() -> {
                    loadUserData();
                    hideLoader();
                });
            } catch (InterruptedException e) {
                LOGGER.warning("Data loading simulation interrupted: " + e.getMessage());
                Thread.currentThread().interrupt();
            }
        });
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
            
            // Set additional address information
            addressLabel.setText(user.getAddress());
            cityLabel.setText(user.getCity());
            countryLabel.setText(user.getCountry());
            
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
            
            // Show user info grid after data is loaded
            userInfoGrid.setVisible(true);
            userInfoGrid.setManaged(true);
        } else {
            // Handle case when user is null (should not happen normally)
            LOGGER.severe("Failed to load user data: user object is null");
            Profile.showProfileLoadingError();
        }
    }
    
    /**
     * Load user data with animation effects
     */
    private void loadDataWithAnimation() {
        // Hide the user info initially
        userInfoGrid.setVisible(false);
        userInfoGrid.setManaged(false);
        
        // Use simulateDataLoading to fetch data with a delay and animations
        simulateDataLoading();
        
        LOGGER.info("Started loading user data with animation");
    }
    
    /**
     * Handle the edit profile button click
     */
    private void handleEditProfile() {
        // This is a placeholder for the edit profile functionality
        // In a real implementation, this would open an edit form
        Profile.showProfileUpdateSuccess();
    }
    
    /**
     * Called when scene becomes visible or active
     * This method is called by NavigationUtil when scene changes
     */
    public void onSceneActive() {
        LOGGER.info("Profile scene became active, refreshing user data");
        refreshUI();
    }
    
    /**
     * Refresh the UI with current auth state
     */
    public void refreshUI() {
        AuthState authState = AuthStateManager.getInstance().getState();
        if (authState != null && authState.isAuthenticated()) {
            loadUserData();
        } else {
            LOGGER.warning("User not authenticated, redirecting to login");
            // If somehow we got to the profile page without authentication, redirect to login
            Platform.runLater(() -> {
                if (homeButton.getScene() != null) {
                    NavigationUtil.navigateToLogin(new ActionEvent(homeButton, null));
                }
            });
        }
    }
    
    /**
     * Cleanup resources before navigating away
     */
    private void cleanup() {
        LOGGER.info("Cleaning up ProfileController before navigation");
        // Reset opacity on the scene root if available
        if (homeButton != null && homeButton.getScene() != null && 
                homeButton.getScene().getRoot() != null) {
            homeButton.getScene().getRoot().setOpacity(1.0);
        }
    }
    
    /**
     * Navigates to the Home screen
     */
    private void navigateToHome(ActionEvent event) {
        cleanup();
        NavigationUtil.navigateToHome(event);
    }
    
    /**
     * Navigates to the About screen
     */
    private void navigateToAbout(ActionEvent event) {
        cleanup();
        NavigationUtil.navigateToAbout(event);
    }
    
    /**
     * Navigates to the Admission screen
     */
    private void navigateToAdmission(ActionEvent event) {
        cleanup();
        NavigationUtil.navigateToAdmission(event);
    }
    
    /**
     * Navigates to the Mock Test screen
     */
    private void navigateToMockTest(ActionEvent event) {
        cleanup();
        // This is a placeholder - Mock Test isn't fully implemented yet
        System.out.println("Navigate to Mock Test page (not implemented yet)");
    }
    
    /**
     * Navigates to the Contact screen
     */
    private void navigateToContact(ActionEvent event) {
        cleanup();
        NavigationUtil.navigateToContact(event);
    }
    
    /**
     * Redirects to the Login screen
     */
    private void navigateToLogin(ActionEvent event) {
        cleanup();
        NavigationUtil.navigateToLogin(event);
    }
    
    /**
     * Navigates to the Applications screen
     */
    private void navigateToApplications(ActionEvent event) {
        cleanup();
        NavigationUtil.navigateToApplications(event);
    }
}
