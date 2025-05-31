package com.ueadmission.chat;
import com.ueadmission.auth.state.AuthState;
import com.ueadmission.auth.state.AuthStateManager;
import com.ueadmission.config.ChatServerConfig;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.ueadmission.db.DatabaseConnection;
import javafx.application.Platform;


/**
 * Client for connecting to the chat server
 */
public class ChatClient {
    private static final Logger LOGGER = Logger.getLogger(ChatClient.class.getName());

    private static ChatClient instance;

    private Socket socket;
    private PrintWriter writer;
    private BufferedReader reader;
    private boolean isConnected = false;
    private int userId;
    private ExecutorService executor;

    private List<ChatMessageListener> messageListeners = new ArrayList<>();
    private List<StatusUpdateListener> statusListeners = new ArrayList<>();

    private ChatClient() {
        // Private constructor for singleton pattern
        executor = Executors.newSingleThreadExecutor();
    }

    /**
     * Get the singleton instance
     */
    public static synchronized ChatClient getInstance() {
        if (instance == null) {
            instance = new ChatClient();
        }
        return instance;
    }

    /**
     * Connect to the chat server
     */
    public boolean connect() {
        try {
            // Get current user ID
            AuthStateManager authManager = AuthStateManager.getInstance();
            if (!authManager.isAuthenticated() || authManager.getState().getUser() == null) {
                LOGGER.warning("Cannot connect to chat server - user not authenticated");
                return false;
            }

            userId = authManager.getState().getUser().getId();

            try {
                // Close any existing connection first
                disconnect();

                // Set a reasonable connection timeout so the UI doesn't hang but gives enough time to connect
                socket = new Socket();

                // Get server host and port from configuration
                String serverHost = ChatServerConfig.getInstance().getServerHost();
                int serverPort = ChatServerConfig.getInstance().getServerPort();

                LOGGER.info("Connecting to chat server at " + serverHost + ":" + serverPort);
                socket.connect(new InetSocketAddress(serverHost, serverPort), 5000);
                writer = new PrintWriter(socket.getOutputStream(), true);
                reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));

                // Send authentication message
                writer.println("AUTH:" + userId);

                // Ensure executor service is running
                ensureExecutorRunning();

                // Start message receiver in a separate thread
                executor.submit(this::messageReceiver);

                isConnected = true;
                LOGGER.info("Connected to chat server as user " + userId);

                // Update user status to online in database
                try {
                    Connection conn = DatabaseConnection.getConnection();
                    PreparedStatement ps = conn.prepareStatement(
                        "INSERT INTO user_status (user_id, status, last_active) VALUES (?, 'online', NOW()) " +
                        "ON DUPLICATE KEY UPDATE status = 'online', last_active = NOW()");
                    ps.setInt(1, userId);
                    ps.executeUpdate();
                    ps.close();
                } catch (SQLException e) {
                    LOGGER.log(Level.WARNING, "Could not update user status in database", e);
                }

                return true;
            } catch (IOException e) {
                LOGGER.log(Level.WARNING, "Failed to connect to chat server. Chat will use offline mode.", e);
                // Set offline mode
                isConnected = false;

                // Update user status to offline in database
                try {
                    Connection conn = DatabaseConnection.getConnection();
                    PreparedStatement ps = conn.prepareStatement(
                        "INSERT INTO user_status (user_id, status, last_active) VALUES (?, 'offline', NOW()) " +
                        "ON DUPLICATE KEY UPDATE status = 'offline', last_active = NOW()");
                    ps.setInt(1, userId);
                    ps.executeUpdate();
                    ps.close();
                } catch (SQLException dbError) {
                    LOGGER.log(Level.WARNING, "Could not update user status in database", dbError);
                }

                return false;
            }
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Unexpected error during chat connection", e);
            isConnected = false;
            return false;
        }
    }

    /**
     * Disconnect from the chat server
     */
    public void disconnect() {
        isConnected = false;

        try {
            if (socket != null && !socket.isClosed()) {
                socket.close();
            }
        } catch (IOException e) {
            LOGGER.log(Level.WARNING, "Error closing socket", e);
        }

        // Shutdown executor service if it exists and is not already shut down
        if (executor != null && !executor.isShutdown()) {
            executor.shutdownNow();
        }

        // Create a new executor service for future connections
        executor = Executors.newSingleThreadExecutor();

        LOGGER.info("Disconnected from chat server");
    }

    /**
     * Send a direct message to another user
     * @return true if message sent successfully
     */
    public boolean sendMessage(int receiverId, String message) {
        if (isConnected && writer != null) {
            try {
                writer.println("MSG:" + receiverId + ":" + message);
                LOGGER.info("Sent message to user " + receiverId);
                return true;
            } catch (Exception e) {
                LOGGER.log(Level.WARNING, "Error sending message", e);
                return false;
            }
        } else {
            LOGGER.warning("Cannot send message: not connected");
            return false;
        }
    }

    /**
     * Send a broadcast message to all users
     * @return true if message sent successfully
     */
    public boolean sendBroadcast(String message) {
        if (isConnected && writer != null) {
            try {
                writer.println("BROADCAST:" + message);
                LOGGER.info("Sent broadcast message");
                return true;
            } catch (Exception e) {
                LOGGER.log(Level.WARNING, "Error sending broadcast", e);
                return false;
            }
        } else {
            LOGGER.warning("Cannot send broadcast: not connected");
            return false;
        }
    }

    /**
     * Update user status
     * @return true if status updated successfully
     */
    public boolean updateStatus(String status) {
        if (isConnected && writer != null) {
            try {
                writer.println("STATUS:" + status);
                LOGGER.info("Updated status to: " + status);
                return true;
            } catch (Exception e) {
                LOGGER.log(Level.WARNING, "Error updating status", e);
                return false;
            }
        } else {
            LOGGER.warning("Cannot update status: not connected");
            return false;
        }
    }

    /**
     * Message receiver method to run in a separate thread
     */
    private void messageReceiver() {
        try {
            String message;
            while (isConnected && (message = reader.readLine()) != null) {
                processMessage(message);
            }
        } catch (IOException e) {
            if (isConnected) {
                LOGGER.log(Level.WARNING, "Error reading from server", e);
                disconnect();
            }
        }
    }

    /**
     * Check if executor service is shut down and recreate if necessary
     */
    private void ensureExecutorRunning() {
        if (executor == null || executor.isShutdown() || executor.isTerminated()) {
            LOGGER.info("Recreating executor service");
            executor = Executors.newSingleThreadExecutor();
        }
    }

    /**
     * Process a message from the server
     */
    /**
     * Process a message from the server
     */
    private void processMessage(String message) {
        try {
            if (message.startsWith("MSG:")) {
                // Message format: MSG:fromUserId:timestamp:content
                String[] parts = message.split(":", 4);
                if (parts.length == 4) {
                    int senderId = Integer.parseInt(parts[1]);
                    // Use a more robust approach to parse the timestamp
                    LocalDateTime timestamp;
                    try {
                        // Try standard ISO format first
                        timestamp = LocalDateTime.parse(parts[2], java.time.format.DateTimeFormatter.ISO_LOCAL_DATE_TIME);
                    } catch (java.time.format.DateTimeParseException e) {
                        try {
                            // Try database format (yyyy-MM-dd HH:mm:ss)
                            java.time.format.DateTimeFormatter dbFormatter = java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
                            timestamp = LocalDateTime.parse(parts[2], dbFormatter);
                        } catch (java.time.format.DateTimeParseException e1) {
                            try {
                                // Try with microseconds/nanoseconds precision
                                java.time.format.DateTimeFormatter preciseFormatter = java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSSSSS");
                                timestamp = LocalDateTime.parse(parts[2], preciseFormatter);
                            } catch (java.time.format.DateTimeParseException e2) {
                                try {
                                    // Try with milliseconds precision
                                    java.time.format.DateTimeFormatter msFormatter = java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS");
                                    timestamp = LocalDateTime.parse(parts[2], msFormatter);
                                } catch (java.time.format.DateTimeParseException e3) {
                                    try {
                                        // Try without nanoseconds but with seconds
                                        java.time.format.DateTimeFormatter secFormatter = java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");
                                        timestamp = LocalDateTime.parse(parts[2], secFormatter);
                                    } catch (java.time.format.DateTimeParseException e4) {
                                        // IMPORTANT: Don't make minutes optional - this causes the 1:00 issue
                                        // Instead use current time as fallback
                                        LOGGER.warning("Could not parse timestamp: " + parts[2] + ". Using current time as fallback.");
                                        timestamp = LocalDateTime.now();
                                    }
                                }
                            }
                        }
                    }

                    // Extract the content part without any timestamp prefix
                    String content = parts[3];
                    // Check if content contains timestamp-like prefix (e.g., "48:32:2342358:message")
                    if (content.contains(":")) {
                        // Try to extract the actual message after the last colon
                        int lastColonIndex = content.lastIndexOf(":");
                        if (lastColonIndex >= 0 && lastColonIndex < content.length() - 1) {
                            content = content.substring(lastColonIndex + 1);
                        }
                    }

                    // Log the original timestamp string and the parsed LocalDateTime for debugging
                    LOGGER.info("Original timestamp string: " + parts[2]);
                    LOGGER.info("Parsed timestamp: " + timestamp);

                    // Notify listeners
                    final int finalSenderId = senderId;
                    final String finalContent = content;
                    final LocalDateTime finalTimestamp = timestamp;
                    Platform.runLater(() -> {
                        for (ChatMessageListener listener : messageListeners) {
                            listener.onMessageReceived(finalSenderId, finalContent, finalTimestamp);
                        }
                    });
                }
            } else if (message.startsWith("STATUS:")) {
                // Status update format: STATUS:userId:status
                String[] parts = message.split(":", 3);
                if (parts.length == 3) {
                    int userIdFromStatus = Integer.parseInt(parts[1]);
                    String status = parts[2];

                    // Notify listeners
                    final int finalUserId = userIdFromStatus;
                    final String finalStatus = status;
                    Platform.runLater(() -> {
                        for (StatusUpdateListener listener : statusListeners) {
                            listener.onStatusUpdate(finalUserId, finalStatus);
                        }
                    });
                }
            } else if (message.startsWith("ERROR:")) {
                // Error message
                String error = message.substring(6);
                LOGGER.warning("Server error: " + error);
            }
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Error processing incoming message: " + message, e);
        }
    }

    /**
     * Add a message listener
     */
    public void addMessageListener(ChatMessageListener listener) {
        messageListeners.add(listener);
    }

    /**
     * Remove a message listener
     */
    public void removeMessageListener(ChatMessageListener listener) {
        messageListeners.remove(listener);
    }

    /**
     * Add a status update listener
     */
    public void addStatusListener(StatusUpdateListener listener) {
        statusListeners.add(listener);
    }

    /**
     * Remove a status update listener
     */
    public void removeStatusListener(StatusUpdateListener listener) {
        statusListeners.remove(listener);
    }

    /**
     * Check if client is connected
     */
    public boolean isConnected() {
        return isConnected;
    }

    /**
     * Chat Message Listener interface
     */
    public interface ChatMessageListener {
        void onMessageReceived(int fromUserId, String message, LocalDateTime timestamp);
    }

    /**
     * Status Update Listener interface
     */
    public interface StatusUpdateListener {
        void onStatusUpdate(int userId, String status);
    }
}
