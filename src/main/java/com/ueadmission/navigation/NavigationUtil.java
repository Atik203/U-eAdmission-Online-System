package com.ueadmission.navigation;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.ueadmission.MainController;
import com.ueadmission.auth.state.AuthState;
import com.ueadmission.auth.state.AuthStateManager;
import com.ueadmission.context.ApplicationContext;
import com.ueadmission.utils.AuthUIUpdater;
import com.ueadmission.utils.TransitionUtil;

import javafx.event.Event;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 * Unified navigation system for managing transitions between screens
 * This class provides a consistent way to navigate between different screens
 * with proper transition effects and state preservation.
 */
public class NavigationUtil {
    private static final Logger LOGGER = Logger.getLogger(NavigationUtil.class.getName());
    
    // Static variable to hold main stage reference
    private static Stage mainStage;
    
    /**
     * Set the main stage reference
     * @param stage The main application stage
     */
    public static void setMainStage(Stage stage) {
        mainStage = stage;
        LOGGER.info("Main stage reference set in NavigationUtil");
    }
    
    /**
     * Navigate to a screen using the event source to determine the current stage
     * This is the main navigation method that should be used in event handlers
     * 
     * @param event The event that triggered navigation (used to get current stage)
     * @param fxmlPath The path to the FXML file for the new screen
     * @param title The title for the new window
     * @return true if navigation was successful, false otherwise
     */
    public static boolean navigateTo(Event event, String fxmlPath, String title) {
        if (event == null || event.getSource() == null) {
            LOGGER.severe("Cannot navigate: event or source is null");
            return false;
        }
        
        try {
            // Get current stage from event source
            Stage currentStage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            return navigateTo(currentStage, fxmlPath, title);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Failed to navigate: " + e.getMessage(), e);
            return false;
        }
    }
    
    /**
     * Navigate to a screen using an explicit current stage
     * 
     * @param currentStage The current stage
     * @param fxmlPath The path to the FXML file for the new screen
     * @param title The title for the new window
     * @return true if navigation was successful, false otherwise
     */
    public static boolean navigateTo(Stage currentStage, String fxmlPath, String title) {
        if (currentStage == null) {
            LOGGER.severe("Cannot navigate: current stage is null");
            return false;
        }
        
        try {
            // Save current window properties
            WindowProperties props = captureWindowProperties(currentStage);
            
            // Get current auth state
            AuthState currentAuthState = AuthStateManager.getInstance().getState();
            boolean isAuthenticated = (currentAuthState != null && currentAuthState.isAuthenticated());
            LOGGER.info("Navigating to " + fxmlPath + " with auth state: " + 
                      (isAuthenticated ? "authenticated" : "not authenticated"));
            
            // Load new scene
            FXMLLoader loader = new FXMLLoader(NavigationUtil.class.getResource(fxmlPath));
            Parent root = loader.load();
            
            // Create the new scene with current dimensions
            Scene scene = new Scene(root, props.width, props.height);
            
            // Update the current stage's scene instead of creating a new stage
            currentStage.setTitle(title);
            currentStage.setScene(scene);
            
            // Restore window properties
            currentStage.setX(props.x);
            currentStage.setY(props.y);
            currentStage.setWidth(props.width);
            currentStage.setHeight(props.height);
            currentStage.setMaximized(props.maximized);
            
            // Set the controller as user data if it's a MainController
            Object controller = loader.getController();
            if (controller instanceof MainController) {
                root.setUserData(controller);
            }
            
            // Ensure UI updates with the current auth state
            refreshControllerUI(controller, currentAuthState);
            
            // Apply fade-in transition using TransitionUtil
            TransitionUtil.applyFadeInTransition(root);
            
            return true;
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Failed to load FXML: " + e.getMessage(), e);
            return false;
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Failed to navigate: " + e.getMessage(), e);
            return false;
        }
    }
    
