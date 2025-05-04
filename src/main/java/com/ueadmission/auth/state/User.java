package com.ueadmission.auth.state;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * User model class to store authenticated user information
 */
public class User implements Serializable {
    private static final long serialVersionUID = 1L;
    private int id;
    private String firstName;
    private String lastName;
    private String email;
    private String phoneNumber;
    private String address;
    private String city;
    private String country;
    private String role;
    private String ipAddress;
    private LocalDateTime lastLoginTime;
    private boolean isLoggedIn;
    
    // Constructor without ID for new users
    public User(String firstName, String lastName, String email, String phoneNumber, String role) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.phoneNumber = phoneNumber;
        this.role = role;
    }
    
    // Full constructor
    public User(int id, String firstName, String lastName, String email, String phoneNumber, String role) {
        this.id = id;
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.phoneNumber = phoneNumber;
        this.role = role;
    }
    
    // Constructor with address fields
    public User(int id, String firstName, String lastName, String email, String phoneNumber, 
                String address, String city, String country, String role) {
        this.id = id;
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.phoneNumber = phoneNumber;
        this.address = address;
        this.city = city;
        this.country = country;
        this.role = role;
    }
    
    // Extended constructor with login tracking fields
    public User(int id, String firstName, String lastName, String email, String phoneNumber, 
                String address, String city, String country, String role, 
                String ipAddress, LocalDateTime lastLoginTime, boolean isLoggedIn) {
        this.id = id;
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.phoneNumber = phoneNumber;
        this.address = address;
        this.city = city;
        this.country = country;
        this.role = role;
        this.ipAddress = ipAddress;
        this.lastLoginTime = lastLoginTime;
        this.isLoggedIn = isLoggedIn;
    }
    
    // Getters and Setters
    public int getId() {
        return id;
    }
    
    public void setId(int id) {
        this.id = id;
    }
    
    public String getFirstName() {
        return firstName;
    }
    
    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }
    
    public String getLastName() {
        return lastName;
    }
    
    public void setLastName(String lastName) {
        this.lastName = lastName;
    }
    
    public String getEmail() {
        return email;
    }
    
    public void setEmail(String email) {
        this.email = email;
    }
    
    public String getPhoneNumber() {
        return phoneNumber;
    }
    
    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }
    
    public String getAddress() {
        return address != null ? address : "N/A";
    }
    
    public void setAddress(String address) {
        this.address = address;
    }
    
    public String getCity() {
        return city != null ? city : "N/A";
    }
    
    public void setCity(String city) {
        this.city = city;
    }
    
    public String getCountry() {
        return country != null ? country : "N/A";
    }
    
    public void setCountry(String country) {
        this.country = country;
    }
    
    public String getRole() {
        return role;
    }
    
    public void setRole(String role) {
        this.role = role;
    }
    
    public String getIpAddress() {
        return ipAddress;
    }
    
    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }
    
    public LocalDateTime getLastLoginTime() {
        return lastLoginTime;
    }
    
    public void setLastLoginTime(LocalDateTime lastLoginTime) {
        this.lastLoginTime = lastLoginTime;
    }
    
    public boolean isLoggedIn() {
        return isLoggedIn;
    }
    
    public void setLoggedIn(boolean loggedIn) {
        isLoggedIn = loggedIn;
    }
    
    public String getFullName() {
        return firstName + " " + lastName;
    }
    
    @Override
    public String toString() {
        return "User{" +
                "id=" + id +
                ", firstName='" + firstName + '\'' +
                ", lastName='" + lastName + '\'' +
                ", email='" + email + '\'' +
                ", address='" + address + '\'' +
                ", city='" + city + '\'' +
                ", country='" + country + '\'' +
                ", role='" + role + '\'' +
                ", ipAddress='" + ipAddress + '\'' +
                ", lastLoginTime=" + lastLoginTime +
                ", isLoggedIn=" + isLoggedIn +
                '}';
    }
}
