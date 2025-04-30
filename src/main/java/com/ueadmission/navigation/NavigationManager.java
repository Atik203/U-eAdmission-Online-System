package com.ueadmission.navigation;

import com.ueadmission.MainController;
import com.ueadmission.auth.state.AuthState;
import com.ueadmission.auth.state.AuthStateManager;
import com.ueadmission.context.ApplicationContext;
import javafx.animation.FadeTransition;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.awt.event.ActionEvent;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Manages navigation between different screens
 * Ensures authentication state is properly maintained across screens
 */
public class NavigationManager {
    private static final Logger LOGGER = Logger.getLogger(NavigationManager.class.getName());
    
    // Static variable to hold main stage reference
    private static Stage mainStage;
    
    /**
     * Navigate to the login screen
     */
    public static void navigateToLogin() {
        navigate("/com.ueadmission/auth/login.fxml", "Login - UeAdmission");
    }

    /**
     * Navigate to the home screen
     */
    public static void navigateToHome() {
        navigate("/com.ueadmission/main.fxml", "UeAdmission - Home");
    }

    /**
     * Navigate to the profile screen
     */
    public static void navigateToProfile() {
        navigate("/com.ueadmission/profile/profile.fxml", "My Profile - UeAdmission");
    }

    /**
     * Navigate to the about screen
     */
    public static void navigateToAbout() {
        navigate("/com.ueadmission/about/about.fxml", "About - UeAdmission");
    }