    /**
     * Navigate with transition effect between two stages
     * 
     * @param oldStage The current stage
     * @param fxmlPath The path to the FXML file for the new screen
     * @param title The title for the new window
     * @return true if navigation was successful, false otherwise
     */
    public static boolean navigateWithTransition(Stage oldStage, String fxmlPath, String title) {
        if (oldStage == null) {
            LOGGER.severe("Cannot navigate: old stage is null");
            return false;
        }
        
        try {
            // Save window properties
            WindowProperties props = captureWindowProperties(oldStage);

            // Get current auth state
            AuthState currentAuthState = AuthStateManager.getInstance().getState();
            
            // Load the FXML
            FXMLLoader loader = new FXMLLoader(NavigationUtil.class.getResource(fxmlPath));
            Parent root = loader.load();

            // Create new scene
            Scene scene = new Scene(root, props.width, props.height);

            // Create transitions using TransitionUtil
            javafx.animation.FadeTransition fadeOut = TransitionUtil.createFadeOutTransition(oldStage.getScene().getRoot());
            javafx.animation.FadeTransition fadeIn = TransitionUtil.createFadeInTransition(root);

            // Get controller 
            Object controller = loader.getController();

            // Execute transition
            fadeOut.setOnFinished(event -> {
                // Update stage with new scene
                oldStage.setTitle(title);
                oldStage.setScene(scene);

                // Restore window properties
                oldStage.setX(props.x);
                oldStage.setY(props.y);
                oldStage.setWidth(props.width);
                oldStage.setHeight(props.height);
                oldStage.setMaximized(props.maximized);

                // Refresh controller's UI with auth state
                refreshControllerUI(controller, currentAuthState);

                // Play fade in animation
                fadeIn.play();
            });

            // Start the transition
            fadeOut.play();
            
            return true;
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Failed to navigate with transition: " + e.getMessage(), e);
            return false;
        }
    }
    
    /**
     * Navigate to login screen
     * 
     * @param event The event that triggered navigation
     * @return true if navigation was successful, false otherwise
     */
    public static boolean navigateToLogin(Event event) {
        return navigateTo(event, "/com.ueadmission/auth/login.fxml", "Login - UeAdmission");
    }
    
    /**
     * Navigate to home screen
     * 
     * @param event The event that triggered navigation
     * @return true if navigation was successful, false otherwise
     */
    public static boolean navigateToHome(Event event) {
        return navigateTo(event, "/com.ueadmission/main.fxml", "UeAdmission - Home");
    }
    
    /**
     * Navigate to about screen
     * 
     * @param event The event that triggered navigation
     * @return true if navigation was successful, false otherwise
     */
    public static boolean navigateToAbout(Event event) {
        return navigateTo(event, "/com.ueadmission/about/about.fxml", "About - UeAdmission");
    }
    
    /**
     * Navigate to admission screen
     * 
     * @param event The event that triggered navigation
     * @return true if navigation was successful, false otherwise
     */
    public static boolean navigateToAdmission(Event event) {
        return navigateTo(event, "/com.ueadmission/admission/admission.fxml", "Admission - UeAdmission");
    }
    
    /**
     * Navigate to profile screen
     * 
     * @param event The event that triggered navigation
     * @return true if navigation was successful, false otherwise
     */
    public static boolean navigateToProfile(Event event) {
        return navigateTo(event, "/com.ueadmission/profile/profile.fxml", "My Profile - UeAdmission");
    }
    
    /**
     * Navigate to registration screen
     * 
     * @param event The event that triggered navigation
     * @return true if navigation was successful, false otherwise
     */
    public static boolean navigateToRegistration(Event event) {
        return navigateTo(event, "/com.ueadmission/auth/registration.fxml", "Registration - UeAdmission");
    }
    
    /**
     * Navigate to contact screen
     * 
     * @param event The event that triggered navigation
     * @return true if navigation was successful, false otherwise
     */
    public static boolean navigateToContact(Event event) {
        return navigateTo(event, "/com.ueadmission/contact/contact.fxml", "Contact - UeAdmission");
    }
    
