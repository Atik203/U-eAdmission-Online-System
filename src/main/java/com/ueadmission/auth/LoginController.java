package com.ueadmission.auth;

import java.io.IOException;
import java.time.LocalDateTime;

import com.ueadmission.auth.state.AuthState;
import com.ueadmission.auth.state.AuthStateManager;
import com.ueadmission.auth.state.User;
import com.ueadmission.utils.IPAddressUtil;
import com.ueadmission.utils.MFXNotifications;

import io.github.palexdev.materialfx.controls.MFXButton;
import io.github.palexdev.materialfx.controls.MFXPasswordField;
import io.github.palexdev.materialfx.controls.MFXTextField;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.stage.Stage;

public class LoginController extends BaseController {
    
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
        
        // Set up navigation buttons
        if (homeButton != null) {
            homeButton.setOnAction(event -> navigateToHome(event));
        }
        
        if (aboutButton != null) {
            aboutButton.setOnAction(event -> navigateToAbout(event));
        }
        
        if (admissionButton != null) {
            admissionButton.setOnAction(event -> {
                if (AuthStateManager.getInstance().isAuthenticated()) {
                    navigateToAdmission(event);
                } else {
                    // Already on login page, so no need to navigate
                    errorLabel.setText("Please login to access Admission!");
                    errorLabel.setVisible(true);
                }
            });
        }
        
        if (mockTestButton != null) {
            mockTestButton.setOnAction(event -> navigateToMockTest(event));
        }
        
