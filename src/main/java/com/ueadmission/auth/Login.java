package com.ueadmission.auth;

/**
 * Login data model class - represents a user's login credentials
 * Note: This is a model class, not a controller. The controller is LoginController.
 */
public class Login {
    private String email;
    private String password;
    
    public Login() {
        // Default constructor
    }
    
    public Login(String email, String password) {
        this.email = email;
        this.password = password;
    }
    
    public String getEmail() {
        return email;
    }
    
    public void setEmail(String email) {
        this.email = email;
    }
    
    public String getPassword() {
        return password;
    }
    
    public void setPassword(String password) {
        this.password = password;
    }
}
