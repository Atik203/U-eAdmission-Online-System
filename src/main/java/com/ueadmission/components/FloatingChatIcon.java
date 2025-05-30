package com.ueadmission.components;

import javafx.animation.ScaleTransition;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Duration;

public class FloatingChatIcon extends Button {
    
    private Stage chatWindow;
    private boolean isOpen = false;
    private double xOffset = -30;
    private double yOffset = -30;

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
            // Use a simpler path format to load the image
            Image chatImage = new Image(getClass().getResourceAsStream("/com.ueadmission/images/chat.png"));
            ImageView imageView = new ImageView(chatImage);
            imageView.setFitWidth(50);
            imageView.setFitHeight(50);
            imageView.setPreserveRatio(true);
            setGraphic(imageView);

            // Clear any background or border styling
            setStyle("-fx-background-color: transparent; -fx-background-insets: 0; -fx-background-radius: 0; -fx-border-width: 0;");

            // Log success to help with debugging
            System.out.println("Chat icon image loaded successfully");
        } catch (Exception e) {
            // Fallback to text if image not found
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
            return;
        }
        
        // Create chat window
        chatWindow = new Stage();
        chatWindow.initStyle(StageStyle.UTILITY);
        chatWindow.initModality(Modality.NONE);
        chatWindow.setTitle("Chat Support");
        chatWindow.setResizable(false);
        
        // Chat window content
        VBox chatContent = new VBox(10);
        chatContent.setPadding(new Insets(20));
        chatContent.setAlignment(Pos.CENTER);
        chatContent.setStyle("-fx-background-color: white; -fx-border-color: #ddd; -fx-border-width: 1px;");
        
        Label welcomeLabel = new Label("Welcome to UIU Chat Support!");
        welcomeLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #333;");
        
        Label statusLabel = new Label("Chat feature is coming soon...");
        statusLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #666;");
        
        Button closeButton = new Button("Close");
        closeButton.setStyle(
            "-fx-background-color: #dc3545;" +
            "-fx-text-fill: white;" +
            "-fx-background-radius: 5px;" +
            "-fx-padding: 8 16 8 16;" +
            "-fx-cursor: hand;"
        );
        closeButton.setOnAction(e -> closeChatWindow());
        
        chatContent.getChildren().addAll(welcomeLabel, statusLabel, closeButton);
        
        Scene chatScene = new Scene(chatContent, 300, 200);
        chatWindow.setScene(chatScene);
        
        // Position window relative to the main stage
        Stage parentStage = (Stage) getScene().getWindow();
        if (parentStage != null) {
            chatWindow.setX(parentStage.getX() + parentStage.getWidth() - 320);
            chatWindow.setY(parentStage.getY() + parentStage.getHeight() - 280);
        }
        
        chatWindow.setOnCloseRequest(e -> {
            isOpen = false;
        });
        
        chatWindow.show();
        isOpen = true;
    }
    
    private void closeChatWindow() {
        if (chatWindow != null) {
            chatWindow.hide();
            isOpen = false;
        }
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