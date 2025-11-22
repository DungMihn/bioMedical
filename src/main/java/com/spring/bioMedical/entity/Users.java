package com.spring.bioMedical.entity;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.Date;
import javax.persistence.*;
import javax.validation.constraints.*;

import org.springframework.format.annotation.DateTimeFormat;

@Entity
@Table(name = "Users")
public class Users implements Serializable {

    private static final long serialVersionUID = 1L;

    // ----- Primary key -----
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    private Long userId;

    // ----- Basic fields -----
    @NotBlank(message = "Username is required")
    @Size(max = 100, message = "Username must be less than 100 characters")
    @Column(name = "username", nullable = false, unique = true)
    private String username;

    @NotBlank(message = "Password is required")
    @Size(min = 6, max = 255, message = "Password must be between 6 and 255 characters")
    @Column(name = "password_hash", nullable = false)
    private String passwordHash;

    @NotBlank(message = "Full name is required")
    @Size(max = 255, message = "Full name must be less than 255 characters")
    @Column(name = "full_name", nullable = false)
    private String fullName;

    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    @Size(max = 255)
    @Column(name = "email", nullable = false, unique = true)
    private String email;

    @Pattern(regexp = "^(\\+?\\d{9,15})$", message = "Invalid phone number")
    @Column(name = "phone")
    private String phone;

    @NotBlank(message = "Gender is required")
    @Column(name = "gender")
    private String gender;

    @NotNull(message = "Date of birth is required")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    @Column(name = "date_of_birth")
    private LocalDate dateOfBirth;

    @Column(name = "role", nullable = false)
    private String role = "PATIENT"; // default role

    @Column(name = "enabled")
    private Boolean enabled = false;

    // ----- OTP -----
    @Column(name = "otp_code", length = 50)
    private String otpCode;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "otp_expiry")
    private Date otpExpiry;

    // ----- Audit fields -----
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "created_at")
    private Date createdAt = new Date();

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "updated_at")
    private Date updatedAt = new Date();

    /* ====== NEW: Branch/Clinic relation ====== */
    @ManyToOne(optional = true)
    @JoinColumn(name = "clinic_id")  // cột FK trong bảng Users
    private Clinics clinic;

    // ----- Constructors -----
    public Users() {
    }

    public Users(Long userId, String username, String passwordHash, String role) {
        this.userId = userId;
        this.username = username;
        this.passwordHash = passwordHash;
        this.role = role;
    }

    public Clinics getClinic() {
        return clinic;
    }

    public void setClinic(Clinics clinic) {
        this.clinic = clinic;
    }

    // ----- Getters & Setters -----
    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public LocalDate getDateOfBirth() {
        return dateOfBirth;
    }

    public void setDateOfBirth(LocalDate dateOfBirth) {
        this.dateOfBirth = dateOfBirth;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public Boolean getEnabled() {
        return enabled;
    }

    public void setEnabled(Boolean enabled) {
        this.enabled = enabled;
    }

    public String getOtpCode() {
        return otpCode;
    }

    public void setOtpCode(String otpCode) {
        this.otpCode = otpCode;
    }

    public Date getOtpExpiry() {
        return otpExpiry;
    }

    public void setOtpExpiry(Date otpExpiry) {
        this.otpExpiry = otpExpiry;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    public Date getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Date updatedAt) {
        this.updatedAt = updatedAt;
    }

    // trong class Users
    @javax.persistence.Transient
    public Long getClinicId() {
        return clinic != null ? clinic.getClinicId() : null;
    }
}
