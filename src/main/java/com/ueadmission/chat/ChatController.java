package com.ueadmission.chat;

import com.ueadmission.auth.state.AuthStateManager;
import com.ueadmission.db.DatabaseConnection;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.stage.Stage;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.ListView;
import javafx.scene.control.ListCell;
import javafx.scene.control.ContentDisplay;
import javafx.geometry.Pos;
import javafx.geometry.Insets;

/**
 * Controller for the chat interface
 */
public class ChatController {
    @FXML
    private Label roleLabel;

    @FXML
    private Label statusLabel;

    @FXML
    private VBox userListPanel;

    @FXML
    private ScrollPane messageScrollPane;

    @FXML
    private VBox messagesContainer;

    @FXML
    private TextField messageInput;

    // Placeholder text for message input
    private final String MESSAGE_PLACEHOLDER = "Type your message here...";

    @FXML
    private Button sendButton;

    @FXML
    private Button attachButton;

    private Stage stage;
    private String currentChatPartner;

    /**
     * Initialize the chat controller
     */
    @FXML
    public void initialize() {
        // Initialize UI components
        updateRoleLabel();
        setupChatInterface();

        // Set up message input with placeholder and focus effects
        messageInput.setPromptText(MESSAGE_PLACEHOLDER);

        // Add focus listeners for enhanced visual feedback
        messageInput.focusedProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue) {
                // When focused
                messageInput.setStyle("-fx-border-color: -primary-light; -fx-border-width: 2px;");
            } else {
                // When focus lost
                messageInput.setStyle("");
            }
        });

        // Add tooltip and visual enhancements to send button
        javafx.scene.control.Tooltip sendTooltip = new javafx.scene.control.Tooltip("Send message");
        sendTooltip.setStyle("-fx-font-size: 12px;");
        sendButton.setTooltip(sendTooltip);

        // Add enter key handler for quick sending
        messageInput.setOnKeyPressed(event -> {
            if (event.getCode() == javafx.scene.input.KeyCode.ENTER) {
                sendMessage();
                event.consume();
            }
        });

        // Auto-scroll to bottom when new messages are added
        messagesContainer.heightProperty().addListener((observable, oldValue, newValue) -> {
            messageScrollPane.setVvalue(1.0);
        });

        // Connect to chat server and register as message listener
        try {
            // Connect to chat server
            ChatClient chatClient = ChatClient.getInstance();
            boolean connected = chatClient.connect();

            // Register message listener regardless of connection status
            chatClient.addMessageListener(this::handleIncomingMessage);

            if (connected) {
                // Set online status
                chatClient.updateStatus("online");
            } else {
                // Show offline message but continue with local functionality
                showOfflineMode();
            }

            // Load chat history from local database regardless of connection status
            if (currentChatPartner != null) {
                loadChatHistory();
            }

        } catch (Exception e) {
            System.err.println("Error setting up chat: " + e.getMessage());
            e.printStackTrace();
            showError("Error initializing chat interface: " + e.getMessage());
            // Still try to set up the UI in a degraded mode
            showOfflineMode();
        }
    }

    /**
     * Show that the chat is in offline mode
     */
    private void showOfflineMode() {
        Platform.runLater(() -> {
            // Add system warning message to chat
            HBox systemMsgBox = new HBox();
            systemMsgBox.setAlignment(Pos.CENTER);
            VBox systemMessage = new VBox();
            systemMessage.getStyleClass().addAll("system-message", "system-warning");
            Label systemText = new Label("Chat server is currently offline. You can view message history, but new messages will be stored locally until connection is restored.");
            systemText.getStyleClass().add("system-message-text");
            systemMessage.getChildren().add(systemText);
            systemMsgBox.getChildren().add(systemMessage);
            messagesContainer.getChildren().add(systemMsgBox);

            // Update status label if it exists
            if (statusLabel != null) {
                statusLabel.setText("Offline");
                statusLabel.getStyleClass().add("status-offline");
            }
        });
    }

    /**
     * Show error message
     */
    private void showError(String message) {
        Platform.runLater(() -> {
            // Add system error message to chat
            HBox systemMsgBox = new HBox();
            systemMsgBox.setAlignment(Pos.CENTER);
            VBox systemMessage = new VBox();
            systemMessage.getStyleClass().addAll("system-message", "system-error");
            Label systemText = new Label(message);
            systemText.getStyleClass().add("system-message-text");
            systemMessage.getChildren().add(systemText);
            systemMsgBox.getChildren().add(systemMessage);
            messagesContainer.getChildren().add(systemMsgBox);
        });
    }

    /**
     * Set the stage reference
     */
    public void setStage(Stage stage) {
        this.stage = stage;
    }

    /**
     * Send a message
     */
    @FXML
    public void sendMessage() {
        String message = messageInput.getText().trim();
        if (!message.isEmpty()) {
            try {
                // Get receiver ID
                int receiverId = getReceiverIdFromCurrentPartner();
                LocalDateTime timestamp = LocalDateTime.now();

                // Add message to UI immediately to improve user experience
                addMessage(message, "You", getCurrentUserRole(), timestamp);

                // Clear input field
                messageInput.clear();

                // Try to send message through chat client
                ChatClient chatClient = ChatClient.getInstance();
                boolean sent = chatClient.sendMessage(receiverId, message);

                if (!sent) {
                    // Store message locally if sending failed
                    storeMessageLocally(receiverId, message, timestamp);

                    // Show a subtle notification that message will be sent when online
                    showInfo("Message will be delivered when connection is restored.");
                }
            } catch (Exception e) {
                System.err.println("Error sending message: " + e.getMessage());
                e.printStackTrace();
                showError("Error sending message: " + e.getMessage());
            }
        }
    }

    /**
     * Store a message locally for later sending
     */
    private void storeMessageLocally(int receiverId, String message, LocalDateTime timestamp) {
        try {
            // Get the current user ID
            AuthStateManager authManager = AuthStateManager.getInstance();
            int senderId = authManager.getState().getUser().getId();

            // Try to store in database if possible
            try (Connection conn = DatabaseConnection.getConnection()) {
                String sql = "INSERT INTO chat_messages_queue (sender_id, receiver_id, message, timestamp, sent) VALUES (?, ?, ?, ?, ?)";
                try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                    stmt.setInt(1, senderId);
                    stmt.setInt(2, receiverId);
                    stmt.setString(3, message);
                    stmt.setTimestamp(4, java.sql.Timestamp.valueOf(timestamp));
                    stmt.setBoolean(5, false); // Not sent yet
                    stmt.executeUpdate();
                }
            } catch (SQLException e) {
                System.err.println("Could not store message in database: " + e.getMessage());
                // Fallback to memory cache or file storage could be implemented here
            }
        } catch (Exception e) {
            System.err.println("Error storing message locally: " + e.getMessage());
        }
    }

    /**
     * Show informational message
     */
    private void showInfo(String message) {
        Platform.runLater(() -> {
            // Add system info message to chat
            HBox systemMsgBox = new HBox();
            systemMsgBox.setAlignment(Pos.CENTER);
            VBox systemMessage = new VBox();
            systemMessage.getStyleClass().addAll("system-message", "system-info");
            Label systemText = new Label(message);
            systemText.getStyleClass().add("system-message-text");
            systemMessage.getChildren().add(systemText);
            systemMsgBox.getChildren().add(systemMessage);
            messagesContainer.getChildren().add(systemMsgBox);
        });
    }

    /**
     * Handle incoming messages from the chat system
     */
    private void handleIncomingMessage(int fromUserId, String message, LocalDateTime timestamp) {
        // Make sure UI updates happen on the JavaFX thread
        Platform.runLater(() -> {
            try {
                // Only process if this is from our current chat partner
                int currentPartnerId = getReceiverIdFromCurrentPartner();
                if (fromUserId == currentPartnerId) {
                    // Lookup sender name from the user database
                    String senderName = getUserNameById(fromUserId);
                    String senderRole = getUserRoleById(fromUserId);

                    // Add message to UI
                    addMessage(message, senderName, senderRole, timestamp);
                } else {
                    // Message from someone other than current partner
                    // Could add notification or update unread count in the future
                }
            } catch (Exception e) {
                System.err.println("Error handling incoming message: " + e.getMessage());
                e.printStackTrace();
            }
        });
    }

    /**
     * Update the role label based on current user
     */
    private void updateRoleLabel() {
        String currentUserRole = getCurrentUserRole();
        roleLabel.setText(currentUserRole.toUpperCase());
        roleLabel.getStyleClass().addAll(
            "admin".equals(currentUserRole) ? "role-admin" : "role-student"
        );
    }

    /**
     * Set up the chat interface based on user role
     */
    private void setupChatInterface() {
        // Clear existing UI elements
        userListPanel.getChildren().clear();
        messagesContainer.getChildren().clear();

        if ("admin".equals(getCurrentUserRole())) {
            setupAdminView();
        } else {
            setupStudentView();
        }


    }

    /**
     * Set up admin view with user list
     */
    private void setupAdminView() {
        Label usersLabel = new Label("Conversations");
        usersLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: -text-dark; -fx-padding: 0 0 10 0;");

        TextField searchField = new TextField();
        searchField.setPromptText("Search conversations");
        searchField.setStyle("-fx-background-color: white; -fx-background-radius: 20px; -fx-border-radius: 20px; -fx-border-color: #e0e0e0; -fx-padding: 8 15;");

        ListView<ChatUser> userListView = new ListView<>();
        userListView.getStyleClass().add("user-list");
        userListView.setItems(loadActualUsers());
        userListView.setCellFactory(lv -> new UserListCell());

        // Add selection listener to change current chat partner
        userListView.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null) {
                // Update current chat partner
                currentChatPartner = newSelection.getName();

                // Load chat history with this partner
                loadChatHistory();
            }
        });

        userListView.getSelectionModel().selectFirst();

        // Set current chat partner from the selected user
        if (!userListView.getItems().isEmpty()) {
            currentChatPartner = userListView.getItems().get(0).getName();
        }

        userListPanel.getChildren().addAll(usersLabel, searchField, userListView);
        VBox.setVgrow(userListView, javafx.scene.layout.Priority.ALWAYS);
    }

    /**
     * Set up student view with admin info
     */
    private void setupStudentView() {
        // Student view - show minimal admin info
        VBox adminInfo = new VBox(15);
        adminInfo.setAlignment(Pos.TOP_CENTER);
        adminInfo.setPadding(new Insets(20, 10, 20, 10));

        StackPane avatarPane = new StackPane();
        avatarPane.setMaxWidth(80);
        avatarPane.setMaxHeight(80);

        Circle avatarCircle = new Circle(40);
        avatarCircle.setFill(Color.valueOf("#F44336"));

        Label initialsLabel = new Label("UA");
        initialsLabel.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: white;");

        avatarPane.getChildren().addAll(avatarCircle, initialsLabel);

        Label adminNameLabel = new Label("UIU Admission Admin");
        adminNameLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: -text-dark;");

        Label roleIndicator = new Label("ADMIN");
        roleIndicator.getStyleClass().addAll("role-badge", "role-admin");

        Label statusIndicator = new Label("Online");
        statusIndicator.setStyle("-fx-font-size: 14px; -fx-text-fill: #4CAF50;");

        // Only add essential elements - remove help links section
        adminInfo.getChildren().addAll(avatarPane, adminNameLabel, roleIndicator, statusIndicator);

        userListPanel.getChildren().add(adminInfo);

        // Set current chat partner
        currentChatPartner = "UIU Admission Admin";
    }



    /**
     * Add a message to the chat interface
     */
    private void addMessage(String content, String sender, String senderRole, LocalDateTime timestamp) {
        HBox messageBox = new HBox(10);
        String currentUserRole = getCurrentUserRole();
        // Check if this message was sent by the current user (sender name will be "You")
        boolean isSentByCurrentUser = "You".equals(sender);

        // Regular message
        if (isSentByCurrentUser) {
            // Messages sent by current user (right-aligned)
            messageBox.setAlignment(Pos.CENTER_RIGHT);

            VBox messageContent = new VBox(5);
            messageContent.getStyleClass().addAll("message", "sent-message");
            messageContent.setMaxWidth(1600);

            Label messageText = new Label(content);
            messageText.getStyleClass().add("message-text");
            messageText.setWrapText(true);
            messageText.setMaxWidth(1560);

            HBox timeBox = new HBox();
            timeBox.setAlignment(Pos.CENTER_RIGHT);
            Label messageTime = new Label(formatTime(timestamp));
            messageTime.getStyleClass().add("message-time");
            timeBox.getChildren().add(messageTime);

            messageContent.getChildren().addAll(messageText, timeBox);
            messageBox.getChildren().add(messageContent);
        } else {
            // Messages received (left-aligned with avatar)
            messageBox.setAlignment(Pos.CENTER_LEFT);

            StackPane avatarPane = new StackPane();
            avatarPane.setPrefSize(35, 35);
            avatarPane.setMaxSize(35, 35);

            Circle avatarCircle = new Circle(17.5);
            avatarCircle.setFill(Color.valueOf("admin".equals(senderRole) ? "#F44336" : "#4CAF50"));

            // Get first letter of sender name for avatar
            String senderInitial = sender.substring(0, 1).toUpperCase();
            Label initialLabel = new Label(senderInitial);
            initialLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: white;");

            avatarPane.getChildren().addAll(avatarCircle, initialLabel);

            VBox messageContent = new VBox(5);
            messageContent.getStyleClass().addAll("message", "received-message",
                                        "admin".equals(senderRole) ? "admin-message" : "student-message");
            messageContent.setMaxWidth(1600);

            HBox messageHeader = new HBox(5);
            messageHeader.setAlignment(Pos.CENTER_LEFT);
            Label senderLabel = new Label(sender);
            senderLabel.setStyle("-fx-font-size: 12px; -fx-font-weight: bold; -fx-text-fill: #555;");

            // Role label removed as per requirement

            messageHeader.getChildren().addAll(senderLabel);

            Label messageText = new Label(content);
            messageText.getStyleClass().add("message-text");
            messageText.setWrapText(true);
            messageText.setMaxWidth(1560);

            HBox timeBox = new HBox();
            timeBox.setAlignment(Pos.CENTER_LEFT);
            Label messageTime = new Label(formatTime(timestamp));
            messageTime.getStyleClass().add("message-time");
            timeBox.getChildren().add(messageTime);

            messageContent.getChildren().addAll(messageHeader, messageText, timeBox);

            messageBox.getChildren().addAll(avatarPane, messageContent);
        }

        VBox.setMargin(messageBox, new Insets(5, 0, 5, 0));

        // Apply a fade-in and slide-up animation for new messages
        messageBox.setOpacity(0);
        messageBox.setTranslateY(20);

        messagesContainer.getChildren().add(messageBox);

        // Create and play the animations
        javafx.animation.FadeTransition fadeIn = new javafx.animation.FadeTransition(javafx.util.Duration.millis(300), messageBox);
        fadeIn.setFromValue(0);
        fadeIn.setToValue(1);

        javafx.animation.TranslateTransition slideUp = new javafx.animation.TranslateTransition(javafx.util.Duration.millis(300), messageBox);
        slideUp.setFromY(20);
        slideUp.setToY(0);

        javafx.animation.ParallelTransition animation = new javafx.animation.ParallelTransition(fadeIn, slideUp);
        animation.play();
    }

    /**
     * Format timestamp for display
     */
    private String formatTime(LocalDateTime time) {
        return time.format(DateTimeFormatter.ofPattern("HH:mm"));
    }

    /**
     * Helper method to get current user role from the authentication state
     */
    private String getCurrentUserRole() {
        try {
            // Get the current auth state from AuthStateManager
            AuthStateManager authManager = AuthStateManager.getInstance();
            if (authManager.isAuthenticated() && authManager.getState().getUser() != null) {
                return authManager.getState().getUser().getRole();
            }
        } catch (Exception e) {
            System.err.println("Error getting user role: " + e.getMessage());
        }
        // Default to student if not authenticated or error occurs
        return "student";
    }

    /**
     * Get user name by ID from the database
     */
    private String getUserNameById(int userId) {
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                 "SELECT first_name, last_name FROM users WHERE id = ?")) {

            stmt.setInt(1, userId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    String firstName = rs.getString("first_name");
                    String lastName = rs.getString("last_name");
                    return firstName + " " + lastName;
                }
            }
        } catch (SQLException e) {
            System.err.println("Error getting user name: " + e.getMessage());
        }
        return "Unknown User";
    }

    /**
     * Get user role by ID from the database
     */
    private String getUserRoleById(int userId) {
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                 "SELECT role FROM users WHERE id = ?")) {

            stmt.setInt(1, userId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getString("role");
                }
            }
        } catch (SQLException e) {
            System.err.println("Error getting user role: " + e.getMessage());
        }
        return "student";
    }

    /**
     * Get receiver ID from current chat partner
     */
    private int getReceiverIdFromCurrentPartner() {
        // If admin is chatting with a student, need to look up the student's ID
        if ("admin".equals(getCurrentUserRole())) {
            return getUserIdByName(currentChatPartner);
        } else {
            // If student, they're always talking to the admin
            return getAdminUserId();
        }
    }

    /**
     * Get user ID by name
     */
    private int getUserIdByName(String name) {
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                 "SELECT id FROM users WHERE CONCAT(first_name, ' ', last_name) = ?")) {

            stmt.setString(1, name);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("id");
                }
            }
        } catch (SQLException e) {
            System.err.println("Error getting user ID: " + e.getMessage());
        }
        return -1; // Invalid ID
    }

    /**
     * Get admin user ID
     */
    private int getAdminUserId() {
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                 "SELECT id FROM users WHERE role = 'admin' LIMIT 1")) {

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("id");
                }
            }
        } catch (SQLException e) {
            System.err.println("Error getting admin ID: " + e.getMessage());
        }
        return 1; // Default to first user as admin
    }

    /**
     * Load actual users from the database
     */
    private ObservableList<ChatUser> loadActualUsers() {
        ObservableList<ChatUser> users = FXCollections.observableArrayList();

        try {
            // Use the ChatManager to load the actual users
            List<ChatManager.ChatUser> chatUsers = ChatManager.getInstance().loadAllUsers();

            for (ChatManager.ChatUser chatUser : chatUsers) {
                // Skip the current user
                if (chatUser.getId() == AuthStateManager.getInstance().getState().getUser().getId()) {
                    continue;
                }

                // Skip non-students if current user is admin
                if ("admin".equals(getCurrentUserRole()) && !"student".equals(chatUser.getRole())) {
                    continue;
                }

                // Skip non-admins if current user is student
                if ("student".equals(getCurrentUserRole()) && !"admin".equals(chatUser.getRole())) {
                    continue;
                }

                // Get unread message count
                int unreadCount = ChatManager.getInstance().getUnreadMessageCount(chatUser.getId());

                users.add(new ChatUser(
                    chatUser.getName(),
                    chatUser.getRole(),
                    chatUser.getAvatarColor(),
                    chatUser.getLastMessage(),
                    LocalDateTime.now(), // This should come from the last message
                    unreadCount
                ));
            }
        } catch (Exception e) {
            System.err.println("Error loading users: " + e.getMessage());
            e.printStackTrace();
        }

        // If no users were loaded, add a default one
        if (users.isEmpty() && "student".equals(getCurrentUserRole())) {
            users.add(new ChatUser("UIU Admission Admin", "admin", "#F44336", "", LocalDateTime.now(), 0));
        }

        return users;
    }

    /**
     * Load chat history from database
     */
    private void loadChatHistory() {
        try {
            // Clear existing messages
            messagesContainer.getChildren().clear();

            // Add welcome message
            HBox systemMsgBox = new HBox();
            systemMsgBox.setAlignment(Pos.CENTER);
            VBox systemMessage = new VBox();
            systemMessage.getStyleClass().add("system-message");
            Label systemText = new Label("Welcome to UIU Admission Chat Support");
            systemText.getStyleClass().add("system-message-text");
            systemMessage.getChildren().add(systemText);
            systemMsgBox.getChildren().add(systemMessage);
            messagesContainer.getChildren().add(systemMsgBox);

            try {
                // Get user IDs
                int currentUserId = AuthStateManager.getInstance().getState().getUser().getId();
                int partnerId = getReceiverIdFromCurrentPartner();

                // Load messages from the database
                List<ChatManager.ChatMessage> messages = ChatManager.getInstance().loadChatHistory(currentUserId, partnerId);

                if (messages.isEmpty()) {
                    // Add a system message indicating no messages yet
                    HBox noMsgBox = new HBox();
                    noMsgBox.setAlignment(Pos.CENTER);
                    VBox noMsgContent = new VBox();
                    noMsgContent.getStyleClass().add("system-message");
                    Label noMsgText = new Label("No previous messages yet. Start a conversation!");
                    noMsgText.getStyleClass().add("system-message-text");
                    noMsgContent.getChildren().add(noMsgText);
                    noMsgBox.getChildren().add(noMsgContent);
                    messagesContainer.getChildren().add(noMsgBox);
                } else {
                    // Display the messages
                    for (ChatManager.ChatMessage msg : messages) {
                        // Get sender info
                        String senderName;
                        String senderRole;

                        if (msg.getSenderId() == currentUserId) {
                            senderName = "You";
                            senderRole = getCurrentUserRole();
                        } else {
                            senderName = getUserNameById(msg.getSenderId());
                            senderRole = getUserRoleById(msg.getSenderId());
                        }

                        // Add message to UI
                        addMessage(msg.getMessage(), senderName, senderRole, msg.getTimestamp());
                    }
                }

                // Try to mark messages as read
                try {
                    ChatManager.getInstance().markMessagesAsRead(partnerId, currentUserId);
                } catch (Exception markError) {
                    // Ignore - this is not critical
                    System.err.println("Could not mark messages as read: " + markError.getMessage());
                }
            } catch (Exception dataError) {
                System.err.println("Error retrieving chat data: " + dataError.getMessage());

                // Add a system error message
                HBox errorMsgBox = new HBox();
                errorMsgBox.setAlignment(Pos.CENTER);
                VBox errorContent = new VBox();
                errorContent.getStyleClass().addAll("system-message", "system-warning");
                Label errorText = new Label("Unable to load previous messages. New messages will be stored locally.");
                errorText.getStyleClass().add("system-message-text");
                errorContent.getChildren().add(errorText);
                errorMsgBox.getChildren().add(errorContent);
                messagesContainer.getChildren().add(errorMsgBox);
            }

        } catch (Exception e) {
            System.err.println("Error setting up chat UI: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Chat user model class
     */
    private static class ChatUser {
        private String name;
        private String role;
        private String avatarColor;
        private String lastMessage;
        private LocalDateTime lastMessageTime;
        private int unreadCount;

        public ChatUser(String name, String role, String avatarColor, String lastMessage,
                        LocalDateTime lastMessageTime, int unreadCount) {
            this.name = name;
            this.role = role;
            this.avatarColor = avatarColor;
            this.lastMessage = lastMessage;
            this.lastMessageTime = lastMessageTime;
            this.unreadCount = unreadCount;
        }

        public String getName() { return name; }
        public String getRole() { return role; }
        public String getAvatarColor() { return avatarColor; }
        public String getLastMessage() { return lastMessage; }
        public LocalDateTime getLastMessageTime() { return lastMessageTime; }
        public int getUnreadCount() { return unreadCount; }
        public String getFormattedTime() {
            return lastMessageTime.format(DateTimeFormatter.ofPattern("HH:mm"));
        }
        public String getInitials() {
            if (name == null || name.isEmpty()) return "";
            String[] parts = name.split(" ");
            if (parts.length == 1) return name.substring(0, 1).toUpperCase();
            return (parts[0].substring(0, 1) + parts[parts.length-1].substring(0, 1)).toUpperCase();
        }
    }

    /**
     * Custom cell factory for user list
     */
    private class UserListCell extends ListCell<ChatUser> {
        private HBox container;
        private StackPane avatarPane;
        private Circle avatarCircle;
        private Label initialsLabel;
        private VBox infoContainer;
        private HBox nameContainer;
        private Label nameLabel;
        private Label unreadBadge;
        private Label lastMessageLabel;
        private Label timeLabel;

        public UserListCell() {
            super();

            container = new HBox(10);
            container.getStyleClass().add("user-item");
            container.setAlignment(Pos.CENTER_LEFT);
            container.setPadding(new Insets(8));

            // Avatar
            avatarPane = new StackPane();
            avatarPane.setPrefSize(40, 40);
            avatarPane.setMaxSize(40, 40);

            avatarCircle = new Circle(20);
            avatarCircle.getStyleClass().add("user-avatar");

            initialsLabel = new Label();
            initialsLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: white;");

            avatarPane.getChildren().addAll(avatarCircle, initialsLabel);

            // User info
            infoContainer = new VBox(3);
            infoContainer.setAlignment(Pos.CENTER_LEFT);
            HBox.setHgrow(infoContainer, javafx.scene.layout.Priority.ALWAYS);

            nameContainer = new HBox();
            nameContainer.setAlignment(Pos.CENTER_LEFT);

            nameLabel = new Label();
            nameLabel.getStyleClass().add("user-name");
            HBox.setHgrow(nameLabel, javafx.scene.layout.Priority.ALWAYS);

            unreadBadge = new Label();
            unreadBadge.getStyleClass().add("unread-badge");
            unreadBadge.setVisible(false);

            timeLabel = new Label();
            timeLabel.getStyleClass().add("timestamp");

            nameContainer.getChildren().addAll(nameLabel, unreadBadge, timeLabel);

            lastMessageLabel = new Label();
            lastMessageLabel.getStyleClass().add("last-message");
            lastMessageLabel.setMaxWidth(Double.MAX_VALUE);
            lastMessageLabel.setWrapText(true);

            infoContainer.getChildren().addAll(nameContainer, lastMessageLabel);

            container.getChildren().addAll(avatarPane, infoContainer);

            // Empty cell should be transparent
            setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
        }

        @Override
        protected void updateItem(ChatUser user, boolean empty) {
            super.updateItem(user, empty);

            if (empty || user == null) {
                setGraphic(null);
            } else {
                // Update avatar
                avatarCircle.setFill(Color.valueOf(user.getAvatarColor()));
                initialsLabel.setText(user.getInitials());

                // Update user info
                nameLabel.setText(user.getName());
                lastMessageLabel.setText(user.getLastMessage());
                timeLabel.setText(user.getFormattedTime());

                // Show unread badge if needed
                if (user.getUnreadCount() > 0) {
                    unreadBadge.setText(String.valueOf(user.getUnreadCount()));
                    unreadBadge.setVisible(true);
                } else {
                    unreadBadge.setVisible(false);
                }

                setGraphic(container);
            }
        }
    }
}
