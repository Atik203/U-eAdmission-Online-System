package com.ueadmission.chat.server;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

/**
 * Manages connected clients and routes messages between them
 */
public class ClientManager {
    private static final Logger LOGGER = Logger.getLogger(ClientManager.class.getName());

    private Map<Integer, ClientHandler> connectedClients = new ConcurrentHashMap<>();

    /**
     * Register a client
     */
    public void registerClient(int userId, ClientHandler handler) {
        connectedClients.put(userId, handler);
        LOGGER.info("Client registered: User ID " + userId);

        // Broadcast status update to other clients
        broadcastStatus(userId, "online");
    }

    /**
     * Unregister a client
     */
    public void unregisterClient(int userId) {
        connectedClients.remove(userId);
        LOGGER.info("Client unregistered: User ID " + userId);

        // Broadcast status update to other clients
        broadcastStatus(userId, "offline");
    }

    /**
     * Send a message to a specific client
     * @return true if message was delivered, false if client is offline
     */
    public boolean sendMessageToClient(int userId, String message) {
        ClientHandler handler = connectedClients.get(userId);

        if (handler != null) {
            handler.sendMessage(message);
            return true;
        }

        return false;
    }

    /**
     * Broadcast a message to all clients except the sender
     */
    public void broadcastMessage(int senderId, String message) {
        for (Map.Entry<Integer, ClientHandler> entry : connectedClients.entrySet()) {
            int clientId = entry.getKey();

            // Skip the sender
            if (clientId != senderId) {
                entry.getValue().sendMessage(message);
            }
        }
    }

    /**
     * Broadcast a status update to all clients
     */
    public void broadcastStatus(int userId, String status) {
        String statusMessage = "STATUS:" + userId + ":" + status;

        for (Map.Entry<Integer, ClientHandler> entry : connectedClients.entrySet()) {
            int clientId = entry.getKey();

            // Skip the user whose status is being updated
            if (clientId != userId) {
                entry.getValue().sendMessage(statusMessage);
            }
        }
    }

    /**
     * Disconnect all clients
     */
    public void disconnectAll() {
        for (ClientHandler handler : connectedClients.values()) {
            handler.disconnect();
        }

        connectedClients.clear();
        LOGGER.info("All clients disconnected");
    }

    /**
     * Get number of connected clients
     */
    public int getConnectedClientCount() {
        return connectedClients.size();
    }
}
