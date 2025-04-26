package com.ueadmission.utils;

import io.github.palexdev.materialfx.controls.MFXNotificationCenter;
import io.github.palexdev.materialfx.controls.MFXSimpleNotification;
import io.github.palexdev.materialfx.enums.NotificationState;
import javafx.animation.FadeTransition;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Simple utility class for handling notifications using MaterialFX
 */
public class MFXNotifications {
    
    private static MFXNotificationCenter notificationCenter;
    private static StackPane notificationPane;
    private static final List<Node> activeNotificationNodes = new ArrayList<>();
    
    // Define our own notification types since NotificationState might not have them
    public enum NotificationType {
        SUCCESS, ERROR, INFO, WARNING
    }
    
    /**
     * Initialize the notification center for a stage
     * @param stage The stage to add notifications to
     */
    public static void initialize(Stage stage) {
        if (notificationCenter == null) {
            // Create notification pane
            notificationPane = new StackPane();
            notificationPane.setPickOnBounds(false);
            notificationPane.setPrefSize(300, 500);
            notificationPane.setMaxSize(300, 500);
            notificationPane.setStyle("-fx-background-color: transparent;");
            notificationPane.setAlignment(Pos.BOTTOM_RIGHT);
            notificationPane.setPadding(new Insets(0, 20, 20, 0));
            
            // Add notifications container to the notification pane
            VBox notificationsContainer = new VBox(10);
            notificationsContainer.setAlignment(Pos.BOTTOM_RIGHT);
            notificationPane.getChildren().add(notificationsContainer);
            
            // Initialize notification center (using constructor instead of setContent)
            notificationCenter = new MFXNotificationCenter();
            
            // Add to stage if possible
            if (stage.getScene() != null && stage.getScene().getRoot() instanceof Pane) {
                Pane rootPane = (Pane) stage.getScene().getRoot();
                rootPane.getChildren().add(notificationPane);
                
                // Make sure the notification pane stays on top
                notificationPane.toFront();
                
                // Make the notification pane fill the entire scene
                notificationPane.prefWidthProperty().bind(rootPane.widthProperty());
                notificationPane.prefHeightProperty().bind(rootPane.heightProperty());
            }
        }
    }
    
    /**
     * Add the notification pane to a scene if it wasn't added during initialization
     * @param pane The root pane of the scene
     */
    public static void addToPane(Pane pane) {
        if (notificationPane != null && !pane.getChildren().contains(notificationPane)) {
            pane.getChildren().add(notificationPane);
        }
    }
    
    /**
     * Show a notification with the specified type
     * @param title The notification title
     * @param message The notification message
     * @param type The notification type (SUCCESS, ERROR, INFO, WARNING)
     */
    public static void show(String title, String message, NotificationType type) {
        if (notificationPane == null) {
            System.err.println("Notification center not initialized");
            return;
        }
        
        // Create content with appropriate styling
        VBox content = new VBox(5);
        content.setPadding(new Insets(15));
        content.setPrefWidth(300);
        content.setMaxWidth(300);
        
        // Style based on notification type
        String backgroundColor;
        String textColor = "-fx-text-fill: white;";
        String borderStyle = "-fx-border-width: 0 0 0 4; ";
        
        switch (type) {
            case SUCCESS:
                backgroundColor = "-fx-background-color: #4caf50;"; // Green
                borderStyle += "-fx-border-color: #2e7d32;"; // Darker green
                break;
            case ERROR:
                backgroundColor = "-fx-background-color: #f44336;"; // Red
                borderStyle += "-fx-border-color: #c62828;"; // Darker red
                break;
            case WARNING:
                backgroundColor = "-fx-background-color: #ff9800;"; // Orange
                borderStyle += "-fx-border-color: #ef6c00;"; // Darker orange
                break;
            case INFO:
            default:
                backgroundColor = "-fx-background-color: #2196f3;"; // Blue
                borderStyle += "-fx-border-color: #1565c0;"; // Darker blue
                break;
        }
        
        content.setStyle(backgroundColor + "-fx-background-radius: 4; " + borderStyle + 
                         "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.3), 10, 0, 0, 3);");
        
        Label titleLabel = new Label(title);
        titleLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 14px; " + textColor);
        
        Label messageLabel = new Label(message);
        messageLabel.setWrapText(true);
        messageLabel.setStyle(textColor + "-fx-font-size: 13px;");
        
        content.getChildren().addAll(titleLabel, messageLabel);
        
        // Create notification
        MFXSimpleNotification notification = new MFXSimpleNotification(content);
        
        // Show notification by adding it directly to the notification pane
        Platform.runLater(() -> {
            // Get the notifications container (first child of notificationPane)
            if (notificationPane.getChildren().size() > 0 && notificationPane.getChildren().get(0) instanceof VBox) {
                VBox notificationsContainer = (VBox) notificationPane.getChildren().get(0);
                
                // Make sure notification pane is visible and on top
                notificationPane.setVisible(true);
                notificationPane.toFront();
                
                // Add the notification
                notificationsContainer.getChildren().add(notification.getContent());
                activeNotificationNodes.add(notification.getContent());
                
                // Add fade-in animation
                FadeTransition fadeIn = new FadeTransition(Duration.millis(200), notification.getContent());
                fadeIn.setFromValue(0.0);
                fadeIn.setToValue(1.0);
                fadeIn.play();
                
                // Auto dismiss after 5 seconds with fade-out
                new Thread(() -> {
                    try {
                        Thread.sleep(4800); // Slightly less to account for fade out time
                        Platform.runLater(() -> {
                            FadeTransition fadeOut = new FadeTransition(Duration.millis(200), notification.getContent());
                            fadeOut.setFromValue(1.0);
                            fadeOut.setToValue(0.0);
                            fadeOut.setOnFinished(e -> {
                                notificationsContainer.getChildren().remove(notification.getContent());
                                activeNotificationNodes.remove(notification.getContent());
                            });
                            fadeOut.play();
                        });
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }).start();
            }
        });
        
        // Log to console
        if (type == NotificationType.ERROR) {
            System.err.println("[" + type.name() + "] " + title + ": " + message);
        } else {
            System.out.println("[" + type.name() + "] " + title + ": " + message);
        }
    }
    
    /**
     * Show a success notification
     * @param title The notification title
     * @param message The notification message
     */
    public static void showSuccess(String title, String message) {
        show(title, message, NotificationType.SUCCESS);
    }
    
    /**
     * Show an error notification
     * @param title The notification title
     * @param message The notification message
     */
    public static void showError(String title, String message) {
        show(title, message, NotificationType.ERROR);
    }
    
    /**
     * Show an info notification
     * @param title The notification title
     * @param message The notification message
     */
    public static void showInfo(String title, String message) {
        show(title, message, NotificationType.INFO);
    }
    
    /**
     * Show a warning notification
     * @param title The notification title
     * @param message The notification message
     */
    public static void showWarning(String title, String message) {
        show(title, message, NotificationType.WARNING);
    }
}
