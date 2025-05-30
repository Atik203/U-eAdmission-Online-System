-- Chat system database tables

-- Table for storing chat messages
CREATE TABLE IF NOT EXISTS chat_messages (
    id INT AUTO_INCREMENT PRIMARY KEY,
    sender_id INT NOT NULL,
    receiver_id INT NULL,  -- NULL for broadcast messages
    message TEXT NOT NULL,
    is_broadcast BOOLEAN DEFAULT FALSE,
    timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    is_read BOOLEAN DEFAULT FALSE,
    FOREIGN KEY (sender_id) REFERENCES users(id),
    FOREIGN KEY (receiver_id) REFERENCES users(id)
);

-- Table for storing chat sessions
CREATE TABLE IF NOT EXISTS chat_sessions (
    id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT NOT NULL,
    session_start TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    session_end TIMESTAMP NULL,
    ip_address VARCHAR(45),
    FOREIGN KEY (user_id) REFERENCES users(id)
);

-- Table for storing user status
CREATE TABLE IF NOT EXISTS user_status (
    user_id INT PRIMARY KEY,
    status ENUM('online', 'offline', 'away', 'busy') DEFAULT 'offline',
    last_active TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id)
);
-- Chat messages table
CREATE TABLE IF NOT EXISTS chat_messages (
    id INT AUTO_INCREMENT PRIMARY KEY,
    sender_id INT NOT NULL,
    receiver_id INT,
    message TEXT NOT NULL,
    timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    is_broadcast BOOLEAN DEFAULT FALSE,
    is_read BOOLEAN DEFAULT FALSE,
    INDEX (sender_id),
    INDEX (receiver_id),
    INDEX (timestamp)
);

-- Queue for offline messages
CREATE TABLE IF NOT EXISTS chat_messages_queue (
    id INT AUTO_INCREMENT PRIMARY KEY,
    sender_id INT NOT NULL,
    receiver_id INT NOT NULL,
    message TEXT NOT NULL,
    timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    sent BOOLEAN DEFAULT FALSE,
    attempts INT DEFAULT 0,
    INDEX (sender_id),
    INDEX (timestamp)
);

-- User status tracking
CREATE TABLE IF NOT EXISTS user_status (
    user_id INT PRIMARY KEY,
    status VARCHAR(20) DEFAULT 'offline',
    last_active TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    INDEX (status)
);

-- Insert default status for existing users (if users table exists)
INSERT IGNORE INTO user_status (user_id, status)
SELECT id, 'offline' FROM users;
-- Insert some sample statuses for existing users
INSERT INTO user_status (user_id, status)
SELECT id, 'offline' FROM users;
