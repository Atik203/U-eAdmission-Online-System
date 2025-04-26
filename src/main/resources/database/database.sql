-- Create the database for UIU Admission System
CREATE DATABASE IF NOT EXISTS uiu_admission_db;

-- Use the database
USE uiu_admission_db;

-- Create users table with role field
CREATE TABLE IF NOT EXISTS users (
    id INT AUTO_INCREMENT PRIMARY KEY,
    first_name VARCHAR(50) NOT NULL,
    last_name VARCHAR(50) NOT NULL,
    email VARCHAR(100) NOT NULL UNIQUE,
    phone VARCHAR(20) NOT NULL,
    address VARCHAR(255) NOT NULL,
    city VARCHAR(50) NOT NULL,
    country VARCHAR(50) NOT NULL,
    password VARCHAR(255) NOT NULL,
    role ENUM('student', 'admin') NOT NULL DEFAULT 'student',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- Insert an admin user (password: admin123)
INSERT INTO users (first_name, last_name, email, phone, address, city, country, password, role)
VALUES ('Admin', 'User', 'admin@uiu.ac.bd', '12345678', 'UIU Campus', 'Dhaka', 'Bangladesh', 'admin123', 'admin');

-- Additional tables can be added as needed for the application
