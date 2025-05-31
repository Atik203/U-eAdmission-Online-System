package com.ueadmission.chat.server;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Standalone Chat Server for UeAdmission application
 * This class provides a main method to run the chat server independently
 * from the main application, allowing multiple application instances to
 * connect to a single chat server.
 */
public class StandaloneChatServer {
    private static final Logger LOGGER = Logger.getLogger(StandaloneChatServer.class.getName());
    private static final int DEFAULT_PORT = 9001;

    /**
     * Main method to start the standalone chat server
     * @param args Command line arguments:
     *             [0] - Optional: port number (default: 9001)
     */
    public static void main(String[] args) {
        int port = DEFAULT_PORT;

        // Parse command line arguments
        if (args.length > 0) {
            try {
                port = Integer.parseInt(args[0]);
            } catch (NumberFormatException e) {
                LOGGER.warning("Invalid port number, using default: " + DEFAULT_PORT);
            }
        }

        // Check if port is already in use
        if (isPortInUse(port)) {
            LOGGER.severe("Port " + port + " is already in use. Chat server cannot start.");
            System.out.println("ERROR: Port " + port + " is already in use. Chat server cannot start.");
            System.exit(1);
        }

        // Start the chat server
        ChatServer server = new ChatServer();
        server.start();

        try {
            // Display server information
            String hostAddress = InetAddress.getLocalHost().getHostAddress();
            System.out.println("Chat server started successfully!");
            System.out.println("Server running at: " + hostAddress + ":" + port);
            System.out.println("Press Ctrl+C to stop the server");
            
            // Add shutdown hook to properly close the server when JVM exits
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                System.out.println("Shutting down chat server...");
                server.stop();
                System.out.println("Chat server stopped.");
            }));
            
            // Keep the main thread alive
            while (true) {
                Thread.sleep(60000); // Sleep for 1 minute
            }
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error in chat server main thread", e);
        }
    }

    /**
     * Check if a port is already in use
     * @param port The port to check
     * @return true if the port is in use, false otherwise
     */
    private static boolean isPortInUse(int port) {
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            // Port is available
            return false;
        } catch (IOException e) {
            // Port is in use
            return true;
        }
    }
}