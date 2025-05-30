package com.ueadmission.components;

import com.ueadmission.chat.ChatController;
import javafx.animation.ScaleTransition;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Duration;

public class FloatingChatIcon extends Button {

    private Stage chatWindow;
    private boolean isOpen = false;
    private double xOffset = -30;
    private double yOffset = -30;

    // Static reference to track all chat windows
    private static java.util.List<Stage> allChatWindows = new java.util.ArrayList<>();

    public FloatingChatIcon() {
        // Set default text as fallback
        setText("💬");
        setStyle("-fx-font-size: 24px;");

        // Ensure visibility
        setVisible(true);
        setManaged(true);

        // Initialize icon and click handler
        initializeIcon();
        setupClickHandler();

        // Set up listener for window scroll
        sceneProperty().addListener((obs, oldScene, newScene) -> {
            if (newScene != null) {
                newScene.widthProperty().addListener((o, oldVal, newVal) -> updatePosition());
                newScene.heightProperty().addListener((o, oldVal, newVal) -> updatePosition());
            }
        });
    }

    private void initializeIcon() {
        // Load chat icon
        try {
            // Try multiple approaches to load the image
            String resourcePath = "/com.ueadmission/chat.png";

            // Approach 1: Using getClass().getResource() directly
            java.net.URL resourceUrl = getClass().getResource(resourcePath);

            if (resourceUrl != null) {
                Image chatImage = new Image(resourceUrl.toExternalForm());
                if (!chatImage.isError()) {
                    ImageView imageView = new ImageView(chatImage);
                    imageView.setFitWidth(50);
                    imageView.setFitHeight(50);
                    imageView.setPreserveRatio(true);
                    setGraphic(imageView);

                    // Clear text to avoid showing both text and image
                    setText("");

                    // Clear any background or border styling
                    setStyle("-fx-background-color: transparent; -fx-background-insets: 0; -fx-background-radius: 0; -fx-border-width: 0;");

                    System.out.println("Chat icon image loaded successfully using getResource()");
                    return;
                }
            }

            // Approach 2: Using getClassLoader().getResourceAsStream()
            java.io.InputStream imageStream = getClass().getClassLoader().getResourceAsStream(resourcePath);

            if (imageStream != null) {
                Image chatImage = new Image(imageStream);
                if (!chatImage.isError()) {
                    ImageView imageView = new ImageView(chatImage);
                    imageView.setFitWidth(50);
                    imageView.setFitHeight(50);
                    imageView.setPreserveRatio(true);
                    setGraphic(imageView);

                    // Clear text to avoid showing both text and image
                    setText("");

                    // Clear any background or border styling
                    setStyle("-fx-background-color: transparent; -fx-background-insets: 0; -fx-background-radius: 0; -fx-border-width: 0;");

                    System.out.println("Chat icon image loaded successfully using getResourceAsStream()");
                    return;
                }
            }

            // Approach 3: Try with images subdirectory
            String altPath = "/com.ueadmission/images/chat.png";
            java.io.InputStream altStream = getClass().getClassLoader().getResourceAsStream(altPath);

            if (altStream != null) {
                Image altImage = new Image(altStream);
                if (!altImage.isError()) {
                    ImageView imageView = new ImageView(altImage);
                    imageView.setFitWidth(50);
                    imageView.setFitHeight(50);
                    imageView.setPreserveRatio(true);
                    setGraphic(imageView);

                    // Clear text to avoid showing both text and image
                    setText("");

                    System.out.println("Chat icon loaded successfully from images subdirectory");
                    return;
                }
            }

            throw new Exception("Could not load image from any path");
        } catch (Exception e) {
            // Try alternative resource paths before falling back to text
            try {
                // Try alternative path without package prefix
                String altPath = "/chat.png";
                java.io.InputStream altStream = getClass().getClassLoader().getResourceAsStream(altPath);

                if (altStream != null) {
                    Image altImage = new Image(altStream);
                    ImageView imageView = new ImageView(altImage);
                    imageView.setFitWidth(50);
                    imageView.setFitHeight(50);
                    imageView.setPreserveRatio(true);
                    setGraphic(imageView);

                    // Clear text to avoid showing both text and image
                    setText("");

                    System.out.println("Chat icon loaded successfully using alternate path");
                    return;
                }
            } catch (Exception ex) {
                System.err.println("Failed to load image from alternate path: " + ex.getMessage());
            }

            // Fallback to text if all image loading attempts fail
            setText("💬");
            setStyle("-fx-font-size: 24px; -fx-background-color: #FA4506; -fx-text-fill: white; -fx-background-radius: 25px;");

            // Log the error to help with debugging
            System.err.println("Failed to load chat icon image: " + e.getMessage());
            e.printStackTrace();
        }

        // Remove CSS class to prevent style conflicts
        getStyleClass().clear();

        // Set base dimensions and style
        setMinSize(50, 50);
        setMaxSize(50, 50);
        setPrefSize(50, 50);

        // Make sure the icon stays in position and doesn't take up extra space
        setPickOnBounds(true);  // Ensure clicks are captured properly
        setMouseTransparent(false);
        setFocusTraversable(true); // Allow focusing for better accessibility

        // Only keep scale transition for hover effect
        setOnMouseEntered(e -> {
            ScaleTransition scaleUp = new ScaleTransition(Duration.millis(200), this);
            scaleUp.setToX(1.1);
            scaleUp.setToY(1.1);
            scaleUp.play();
        });

        setOnMouseExited(e -> {
            ScaleTransition scaleDown = new ScaleTransition(Duration.millis(200), this);
            scaleDown.setToX(1.0);
            scaleDown.setToY(1.0);
            scaleDown.play();
        });
    }

