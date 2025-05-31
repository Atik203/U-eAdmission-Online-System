# UeAdmission Chat System Documentation

## Overview

This document explains how the chat system works in the UeAdmission application and provides instructions for running the chat server. The chat system enables real-time communication between students and administrators within the application.

## Server Architecture

The chat system consists of several key components:

1. **ChatServer**: The main server that accepts client connections and manages communication
2. **ClientHandler**: Handles individual client connections and message processing
3. **ClientManager**: Manages connected clients and routes messages between them
4. **ChatServerLauncher**: Provides methods to start and stop the server in a separate thread
5. **ChatClient**: Client-side component that connects to the server

## Server Configuration

The server uses the following default configuration:

- **Host**: localhost
- **Port**: 9001

These settings can be found in both the `ChatServer` and `ChatClient` classes.

## Database Tables

The chat system uses the following database tables:

1. **chat_messages**: Stores all chat messages
2. **user_status**: Tracks online/offline status of users
3. **chat_messages_queue**: Stores messages that couldn't be delivered immediately

## Offline Message Handling

Messages sent to offline users are stored in the database and delivered when they come online. The system works as follows:

1. When a message is sent, it's always stored in the database
2. If the recipient is online, the message is delivered immediately
3. If the recipient is offline, the message remains in the database
4. When a user connects, stored messages are sent automatically

## Client-Server Communication Protocol

Clients and servers communicate using a simple text-based protocol:

- **Authentication**: `AUTH:userId`
- **Direct Message**: `MSG:receiverId:message`
- **Broadcast**: `BROADCAST:message`
- **Status Update**: `STATUS:status`
- **Server Responses**: `MSG:senderId:timestamp:message` or `ERROR:message`

## Running the Standalone Chat Server

### Problem

When running multiple instances of the UeAdmission application using `mvn javafx:run`, each instance starts its own chat server on the same port (9001), causing conflicts and errors for real-time chatting.

### Solution

The solution is to run a single standalone chat server that all application instances can connect to. This section provides instructions for setting up and running the standalone chat server.

### Using the Batch File (Windows)

1. Open a command prompt
2. Navigate to the project root directory
3. Run the batch file:
   ```
   run-chat-server.bat
   ```
4. The server will start and display its host address and port
5. Keep this window open while using the UeAdmission application

### Using Java Command (Cross-Platform)

1. Open a terminal/command prompt
2. Navigate to the project root directory
3. Compile the project if not already compiled:
   ```
   mvn compile
   ```
4. Run the standalone server:
   ```
   java -cp target\classes com.ueadmission.chat.server.StandaloneChatServer
   ```
5. Optionally, specify a custom port:
   ```
   java -cp target\classes com.ueadmission.chat.server.StandaloneChatServer 9002
   ```

## Configuring the Application

The UeAdmission application can be configured to use an external chat server instead of starting its own. This configuration is stored in `config/chat-server.properties`.

### Configuration Options

- `server.host`: The hostname or IP address of the chat server (default: localhost)
- `server.port`: The port number of the chat server (default: 9001)
- `server.autoStart`: Whether to automatically start the chat server if one is not already running (default: false)

### Editing Configuration

The configuration file is automatically created with default values when the application is first run. You can edit it manually:

1. Navigate to the `config` directory in the project root
2. Open `chat-server.properties` in a text editor
3. Modify the values as needed:
   ```
   server.host=localhost
   server.port=9001
   server.autoStart=false
   ```
4. Save the file

## Usage Scenarios

### Scenario 1: Running a Standalone Server

1. Start the standalone chat server using one of the methods above
2. Set `server.autoStart=false` in the configuration
3. Run multiple instances of the UeAdmission application
4. All instances will connect to the standalone server

### Scenario 2: First Instance Starts Server

1. Set `server.autoStart=true` in the configuration
2. Run the first instance of the UeAdmission application, which will start the chat server
3. Run additional instances, which will detect the running server and connect to it

## Troubleshooting

Common issues and solutions:

1. **Connection Refused**: Ensure the server is running and listening on port 9001
2. **Authentication Errors**: Verify the user exists in the database
3. **Messages Not Delivered**: Check if the recipient is online
4. **Database Errors**: Make sure all required tables exist
5. **Port Already in Use**: Another application or instance is using the specified port. Choose a different port or stop the other application
6. **Cannot Connect**: Check firewall settings to ensure the port is open for local connections
7. **MySQL JDBC Driver not found**: The standalone server requires the MySQL JDBC driver to connect to the database. If you see a `ClassNotFoundException: com.mysql.cj.jdbc.Driver` error, follow these steps:
   1. Download the MySQL Connector/J JAR file (version 8.0.33) from: https://repo1.maven.org/maven2/com/mysql/mysql-connector-java/8.0.33/mysql-connector-java-8.0.33.jar
   2. Place the downloaded JAR file in the `lib` directory
   3. Run the chat server again

   Alternatively, if you've built the project with Maven, the script will automatically try to use the MySQL driver from your Maven repository.

If the error persists, check the logs for detailed error messages.

## For Developers

To extend the chat functionality:

1. The `ChatClient` class provides methods to send messages and receive events
2. The `ChatController` handles the UI for the chat window
3. The `ChatManager` provides higher-level chat functionality

Follow the existing protocol patterns when adding new message types.