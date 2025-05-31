package com.ueadmission.chat;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

/**
 * Chat User class to represent a user in the chat system
 */
public class ChatUser {
    private int id;
    private String name;
    private String email;
    private String role;
    private String status;
    private String avatarColor;
    private String lastMessage;
    private LocalDateTime lastMessageTime;
    private int unreadCount;

    public ChatUser(int id, String name, String email, String role, 
                    String status, String avatarColor, String lastMessage,
                    LocalDateTime lastMessageTime, int unreadCount) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.role = role;
        this.status = status;
        this.avatarColor = avatarColor;
        this.lastMessage = lastMessage;
        this.lastMessageTime = lastMessageTime;
        this.unreadCount = unreadCount;
    }

    // Simplified constructor for compatibility with ChatController
    public ChatUser(String name, String role, String avatarColor, String lastMessage,
                    LocalDateTime lastMessageTime, int unreadCount) {
        this.id = -1; // Default ID
        this.name = name;
        this.email = "";
        this.role = role;
        this.status = "offline";
        this.avatarColor = avatarColor;
        this.lastMessage = lastMessage;
        this.lastMessageTime = lastMessageTime;
        this.unreadCount = unreadCount;
    }

    public int getId() { return id; }
    public String getName() { return name; }
    public String getEmail() { return email; }
    public String getRole() { return role; }
    public String getStatus() { return status; }
    public String getAvatarColor() { return avatarColor; }
    public String getLastMessage() { return lastMessage; }
    public LocalDateTime getLastMessageTime() { return lastMessageTime; }
    public int getUnreadCount() { return unreadCount; }

    public String getFormattedTime() {
        if (lastMessageTime == null) {
            return "";
        }
        return lastMessageTime.format(DateTimeFormatter.ofPattern("hh:mm a", Locale.ENGLISH));
    }
    public String getFormattedFullDateTime() {
        if (lastMessageTime == null) {
            return "";
        }
        return lastMessageTime.format(DateTimeFormatter.ofPattern("MMMM d, yyyy hh:mm a", Locale.ENGLISH));
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
