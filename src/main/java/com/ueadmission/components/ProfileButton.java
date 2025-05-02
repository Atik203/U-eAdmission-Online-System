package com.ueadmission.components;
import java.util.function.Consumer;

import com.ueadmission.auth.state.AuthState;
import com.ueadmission.auth.state.AuthStateManager;
import com.ueadmission.auth.state.User;

import io.github.palexdev.materialfx.controls.MFXButton;
import javafx.animation.FadeTransition;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.util.Duration;

/**
 * A reusable profile button component that displays the user's
 * profile information or login button based on auth state
 */
public class ProfileButton extends HBox {
    
    private final MFXButton loginButton = new MFXButton("Login");
    private final MFXButton profileButton = new MFXButton();
    private final Circle profileCircle = new Circle(20, Color.valueOf("#FA4506"));  // Increased size to 20
    private final Text initialsText = new Text();
    private final StringProperty displayName = new SimpleStringProperty("Guest");
    
    private ContextMenu profileMenu;
    private Consumer<AuthState> authStateListener;
    
    public ProfileButton() {
        this.getStyleClass().add("profile-button-container");
        
        // Initialize login button
        loginButton.getStyleClass().add("mfx-button-login");
        loginButton.setOnAction(e -> handleLoginClick());
        
        // Initialize profile button
        profileButton.getStyleClass().add("profile-button");
        initialsText.setFill(Color.WHITE);
        profileButton.setGraphic(createProfileGraphic());
        
        // Create profile menu
        createProfileMenu();
        
        // Add the login button initially
        this.getChildren().add(loginButton);
        
        // Subscribe to auth state changes
        subscribeToAuthState();
        
        // Initialize with current auth state
        javafx.application.Platform.runLater(this::initializeWithCurrentAuthState);
    }
    
    /**
     * Initialize the button with the current auth state when loaded
     */
    public void initializeWithCurrentAuthState() {
        try {
            // Check if we have an active session and update UI accordingly
            AuthState state = AuthStateManager.getInstance().getState();
            if (state != null) {
                // Use Platform.runLater to ensure UI updates happen on JavaFX thread
                javafx.application.Platform.runLater(() -> {
                    updateUIFromAuthState(state);
                    System.out.println("ProfileButton initialized with current auth state");
                });
            } else {
                System.out.println("No auth state available for initialization");
            }
        } catch (Exception e) {
            System.err.println("Error initializing ProfileButton: " + e.getMessage());
        }
    }
    
    /**
     * Force update the UI with current auth state
     * This can be called from controllers to ensure the button shows the correct state
     */
    public void refreshAuthState() {
        // This ensures that even if the subscriber hasn't been notified yet,
        // we still get the latest state
        AuthState currentState = AuthStateManager.getInstance().getState();
        if (currentState != null) {
            updateUIFromAuthState(currentState);
            System.out.println("ProfileButton explicitly refreshed with current auth state");
        }
    }
    
    /**
     * Create the profile graphic with circle and initials
     */
    private HBox createProfileGraphic() {
        HBox graphic = new HBox();  // Removed spacing since we're only showing the avatar
        graphic.setAlignment(Pos.CENTER);
        graphic.getStyleClass().add("profile-graphic");

        // Add initials to circle
        profileCircle.setRadius(20);  // Increased size to 20
        initialsText.textProperty().bind(new SimpleStringProperty("").concat(
            displayName.map(name -> {
                if (name == null || name.isEmpty()) return "";
                String[] parts = name.split(" ");
                if (parts.length >= 2) {
                    return String.valueOf(parts[0].charAt(0)) + parts[1].charAt(0);
                } else if (parts.length == 1 && !parts[0].isEmpty()) {
                    return String.valueOf(parts[0].charAt(0));
                }
                return "";
            })
        ));

        // Stack circle and text
        javafx.scene.layout.StackPane circleStack = new javafx.scene.layout.StackPane();
        circleStack.getChildren().addAll(profileCircle, initialsText);

        // Only add the circle stack (removed usernameText)
        graphic.getChildren().add(circleStack);
        return graphic;
    }

