package com.ueadmission.chat;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.ueadmission.auth.state.AuthStateManager;
import com.ueadmission.db.DatabaseConnection;
import com.ueadmission.db.DatabaseInitializer;
import javafx.application.Platform;

/**
 * Manages chat functionality for the application
 */
public class ChatManager {
    private static final Logger LOGGER = Logger.getLogger(ChatManager.class.getName());
    private static final int PORT = 9001;

    private static ChatManager instance;
    private ExecutorService executor;
    private ServerSocket serverSocket;
    private boolean isRunning = false;

    // Store client handlers by user ID
    private Map<Integer, ClientHandler> connectedClients = new HashMap<>();

    // Store message listeners by user ID
    private Map<Integer, List<ChatMessageListener>> messageListeners = new HashMap<>();

    private ChatManager() {
        executor = Executors.newCachedThreadPool();
    }

    public static ChatManager getInstance() {
        if (instance == null) {
            instance = new ChatManager();
        }
        return instance;
    }

    /**
     * Start the chat server
     */
    public void startServer() {
        if (isRunning) return;

        try {
            serverSocket = new ServerSocket(PORT);
            isRunning = true;
            LOGGER.info("Chat server started on port " + PORT);

            // Start server in a separate thread
            executor.submit(() -> {
                while (isRunning) {
                    try {
                        Socket clientSocket = serverSocket.accept();
                        ClientHandler clientHandler = new ClientHandler(clientSocket);
                        executor.submit(clientHandler);
                    } catch (IOException e) {
                        if (isRunning) {
                            LOGGER.log(Level.SEVERE, "Error accepting client connection", e);
                        }
                    }
                }
            });
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Could not start chat server", e);
        }
    }

    /**
     * Stop the chat server
     */
    public void stopServer() {
        isRunning = false;

        // Close all client connections
        for (ClientHandler handler : connectedClients.values()) {
            handler.disconnect();
        }
        connectedClients.clear();

        // Close server socket
        if (serverSocket != null && !serverSocket.isClosed()) {
            try {
                serverSocket.close();
            } catch (IOException e) {
                LOGGER.log(Level.WARNING, "Error closing server socket", e);
            }
        }

        // Shutdown executor
        executor.shutdownNow();
        LOGGER.info("Chat server stopped");
    }

    /**
     * Add a new connected client
     */
    public void addClient(int userId, ClientHandler handler) {
        connectedClients.put(userId, handler);
        updateUserStatus(userId, "online");
        broadcastUserStatus(userId, "online");
        LOGGER.info("Client added: User ID " + userId);
    }

    /**
     * Remove a client when disconnected
     */
    public void removeClient(int userId) {
        connectedClients.remove(userId);
        updateUserStatus(userId, "offline");
        broadcastUserStatus(userId, "offline");
        LOGGER.info("Client removed: User ID " + userId);
    }

    /**
     * Send message to a specific client
     */
    public void sendMessage(int senderId, int receiverId, String message) {
        // Store message in database
        storeMessage(senderId, receiverId, message, false);

        // If receiver is connected, send message directly
        ClientHandler receiverHandler = connectedClients.get(receiverId);
        if (receiverHandler != null) {
            receiverHandler.sendMessage(senderId, message);
        }

        // Notify listeners for both sender and receiver
        notifyMessageListeners(senderId, receiverId, message, LocalDateTime.now());
        notifyMessageListeners(receiverId, senderId, message, LocalDateTime.now());
    }

    /**
     * Broadcast message to all connected clients
     */
    public void broadcastMessage(int senderId, String message) {
        // Store broadcast message in database
        storeMessage(senderId, null, message, true);

        // Send to all connected clients except sender
        for (Map.Entry<Integer, ClientHandler> entry : connectedClients.entrySet()) {
            if (entry.getKey() != senderId) {
                entry.getValue().sendMessage(senderId, message);
                notifyMessageListeners(entry.getKey(), senderId, message, LocalDateTime.now());
            }
        }

        // Also notify sender's listeners
        notifyMessageListeners(senderId, null, message, LocalDateTime.now());
    }

