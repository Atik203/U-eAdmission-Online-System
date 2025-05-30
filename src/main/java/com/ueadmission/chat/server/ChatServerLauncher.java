package com.ueadmission.chat.server;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Launcher for the chat server in a separate thread
 */
public class ChatServerLauncher {
    private static final Logger LOGGER = Logger.getLogger(ChatServerLauncher.class.getName());
    private static ChatServer chatServer;
    private static Thread serverThread;
    private static boolean serverRunning = false;

    /**
     * Start the chat server in a separate thread
     */
    public static synchronized void startServer() {
        if (serverRunning) {
            LOGGER.info("Chat server is already running");
            return;
        }

        try {
            chatServer = new ChatServer();

            // Create a thread for the server
            serverThread = new Thread(() -> {
                try {
                    chatServer.start();
                } catch (Exception e) {
                    LOGGER.log(Level.SEVERE, "Error in chat server thread", e);
                }
            });

            // Set as daemon so it doesn't prevent JVM shutdown
            serverThread.setDaemon(true);
            serverThread.setName("ChatServerThread");
            serverThread.start();

            serverRunning = true;
            LOGGER.info("Chat server thread started");
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Failed to start chat server", e);
        }
    }

    /**
     * Stop the chat server
     */
    public static synchronized void stopServer() {
        if (!serverRunning) {
            LOGGER.info("Chat server is not running");
            return;
        }

        try {
            if (chatServer != null) {
                chatServer.stop();
            }

            if (serverThread != null && serverThread.isAlive()) {
                serverThread.interrupt();
            }

            serverRunning = false;
            LOGGER.info("Chat server stopped");
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error stopping chat server", e);
        }
    }

    /**
     * Check if server is running
     */
    public static boolean isServerRunning() {
        return serverRunning;
    }
}
