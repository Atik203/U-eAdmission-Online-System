package com.ueadmission.publishResult;

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
 * Handles all publish result-related functionality and utility methods
 */
public class PublishResult {
    private static final Logger LOGGER = Logger.getLogger(PublishResult.class.getName());

    /**
     * Prepares a Publish Result window with the specified dimensions
     * @param width The width of the window
     * @param height The height of the window
     * @param x The x position of the window
     * @param y The y position of the window
     * @param maximized Whether the window should be maximized
     * @return The prepared Stage with the Publish Result UI loaded
     */
    @Nullable
    public static Stage preparePublishResultWindow(double width, double height, double x, double y, boolean maximized) {
        try {
            FXMLLoader loader = new FXMLLoader(PublishResult.class.getResource("/com.ueadmission/publishResult/publish-result.fxml"));
            Parent root = loader.load();
            root.setOpacity(0.0);

            Stage stage = new Stage();
            stage.setTitle("Publish Results");
            Image icon = new Image(PublishResult.class.getResourceAsStream("/com.ueadmission/uiu_logo_update.png"));
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
            LOGGER.severe("Failed to load publish-result.fxml: " + e.getMessage());
            return null;
        }
    }

    /**
     * Shows a publish result loading error notification
     */
    public static void showPublishResultLoadingError() {
        MFXNotifications.showError(
                "Loading Error",
                "Failed to load publish result information. Please try again later."
        );
    }

    /**
     * Shows a publish success notification
     */
    public static void showPublishSuccess() {
        MFXNotifications.showSuccess(
                "Result Published",
                "The examination result has been successfully published."
        );
    }

    /**
     * Shows a publish error notification
     */
    public static void showPublishError(String errorMessage) {
        MFXNotifications.showError(
                "Publication Error",
                "Failed to publish result: " + errorMessage
        );
    }

    /**
     * Apply fade-in transition to the publish result window
     * @param root The root node of the publish result window
     */
    public static void applyFadeInTransition(Parent root) {
        FadeTransition fadeIn = new FadeTransition(Duration.millis(300), root);
        fadeIn.setFromValue(0.0);
        fadeIn.setToValue(1.0);
        fadeIn.play();
    }
}