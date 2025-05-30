-- Chat system tables

-- Table for storing chat messages
CREATE TABLE IF NOT EXISTS chat_messages (
    id INT AUTO_INCREMENT PRIMARY KEY,
    sender_id INT NOT NULL,
    receiver_id INT NOT NULL,
    message TEXT NOT NULL,
    timestamp TIMESTAMP NOT NULL,
    read BOOLEAN DEFAULT FALSE,
    FOREIGN KEY (sender_id) REFERENCES users(id),
    FOREIGN KEY (receiver_id) REFERENCES users(id),
    INDEX (sender_id),
    INDEX (receiver_id),
    INDEX (timestamp)
);

-- Table for storing user status
CREATE TABLE IF NOT EXISTS user_status (
    user_id INT PRIMARY KEY,
    status VARCHAR(20) NOT NULL DEFAULT 'offline',
    last_active TIMESTAMP NOT NULL,
    FOREIGN KEY (user_id) REFERENCES users(id)
);

-- Table for storing queued messages that couldn't be delivered
CREATE TABLE IF NOT EXISTS chat_messages_queue (
    id INT AUTO_INCREMENT PRIMARY KEY,
    sender_id INT NOT NULL,
    receiver_id INT NOT NULL,
    message TEXT NOT NULL,
    timestamp TIMESTAMP NOT NULL,
    sent BOOLEAN DEFAULT FALSE,
    attempts INT DEFAULT 0,
    last_attempt TIMESTAMP NULL,
    FOREIGN KEY (sender_id) REFERENCES users(id),
    FOREIGN KEY (receiver_id) REFERENCES users(id),
    INDEX (sender_id),
    INDEX (receiver_id),
    INDEX (timestamp)
);
