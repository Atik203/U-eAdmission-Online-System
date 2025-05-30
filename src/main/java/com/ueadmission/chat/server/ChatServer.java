package com.ueadmission.chat.server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.ueadmission.db.DatabaseConnection;

/**
 * Chat server for handling client connections and message routing
 */
public class ChatServer {
    private static final Logger LOGGER = Logger.getLogger(ChatServer.class.getName());
    private static final int SERVER_PORT = 9001;

    private ServerSocket serverSocket;
    private boolean running;
    private ExecutorService threadPool;
    private ClientManager clientManager;

    /**
     * Initialize the chat server
     */
    public ChatServer() {
        clientManager = new ClientManager();
        threadPool = Executors.newCachedThreadPool();
    }

    /**
     * Start the server
     */
    public void start() {
        try {
            serverSocket = new ServerSocket(SERVER_PORT);
            running = true;

            LOGGER.info("Chat server started on port " + SERVER_PORT);

            // Reset all users to offline status when server starts
            resetAllUserStatus();

            // Accept client connections in a separate thread
            threadPool.submit(this::acceptClients);

        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Error starting chat server", e);
        }
    }

    /**
     * Reset all user status to offline in the database
     */
    private void resetAllUserStatus() {
        try {
            Connection conn = DatabaseConnection.getConnection();
            PreparedStatement ps = conn.prepareStatement(
                "UPDATE user_status SET status = 'offline', last_active = NOW()");
            ps.executeUpdate();
            ps.close();
            LOGGER.info("Reset all users to offline status");
        } catch (SQLException e) {
            LOGGER.log(Level.WARNING, "Could not reset user status in database", e);
        }
    }

    /**
     * Accept incoming client connections
     */
    private void acceptClients() {
        while (running) {
            try {
                Socket clientSocket = serverSocket.accept();
                LOGGER.info("New client connected: " + clientSocket.getInetAddress());

                // Handle each client in a separate thread
                ClientHandler handler = new ClientHandler(clientSocket, clientManager);
                threadPool.submit(handler);

            } catch (IOException e) {
                if (running) {
                    LOGGER.log(Level.WARNING, "Error accepting client connection", e);
                }
            }
        }
    }

    /**
     * Stop the server
     */
    public void stop() {
        running = false;
        clientManager.disconnectAll();

        try {
            if (serverSocket != null && !serverSocket.isClosed()) {
                serverSocket.close();
            }
        } catch (IOException e) {
            LOGGER.log(Level.WARNING, "Error closing server socket", e);
        }

        threadPool.shutdown();
        LOGGER.info("Chat server stopped");
    }

    /**
     * Main method to start the server
     */
    public static void main(String[] args) {
        ChatServer server = new ChatServer();
        server.start();

        // Add shutdown hook to properly close the server when JVM exits
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            LOGGER.info("Shutting down chat server...");
            server.stop();
        }));
    }
}
