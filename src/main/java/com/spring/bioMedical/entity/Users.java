/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.spring.bioMedical.entity;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EntityListeners;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import javax.xml.bind.annotation.XmlRootElement;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import org.springframework.format.annotation.DateTimeFormat;

/**
 *
 * @author macbookprom1
 */
@Entity
@Table(name = "Users")
@XmlRootElement
@EntityListeners(AuditingEntityListener.class)
@NamedQueries({
    @NamedQuery(name = "Users.findAll", query = "SELECT u FROM Users u"),
    @NamedQuery(name = "Users.findByUserId", query = "SELECT u FROM Users u WHERE u.userId = :userId"),
    @NamedQuery(name = "Users.findByUsername", query = "SELECT u FROM Users u WHERE u.username = :username"),
    @NamedQuery(name = "Users.findByPasswordHash", query = "SELECT u FROM Users u WHERE u.passwordHash = :passwordHash"),
    @NamedQuery(name = "Users.findByEmail", query = "SELECT u FROM Users u WHERE u.email = :email"),
    @NamedQuery(name = "Users.findByPhone", query = "SELECT u FROM Users u WHERE u.phone = :phone"),
    @NamedQuery(name = "Users.findByFullName", query = "SELECT u FROM Users u WHERE u.fullName = :fullName"),
    @NamedQuery(name = "Users.findByGender", query = "SELECT u FROM Users u WHERE u.gender = :gender"),
    @NamedQuery(name = "Users.findByDateOfBirth", query = "SELECT u FROM Users u WHERE u.dateOfBirth = :dateOfBirth"),
    @NamedQuery(name = "Users.findByRole", query = "SELECT u FROM Users u WHERE u.role = :role"),
    @NamedQuery(name = "Users.findByEnabled", query = "SELECT u FROM Users u WHERE u.enabled = :enabled"),
    @NamedQuery(name = "Users.findByCreatedAt", query = "SELECT u FROM Users u WHERE u.createdAt = :createdAt"),
    @NamedQuery(name = "Users.findByUpdatedAt", query = "SELECT u FROM Users u WHERE u.updatedAt = :updatedAt")
})
public class Users implements Serializable {

    private static final long serialVersionUID = 1L;

    /* ====== Keys & basic info ====== */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "user_id")
    private Long userId;

    @Basic(optional = false)
    @NotNull
    @Size(min = 1, max = 100)
    @Column(name = "username")
    private String username;

    @Basic(optional = false)
    @NotNull
    @Size(min = 1, max = 255)
    @Column(name = "password_hash")
    private String passwordHash;

    @Size(max = 255)
    @Column(name = "email")
    private String email;

    @Size(max = 50)
    @Column(name = "phone")
    private String phone;

    @Size(max = 255)
    @Column(name = "full_name")
    private String fullName;

    @Size(max = 20)
    @Column(name = "gender")
    private String gender;

    @DateTimeFormat(pattern = "yyyy-MM-dd")
    @Column(name = "date_of_birth")
    private LocalDate dateOfBirth;

    @Basic(optional = false)
    @NotNull
    @Size(min = 1, max = 20)
    @Column(name = "role")
    private String role;

    @Column(name = "enabled")
    private Boolean enabled;

    /* ====== Auditing ====== */
    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    /* ====== NEW: Branch/Clinic relation ====== */
    @ManyToOne(optional = true)
    @JoinColumn(name = "clinic_id")  // cột FK trong bảng Users
    private Clinic clinic;
    

    /* ====== Constructors ====== */
    public Users() { }

    public Users(Long userId) {
        this.userId = userId;
    }

    public Users(Long userId, String username, String passwordHash, String role) {
        this.userId = userId;
        this.username = username;
        this.passwordHash = passwordHash;
        this.role = role;
    }

    /* ====== Getters / Setters ====== */
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

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

    public Boolean getEnabled() { return enabled; }
    public void setEnabled(Boolean enabled) { this.enabled = enabled; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    /* NEW: clinic getter/setter */
    public Clinic getClinic() { return clinic; }
    public void setClinic(Clinic clinic) { this.clinic = clinic; }

    /* ====== equals/hashCode/toString ====== */
    @Override
    public int hashCode() {
        int hash = 0;
        hash += (userId != null ? userId.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        if (!(object instanceof Users)) return false;
        Users other = (Users) object;
        if ((this.userId == null && other.userId != null) ||
            (this.userId != null && !this.userId.equals(other.userId))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "com.spring.bioMedical.entity.Users[ userId=" + userId + " ]";
    }
}
