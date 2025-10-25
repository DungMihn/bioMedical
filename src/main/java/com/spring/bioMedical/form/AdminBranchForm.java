package com.spring.bioMedical.dto;

import javax.validation.constraints.*;
import org.springframework.format.annotation.DateTimeFormat;

public class AdminBranchForm {

    @NotBlank(message = "Username is required")
    @Size(max = 100, message = "Username tối đa 100 ký tự")
    private String username;

    @NotBlank(message = "Password is required")
    // ≥8 ký tự, có hoa/thường/số/ký tự đặc biệt
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

    @NotNull(message = "Clinic is required")
    private Long clinicId;

    // getters/setters
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
    public Long getClinicId() { return clinicId; }
    public void setClinicId(Long clinicId) { this.clinicId = clinicId; }
}
