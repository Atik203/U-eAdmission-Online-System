package com.ueadmission.application.model;

import java.time.LocalDate;

/**
 * Model class representing an application in the system
 */
public class Application {
    private String id;
    private String programName;
    private String semesterName;
    private LocalDate applicationDate;
    private ApplicationStatus status;
    private PaymentStatus paymentStatus;
    private double totalFee;
    private double paidAmount;
    private String applicantName;
    private String applicantId;
    private String email;
    private String phoneNumber;
    private LocalDate dateOfBirth;
    private String gender;
    private String address;
    private String city;
    private String postalCode;
    private double sscGpa;
    private double hscGpa;
    private String sscYear;
    private String hscYear;
    private String fatherName;
    private String fatherOccupation;
    private String motherName;
    private String motherOccupation;
    private String guardianPhone;
    private String guardianEmail;
    
    public Application() {
        // Default constructor for JavaFX binding
    }
    
    public Application(String id, String programName, String semesterName, LocalDate applicationDate, 
                       ApplicationStatus status, PaymentStatus paymentStatus, double totalFee, 
                       double paidAmount, String applicantName, String applicantId) {
        this.id = id;
        this.programName = programName;
        this.semesterName = semesterName;
        this.applicationDate = applicationDate;
        this.status = status;
        this.paymentStatus = paymentStatus;
        this.totalFee = totalFee;
        this.paidAmount = paidAmount;
        this.applicantName = applicantName;
        this.applicantId = applicantId;
    }

    // Extended constructor with all fields
    public Application(String id, String programName, String semesterName, LocalDate applicationDate, 
                      ApplicationStatus status, PaymentStatus paymentStatus, double totalFee, 
                      double paidAmount, String applicantName, String applicantId, String email, 
                      String phoneNumber, LocalDate dateOfBirth, String gender, String address, 
                      String city, String postalCode, double sscGpa, double hscGpa, String sscYear, 
                      String hscYear, String fatherName, String fatherOccupation, String motherName, 
                      String motherOccupation, String guardianPhone, String guardianEmail) {
        this(id, programName, semesterName, applicationDate, status, paymentStatus, totalFee, paidAmount, 
            applicantName, applicantId);
        this.email = email;
        this.phoneNumber = phoneNumber;
        this.dateOfBirth = dateOfBirth;
        this.gender = gender;
        this.address = address;
        this.city = city;
        this.postalCode = postalCode;
        this.sscGpa = sscGpa;
        this.hscGpa = hscGpa;
        this.sscYear = sscYear;
        this.hscYear = hscYear;
        this.fatherName = fatherName;
        this.fatherOccupation = fatherOccupation;
        this.motherName = motherName;
        this.motherOccupation = motherOccupation;
        this.guardianPhone = guardianPhone;
        this.guardianEmail = guardianEmail;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getProgramName() {
        return programName;
    }

    public void setProgramName(String programName) {
        this.programName = programName;
    }

    public String getSemesterName() {
        return semesterName;
    }

    public void setSemesterName(String semesterName) {
        this.semesterName = semesterName;
    }

    public LocalDate getApplicationDate() {
        return applicationDate;
    }

    public void setApplicationDate(LocalDate applicationDate) {
        this.applicationDate = applicationDate;
    }

    public ApplicationStatus getStatus() {
        return status;
    }

    public void setStatus(ApplicationStatus status) {
        this.status = status;
    }

    public PaymentStatus getPaymentStatus() {
        return paymentStatus;
    }

    public void setPaymentStatus(PaymentStatus paymentStatus) {
        this.paymentStatus = paymentStatus;
    }

    public double getTotalFee() {
        return totalFee;
    }

    public void setTotalFee(double totalFee) {
        this.totalFee = totalFee;
    }

    public double getPaidAmount() {
        return paidAmount;
    }

    public void setPaidAmount(double paidAmount) {
        this.paidAmount = paidAmount;
    }

    public String getApplicantName() {
        return applicantName;
    }

    public void setApplicantName(String applicantName) {
        this.applicantName = applicantName;
    }

    public String getApplicantId() {
        return applicantId;
    }

    public void setApplicantId(String applicantId) {
        this.applicantId = applicantId;
    }
    
    public double getBalanceAmount() {
        return totalFee - paidAmount;
    }
    
    public boolean isPaymentComplete() {
        return paidAmount >= totalFee;
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

    /**
     * Get semester and year as a combined string
     * @return Semester and year (e.g., "Summer 2025")
     */
    public String getSemesterAndYear() {
        return semesterName;
    }
}