    /**
     * Create the profile context menu
     */
    private void createProfileMenu() {
        profileMenu = new ContextMenu();
        profileMenu.getStyleClass().add("profile-context-menu");

        MenuItem profileMenuItem = new MenuItem("My Profile");
        profileMenuItem.setStyle("-fx-text-fill: #2E3B55; -fx-font-weight: bold; -fx-padding: 8 15;");
        profileMenuItem.setOnAction(e -> handleProfileClick());

        // Role-based menu items will be added dynamically when user logs in

        MenuItem logoutMenuItem = new MenuItem("Logout");
        logoutMenuItem.setStyle("-fx-text-fill: #FF3B30; -fx-font-weight: bold; -fx-padding: 8 15;");  // Red text
        logoutMenuItem.setOnAction(e -> handleLogoutClick());

        // Add the basic items that all users have
        profileMenu.getItems().addAll(profileMenuItem, logoutMenuItem);

        // Show menu on profile button click
        profileButton.setOnAction(e -> {
            profileMenu.show(profileButton, javafx.geometry.Side.BOTTOM, 0, 10);
        });
    }

    /**
     * Subscribe to auth state changes
     */
    private void subscribeToAuthState() {
        // Create listener
        authStateListener = state -> {
            updateUIFromAuthState(state);
        };

        // Subscribe to auth state changes
        AuthStateManager.getInstance().subscribe(authStateListener);
    }

    /**
     * Update UI based on auth state
     */
    public void updateUIFromAuthState(AuthState state) {
        System.out.println("ProfileButton.updateUIFromAuthState called");

        if (state != null && state.isAuthenticated() && state.getUser() != null) {
            // User is authenticated, show profile button
            User user = state.getUser();
            String fullName = user.getFirstName() + " " + user.getLastName();
            System.out.println("Authenticated user: " + fullName + " (Email: " + user.getEmail() + ")");

            displayName.set(fullName);

            // Update menu based on user role
            updateMenuForUserRole(user.getRole());

            // Update UI on JavaFX thread
            javafx.application.Platform.runLater(() -> {
                try {
                    this.getChildren().clear();

                    // Add profile button and check if it was successful
                    this.getChildren().add(profileButton);

                    // Log successful update
                    System.out.println("UI updated to show profile button for: " + fullName);

                    // Force layout refresh
                    this.requestLayout();

                    // Update button tooltip only, don't set text to avoid duplication
                    profileButton.setText("");
                    profileButton.setTooltip(new javafx.scene.control.Tooltip("Logged in as " + fullName));
                } catch (Exception e) {
                    System.err.println("Error updating profile button UI: " + e.getMessage());
                    e.printStackTrace();
                }
            });
        } else {
            // User is not authenticated, show login button
            System.out.println("No authenticated user, showing login button");
            displayName.set("Guest");

            // Update UI on JavaFX thread
            javafx.application.Platform.runLater(() -> {
                try {
                    this.getChildren().clear();
                    this.getChildren().add(loginButton);
                    System.out.println("UI updated to show login button");

                    // Force layout refresh
                    this.requestLayout();
                } catch (Exception e) {
                    System.err.println("Error updating login button UI: " + e.getMessage());
                    e.printStackTrace();
                }
            });
        }
    }

