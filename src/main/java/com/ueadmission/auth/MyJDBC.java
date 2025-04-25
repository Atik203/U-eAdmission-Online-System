package com.ueadmission.auth;

import java.sql.*;

public class MyJDBC {
    // Database connection parameters
    private static final String DB_URL = "jdbc:mysql://127.0.0.1:3306/login_schema";
    private static final String DB_USER = "root";
    private static final String DB_PASSWORD = "";

    public static boolean authenticateUser(String email, String password) {
        String query = "SELECT * FROM users WHERE email_address = ? AND password = ?";

        try (Connection connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             PreparedStatement preparedStatement = connection.prepareStatement(query)) {

            // Set parameters
            preparedStatement.setString(1, email);
            preparedStatement.setString(2, password);

            // Execute query
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                // If a record is found, authentication is successful
                return resultSet.next();
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // Keep your existing main method for testing if needed
    public static void main(String[] args) {
        // Test authentication
        boolean isAuthenticated = authenticateUser("test@example.com", "password123");
        System.out.println("Authentication result: " + isAuthenticated);

        // Your existing test code
        try (Connection connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery("SELECT * FROM users")) {

            while (resultSet.next()) {
                System.out.println("Email: " + resultSet.getString("email_address"));
                System.out.println("Password: " + resultSet.getString("password"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}