    /**
     * Store message in database
     */
    private void storeMessage(int senderId, Integer receiverId, String message, boolean isBroadcast) {
        Connection conn = null;
        PreparedStatement ps = null;

        try {
            conn = DatabaseConnection.getConnection();
            String sql = "INSERT INTO chat_messages (sender_id, receiver_id, message, is_broadcast) VALUES (?, ?, ?, ?)";  
            ps = conn.prepareStatement(sql);
            ps.setInt(1, senderId);
            if (receiverId != null) {
                ps.setInt(2, receiverId);
            } else {
                ps.setNull(2, Types.INTEGER);
            }
            ps.setString(3, message);
            ps.setBoolean(4, isBroadcast);
            ps.executeUpdate();

            LOGGER.info("Message stored in database");
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error storing message in database", e);
        } finally {
            DatabaseConnection.closeResources(ps, null);
        }
    }

    /**
     * Update user status in database
     */
    private void updateUserStatus(int userId, String status) {
        Connection conn = null;
        PreparedStatement ps = null;

        try {
            conn = DatabaseConnection.getConnection();
            String sql = "UPDATE user_status SET status = ?, last_active = CURRENT_TIMESTAMP WHERE user_id = ?";  
            ps = conn.prepareStatement(sql);
            ps.setString(1, status);
            ps.setInt(2, userId);
            ps.executeUpdate();
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error updating user status", e);
        } finally {
            DatabaseConnection.closeResources(ps, null);
        }
    }

    /**
     * Broadcast user status change to all clients
     */
    private void broadcastUserStatus(int userId, String status) {
        // Implementation depends on protocol details
        // This is a simplified version
        for (ClientHandler handler : connectedClients.values()) {
            if (handler.getUserId() != userId) {
                handler.sendStatusUpdate(userId, status);
            }
        }
    }

    /**
     * Add a listener for new messages
     */
    public void addMessageListener(int userId, ChatMessageListener listener) {
        messageListeners.computeIfAbsent(userId, k -> new ArrayList<>()).add(listener);
    }

    /**
     * Remove a message listener
     */
    public void removeMessageListener(int userId, ChatMessageListener listener) {
        List<ChatMessageListener> listeners = messageListeners.get(userId);
        if (listeners != null) {
            listeners.remove(listener);
        }
    }

    /**
     * Notify all listeners for a user about a new message
     */
    private void notifyMessageListeners(int userId, Integer fromUserId, String message, LocalDateTime timestamp) {
        List<ChatMessageListener> listeners = messageListeners.get(userId);
        if (listeners != null) {
            for (ChatMessageListener listener : listeners) {
                Platform.runLater(() -> {
                    listener.onMessageReceived(fromUserId, message, timestamp);
                });
            }
        }
    }

    /**
     * Load chat history between two users
     */
    public List<ChatMessage> loadChatHistory(int userId1, int userId2) {
        List<ChatMessage> messages = new ArrayList<>();
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            conn = DatabaseConnection.getConnection();

            // Check if chat_messages table exists
            try {
                DatabaseMetaData dbm = conn.getMetaData();
                ResultSet tables = dbm.getTables(null, null, "chat_messages", null);
                boolean tableExists = tables.next();
                tables.close();

                if (!tableExists) {
                    // Table doesn't exist, initialize database
                    DatabaseInitializer.initializeDatabase();
                    // Return empty list for now
                    return messages;
                }
            } catch (SQLException e) {
                LOGGER.log(Level.WARNING, "Could not check if chat_messages table exists", e);
                return messages;
            }

            // Get direct messages between the two users or broadcast messages
            String sql = "SELECT * FROM chat_messages WHERE "
                + "((sender_id = ? AND receiver_id = ?) OR (sender_id = ? AND receiver_id = ?) OR "
                + "(is_broadcast = TRUE AND (sender_id = ? OR sender_id = ?))) "
                + "ORDER BY timestamp ASC";

            ps = conn.prepareStatement(sql);
            ps.setInt(1, userId1);
            ps.setInt(2, userId2);
            ps.setInt(3, userId2);
            ps.setInt(4, userId1);
            ps.setInt(5, userId1);
            ps.setInt(6, userId2);
            rs = ps.executeQuery();

            while (rs.next()) {
                int senderId = rs.getInt("sender_id");
                Integer receiverId = rs.getObject("receiver_id") != null ? rs.getInt("receiver_id") : null;
                String messageText = rs.getString("message");
                Timestamp timestamp = rs.getTimestamp("timestamp");
                boolean isBroadcast = rs.getBoolean("is_broadcast");

                ChatMessage message = new ChatMessage(
                    senderId,
                    receiverId,
                    messageText,
                    timestamp.toLocalDateTime(),
                    isBroadcast
                );
                messages.add(message);
            }
        } catch (SQLException e) {
            // Log error but don't crash - we'll just show an empty chat history
            LOGGER.log(Level.WARNING, "Error loading chat history: " + e.getMessage());
        } finally {
            DatabaseConnection.closeResources(ps, rs);
        }