    /**
     * Navigate to the admission screen
     */
    public void handleAdmissionButton(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/ueadmission/admissionPage.fxml"));
            Parent root = loader.load();

            Stage stage = new Stage();
            stage.setTitle("Admission Page");
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Helper method to update containers directly using reflection
     * @param controller The controller instance
     * @param authState The current auth state
     */
    private static void updateContainersDirectly(Object controller, AuthState authState) throws Exception {
        boolean isAuthenticated = (authState != null && authState.isAuthenticated());

        // Try to find loginButtonContainer and profileButtonContainer
        java.lang.reflect.Field loginContainerField =
            controller.getClass().getDeclaredField("loginButtonContainer");
        loginContainerField.setAccessible(true);
        Object loginContainer = loginContainerField.get(controller);

        if (loginContainer != null) {
            java.lang.reflect.Method setVisibleMethod =
                loginContainer.getClass().getMethod("setVisible", boolean.class);
            setVisibleMethod.invoke(loginContainer, !isAuthenticated);

            java.lang.reflect.Method setManagedMethod =
                loginContainer.getClass().getMethod("setManaged", boolean.class);
            setManagedMethod.invoke(loginContainer, !isAuthenticated);

            LOGGER.info("Updated loginButtonContainer visibility in " + controller.getClass().getSimpleName());
        }

        java.lang.reflect.Field profileContainerField =
            controller.getClass().getDeclaredField("profileButtonContainer");
        profileContainerField.setAccessible(true);
        Object profileContainer = profileContainerField.get(controller);

        if (profileContainer != null) {
            java.lang.reflect.Method setVisibleMethod =
                profileContainer.getClass().getMethod("setVisible", boolean.class);
            setVisibleMethod.invoke(profileContainer, isAuthenticated);

            java.lang.reflect.Method setManagedMethod =
                profileContainer.getClass().getMethod("setManaged", boolean.class);
            setManagedMethod.invoke(profileContainer, isAuthenticated);

            LOGGER.info("Updated profileButtonContainer visibility in " + controller.getClass().getSimpleName());
        }
    }

    /**
     * Set the main stage reference
     * @param stage The main application stage
     */
    public static void setMainStage(Stage stage) {
        mainStage = stage;
        LOGGER.info("Main stage reference set in NavigationManager");
    }

    /**
     * Navigate to a specific FXML with title
     * This method ensures the auth state is properly maintained across screens
     *
     * @param fxmlPath The path to the FXML resource
     * @param title The title for the window
     */
    public static void navigate(String fxmlPath, String title) {
        try {
            // Try to get current stage from various locations
            Stage currentStage = null;

            // First try our static reference
            if (mainStage != null) {
                currentStage = mainStage;
            } else {
                // Then try reflection on ApplicationContext
                try {
                    java.lang.reflect.Field stageField =
                        ApplicationContext.class.getDeclaredField("currentStage");
                    stageField.setAccessible(true);
                    currentStage = (Stage) stageField.get(ApplicationContext.getInstance());
                } catch (Exception e) {
                    LOGGER.warning("Could not get stage via reflection: " + e.getMessage());
                }
            }

            // Still no stage? Give up
            if (currentStage == null) {
                LOGGER.warning("No current stage found");
                return;
            }

            // Save window properties
            double width = currentStage.getWidth();
            double height = currentStage.getHeight();
            double x = currentStage.getX();
            double y = currentStage.getY();
            boolean maximized = currentStage.isMaximized();

            // Load the FXML
            FXMLLoader loader = new FXMLLoader(NavigationManager.class.getResource(fxmlPath));
            Parent root = loader.load();

            // Create new scene with current dimensions
            Scene scene = new Scene(root, width, height);

            // Get controller
            Object controller = loader.getController();

            // Set the controller as user data if it's a MainController
            if (controller instanceof MainController) {
                root.setUserData(controller);
            }

            // Update the stage
            currentStage.setTitle(title);
            currentStage.setScene(scene);

            // Restore window properties
            currentStage.setX(x);
            currentStage.setY(y);
            currentStage.setWidth(width);
            currentStage.setHeight(height);
            currentStage.setMaximized(maximized);

            // Check if controller is implementing our state-aware interface
            if (controller instanceof AuthStateAware) {
                ((AuthStateAware) controller).refreshUI();
                LOGGER.info("Called refreshUI on AuthStateAware controller: " + controller.getClass().getSimpleName());
            }
            // Fallback to reflection for controllers that don't implement the interface
            else if (controller != null) {
                // Get current auth state
                AuthState currentAuthState = AuthStateManager.getInstance().getState();

                // Try to find and invoke appropriate methods to update the UI
                try {
                    // Try refreshUI first (our common method)
                    try {
                        java.lang.reflect.Method refreshUIMethod =
                            controller.getClass().getMethod("refreshUI");
                        refreshUIMethod.invoke(controller);
                        LOGGER.info("Called refreshUI on " + controller.getClass().getSimpleName());
                    } catch (NoSuchMethodException e1) {
                        // If refreshUI isn't available, try onSceneActive
                        try {
                            java.lang.reflect.Method onSceneActiveMethod =
                                controller.getClass().getMethod("onSceneActive");
                            onSceneActiveMethod.invoke(controller);
                            LOGGER.info("Called onSceneActive on " + controller.getClass().getSimpleName());
                        } catch (NoSuchMethodException e2) {
                            // If onSceneActive isn't available, try updateAuthUI
                            try {
                                java.lang.reflect.Method updateAuthUIMethod =
                                    controller.getClass().getMethod("updateAuthUI", AuthState.class);
                                updateAuthUIMethod.invoke(controller, currentAuthState);
                                LOGGER.info("Called updateAuthUI on " + controller.getClass().getSimpleName());
                            } catch (NoSuchMethodException e3) {
                                // If no methods work, try direct access to profileButton
                                try {
                                    // Get profileButton field
                                    java.lang.reflect.Field profileButtonField =
                                        controller.getClass().getDeclaredField("profileButton");
                                    profileButtonField.setAccessible(true);
                                    Object profileBtn = profileButtonField.get(controller);

                                    // Call updateUIFromAuthState on it
                                    if (profileBtn != null) {
                                        java.lang.reflect.Method updateUiMethod =
                                            profileBtn.getClass().getMethod("updateUIFromAuthState", AuthState.class);
                                        updateUiMethod.invoke(profileBtn, currentAuthState);
                                        LOGGER.info("Directly updated ProfileButton in " + controller.getClass().getSimpleName());
                                    }
                                } catch (Exception e4) {
                                    // Try to update login/profile containers directly
                                    try {
                                        updateContainersDirectly(controller, currentAuthState);
                                    } catch (Exception e5) {
                                        // If all else fails, log the issue
                                        LOGGER.warning("Cannot update UI in " + controller.getClass().getSimpleName() +
                                                     ": none of the update methods worked");
                                    }
                                }
                            }
                        }
                    }
                } catch (Exception e) {
                    LOGGER.warning("Error updating " + controller.getClass().getSimpleName() + ": " + e.getMessage());
                }
            }

            LOGGER.info("Navigated to: " + fxmlPath);

        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Failed to navigate to " + fxmlPath, e);
        }
    }

    /**
     * Navigate with transition effect
     * This provides a smoother transition between screens
     *
     * @param fxmlPath The path to the FXML resource
     * @param title The title for the window
     */
    public static void navigateWithTransition(String fxmlPath, String title) {
        try {
            // Get current stage
            Stage currentStage = getCurrentStage();
            if (currentStage == null) {
                LOGGER.warning("No current stage found");
                return;
            }

            // Save window properties
            double width = currentStage.getWidth();
            double height = currentStage.getHeight();
            double x = currentStage.getX();
            double y = currentStage.getY();
            boolean maximized = currentStage.isMaximized();

            // Load the FXML
            FXMLLoader loader = new FXMLLoader(NavigationManager.class.getResource(fxmlPath));
            Parent root = loader.load();

            // Create new scene
            Scene scene = new Scene(root, width, height);

            // Create fade out transition for current scene
            FadeTransition fadeOut = new FadeTransition(Duration.millis(150), currentStage.getScene().getRoot());
            fadeOut.setFromValue(1.0);
            fadeOut.setToValue(0.0);

            // Create fade in transition for new scene
            FadeTransition fadeIn = new FadeTransition(Duration.millis(300), root);
            fadeIn.setFromValue(0.0);
            fadeIn.setToValue(1.0);

            // Execute transition
            fadeOut.setOnFinished(event -> {
                // Update stage with new scene
                currentStage.setTitle(title);
                currentStage.setScene(scene);

                // Restore window properties
                currentStage.setX(x);
                currentStage.setY(y);
                currentStage.setWidth(width);
                currentStage.setHeight(height);
                currentStage.setMaximized(maximized);

                // Get controller and refresh UI
                Object controller = loader.getController();

                // Check if controller implements AuthStateAware
                if (controller instanceof AuthStateAware) {
                    ((AuthStateAware) controller).refreshUI();
                    LOGGER.info("Called refreshUI on AuthStateAware controller: " + controller.getClass().getSimpleName());
                }
                // Fallback to reflection for other controllers
                else if (controller != null) {
                    refreshControllerUsingReflection(controller);
                }

                // Play fade in animation
                fadeIn.play();
            });

            // Start the transition
            fadeOut.play();

            LOGGER.info("Navigated with transition to: " + fxmlPath);

        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Failed to navigate to " + fxmlPath, e);
            // Fallback to regular navigation
            navigate(fxmlPath, title);
        }
    }

    /**
     * Get the current stage from various sources
     * @return The current stage or null if not found
     */
    private static Stage getCurrentStage() {
        // First try our static reference
        if (mainStage != null) {
            return mainStage;
        }

        // Then try reflection on ApplicationContext
        try {
            java.lang.reflect.Field stageField =
                ApplicationContext.class.getDeclaredField("currentStage");
            stageField.setAccessible(true);
            return (Stage) stageField.get(ApplicationContext.getInstance());
        } catch (Exception e) {
            LOGGER.warning("Could not get stage via reflection: " + e.getMessage());
        }

        return null;
    }

    /**
     * Refresh a controller's UI using reflection
     * @param controller The controller to refresh
     */
    private static void refreshControllerUsingReflection(Object controller) {
        if (controller == null) return;

        try {
            // Get current auth state
            AuthState currentAuthState = AuthStateManager.getInstance().getState();

            // Try refreshUI first
            try {
                java.lang.reflect.Method refreshUIMethod =
                    controller.getClass().getMethod("refreshUI");
                refreshUIMethod.invoke(controller);
                LOGGER.info("Called refreshUI on " + controller.getClass().getSimpleName());
                return;
            } catch (NoSuchMethodException e1) {
                // Ignore and try next method
            }

            // Try onSceneActive next
            try {
                java.lang.reflect.Method onSceneActiveMethod =
                    controller.getClass().getMethod("onSceneActive");
                onSceneActiveMethod.invoke(controller);
                LOGGER.info("Called onSceneActive on " + controller.getClass().getSimpleName());
                return;
            } catch (NoSuchMethodException e2) {
                // Ignore and try next method
            }

            // Try updateAuthUI next
            try {
                java.lang.reflect.Method updateAuthUIMethod =
                    controller.getClass().getMethod("updateAuthUI", AuthState.class);
                updateAuthUIMethod.invoke(controller, currentAuthState);
                LOGGER.info("Called updateAuthUI on " + controller.getClass().getSimpleName());
                return;
            } catch (NoSuchMethodException e3) {
                // Ignore and try next approach
            }

            // Try direct access to profileButton
            try {
                java.lang.reflect.Field profileButtonField =
                    controller.getClass().getDeclaredField("profileButton");
                profileButtonField.setAccessible(true);
                Object profileBtn = profileButtonField.get(controller);

                if (profileBtn != null) {
                    java.lang.reflect.Method updateUiMethod =
                        profileBtn.getClass().getMethod("updateUIFromAuthState", AuthState.class);
                    updateUiMethod.invoke(profileBtn, currentAuthState);
                    LOGGER.info("Directly updated ProfileButton in " + controller.getClass().getSimpleName());
                    return;
                }
            } catch (Exception e4) {
                // Ignore and try next approach
            }

            // As a last resort, try to update containers directly
            try {
                updateContainersDirectly(controller, currentAuthState);
            } catch (Exception e5) {
                LOGGER.warning("Cannot update UI in " + controller.getClass().getSimpleName() +
                             ": none of the update methods worked");
            }

        } catch (Exception e) {
            LOGGER.warning("Error updating " + controller.getClass().getSimpleName() + ": " + e.getMessage());
        }
    }

    /**
     * Interface for controllers that can handle auth state changes
     */
    public interface AuthStateAware {
        /**
         * Refresh the UI based on current auth state
         */
        void refreshUI();
    }
}