    private void setupClickHandler() {
        setOnAction(e -> {
            // This is the only action we want to happen - toggle the chat window
            if (isOpen) {
                closeChatWindow();
            } else {
                openChatWindow();
            }

            // Stop event propagation to prevent it from reaching other handlers
            e.consume();
        });
    }

    // Helper method to get current user role from the authentication state
    private String getCurrentUserRole() {
        try {
            // Get the current auth state from AuthStateManager
            com.ueadmission.auth.state.AuthStateManager authManager = com.ueadmission.auth.state.AuthStateManager.getInstance();
            if (authManager.isAuthenticated() && authManager.getState().getUser() != null) {
                return authManager.getState().getUser().getRole();
            }
        } catch (Exception e) {
            System.err.println("Error getting user role: " + e.getMessage());
        }
        // Default to student if not authenticated or error occurs
        return "student";
    }

    private void openChatWindow() {
        if (chatWindow != null && chatWindow.isShowing()) {
            // If already showing, bring to front
            chatWindow.toFront();
            isOpen = true;
            return;
        } else if (chatWindow != null) {
            // If exists but not showing
            chatWindow.show();
            isOpen = true;
            // Make sure it's in the list
            if (!allChatWindows.contains(chatWindow)) {
                allChatWindows.add(chatWindow);
            }
            return;
        }

        try {
            // Load chat FXML
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com.ueadmission/chat/chat.fxml"));
            Parent chatRoot = loader.load();

            // Get controller and set stage reference
            ChatController chatController = loader.getController();

            // Create chat window
            chatWindow = new Stage();
            chatWindow.initStyle(StageStyle.DECORATED);
            chatWindow.initModality(Modality.NONE);
            chatWindow.setTitle("UIU Chat Support");
            chatWindow.setResizable(true);
            chatWindow.setMinWidth(400);
            chatWindow.setMinHeight(500);
            chatWindow.setWidth(800);
            chatWindow.setHeight(600);

            // Load stylesheets
            Scene scene = new Scene(chatRoot, 800, 600);
            scene.getStylesheets().add(getClass().getResource("/com.ueadmission/common.css").toExternalForm());
            scene.getStylesheets().add(getClass().getResource("/com.ueadmission/chat/chat.css").toExternalForm());
            chatWindow.setScene(scene);

            // Set controller's stage reference
            chatController.setStage(chatWindow);

        // Position window relative to the main stage with more space on the right side
        Stage parentStage = (Stage) getScene().getWindow();
        if (parentStage != null) {
            chatWindow.setX(parentStage.getX() + parentStage.getWidth() - 850);
            chatWindow.setY(parentStage.getY() + parentStage.getHeight() - 650);
        }

        chatWindow.setOnCloseRequest(e -> {
            isOpen = false;
            // Remove from list when closed
            allChatWindows.remove(chatWindow);
        });

        chatWindow.show();
        isOpen = true;

        // Add to the list of all chat windows
        allChatWindows.add(chatWindow);

        } catch (Exception e) {
            System.err.println("Error loading chat window: " + e.getMessage());
            e.printStackTrace();

            // Show error alert
            javafx.scene.control.Alert alert = new javafx.scene.control.Alert(
                javafx.scene.control.Alert.AlertType.ERROR,
                "Could not open chat window: " + e.getMessage(),
                javafx.scene.control.ButtonType.OK
            );
            alert.setTitle("Chat Error");
            alert.setHeaderText("Failed to open chat");
            alert.showAndWait();
        }
    }

    private void closeChatWindow() {
        if (chatWindow != null) {
            chatWindow.hide();
            isOpen = false;
            // Remove from list when closed
            allChatWindows.remove(chatWindow);
        }
    }

    /**
     * Static method to close all chat windows
     * This should be called when the user logs out
     */
    public static void closeAllChatWindows() {
        // Create a copy of the list to avoid ConcurrentModificationException
        java.util.List<Stage> windowsToClose = new java.util.ArrayList<>(allChatWindows);

        // Close all windows
        for (Stage window : windowsToClose) {
            if (window != null && window.isShowing()) {
                window.hide();
            }
        }

        // Clear the list
        allChatWindows.clear();

        System.out.println("All chat windows closed");
    }

    /**
     * Updates the position of the chat icon to stay fixed relative to the window size
     */
    private void updatePosition() {
        if (getScene() != null) {
            // Ensure chat icon is visible in the bottom right corner
            // by using the scene dimensions to position it correctly
            double width = getScene().getWidth();
            double height = getScene().getHeight();

            // Calculate position based on parent container dimensions
            // Use a fixed position from right and bottom edges
            setLayoutX(width - getWidth() + xOffset);
            setLayoutY(height - getHeight() + yOffset);

            // Make sure it's visible and on top
            setVisible(true);
            toFront();

            // Ensure the button can receive events
            setDisable(false);

            // Only log position updates in development environments
            if (Boolean.getBoolean("debug")) {
                System.out.println("Chat icon position updated: " + getLayoutX() + ", " + getLayoutY());
            }
        }
    }
}
