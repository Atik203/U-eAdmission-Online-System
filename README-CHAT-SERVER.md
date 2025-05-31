# UeAdmission Chat Server

This document provides instructions for running the UeAdmission chat server as a standalone application, allowing multiple instances of the UeAdmission application to connect to a single chat server.

## Problem

When running multiple instances of the UeAdmission application using `mvn javafx:run`, each instance starts its own chat server on the same port (9001), causing conflicts and errors for real-time chatting.

## Solution

The solution is to run a single standalone chat server that all application instances can connect to. This README provides instructions for setting up and running the standalone chat server.

## Running the Standalone Chat Server

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

- **Connection Refused**: Make sure the chat server is running and the host/port configuration is correct
- **Port Already in Use**: Another application or instance is using the specified port. Choose a different port or stop the other application
- **Cannot Connect**: Check firewall settings to ensure the port is open for local connections
- **MySQL JDBC Driver not found**: The standalone server requires the MySQL JDBC driver to connect to the database. If you see a `ClassNotFoundException: com.mysql.cj.jdbc.Driver` error, follow these steps:
  1. Download the MySQL Connector/J JAR file (version 8.0.33) from: https://repo1.maven.org/maven2/com/mysql/mysql-connector-java/8.0.33/mysql-connector-java-8.0.33.jar
  2. Place the downloaded JAR file in the `lib` directory
  3. Run the chat server again

  Alternatively, if you've built the project with Maven, the script will automatically try to use the MySQL driver from your Maven repository.
