package com.ueadmission.chat.server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.ueadmission.db.DatabaseConnection;

/**
 * Handles communication with a single chat client
 */
public class ClientHandler implements Runnable {
    private static final Logger LOGGER = Logger.getLogger(ClientHandler.class.getName());

    private Socket clientSocket;
    private BufferedReader reader;
    private PrintWriter writer;
    private ClientManager clientManager;
    private int userId = -1;
    private boolean authenticated = false;

    /**
     * Create a new client handler
     * @param clientSocket The client socket
     * @param clientManager The client manager for tracking all clients
     */
    public ClientHandler(Socket clientSocket, ClientManager clientManager) {
        this.clientSocket = clientSocket;
        this.clientManager = clientManager;

        try {
            this.reader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            this.writer = new PrintWriter(clientSocket.getOutputStream(), true);
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Error creating client streams", e);
        }
    }

    /**
     * Handle client messages
     */
    @Override
    public void run() {
        try {
            String message;
            while ((message = reader.readLine()) != null) {
                processMessage(message);
            }
        } catch (IOException e) {
            LOGGER.log(Level.INFO, "Client disconnected", e);
        } finally {
            disconnect();
        }
    }

    /**
     * Process a message from the client
     */
    private void processMessage(String message) {
        try {
            if (message.startsWith("AUTH:")) {
                // Handle authentication
                String[] parts = message.split(":", 2);
                if (parts.length == 2) {
                    authenticate(Integer.parseInt(parts[1]));
                }
            } else if (!authenticated) {
                // Reject messages from unauthenticated clients
                sendError("Not authenticated");
            } else if (message.startsWith("MSG:")) {
                // Handle direct message
                String[] parts = message.split(":", 3);
                if (parts.length == 3) {
                    int receiverId = Integer.parseInt(parts[1]);
                    String content = parts[2];
                    handleDirectMessage(receiverId, content);
                }
            } else if (message.startsWith("BROADCAST:")) {
                // Handle broadcast message
                String[] parts = message.split(":", 2);
                if (parts.length == 2) {
                    String content = parts[1];
                    handleBroadcastMessage(content);
                }
            } else if (message.startsWith("STATUS:")) {
                // Handle status update
                String[] parts = message.split(":", 2);
                if (parts.length == 2) {
                    String status = parts[1];
                    handleStatusUpdate(status);
                }
            }
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Error processing message: " + message, e);
            sendError("Error processing message: " + e.getMessage());
        }
    }

    /**
     * Authenticate a client
     */
    private void authenticate(int userId) {
        try {
            // Verify user exists
            if (verifyUser(userId)) {
                this.userId = userId;
                this.authenticated = true;

                // Register client with manager
                clientManager.registerClient(userId, this);

                // Update user status in the database
                updateUserStatus("online");

                LOGGER.info("User " + userId + " authenticated");

                // Send stored offline messages if any
                sendStoredMessages();
            } else {
                sendError("Authentication failed: Invalid user ID");
            }
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Authentication error", e);
            sendError("Authentication failed: " + e.getMessage());
        }
    }

