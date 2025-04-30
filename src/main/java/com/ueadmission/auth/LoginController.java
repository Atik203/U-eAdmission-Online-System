package com.ueadmission.auth;

import java.io.IOException;

import com.ueadmission.about.About;
import com.ueadmission.auth.state.AuthState;
import com.ueadmission.auth.state.AuthStateManager;
import com.ueadmission.auth.state.User;
import com.ueadmission.utils.MFXNotifications;

import io.github.palexdev.materialfx.controls.MFXButton;
import io.github.palexdev.materialfx.controls.MFXPasswordField;
import io.github.palexdev.materialfx.controls.MFXTextField;
import javafx.animation.FadeTransition;
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
import javafx.util.Duration;

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
            admissionButton.setOnAction(event -> navigateToAdmission(event));
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
            // Create User object for AuthStateManager
            // Use a default ID if not available (0 is often used as a sentinel value for unset IDs)
            int userId = 0;
            try {
                userId = userRecord.getId();
            } catch (Exception e) {
                System.err.println("Warning: Could not get user ID, using default: " + e.getMessage());
            }
            
            User user = new User(
                userId,
                userRecord.getFirstName(), 
                userRecord.getLastName(), 
                userRecord.getEmail(),
                userRecord.getPhone(),
                userRecord.getRole()
            );
            
            // Update global auth state
            AuthStateManager.getInstance().login(user, rememberMe);
            
            // Show success notification
            MFXNotifications.showSuccess("Login Successful",
                "Welcome back, " + user.getFirstName() + "! You've successfully logged in.");
            
            // Log to console
            System.out.println("Login successful for " + user.getFullName() + 
                             " (" + user.getEmail() + ") with role: " + user.getRole());
            
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
                
                // Update any ProfileButton in new scene with current auth state
                javafx.application.Platform.runLater(() -> {
                    try {
                        // Find ProfileButton in new scene
                        for (javafx.scene.Node node : mainStage.getScene().getRoot().lookupAll("*")) {
                            if (node instanceof com.ueadmission.components.ProfileButton) {
                                com.ueadmission.components.ProfileButton profileButton = 
                                    (com.ueadmission.components.ProfileButton) node;
                                profileButton.updateUIFromAuthState(AuthStateManager.getInstance().getState());
                                System.out.println("Updated ProfileButton after login navigation");
                                break;
                            }
                        }
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                });
                
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
            
            // Make the new stage ready but not visible yet
            aboutStage.setOpacity(0.0);
            aboutStage.show();
            
            // Use a fade transition for the new window
            FadeTransition fadeIn = new FadeTransition(Duration.millis(300), aboutStage.getScene().getRoot());
            fadeIn.setFromValue(0.0);
            fadeIn.setToValue(1.0);
            
            // Add a fade out transition for the current window
            FadeTransition fadeOut = new FadeTransition(Duration.millis(200), currentStage.getScene().getRoot());
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
            
            // Prepare the Admission window before closing current one
            Stage admissionStage = com.ueadmission.admission.Admission.prepareAdmissionWindow(width, height, x, y, maximized);
            
            if (admissionStage == null) {
                System.err.println("Failed to create Admission window.");
                return;
            }
            
            // Make the new stage ready but not visible yet
            admissionStage.setOpacity(0.0);
            admissionStage.show();
            
            // Use a fade transition for the new window
            FadeTransition fadeIn = new FadeTransition(Duration.millis(300), admissionStage.getScene().getRoot());
            fadeIn.setFromValue(0.0);
            fadeIn.setToValue(1.0);
            
            // Add a fade out transition for the current window
            FadeTransition fadeOut = new FadeTransition(Duration.millis(200), currentStage.getScene().getRoot());
            fadeOut.setFromValue(1.0);
            fadeOut.setToValue(0.0);
            
            // Start the fade out, then hide current stage when done
            fadeOut.setOnFinished(e -> {
                currentStage.hide();
                admissionStage.setOpacity(1.0);
                fadeIn.play();
                
                // Finally close the original stage after transition completes
                fadeIn.setOnFinished(f -> currentStage.close());
            });
            
            fadeOut.play();
            
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
        // This is a placeholder for Mock Test navigation
        System.out.println("Navigate to Mock Test (not implemented yet)");
    }
    
    /**
     * Navigates to the Contact screen
     */
    @FXML
    public void navigateToContact(ActionEvent event) {
        // This is a placeholder for Contact navigation
        System.out.println("Navigate to Contact (not implemented yet)");
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
            
            // Make the new stage ready but not visible yet
            registrationStage.setOpacity(0.0);
            registrationStage.show();
            
            // Use a fade transition for the new window
            FadeTransition fadeIn = new FadeTransition(Duration.millis(300), registrationStage.getScene().getRoot());
            fadeIn.setFromValue(0.0);
            fadeIn.setToValue(1.0);
            
            // Add a fade out transition for the current window
            FadeTransition fadeOut = new FadeTransition(Duration.millis(200), currentStage.getScene().getRoot());
            fadeOut.setFromValue(1.0);
            fadeOut.setToValue(0.0);
            
            // Start the fade out, then hide current stage when done
            fadeOut.setOnFinished(e -> {
                currentStage.hide();
                registrationStage.setOpacity(1.0);
                fadeIn.play();
                
                // Finally close the original stage after transition completes
                fadeIn.setOnFinished(f -> currentStage.close());
            });
            
            fadeOut.play();
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Failed to navigate to registration: " + e.getMessage());
        }
    }
    /**
     * Opens the Login page
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
