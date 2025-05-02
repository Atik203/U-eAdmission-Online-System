package com.ueadmission.profile;

import java.io.IOException;
import java.util.logging.Logger;

import org.jetbrains.annotations.Nullable;

import com.ueadmission.utils.MFXNotifications;

import javafx.animation.FadeTransition;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import javafx.util.Duration;

/**
 * Handles all profile-related functionality and utility methods
 */
public class Profile {
    private static final Logger LOGGER = Logger.getLogger(Profile.class.getName());

    /**
     * Prepares a Profile window with the specified dimensions
     * @param width The width of the window
     * @param height The height of the window
     * @param x The x position of the window
     * @param y The y position of the window
     * @param maximized Whether the window should be maximized
     * @return The prepared Stage with the Profile UI loaded
     */
    @Nullable
    public static Stage prepareProfileWindow(double width, double height, double x, double y, boolean maximized) {
        try {
            FXMLLoader loader = new FXMLLoader(Profile.class.getResource("/com.ueadmission/profile/profile.fxml"));
            Parent root = loader.load();
            root.setOpacity(0.0);

            Stage stage = new Stage();
            stage.setTitle("My Profile");
            Image icon = new Image(Profile.class.getResourceAsStream("/com.ueadmission/uiu_logo_update.png"));
            stage.getIcons().add(icon);

            Scene scene = new Scene(root, width, height);
            stage.setScene(scene);
            stage.setX(x);
            stage.setY(y);

            if (maximized) {
                stage.setMaximized(true);
            }

            // Store the loader as user data for later access
            scene.setUserData(loader);

            return stage;
        } catch (IOException e) {
            e.printStackTrace();
            LOGGER.severe("Failed to load profile.fxml: " + e.getMessage());
            return null;
        }
    }

    /**
     * Shows a profile loading error notification
     */
    public static void showProfileLoadingError() {
        MFXNotifications.showError(
                "Profile Loading Error",
                "Failed to load profile information. Please try again later."
        );
    }

    /**
     * Shows a profile update success notification
     */
    public static void showProfileUpdateSuccess() {
        MFXNotifications.showSuccess(
                "Profile Updated",
                "Your profile has been successfully updated."
        );
    }

    /**
     * Apply fade-in transition to the profile window
     * @param root The root node of the profile window
     */
    public static void applyFadeInTransition(Parent root) {
        FadeTransition fadeIn = new FadeTransition(Duration.millis(300), root);
        fadeIn.setFromValue(0.0);
        fadeIn.setToValue(1.0);
        fadeIn.play();
    }
}