        if (contactButton != null) {
            contactButton.setOnAction(event -> navigateToContact(event));
        }
    }
    
    // Add checkbox for remember me
    @FXML
    private CheckBox rememberMeCheckbox;
    
    /**
     * Handles the login form submission
     */
    @FXML
    public void handleLogin(ActionEvent event) {
        // Reset error state
        errorLabel.setVisible(false);
        
        String email = emailField.getText();
        String password = passwordField.getText();
        boolean rememberMe = rememberMeCheckbox != null && rememberMeCheckbox.isSelected();
        
        // Check if fields are filled
        if (isEmpty(email) || isEmpty(password)) {
            errorLabel.setText("Please fill all required fields!");
            errorLabel.setVisible(true);
            return;
        }
        
        // Validate against database
        Registration userRecord = UserDAO.authenticateUser(email, password);
        
        if (userRecord != null) {
            // Check if user is already logged in
            if (userRecord.isAlreadyLoggedIn()) {
                errorLabel.setText("This account is already logged in from another device!");
                errorLabel.setVisible(true);
                
                // Show error notification
                MFXNotifications.showError("Login Failed", 
                    "This account is already active on another device or browser. " +
                    "Please log out from other sessions or contact support.");
                
                // Log to console
                System.err.println("Login prevented - user " + email + " is already logged in");
                return;
            }
            
            // Get client IP address
            String ipAddress = IPAddressUtil.getClientIPAddress();
            
            // Create User object for AuthStateManager
            // Use a default ID if not available
            int userId = 0;
            try {
                userId = userRecord.getId();
            } catch (Exception e) {
                System.err.println("Warning: Could not get user ID, using default: " + e.getMessage());
            }
            
            // Update the user's login status in the database first
            boolean statusUpdated = UserDAO.updateLoginStatus(userId, ipAddress, true);
            if (!statusUpdated) {
                System.err.println("Warning: Failed to update login status in database");
            }
            
            // Create the User object with login tracking information
            User user = new User(
                userId,
                userRecord.getFirstName(), 
                userRecord.getLastName(), 
                userRecord.getEmail(),
                userRecord.getPhone(),
                userRecord.getAddress(),
                userRecord.getCity(),
                userRecord.getCountry(),
                userRecord.getRole(),
                ipAddress,
                LocalDateTime.now(),
                true
            );
            
            // Update global auth state
            AuthStateManager.getInstance().login(user, rememberMe);
            
            // Show success notification
            MFXNotifications.showSuccess("Login Successful",
                "Welcome back, " + user.getFirstName() + "! You've successfully logged in from " + ipAddress);
            
            // Log to console
            System.out.println("Login successful for " + user.getFullName() + 
                             " (" + user.getEmail() + ") with role: " + user.getRole() + 
                             " from IP: " + ipAddress);
            
            // Log the authentication state before navigation
            System.out.println("Authentication state before navigation: " + 
                               AuthStateManager.getInstance().isAuthenticated());
            System.out.println("User info: " + user.getFirstName() + " " + user.getLastName());
            
            // Instead of creating a new window, update the existing one
            try {
                // Get the current stage
                Stage currentStage = (Stage) ((Node) event.getSource()).getScene().getWindow();
                
                // Load the main screen
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/com.ueadmission/main.fxml"));
                Parent root = loader.load();
                
                // Set up scene
                Scene scene = new Scene(root, currentStage.getWidth(), currentStage.getHeight());
                currentStage.setScene(scene);
                currentStage.setTitle("UeAdmission - Home");
                
                // Get reference to MainController if possible
                Object controller = loader.getController();
                if (controller != null) {
                    System.out.println("Main controller loaded: " + controller.getClass().getName());
                    // Try to find and update ProfileButton directly by searching the scene
                    try {
                        // Get the scene
                         scene = currentStage.getScene();
                        if (scene != null) {
                            // Search for ProfileButton or containers
                            updateUIBasedOnAuthState(scene);
                            System.out.println("Attempted to update UI elements directly");
                        }
                    } catch (Exception e) {
                        System.out.println("Could not update UI elements: " + e.getMessage());
                        
                        // Try calling refreshUI method if available as a fallback
                        try {
                            java.lang.reflect.Method refreshMethod = controller.getClass().getMethod("refreshUI");
                            refreshMethod.invoke(controller);
                            System.out.println("Called refreshUI on controller");
                        } catch (Exception e2) {
                            System.out.println("Could not call refreshUI: " + e2.getMessage());
                            
                            // Fall back to trying onSceneActive if available
                            try {
                                java.lang.reflect.Method method = controller.getClass().getMethod("onSceneActive");
                                method.invoke(controller);
                                System.out.println("Called onSceneActive as fallback");
                            } catch (Exception e3) {
                                System.out.println("Could not call onSceneActive: " + e3.getMessage());
                            }
                        }
                    }
                    
                    // Force the scene to redraw
                    currentStage.getScene().getRoot().requestLayout();
                }
                
                // Log auth state after setup
                System.out.println("Authentication state after scene setup: " + 
                                  AuthStateManager.getInstance().isAuthenticated());
                
                // Add stage close handler to properly log out when window is closed
                currentStage.setOnCloseRequest(windowEvent -> {
                    // Log out the user when the application is closed
                    if (user != null && AuthStateManager.getInstance().isAuthenticated()) {
                        System.out.println("Application closing, logging out user: " + user.getEmail());
                        UserDAO.logoutUser(user.getId());
                    }
                });
                
                // Show the stage
                currentStage.show();
                
            } catch (IOException e) {
                e.printStackTrace();
                System.err.println("Error navigating to home screen: " + e.getMessage());
                // Fall back to original navigation
                navigateToHome(event);
            }
        } else {
            // Show error message
            errorLabel.setText("Invalid email or password!");
            errorLabel.setVisible(true);
            
            // Show error notification
            MFXNotifications.showError("Login Failed", "Invalid email or password. Please try again.");
            
            // Log to console
            System.err.println("Login failed for email: " + email);
        }
    }
    
    protected void onInitialize() {
        // Add any initialization logic here if needed
    }

    /**
     * Directly update UI elements in the scene based on auth state
     */
    private void updateUIBasedOnAuthState(Scene scene) {
        // Get current auth state
        AuthState state = AuthStateManager.getInstance().getState();
        boolean isAuthenticated = AuthStateManager.getInstance().isAuthenticated();
        
        System.out.println("Updating UI based on auth state: " + isAuthenticated);
        
        // Update on JavaFX thread to be safe
        javafx.application.Platform.runLater(() -> {
            try {
                // First, try with ID lookup which is more reliable
                javafx.scene.Node loginContainer = scene.lookup("#loginButtonContainer");
                javafx.scene.Node profileContainer = scene.lookup("#profileButtonContainer");
                
                System.out.println("Login container found: " + (loginContainer != null));
                System.out.println("Profile container found: " + (profileContainer != null));
                
                if (loginContainer != null) {
                    loginContainer.setVisible(!isAuthenticated);
                    loginContainer.setManaged(!isAuthenticated);
                    System.out.println("Updated login container visibility to: " + !isAuthenticated);
                } else {
                    // If container not found by ID, try to find login button directly
                    javafx.scene.Node loginBtn = scene.lookup("#loginButton");
                    if (loginBtn != null) {
                        loginBtn.setVisible(!isAuthenticated);
                        loginBtn.setManaged(!isAuthenticated);
                        System.out.println("Updated login button visibility directly: " + !isAuthenticated);
                    }
                }
                
                if (profileContainer != null) {
                    profileContainer.setVisible(isAuthenticated);
                    profileContainer.setManaged(isAuthenticated);
                    System.out.println("Updated profile container visibility to: " + isAuthenticated);
                    
                    // Look for ProfileButton inside the container
                    if (profileContainer instanceof javafx.scene.Parent) {
                        for (javafx.scene.Node child : ((javafx.scene.Parent)profileContainer).getChildrenUnmodifiable()) {
                            if (child instanceof com.ueadmission.components.ProfileButton) {
                                com.ueadmission.components.ProfileButton profileBtn = 
                                    (com.ueadmission.components.ProfileButton) child;
                                profileBtn.updateUIFromAuthState(state);
                                System.out.println("Updated ProfileButton directly from scene lookup");
                                break;
                            }
                        }
                    }
                }
                
                // If we couldn't find by ID, try looking for the profile-button-container class
                for (javafx.scene.Node node : scene.getRoot().lookupAll(".profile-button-container")) {
                    node.setVisible(isAuthenticated);
                    node.setManaged(isAuthenticated);
                    System.out.println("Found and updated profile component by class selector");
                    
                    if (node instanceof com.ueadmission.components.ProfileButton) {
                        com.ueadmission.components.ProfileButton profileBtn = 
                            (com.ueadmission.components.ProfileButton) node;
                        profileBtn.updateUIFromAuthState(state);
                        System.out.println("Updated ProfileButton found by class selector");
                    }
                }
                
                // Also look for all login-related UI elements
                for (javafx.scene.Node node : scene.getRoot().lookupAll(".mfx-button-login")) {
                    node.setVisible(!isAuthenticated);
                    node.setManaged(!isAuthenticated);
                    System.out.println("Found and updated login button by class selector");
                }
            } catch (Exception e) {
                System.err.println("Error directly updating UI elements: " + e.getMessage());
                e.printStackTrace();
            }
        });
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
     * Navigates to the Admission screen
     */
    @FXML
    public void navigateToAdmission(ActionEvent event) {
        com.ueadmission.navigation.NavigationUtil.navigateToAdmission(event);
    }
    
    /**
     * Navigates to the Mock Test screen
     */
    @FXML
    public void navigateToMockTest(ActionEvent event) {
        // This is a placeholder for Mock Test navigation
        System.out.println("Navigate to Mock Test (not implemented yet)");
    }
    
    /**
     * Navigates to the Contact screen
     */
    @FXML
    public void navigateToContact(ActionEvent event) {
        com.ueadmission.navigation.NavigationUtil.navigateToContact(event);
    }
    
    /**
     * Navigates to the Registration screen
     */
    @FXML
    public void navigateToRegistration(ActionEvent event) {
        com.ueadmission.navigation.NavigationUtil.navigateToRegistration(event);
    }
    
    /**
     * Opens the Login page
     * @param event The event that triggered this action
     */
    private void navigateToLogin(javafx.event.ActionEvent event) {
        com.ueadmission.navigation.NavigationUtil.navigateToLogin(event);
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
