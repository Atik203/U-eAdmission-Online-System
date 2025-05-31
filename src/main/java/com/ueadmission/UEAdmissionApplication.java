package com.ueadmission;

import com.ueadmission.chat.ChatClient;
import com.ueadmission.chat.ChatManager;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;

import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;

public class UEAdmissionApplication extends Application {
    private static final Logger LOGGER = Logger.getLogger(UEAdmissionApplication.class.getName());

    @Override
    public void init() {
        // Initialize database
        try {
            com.ueadmission.db.DatabaseInitializer.initializeDatabase();
            LOGGER.info("Database initialized successfully");
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Failed to initialize database: " + e.getMessage(), e);
        }

        // Check if we should start the chat server
        try {
            // Load chat server configuration
            com.ueadmission.config.ChatServerConfig config = com.ueadmission.config.ChatServerConfig.getInstance();

            // Check if auto-start is enabled
            if (config.isAutoStartEnabled()) {
                // Check if server is already running on the configured port
                if (isServerRunning(config.getServerHost(), config.getServerPort())) {
                    LOGGER.info("Chat server is already running at " + config.getServerHost() + ":" + config.getServerPort());
                } else {
                    // Start the chat server
                    ChatManager.getInstance().startServer();
                    LOGGER.info("Chat server started successfully on port " + config.getServerPort());
                }
            } else {
                LOGGER.info("Chat server auto-start is disabled. Using external server at " + 
                           config.getServerHost() + ":" + config.getServerPort());
            }
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Failed to start chat server: " + e.getMessage(), e);
        }
    }

    @Override
    public void start(Stage primaryStage) {
        try {
            // Load main FXML
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com.ueadmission/main.fxml"));
            Parent root = loader.load();

            // Set up the scene
            Scene scene = new Scene(root, 1200, 800);
            scene.getStylesheets().add(getClass().getResource("/com.ueadmission/common.css").toExternalForm());

            // Configure stage
            primaryStage.setTitle("UIU Admission System");
            primaryStage.setScene(scene);
            primaryStage.setMinWidth(800);
            primaryStage.setMinHeight(600);
            primaryStage.show();

            LOGGER.info("Application started successfully");
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Failed to start application: " + e.getMessage(), e);
            e.printStackTrace();

            // Show error alert
            Alert alert = new Alert(Alert.AlertType.ERROR,
                    "Application error: " + e.getMessage(),
                    ButtonType.OK);
            alert.setTitle("Application Error");
            alert.setHeaderText("Failed to Start Application");
            alert.showAndWait();
        }
    }

    @Override
    public void stop() {
        try {
            // Disconnect chat client if connected
            try {
                ChatClient.getInstance().disconnect();
                LOGGER.info("Chat client disconnected");
            } catch (Exception e) {
                LOGGER.log(Level.WARNING, "Error disconnecting chat client: " + e.getMessage(), e);
            }

            // Stop chat server
            try {
                ChatManager.getInstance().stopServer();
                LOGGER.info("Chat server stopped successfully");
            } catch (Exception e) {
                LOGGER.log(Level.WARNING, "Error stopping chat server: " + e.getMessage(), e);
            }

            // Close database connection
            try {
                com.ueadmission.db.DatabaseConnection.closeConnection();
                LOGGER.info("Database connection closed");
            } catch (Exception e) {
                LOGGER.log(Level.WARNING, "Error closing database connection: " + e.getMessage(), e);
            }
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error during application shutdown", e);
        }
    }

    public static void main(String[] args) {
        launch(args);
    }

    /**
     * Check if a server is running on the specified host and port
     * @param host The host to check
     * @param port The port to check
     * @return true if a server is running, false otherwise
     */
    private boolean isServerRunning(String host, int port) {
        try (Socket socket = new Socket()) {
            // Try to connect to the server with a short timeout
            socket.connect(new InetSocketAddress(host, port), 1000);
            return true;
        } catch (IOException e) {
            // If connection fails, server is not running
            return false;
        }
    }
}
