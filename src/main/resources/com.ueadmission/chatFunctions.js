/**
 * Chat functions for UIU Chat System
 * This file provides helper functions for the chat interface
 */

// Format timestamp for chat messages
function formatTimestamp(timestamp) {
    const now = new Date();
    const messageDate = new Date(timestamp);

    // If same day, show time only
    if (now.toDateString() === messageDate.toDateString()) {
        return messageDate.toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' });
    }

    // If within last week, show day name and time
    const weekAgo = new Date(now.getTime() - 7 * 24 * 60 * 60 * 1000);
    if (messageDate > weekAgo) {
        return messageDate.toLocaleDateString([], { weekday: 'short' }) + ' ' + 
               messageDate.toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' });
    }

    // Otherwise show date and time
    return messageDate.toLocaleDateString([], { month: 'short', day: 'numeric' }) + ' ' + 
           messageDate.toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' });
}

// Generate avatar for users without images
function generateAvatarColor(name) {
    // Simple hash function to generate consistent colors for the same name
    let hash = 0;
    for (let i = 0; i < name.length; i++) {
        hash = name.charCodeAt(i) + ((hash << 5) - hash);
    }

    // Generate HSL color with fixed saturation and lightness for good contrast
    const h = Math.abs(hash % 360);
    return `hsl(${h}, 70%, 60%)`;
}

// Get user initials for avatar
function getUserInitials(name) {
    if (!name || name.length === 0) return '';

    const parts = name.split(' ');
    if (parts.length === 1) {
        return name.substring(0, 1).toUpperCase();
    }

    return (parts[0].substring(0, 1) + parts[parts.length - 1].substring(0, 1)).toUpperCase();
}

// Handle message sending
function sendMessage(messageText, userId, conversationId) {
    if (!messageText || messageText.trim() === '') {
        return false;
    }

    const message = {
        id: generateId(),
        conversationId: conversationId,
        senderId: userId,
        content: messageText.trim(),
        timestamp: new Date().toISOString(),
        status: 'sent'
    };

    // In a real app, this would send to a server
    console.log('Sending message:', message);
    return message;
}

// Generate unique ID for messages
function generateId() {
    return 'msg_' + Math.random().toString(36).substr(2, 9) + '_' + Date.now();
}

// Get typing indicator text
function getTypingText(users) {
    if (!users || users.length === 0) {
        return '';
    }

    if (users.length === 1) {
        return `${users[0]} is typing...`;
    }

    if (users.length === 2) {
        return `${users[0]} and ${users[1]} are typing...`;
    }

    return `${users[0]} and ${users.length - 1} others are typing...`;
}

// Export functions for use in JavaFX WebView
if (typeof window !== 'undefined') {
    window.chatFunctions = {
        formatTimestamp,
        generateAvatarColor,
        getUserInitials,
        sendMessage,
        getTypingText
    };
}
