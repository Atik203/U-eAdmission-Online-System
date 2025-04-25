package com.ueadmission.auth;

import java.sql.*;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.security.SecureRandom;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

public class MyJDBC {
    // Database connection parameters
    private static final String DB_URL = "jdbc:mysql://127.0.0.1:3306/login_schema";
    private static final String DB_USER = "root";
    private static final String DB_PASSWORD = "";

    // Password hashing parameters
    private static final int SALT_LENGTH = 16;
    private static final int ITERATIONS = 65536;
    private static final int KEY_LENGTH = 256;

    /**
     * Registers a new user in the database
     */
    public static boolean registerUser(String firstName, String lastName, String email, String phone,
                                       String address, String city, String country, String password) {
        // First check if email already exists
        if (emailExists(email)) {
            System.err.println("Registration failed: Email already exists");
            return false;
        }

        String insertQuery = "INSERT INTO users (first_name, last_name, email_address, phone, address, city, country, password, salt) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             PreparedStatement preparedStatement = connection.prepareStatement(insertQuery)) {

            // Generate salt and hash password
            byte[] salt = generateSalt();
            String hashedPassword = hashPassword(password, salt);

            preparedStatement.setString(1, firstName);
            preparedStatement.setString(2, lastName);
            preparedStatement.setString(3, email);
            preparedStatement.setString(4, phone);
            preparedStatement.setString(5, address);
            preparedStatement.setString(6, city);
            preparedStatement.setString(7, country);
            preparedStatement.setString(8, hashedPassword);
            preparedStatement.setBytes(9, salt);

            int affectedRows = preparedStatement.executeUpdate();
            return affectedRows > 0;

        } catch (SQLException e) {
            System.err.println("Database error during registration: " + e.getMessage());
            return false;
        } catch (Exception e) {
            System.err.println("Error during password hashing: " + e.getMessage());
            return false;
        }
    }

    /**
     * Authenticates a user with email and password
     */
    public static boolean authenticateUser(String email, String password) {
        String query = "SELECT password, salt FROM users WHERE email_address = ?";

        try (Connection connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             PreparedStatement preparedStatement = connection.prepareStatement(query)) {

            preparedStatement.setString(1, email);

            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    String storedHash = resultSet.getString("password");
                    byte[] salt = resultSet.getBytes("salt");

                    // Hash the provided password with the stored salt
                    String hashedPassword = hashPassword(password, salt);

                    // Compare the hashes
                    return hashedPassword.equals(storedHash);
                }
                return false;
            }
        } catch (SQLException e) {
            System.err.println("Database error during authentication: " + e.getMessage());
            return false;
        } catch (Exception e) {
            System.err.println("Error during password verification: " + e.getMessage());
            return false;
        }
    }

    /**
     * Checks if an email already exists in the database
     */
    private static boolean emailExists(String email) {
        String query = "SELECT 1 FROM users WHERE email_address = ?";

        try (Connection connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             PreparedStatement preparedStatement = connection.prepareStatement(query)) {

            preparedStatement.setString(1, email);
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                return resultSet.next();
            }
        } catch (SQLException e) {
            System.err.println("Error checking email existence: " + e.getMessage());
            return true; // Assume email exists to prevent duplicates if error occurs
        }
    }

    /**
     * Generates a random salt for password hashing
     */
    private static byte[] generateSalt() {
        SecureRandom random = new SecureRandom();
        byte[] salt = new byte[SALT_LENGTH];
        random.nextBytes(salt);
        return salt;
    }

    /**
     * Hashes a password with PBKDF2 algorithm
     */
    private static String hashPassword(String password, byte[] salt)
            throws NoSuchAlgorithmException, InvalidKeySpecException {
        PBEKeySpec spec = new PBEKeySpec(
                password.toCharArray(),
                salt,
                ITERATIONS,
                KEY_LENGTH
        );
        SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
        byte[] hash = factory.generateSecret(spec).getEncoded();
        return bytesToHex(hash);
    }

    /**
     * Converts byte array to hexadecimal string
     */
    private static String bytesToHex(byte[] bytes) {
        StringBuilder hexString = new StringBuilder();
        for (byte b : bytes) {
            String hex = Integer.toHexString(0xff & b);
            if (hex.length() == 1) {
                hexString.append('0');
            }
            hexString.append(hex);
        }
        return hexString.toString();
    }

    /**
     * Test method - can be removed in production
     */
    public static void main(String[] args) {
        // Test registration
        boolean registered = registerUser(
                "John",
                "Doe",
                "john.doe@example.com",
                "1234567890",
                "123 Main St",
                "New York",
                "USA",
                "securePassword123"
        );
        System.out.println("Registration result: " + registered);

        // Test authentication
        boolean authenticated = authenticateUser("john.doe@example.com", "securePassword123");
        System.out.println("Authentication result: " + authenticated);

        // Test failed authentication
        boolean failedAuth = authenticateUser("john.doe@example.com", "wrongPassword");
        System.out.println("Failed authentication test: " + (failedAuth == false));
    }
}