        /**
         * Updates the profile menu based on user role
         */
        private void updateMenuForUserRole(String role) {
            // Clear existing menu items
            profileMenu.getItems().clear();

            // Common items for all users
            MenuItem profileMenuItem = new MenuItem("My Profile");
            profileMenuItem.setStyle("-fx-text-fill: #2E3B55; -fx-font-weight: bold; -fx-padding: 8 15;");
            profileMenuItem.setOnAction(e -> handleProfileClick());

            // Add role-specific menu items with styling
            if ("student".equalsIgnoreCase(role)) {
                // Student menu items
                MenuItem applicationMenuItem = new MenuItem("Application");
                applicationMenuItem.setStyle("-fx-text-fill: #2E3B55; -fx-padding: 8 15;");
                applicationMenuItem.setOnAction(e -> navigateTo("application"));

                MenuItem resultMenuItem = new MenuItem("Result");
                resultMenuItem.setStyle("-fx-text-fill: #2E3B55; -fx-padding: 8 15;");
                resultMenuItem.setOnAction(e -> navigateTo("result"));

                MenuItem examScheduleMenuItem = new MenuItem("Exam Schedule");
                examScheduleMenuItem.setStyle("-fx-text-fill: #2E3B55; -fx-padding: 8 15;");
                examScheduleMenuItem.setOnAction(e -> navigateTo("examschedule"));

                profileMenu.getItems().addAll(
                    profileMenuItem,
                    applicationMenuItem,
                    resultMenuItem,
                    examScheduleMenuItem
                );
            } else if ("admin".equalsIgnoreCase(role)) {
                // Admin menu items with styled menu items
                MenuItem manageStudentMenuItem = new MenuItem("Manage Student");
                manageStudentMenuItem.setStyle("-fx-text-fill: #2E3B55; -fx-padding: 8 15;");
                manageStudentMenuItem.setOnAction(e -> navigateTo("managestudent"));

                MenuItem manageAdminMenuItem = new MenuItem("Manage User (Admin)");
                manageAdminMenuItem.setStyle("-fx-text-fill: #2E3B55; -fx-padding: 8 15;");
                manageAdminMenuItem.setOnAction(e -> navigateTo("manageadmin"));

                MenuItem examScheduleMenuItem = new MenuItem("Set Exam Schedule");
                examScheduleMenuItem.setStyle("-fx-text-fill: #2E3B55; -fx-padding: 8 15;");
                examScheduleMenuItem.setOnAction(e -> navigateTo("setexamschedule"));

                MenuItem publishResultMenuItem = new MenuItem("Publish Result");
                publishResultMenuItem.setStyle("-fx-text-fill: #2E3B55; -fx-padding: 8 15;");
                publishResultMenuItem.setOnAction(e -> navigateTo("publishresult"));

                MenuItem approveApplicationMenuItem = new MenuItem("Approve Application");
                approveApplicationMenuItem.setStyle("-fx-text-fill: #2E3B55; -fx-padding: 8 15;");
                approveApplicationMenuItem.setOnAction(e -> navigateTo("approveapplication"));

                MenuItem addQuestionMenuItem = new MenuItem("Add Question Paper");
                addQuestionMenuItem.setStyle("-fx-text-fill: #2E3B55; -fx-padding: 8 15;");
                addQuestionMenuItem.setOnAction(e -> navigateTo("addquestion"));

                MenuItem monitorExamMenuItem = new MenuItem("Exam Monitoring");
                monitorExamMenuItem.setStyle("-fx-text-fill: #2E3B55; -fx-padding: 8 15;");
                monitorExamMenuItem.setOnAction(e -> navigateTo("exammonitoring"));

                profileMenu.getItems().addAll(
                    profileMenuItem,
                    manageStudentMenuItem,
                    manageAdminMenuItem,
                    examScheduleMenuItem,
                    publishResultMenuItem,
                    approveApplicationMenuItem,
                    addQuestionMenuItem,
                    monitorExamMenuItem
                );
            } else {
                // Default menu for unknown roles
                profileMenu.getItems().add(profileMenuItem);
            }

            // Add separator before logout
            javafx.scene.control.SeparatorMenuItem separator = new javafx.scene.control.SeparatorMenuItem();
            profileMenu.getItems().add(separator);

            // Logout menu item for all users - Red text
            MenuItem logoutMenuItem = new MenuItem("Logout");
            logoutMenuItem.setStyle("-fx-text-fill: #FF3B30; -fx-font-weight: bold; -fx-padding: 8 15;");
            logoutMenuItem.setOnAction(e -> handleLogoutClick());
            profileMenu.getItems().add(logoutMenuItem);
        }

