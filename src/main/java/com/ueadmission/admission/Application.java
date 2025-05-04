package com.ueadmission.admission;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Application model class to store application information
 */
public class Application implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private int id;
    private int userId;
    private String firstName;
    private String lastName;
    private String email;
    private String phoneNumber;
    private LocalDate dateOfBirth;
    private String gender;
    private String address;
    private String city;
    private String postalCode;
    
    // Guardian Information
    private String fatherName;
    private String fatherOccupation;
    private String motherName;
    private String motherOccupation;
    private String guardianPhone;
    private String guardianEmail;
    
    // Academic Information
    private String program;
    private String institution;
    private double sscGpa;
    private double hscGpa;
    private String sscYear;
    private String hscYear;
    
    // Application status
    private String status;
    private boolean paymentComplete;
    private LocalDateTime applicationDate;
    
    // Default constructor
    public Application() {
        this.applicationDate = LocalDateTime.now();
        this.status = "Pending";
        this.paymentComplete = false;
    }
    
    // Full constructor
    public Application(int userId, String firstName, String lastName, String email, String phoneNumber,
                      LocalDate dateOfBirth, String gender, String address, String city, String postalCode,
                      String fatherName, String fatherOccupation, String motherName, String motherOccupation,
                      String guardianPhone, String guardianEmail, String program, String institution,
                      double sscGpa, double hscGpa, String sscYear, String hscYear) {
        this.userId = userId;
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.phoneNumber = phoneNumber;
        this.dateOfBirth = dateOfBirth;
        this.gender = gender;
        this.address = address;
        this.city = city;
        this.postalCode = postalCode;
        this.fatherName = fatherName;
        this.fatherOccupation = fatherOccupation;
        this.motherName = motherName;
        this.motherOccupation = motherOccupation;
        this.guardianPhone = guardianPhone;
        this.guardianEmail = guardianEmail;
        this.program = program;
        this.institution = institution;
        this.sscGpa = sscGpa;
        this.hscGpa = hscGpa;
        this.sscYear = sscYear;
        this.hscYear = hscYear;
        this.applicationDate = LocalDateTime.now();
        this.status = "Pending";
        this.paymentComplete = false;
    }
    
    // Getters and Setters
    public int getId() {
        return id;
    }
    
    public void setId(int id) {
        this.id = id;
    }
    
    public int getUserId() {
        return userId;
    }
    
    public void setUserId(int userId) {
        this.userId = userId;
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
    
    public LocalDate getDateOfBirth() {
        return dateOfBirth;
    }
    
    public void setDateOfBirth(LocalDate dateOfBirth) {
        this.dateOfBirth = dateOfBirth;
    }
    
    public String getGender() {
        return gender;
    }
    
    public void setGender(String gender) {
        this.gender = gender;
    }
    
    public String getAddress() {
        return address;
    }
    
    public void setAddress(String address) {
        this.address = address;
    }
    
    public String getCity() {
        return city;
    }
    
    public void setCity(String city) {
        this.city = city;
    }
    
    public String getPostalCode() {
        return postalCode;
    }
    
    public void setPostalCode(String postalCode) {
        this.postalCode = postalCode;
    }
    
    public String getFatherName() {
        return fatherName;
    }
    
    public void setFatherName(String fatherName) {
        this.fatherName = fatherName;
    }
    
    public String getFatherOccupation() {
        return fatherOccupation;
    }
    
    public void setFatherOccupation(String fatherOccupation) {
        this.fatherOccupation = fatherOccupation;
    }
    
    public String getMotherName() {
        return motherName;
    }
    
    public void setMotherName(String motherName) {
        this.motherName = motherName;
    }
    
    public String getMotherOccupation() {
        return motherOccupation;
    }
    
    public void setMotherOccupation(String motherOccupation) {
        this.motherOccupation = motherOccupation;
    }
    
    public String getGuardianPhone() {
        return guardianPhone;
    }
    
    public void setGuardianPhone(String guardianPhone) {
        this.guardianPhone = guardianPhone;
    }
    
    public String getGuardianEmail() {
        return guardianEmail;
    }
    
    public void setGuardianEmail(String guardianEmail) {
        this.guardianEmail = guardianEmail;
    }
    
    public String getProgram() {
        return program;
    }
    
    public void setProgram(String program) {
        this.program = program;
    }
    
    public String getInstitution() {
        return institution;
    }
    
    public void setInstitution(String institution) {
        this.institution = institution;
    }
    
    public double getSscGpa() {
        return sscGpa;
    }
    
    public void setSscGpa(double sscGpa) {
        this.sscGpa = sscGpa;
    }
    
    public double getHscGpa() {
        return hscGpa;
    }
    
    public void setHscGpa(double hscGpa) {
        this.hscGpa = hscGpa;
    }
    
    public String getSscYear() {
        return sscYear;
    }
    
    public void setSscYear(String sscYear) {
        this.sscYear = sscYear;
    }
    
    public String getHscYear() {
        return hscYear;
    }
    
    public void setHscYear(String hscYear) {
        this.hscYear = hscYear;
    }
    
    public String getStatus() {
        return status;
    }
    
    public void setStatus(String status) {
        this.status = status;
    }
    
    public boolean isPaymentComplete() {
        return paymentComplete;
    }
    
    public void setPaymentComplete(boolean paymentComplete) {
        this.paymentComplete = paymentComplete;
    }
    
    public LocalDateTime getApplicationDate() {
        return applicationDate;
    }
    
    public void setApplicationDate(LocalDateTime applicationDate) {
        this.applicationDate = applicationDate;
    }
    
    @Override
    public String toString() {
        return "Application{" +
                "id=" + id +
                ", userId=" + userId +
                ", firstName='" + firstName + '\'' +
                ", lastName='" + lastName + '\'' +
                ", email='" + email + '\'' +
                ", program='" + program + '\'' +
                ", status='" + status + '\'' +
                ", paymentComplete=" + paymentComplete +
                ", applicationDate=" + applicationDate +
                '}';
    }
}