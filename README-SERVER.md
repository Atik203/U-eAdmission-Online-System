# UeAdmission Chat Server Documentation

## Overview

This document explains how the chat server works in the UeAdmission application. The chat server enables real-time communication between students and administrators within the application.

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

## Starting the Server

The chat server is automatically started when the application launches. The `Main` class initializes the server through the `ServerLauncher.startAllServers()` method.

If you need to manually start the server, you can use:

```java
com.ueadmission.chat.server.ChatServerLauncher.startServer();
```

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

## Troubleshooting

Common issues and solutions:

1. **Connection Refused**: Ensure the server is running and listening on port 9001
2. **Authentication Errors**: Verify the user exists in the database
3. **Messages Not Delivered**: Check if the recipient is online
4. **Database Errors**: Make sure all required tables exist

If the error persists, check the logs for detailed error messages.

## For Developers

To extend the chat functionality:

1. The `ChatClient` class provides methods to send messages and receive events
2. The `ChatController` handles the UI for the chat window
3. The `ChatManager` provides higher-level chat functionality

Follow the existing protocol patterns when adding new message types.