        /**
         * Navigate to a specific screen
         */
        private void navigateTo(String screen) {
            System.out.println("Navigate to " + screen + " screen");
            try {
                // Use NavigationUtil instead of NavigationManager
                String fxmlPath = "/com.ueadmission/" + screen + ".fxml";
                String title = "UeAdmission - " + screen.substring(0, 1).toUpperCase() + screen.substring(1);
                Stage currentStage = (Stage) this.getScene().getWindow();
                com.ueadmission.navigation.NavigationUtil.navigateTo(currentStage, fxmlPath, title);
            } catch (Exception e) {
                System.err.println("Error navigating to " + screen + ": " + e.getMessage());
                e.printStackTrace();
            }
        }

    /**
     * Handle login button click
     */
    private void handleLoginClick() {
        try {
            // Get the current stage
            Stage stage = (Stage) this.getScene().getWindow();
            double width = stage.getWidth();
            double height = stage.getHeight();
            double x = stage.getX();
            double y = stage.getY();
            boolean maximized = stage.isMaximized();

            // Prepare the Login window
            Stage loginWindow = com.ueadmission.auth.Auth.prepareLoginWindow(width, height, x, y, maximized);
            if (loginWindow != null) {
                // Close current window and show login
                stage.close();
                loginWindow.show();

                // Apply fade-in animation
                Parent root = loginWindow.getScene().getRoot();
                FadeTransition fadeIn = new FadeTransition(Duration.millis(800), root);
                fadeIn.setFromValue(0.0);
                fadeIn.setToValue(1.0);
                fadeIn.play();

                System.out.println("Navigated to login screen");
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Failed to navigate to login screen: " + e.getMessage());
        }
    }

        /**
         * Update user information displayed in the button
         * @param user The current authenticated user
         */
        public void updateUserInfo(User user) {
            if (user != null) {
                // Update display name property which is bound to the UI
                displayName.set(user.getFirstName() + " " + user.getLastName());

                // Update profile menu for user role
                updateMenuForUserRole(user.getRole());

                System.out.println("Profile button updated with user: " + user.getEmail());
            }
        }

        /**
         * Handle profile click
         */
        private void handleProfileClick() {
            try {
                // Use NavigationUtil instead of NavigationManager
                Stage currentStage = (Stage) this.getScene().getWindow();
                com.ueadmission.navigation.NavigationUtil.navigateTo(currentStage, 
                                                                     "/com.ueadmission/profile/profile.fxml", 
                                                                     "My Profile - UeAdmission");
                System.out.println("Navigated to profile screen");
            } catch (Exception e) {
                e.printStackTrace();
                System.err.println("Failed to navigate to profile screen: " + e.getMessage());
            }
        }

        /**
         * Handle logout click
         */
        private void handleLogoutClick() {
            try {
                System.out.println("Logout menu item clicked");
                AuthStateManager.getInstance().logout();

                // Navigate to home screen using NavigationUtil
                javafx.application.Platform.runLater(() -> {
                    try {
                        Stage currentStage = (Stage) this.getScene().getWindow();
                        com.ueadmission.navigation.NavigationUtil.navigateTo(
                            currentStage, 
                            "/com.ueadmission/main.fxml", 
                            "UeAdmission - Home"
                        );
                        System.out.println("User logged out and redirected to home");
                    } catch (Exception e) {
                        System.err.println("Failed to navigate to home screen after logout: " + e.getMessage());
                        e.printStackTrace();
                    }
                });
            } catch (Exception e) {
                System.err.println("Error during logout process: " + e.getMessage());
                e.printStackTrace();
            }
        }

    /**
     * Clean up resources when this component is no longer needed
     */
    public void cleanup() {
        // Unsubscribe from auth state changes
        if (authStateListener != null) {
            AuthStateManager.getInstance().unsubscribe(authStateListener);
        }
    }

    /**
     * Set the login button text
     * @param text The new text for the login button
     */
    public void setLoginButtonText(String text) {
        loginButton.setText(text);
    }

    /**
     * Set the profile circle color
     * @param color The new color for the profile circle
     */
    public void setProfileCircleColor(Color color) {
        profileCircle.setFill(color);
    }
}