    /**
     * Get the current stage from known references
     * @return The current stage or null if not found
     */
    public static Stage getCurrentStage() {
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
     * Capture window properties for consistent navigation
     * 
     * @param stage The stage to capture properties from
     * @return The window properties
     */
    private static WindowProperties captureWindowProperties(Stage stage) {
        WindowProperties props = new WindowProperties();
        props.width = stage.getWidth();
        props.height = stage.getHeight();
        props.x = stage.getX();
        props.y = stage.getY();
        props.maximized = stage.isMaximized();
        return props;
    }
    
    /**
     * Apply a smooth transition between stages
     * 
     * @param oldStage The stage to close
     * @param newStage The stage to show
     */
    private static void applyTransition(Stage oldStage, Stage newStage) {
        // Use the new TransitionUtil class for stage transitions
        TransitionUtil.applyStageTransition(oldStage, newStage);
    }
    
    /**
     * Try to refresh the controller's UI with authentication state
     * Uses multiple approaches to ensure the UI is updated correctly
     * 
     * @param controller The controller to refresh
     * @param authState The current authentication state
     */
    private static void refreshControllerUI(Object controller, AuthState authState) {
        if (controller == null) return;
        
        LOGGER.info("Refreshing UI for controller: " + controller.getClass().getSimpleName());
        
        try {
            // First check if controller implements our new AuthStateAware interface
            if (controller instanceof AuthStateAware) {
                ((AuthStateAware) controller).refreshUI();
                LOGGER.info("Called refreshUI on AuthStateAware controller");
                return;
            }
            
            // Legacy compatibility with the old interface
            if (controller instanceof NavigationUtil.AuthStateAware) {
                ((NavigationUtil.AuthStateAware) controller).refreshUI();
                LOGGER.info("Called refreshUI on legacy AuthStateAware controller");
                return;
            }
            
            // Try standard methods via reflection for backward compatibility
            try {
                java.lang.reflect.Method refreshUIMethod = 
                    controller.getClass().getMethod("refreshUI");
                refreshUIMethod.invoke(controller);
                LOGGER.info("Called refreshUI via reflection");
                return;
            } catch (NoSuchMethodException e) {
                // Continue to next method
            }
            
            try {
                java.lang.reflect.Method onSceneActiveMethod = 
                    controller.getClass().getMethod("onSceneActive");
                onSceneActiveMethod.invoke(controller);
                LOGGER.info("Called onSceneActive via reflection");
                return;
            } catch (NoSuchMethodException e) {
                // Continue to next method
            }
            
            try {
                java.lang.reflect.Method updateAuthUIMethod = 
                    controller.getClass().getMethod("updateAuthUI", AuthState.class);
                updateAuthUIMethod.invoke(controller, authState);
                LOGGER.info("Called updateAuthUI via reflection");
                return;
            } catch (NoSuchMethodException e) {
                // Continue to next approach
            }
            
            // Try to use AuthUIUpdater if we can get the scene
            try {
                java.lang.reflect.Method getSceneMethod = 
                    controller.getClass().getMethod("getScene");
                Scene scene = (Scene)getSceneMethod.invoke(controller);
                if (scene != null) {
                    AuthUIUpdater.refreshUI(scene);
                    LOGGER.info("Used AuthUIUpdater to refresh UI");
                    return;
                }
            } catch (NoSuchMethodException e) {
                // Continue to next approach
            }
            
            // Fall back to direct container updates
            try {
                // Let's use our AuthUIUpdater utility class
                boolean isAuthenticated = (authState != null && authState.isAuthenticated());
                
                // Try to get containers via reflection
                java.lang.reflect.Field loginContainerField = 
                    controller.getClass().getDeclaredField("loginButtonContainer");
                loginContainerField.setAccessible(true);
                javafx.scene.layout.HBox loginContainer = 
                    (javafx.scene.layout.HBox)loginContainerField.get(controller);
                
                java.lang.reflect.Field profileContainerField = 
                    controller.getClass().getDeclaredField("profileButtonContainer");
                profileContainerField.setAccessible(true);
                javafx.scene.layout.HBox profileContainer = 
                    (javafx.scene.layout.HBox)profileContainerField.get(controller);
                
                // Update container visibility
                AuthUIUpdater.updateContainersVisibility(loginContainer, profileContainer, isAuthenticated);
                LOGGER.info("Updated containers visibility directly");
            } catch (Exception e) {
                LOGGER.warning("Failed to update containers directly: " + e.getMessage());
            }
        } catch (Exception e) {
            LOGGER.warning("Error during UI refresh: " + e.getMessage());
        }
    }
    
    /**
     * Simple class to hold window properties
     */
    private static class WindowProperties {
        double width;
        double height;
        double x;
        double y;
        boolean maximized;
    }
    
    /**
     * @deprecated Use {@link com.ueadmission.auth.state.AuthStateAware} instead
     * Interface for controllers that can handle auth state changes
     * Kept for backward compatibility
     */
    @Deprecated
    public interface AuthStateAware {
        /**
         * Refresh the UI based on current auth state
         */
        void refreshUI();
    }
}
