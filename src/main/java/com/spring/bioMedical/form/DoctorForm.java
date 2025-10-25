package com.spring.bioMedical.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

import org.springframework.format.annotation.DateTimeFormat;

public class DoctorForm {

    @NotBlank(message = "Username is required")
    @Size(max = 100, message = "Username tối đa 100 ký tự")
    private String username;

    @NotBlank(message = "Password is required")
    // ≥8 ký tự, có chữ thường, chữ hoa, số, và ký tự đặc biệt (@$!%*?&._-)
    @Pattern(
        regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&._-])[A-Za-z\\d@$!%*?&._-]{8,}$",
        message = "Password phải ≥8 ký tự, gồm chữ hoa, chữ thường, số và ký tự đặc biệt"
    )
    private String passwordHash;

    @NotBlank(message = "Email is required")
    @Email(message = "Email không hợp lệ")
    @Size(max = 255, message = "Email tối đa 255 ký tự")
    private String email;

    @Size(max = 50, message = "Phone tối đa 50 ký tự")
    private String phone;

    @NotBlank(message = "Full name is required")
    @Size(max = 255, message = "Full name tối đa 255 ký tự")
    private String fullName;

    @Size(max = 20, message = "Gender tối đa 20 ký tự")
    private String gender;

    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate dateOfBirth;

    // Role có thể để controller/service set mặc định ("DOCTOR")
    private String role;

    @NotNull(message = "Clinic is required")
    private Long clinicId;

    private Long specialtyId;

    @Size(max = 2000, message = "Bio tối đa 2000 ký tự")
    private String bio;

    @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime createdAt;

    // Getters & Setters
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getPasswordHash() { return passwordHash; }
    public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public String getGender() { return gender; }
    public void setGender(String gender) { this.gender = gender; }

    public LocalDate getDateOfBirth() { return dateOfBirth; }
    public void setDateOfBirth(LocalDate dateOfBirth) { this.dateOfBirth = dateOfBirth; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public Long getClinicId() { return clinicId; }
    public void setClinicId(Long clinicId) { this.clinicId = clinicId; }

    public Long getSpecialtyId() { return specialtyId; }
    public void setSpecialtyId(Long specialtyId) { this.specialtyId = specialtyId; }

    public String getBio() { return bio; }
    public void setBio(String bio) { this.bio = bio; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
