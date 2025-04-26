package com.ueadmission.auth.state;

import java.io.Serializable;

/**
 * AuthState model class to store the current authentication state
 */
public class AuthState implements Serializable {
    private static final long serialVersionUID = 1L;
    private boolean authenticated;
    private User user;
    private String authToken;
    private long expiresAt;
    
    public AuthState() {
        this.authenticated = false;
        this.user = null;
        this.authToken = null;
        this.expiresAt = 0;
    }
    
    public AuthState(boolean authenticated, User user, String authToken, long expiresAt) {
        this.authenticated = authenticated;
        this.user = user;
        this.authToken = authToken;
        this.expiresAt = expiresAt;
    }
    
    // Getters and Setters
    public boolean isAuthenticated() {
        return authenticated;
    }
    
    public void setAuthenticated(boolean authenticated) {
        this.authenticated = authenticated;
    }
    
    public User getUser() {
        return user;
    }
    
    public void setUser(User user) {
        this.user = user;
    }
    
    public String getAuthToken() {
        return authToken;
    }
    
    public void setAuthToken(String authToken) {
        this.authToken = authToken;
    }
    
    public long getExpiresAt() {
        return expiresAt;
    }
    
    public void setExpiresAt(long expiresAt) {
        this.expiresAt = expiresAt;
    }
    
    public boolean isExpired() {
        if (expiresAt == 0) {
            return false;
        }
        return System.currentTimeMillis() > expiresAt;
    }
    
    @Override
    public String toString() {
        return "AuthState{" +
                "authenticated=" + authenticated +
                ", user=" + (user != null ? user.toString() : "null") +
                ", authToken='" + (authToken != null ? "[TOKEN]" : "null") + '\'' +
                ", expiresAt=" + expiresAt +
                '}';
    }
}
