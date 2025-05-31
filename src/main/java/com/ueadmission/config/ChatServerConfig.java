package com.ueadmission.config;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Configuration class for chat server settings
 * This class provides methods to load and save chat server configuration
 */
public class ChatServerConfig {
    private static final Logger LOGGER = Logger.getLogger(ChatServerConfig.class.getName());
    
    // Default values
    private static final String DEFAULT_HOST = "localhost";
    private static final int DEFAULT_PORT = 9001;
    private static final boolean DEFAULT_AUTO_START = false;
    
    // Config file path
    private static final String CONFIG_DIR = "config";
    private static final String CONFIG_FILE = "chat-server.properties";
    
    // Properties
    private static Properties properties;
    
    // Singleton instance
    private static ChatServerConfig instance;
    
    /**
     * Private constructor for singleton pattern
     */
    private ChatServerConfig() {
        loadConfig();
    }
    
    /**
     * Get the singleton instance
     */
    public static synchronized ChatServerConfig getInstance() {
        if (instance == null) {
            instance = new ChatServerConfig();
        }
        return instance;
    }
    
    /**
     * Load configuration from file
     */
    private void loadConfig() {
        properties = new Properties();
        
        // Create config directory if it doesn't exist
        Path configDir = Paths.get(CONFIG_DIR);
        if (!Files.exists(configDir)) {
            try {
                Files.createDirectories(configDir);
            } catch (IOException e) {
                LOGGER.log(Level.WARNING, "Could not create config directory", e);
            }
        }
        
        // Load properties from file if it exists
        Path configFile = Paths.get(CONFIG_DIR, CONFIG_FILE);
        if (Files.exists(configFile)) {
            try (FileInputStream fis = new FileInputStream(configFile.toFile())) {
                properties.load(fis);
                LOGGER.info("Loaded chat server configuration from " + configFile);
            } catch (IOException e) {
                LOGGER.log(Level.WARNING, "Could not load chat server configuration", e);
            }
        } else {
            // Create default configuration
            properties.setProperty("server.host", DEFAULT_HOST);
            properties.setProperty("server.port", String.valueOf(DEFAULT_PORT));
            properties.setProperty("server.autoStart", String.valueOf(DEFAULT_AUTO_START));
            
            // Save default configuration
            saveConfig();
        }
    }
    
    /**
     * Save configuration to file
     */
    public void saveConfig() {
        Path configFile = Paths.get(CONFIG_DIR, CONFIG_FILE);
        try (FileOutputStream fos = new FileOutputStream(configFile.toFile())) {
            properties.store(fos, "Chat Server Configuration");
            LOGGER.info("Saved chat server configuration to " + configFile);
        } catch (IOException e) {
            LOGGER.log(Level.WARNING, "Could not save chat server configuration", e);
        }
    }
    
    /**
     * Get the server host
     */
    public String getServerHost() {
        return properties.getProperty("server.host", DEFAULT_HOST);
    }
    
    /**
     * Set the server host
     */
    public void setServerHost(String host) {
        properties.setProperty("server.host", host);
    }
    
    /**
     * Get the server port
     */
    public int getServerPort() {
        try {
            return Integer.parseInt(properties.getProperty("server.port", String.valueOf(DEFAULT_PORT)));
        } catch (NumberFormatException e) {
            return DEFAULT_PORT;
        }
    }
    
    /**
     * Set the server port
     */
    public void setServerPort(int port) {
        properties.setProperty("server.port", String.valueOf(port));
    }
    
    /**
     * Check if server should be auto-started
     */
    public boolean isAutoStartEnabled() {
        return Boolean.parseBoolean(properties.getProperty("server.autoStart", String.valueOf(DEFAULT_AUTO_START)));
    }
    
    /**
     * Set auto-start option
     */
    public void setAutoStartEnabled(boolean autoStart) {
        properties.setProperty("server.autoStart", String.valueOf(autoStart));
    }
}