    /**
     * Verify user exists in the database
     */
    private boolean verifyUser(int userId) {
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            conn = DatabaseConnection.getConnection();
            ps = conn.prepareStatement("SELECT id FROM users WHERE id = ?");
            ps.setInt(1, userId);

            rs = ps.executeQuery();
            boolean userExists = rs.next();

            return userExists;
        } catch (SQLException e) {
            LOGGER.log(Level.WARNING, "Error verifying user", e);
            return false;
        } finally {
            // Close resources
            DatabaseConnection.closeResources(ps, rs);
        }
    }

    /**
     * Handle direct message
     */
    private void handleDirectMessage(int receiverId, String content) {
        // Get current timestamp
        LocalDateTime timestamp = LocalDateTime.now();
        String formattedTimestamp = timestamp.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);

        // Store message in database
        storeMessage(userId, receiverId, content, timestamp);

        // Try to send to receiver if online
        boolean delivered = clientManager.sendMessageToClient(receiverId,
                "MSG:" + userId + ":" + formattedTimestamp + ":" + content);

        if (!delivered) {
            LOGGER.info("Message stored for offline user " + receiverId);
        }
    }

    /**
     * Handle broadcast message
     */
    private void handleBroadcastMessage(String content) {
        // Get current timestamp
        LocalDateTime timestamp = LocalDateTime.now();
        String formattedTimestamp = timestamp.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);

        // Broadcast to all connected clients except sender
        clientManager.broadcastMessage(userId,
                "MSG:" + userId + ":" + formattedTimestamp + ":" + content);

        // Store broadcast message in database for offline users
        storeBroadcastMessage(userId, content, timestamp);
    }

    /**
     * Handle status update
     */
    private void handleStatusUpdate(String status) {
        // Update user status in database
        updateUserStatus(status);

        // Broadcast status update to other clients
        clientManager.broadcastStatus(userId, status);
    }

    /**
     * Store message in database
     */
    private void storeMessage(int senderId, int receiverId, String content, LocalDateTime timestamp) {
        Connection conn = null;
        PreparedStatement ps = null;
        try {
            conn = DatabaseConnection.getConnection();
            ps = conn.prepareStatement(
                "INSERT INTO chat_messages (sender_id, receiver_id, message, timestamp, is_read) " +
                "VALUES (?, ?, ?, ?, false)");

            ps.setInt(1, senderId);
            ps.setInt(2, receiverId);
            ps.setString(3, content);
            ps.setTimestamp(4, java.sql.Timestamp.valueOf(timestamp));

            ps.executeUpdate();
        } catch (SQLException e) {
            LOGGER.log(Level.WARNING, "Error storing message in database", e);
        } finally {
            // Close resources
            DatabaseConnection.closeResources(ps, null);
        }
    }

    /**
     * Store broadcast message in database
     */
    private void storeBroadcastMessage(int senderId, String content, LocalDateTime timestamp) {
        Connection conn = null;
        PreparedStatement getUsersPs = null;
        ResultSet rs = null;
        try {
            conn = DatabaseConnection.getConnection();

            // Get all user IDs except the sender
            getUsersPs = conn.prepareStatement(
                "SELECT id FROM users WHERE id != ?");
            getUsersPs.setInt(1, senderId);

            rs = getUsersPs.executeQuery();

            // Store message for each user
            while (rs.next()) {
                int receiverId = rs.getInt("id");
                storeMessage(senderId, receiverId, content, timestamp);
            }
        } catch (SQLException e) {
            LOGGER.log(Level.WARNING, "Error storing broadcast message", e);
        } finally {
            // Close resources
            DatabaseConnection.closeResources(getUsersPs, rs);
        }
    }

    /**
     * Update user status in database
     */
    private void updateUserStatus(String status) {
        Connection conn = null;
        PreparedStatement ps = null;
        try {
            conn = DatabaseConnection.getConnection();
            ps = conn.prepareStatement(
                "INSERT INTO user_status (user_id, status, last_active) VALUES (?, ?, NOW()) " +
                "ON DUPLICATE KEY UPDATE status = ?, last_active = NOW()");

            ps.setInt(1, userId);
            ps.setString(2, status);
            ps.setString(3, status);

            ps.executeUpdate();
        } catch (SQLException e) {
            LOGGER.log(Level.WARNING, "Error updating user status in database", e);
        } finally {
            // Close resources
            DatabaseConnection.closeResources(ps, null);
        }
    }

    /**
     * Send stored messages to client
     */
    private void sendStoredMessages() {
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            conn = DatabaseConnection.getConnection();
            ps = conn.prepareStatement(
                "SELECT sender_id, message, timestamp, id FROM chat_messages " +
                "WHERE receiver_id = ? AND is_read = false ORDER BY timestamp ASC");

            ps.setInt(1, userId);

            rs = ps.executeQuery();

            // Check if we got a valid result set
            if (rs == null) {
                LOGGER.warning("Query returned null ResultSet");
                return;
            }

            while (rs.next()) {
                try {
                    int senderId = rs.getInt("sender_id");
                    String content = rs.getString("message");
                    java.sql.Timestamp sqlTimestamp = rs.getTimestamp("timestamp");
                    int messageId = rs.getInt("id");

                    if (sqlTimestamp == null) {
                        LOGGER.warning("Null timestamp for message ID " + messageId);
                        continue;
                    }

                    LocalDateTime timestamp = sqlTimestamp.toLocalDateTime();

                    // Send message to client
                    String formattedTimestamp = timestamp.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
                    sendMessage("MSG:" + senderId + ":" + formattedTimestamp + ":" + content);

                    // Mark message as read
                    markMessageAsRead(messageId);
                } catch (SQLException ex) {
                    LOGGER.log(Level.WARNING, "Error processing a message in ResultSet", ex);
                    // Continue with the next message instead of failing completely
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.WARNING, "Error sending stored messages", e);
        } finally {
            // Close resources in reverse order of acquisition
            DatabaseConnection.closeResources(ps, rs);
        }
    }

    /**
     * Mark message as read
     */
    private void markMessageAsRead(int messageId) {
        Connection conn = null;
        PreparedStatement ps = null;
        try {
            conn = DatabaseConnection.getConnection();
            ps = conn.prepareStatement(
                "UPDATE chat_messages SET is_read = true WHERE id = ?");

            ps.setInt(1, messageId);
            ps.executeUpdate();
        } catch (SQLException e) {
            LOGGER.log(Level.WARNING, "Error marking message as read", e);
        } finally {
            // Close resources
            DatabaseConnection.closeResources(ps, null);
        }
    }

    /**
     * Send a message to the client
     */
    public void sendMessage(String message) {
        try {
            if (writer != null && !clientSocket.isClosed()) {
                writer.println(message);
            }
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Error sending message to client", e);
        }
    }

    /**
     * Send an error message to the client
     */
    private void sendError(String errorMessage) {
        sendMessage("ERROR:" + errorMessage);
    }

    /**
     * Disconnect client
     */
    public void disconnect() {
        try {
            if (authenticated) {
                // Update user status to offline
                updateUserStatus("offline");

                // Unregister from client manager
                clientManager.unregisterClient(userId);

                LOGGER.info("User " + userId + " disconnected");
            }

            if (clientSocket != null && !clientSocket.isClosed()) {
                clientSocket.close();
            }

            if (reader != null) {
                reader.close();
            }

            if (writer != null) {
                writer.close();
            }
        } catch (IOException e) {
            LOGGER.log(Level.WARNING, "Error disconnecting client", e);
        }
    }

    /**
     * Get user ID
     */
    public int getUserId() {
        return userId;
    }
}
