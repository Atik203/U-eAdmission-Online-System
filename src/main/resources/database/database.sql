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
    last_login_time TIMESTAMP NULL,
    ip_address VARCHAR(45) NULL,
    is_logged_in BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- Insert an admin user (password: admin123)
INSERT INTO users (first_name, last_name, email, phone, address, city, country, password, role)
VALUES ('Admin', 'User', 'admin@uiu.ac.bd', '12345678', 'UIU Campus', 'Dhaka', 'Bangladesh', 'admin123', 'admin');

-- Applications table for storing admission applications
CREATE TABLE IF NOT EXISTS applications (
    id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT NOT NULL,
    first_name VARCHAR(100) NOT NULL,
    last_name VARCHAR(100) NOT NULL,
    email VARCHAR(100) NOT NULL,
    phone VARCHAR(20) NOT NULL,
    date_of_birth DATE NOT NULL,
    gender VARCHAR(10) NOT NULL,
    address VARCHAR(255) NOT NULL,
    city VARCHAR(100) NOT NULL,
    postal_code VARCHAR(20),
    father_name VARCHAR(100) NOT NULL,
    father_occupation VARCHAR(100),
    mother_name VARCHAR(100) NOT NULL,
    mother_occupation VARCHAR(100),
    guardian_phone VARCHAR(20) NOT NULL,
    guardian_email VARCHAR(100),
    program VARCHAR(100) NOT NULL,
    institution VARCHAR(255) NOT NULL,
    ssc_gpa DECIMAL(3,2) NOT NULL,
    hsc_gpa DECIMAL(3,2) NOT NULL,
    ssc_year VARCHAR(4) NOT NULL,
    hsc_year VARCHAR(4) NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'Pending',
    payment_complete BOOLEAN NOT NULL DEFAULT FALSE,
    application_date TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id)
);

-- Additional tables can be added as needed for the application