        return messages;
    }

    /**
     * Load all users with their statuses
     */
    public List<ChatUser> loadAllUsers() {
        List<ChatUser> users = new ArrayList<>();
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            conn = DatabaseConnection.getConnection();
            String sql = "SELECT u.id, u.first_name, u.last_name, u.email, u.role, s.status "
                + "FROM users u JOIN user_status s ON u.id = s.user_id";

            ps = conn.prepareStatement(sql);
            rs = ps.executeQuery();

            while (rs.next()) {
                int id = rs.getInt("id");
                String firstName = rs.getString("first_name");
                String lastName = rs.getString("last_name");
                String email = rs.getString("email");
                String role = rs.getString("role");
                String status = rs.getString("status");

                // Get last message for this user
                String lastMessage = getLastMessageWithUser(id);

                // Generate avatar color based on user ID
                String color = generateAvatarColor(id);

                ChatUser user = new ChatUser(
                    id,
                    firstName + " " + lastName,
                    email,
                    role,
                    status,
                    color,
                    lastMessage
                );
                users.add(user);
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error loading users", e);
        } finally {
            DatabaseConnection.closeResources(ps, rs);
        }

        return users;
    }

    /**
     * Get the last message exchanged with a user
     */
    private String getLastMessageWithUser(int userId) {
        // Get current user ID
        Integer currentUserId = AuthStateManager.getInstance().getState().getUser().getId();
        if (currentUserId == null) return "";

        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            conn = DatabaseConnection.getConnection();
            String sql = "SELECT message FROM chat_messages WHERE "
                + "((sender_id = ? AND receiver_id = ?) OR (sender_id = ? AND receiver_id = ?)) "
                + "ORDER BY timestamp DESC LIMIT 1";

            ps = conn.prepareStatement(sql);
            ps.setInt(1, currentUserId);
            ps.setInt(2, userId);
            ps.setInt(3, userId);
            ps.setInt(4, currentUserId);
            rs = ps.executeQuery();

            if (rs.next()) {
                return rs.getString("message");
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error getting last message", e);
        } finally {
            DatabaseConnection.closeResources(ps, rs);
        }

        return "";
    }

    /**
     * Generate a consistent color for avatar based on user ID
     */
    private String generateAvatarColor(int userId) {
        // Simple hash to generate consistent colors
        String[] colors = {
            "#3498db", "#e74c3c", "#2ecc71", "#f39c12", "#9b59b6",
            "#1abc9c", "#d35400", "#c0392b", "#16a085", "#8e44ad"
        };

        return colors[Math.abs(userId) % colors.length];
    }

    /**
     * Mark messages as read
     */
    public void markMessagesAsRead(int senderId, int receiverId) {
        Connection conn = null;
        PreparedStatement ps = null;

        try {
            conn = DatabaseConnection.getConnection();

            // Check if chat_messages table exists
            try {
                DatabaseMetaData dbm = conn.getMetaData();
                ResultSet tables = dbm.getTables(null, null, "chat_messages", null);
                boolean tableExists = tables.next();
                tables.close();

                if (!tableExists) {
                    // Table doesn't exist, initialize database but don't try to update
                    DatabaseInitializer.initializeDatabase();
                    return;
                }
            } catch (SQLException e) {
                LOGGER.log(Level.WARNING, "Could not check if chat_messages table exists", e);
                return;
            }

            String sql = "UPDATE chat_messages SET is_read = TRUE "
                + "WHERE sender_id = ? AND receiver_id = ? AND is_read = FALSE";

            ps = conn.prepareStatement(sql);
            ps.setInt(1, senderId);
            ps.setInt(2, receiverId);
            ps.executeUpdate();
        } catch (SQLException e) {
            // Log error but don't crash
            LOGGER.log(Level.WARNING, "Error marking messages as read: " + e.getMessage());
        } finally {
            DatabaseConnection.closeResources(ps, null);
        }
    }

    /**
     * Get unread message count from a specific user
     */
    public int getUnreadMessageCount(int fromUserId) {
        // Get current user ID
        Integer currentUserId = AuthStateManager.getInstance().getState().getUser().getId();
        if (currentUserId == null) return 0;

        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            conn = DatabaseConnection.getConnection();
            String sql = "SELECT COUNT(*) FROM chat_messages "
                + "WHERE sender_id = ? AND receiver_id = ? AND is_read = FALSE";

            ps = conn.prepareStatement(sql);
            ps.setInt(1, fromUserId);
            ps.setInt(2, currentUserId);
            rs = ps.executeQuery();

            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error getting unread message count", e);
        } finally {
            DatabaseConnection.closeResources(ps, rs);
        }

        return 0;
    }

    /**
     * Client handler inner class for handling client connections
     */
    public class ClientHandler implements Runnable {
        private Socket clientSocket;
        private java.io.BufferedReader reader;
        private java.io.PrintWriter writer;
        private int userId;
        private boolean isConnected = false;

        public ClientHandler(Socket socket) {
            this.clientSocket = socket;
            try {
                reader = new java.io.BufferedReader(new java.io.InputStreamReader(socket.getInputStream()));
                writer = new java.io.PrintWriter(socket.getOutputStream(), true);
            } catch (IOException e) {
                LOGGER.log(Level.SEVERE, "Error creating client handler", e);
            }
        }

        @Override
        public void run() {
            try {
                // First message should be authentication
                String authMessage = reader.readLine();
                // Parse auth message (format: "AUTH:userId")
                if (authMessage != null && authMessage.startsWith("AUTH:")) {
                    userId = Integer.parseInt(authMessage.substring(5));
                    isConnected = true;

                    // Add this client to the manager
                    addClient(userId, this);

                    // Main message loop
                    String message;
                    while (isConnected && (message = reader.readLine()) != null) {
                        processMessage(message);
                    }
                } else {
                    // Invalid authentication
                    writer.println("ERROR:Authentication required");
                }
            } catch (IOException e) {
                LOGGER.log(Level.WARNING, "Client connection error", e);
            } finally {
                disconnect();
            }
        }

        /**
         * Process an incoming message
         */
        private void processMessage(String message) {
            // Parse message format (e.g., "MSG:receiverId:message" or "BROADCAST:message")
            if (message.startsWith("MSG:")) {
                // Direct message
                int separatorIndex = message.indexOf(':', 4);
                if (separatorIndex > 0) {
                    int receiverId = Integer.parseInt(message.substring(4, separatorIndex));
                    String content = message.substring(separatorIndex + 1);
                    sendMessage(userId, content);
                }
            } else if (message.startsWith("BROADCAST:")) {
                // Broadcast message
                String content = message.substring(10);
                broadcastMessage(userId, content);
            } else if (message.startsWith("STATUS:")) {
                // Status update
                String status = message.substring(7);
                updateUserStatus(userId, status);
                broadcastUserStatus(userId, status);
            }
        }

        /**
         * Send a message to this client
         */
        public void sendMessage(int fromUserId, String message) {
            if (isConnected) {
                writer.println("MSG:" + fromUserId + ":" + message);
            }
        }

        /**
         * Send a status update to this client
         */
        public void sendStatusUpdate(int userId, String status) {
            if (isConnected) {
                writer.println("STATUS:" + userId + ":" + status);
            }
        }

        /**
         * Disconnect this client
         */
        public void disconnect() {
            if (isConnected) {
                isConnected = false;
                try {
                    if (clientSocket != null && !clientSocket.isClosed()) {
                        clientSocket.close();
                    }
                } catch (IOException e) {
                    LOGGER.log(Level.WARNING, "Error closing client socket", e);
                }

                // Remove from connected clients
                removeClient(userId);
            }
        }

        /**
         * Get the user ID for this client
         */
        public int getUserId() {
            return userId;
        }
    }

    /**
     * Chat Message class to represent a message
     */
    public static class ChatMessage {
        private int senderId;
        private Integer receiverId;
        private String message;
        private LocalDateTime timestamp;
        private boolean isBroadcast;

        public ChatMessage(int senderId, Integer receiverId, String message, 
                           LocalDateTime timestamp, boolean isBroadcast) {
            this.senderId = senderId;
            this.receiverId = receiverId;
            this.message = message;
            this.timestamp = timestamp;
            this.isBroadcast = isBroadcast;
        }

        public int getSenderId() { return senderId; }
        public Integer getReceiverId() { return receiverId; }
        public String getMessage() { return message; }
        public LocalDateTime getTimestamp() { return timestamp; }
        public boolean isBroadcast() { return isBroadcast; }
    }

    /**
     * Chat User class to represent a user in the chat system
     */
    public static class ChatUser {
        private int id;
        private String name;
        private String email;
        private String role;
        private String status;
        private String avatarColor;
        private String lastMessage;
        private LocalDateTime lastMessageTime;

        public ChatUser(int id, String name, String email, String role, 
                        String status, String avatarColor, String lastMessage) {
            this.id = id;
            this.name = name;
            this.email = email;
            this.role = role;
            this.status = status;
            this.avatarColor = avatarColor;
            this.lastMessage = lastMessage;
            this.lastMessageTime = LocalDateTime.now(); // Default to current time
        }

        public ChatUser(int id, String name, String email, String role, 
                        String status, String avatarColor, String lastMessage,
                        LocalDateTime lastMessageTime) {
            this.id = id;
            this.name = name;
            this.email = email;
            this.role = role;
            this.status = status;
            this.avatarColor = avatarColor;
            this.lastMessage = lastMessage;
            this.lastMessageTime = lastMessageTime;
        }

        public int getId() { return id; }
        public String getName() { return name; }
        public String getEmail() { return email; }
        public String getRole() { return role; }
        public String getStatus() { return status; }
        public String getAvatarColor() { return avatarColor; }
        public String getLastMessage() { return lastMessage; }
        public LocalDateTime getLastMessageTime() { return lastMessageTime; }

        public String getFormattedTime() {
            if (lastMessageTime == null) {
                return "";
            }
            return lastMessageTime.format(java.time.format.DateTimeFormatter.ofPattern("h:mm a", java.util.Locale.ENGLISH));
        }

        public String getFormattedFullDateTime() {
            if (lastMessageTime == null) {
                return "";
            }
            return lastMessageTime.format(java.time.format.DateTimeFormatter.ofPattern("MMMM d, yyyy h:mm a", java.util.Locale.ENGLISH));
        }

        public String getInitials() {
            if (name == null || name.isEmpty()) return "";

            String[] parts = name.split(" ");
            if (parts.length >= 2) {
                return (parts[0].charAt(0) + "" + parts[1].charAt(0)).toUpperCase();
            } else if (parts.length == 1) {
                return parts[0].substring(0, 1).toUpperCase();
            }
            return "";
        }
    }

    /**
     * Chat Message Listener interface
     */
    public interface ChatMessageListener {
        void onMessageReceived(Integer fromUserId, String message, LocalDateTime timestamp);
    }
}
