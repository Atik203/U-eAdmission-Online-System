package com.ueadmission.server;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Main server launcher for the UeAdmission application
 * This class is responsible for starting all required servers
 */
public class ServerLauncher {
    private static final Logger LOGGER = Logger.getLogger(ServerLauncher.class.getName());

    /**
     * Start all required servers
     */
    public static void startAllServers() {
        // Start chat server
        startChatServer();

        // Additional servers can be started here in the future
    }

    /**
     * Stop all servers
     */
    public static void stopAllServers() {
        // Stop chat server
        stopChatServer();

        // Additional servers can be stopped here in the future
    }

    /**
     * Start the chat server
     */
    private static void startChatServer() {
        try {
            com.ueadmission.chat.server.ChatServerLauncher.startServer();
            LOGGER.info("Chat server started successfully");
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Failed to start chat server", e);
        }
    }

    /**
     * Stop the chat server
     */
    private static void stopChatServer() {
        try {
            com.ueadmission.chat.server.ChatServerLauncher.stopServer();
            LOGGER.info("Chat server stopped successfully");
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Error stopping chat server", e);
        }
    }
}
