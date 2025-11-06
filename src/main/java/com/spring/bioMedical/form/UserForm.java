// src/main/java/com/spring/bioMedical/dto/UserForm.java
package com.spring.bioMedical.dto;

import java.time.LocalDate;
import javax.validation.constraints.*;
import org.springframework.format.annotation.DateTimeFormat;

public class UserForm {

    @NotBlank @Size(max = 100)
    private String username;

    @NotBlank @Size(min = 8, max = 255)
    private String password; // sẽ map sang Users.passwordHash

    @NotBlank @Email @Size(max = 255)
    private String email;

    @Size(max = 50)
    private String phone;

    @NotBlank @Size(max = 255)
    private String fullName;

    @Size(max = 20)
    private String gender; // "MALE" / "FEMALE" / "OTHER"

    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate dateOfBirth;

    @NotBlank
    private String role; // "DOCTOR" hoặc "PATIENT" (tuỳ allowedRoles)

    @NotNull
    private Boolean enabled = Boolean.TRUE;

    // --- GETTER/SETTER CHUẨN JavaBean ---
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

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

    public Boolean getEnabled() { return enabled; }
    public void setEnabled(Boolean enabled) { this.enabled = enabled; }
}
