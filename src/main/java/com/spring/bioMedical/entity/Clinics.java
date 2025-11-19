package com.spring.bioMedical.entity;

import javax.persistence.*;
import java.time.LocalTime;
import java.util.Date;

@Entity
@Table(name = "Clinics")
public class Clinics {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "clinic_id")
    private Long clinicId;
    
    @Column(name = "name", nullable = false)
    private String name;
    
    @Column(name = "address")
    private String address;
    
    @Column(name = "phone")
    private String phone;
    
    @Column(name = "open_time")
    private LocalTime openTime;
    
    @Column(name = "close_time")
    private LocalTime closeTime;
    
    @Column(name = "slot_time")
    private Integer slotTime;
    
    @Column(name = "created_at")
    private Date createdAt;
    
    // Constructors
    public Clinics() {
        this.createdAt = new Date();
    }
    
    public Clinics(String name, String address, String phone) {
        this();
        this.name = name;
        this.address = address;
        this.phone = phone;
        this.openTime = LocalTime.of(8, 0); // Default 08:00
        this.closeTime = LocalTime.of(17, 0); // Default 17:00
        this.slotTime = 60; // Default 60 minutes
    }
    
    // Getters and Setters
    public Long getClinicId() {
        return clinicId;
    }
    
    public void setClinicId(Long clinicId) {
        this.clinicId = clinicId;
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public String getAddress() {
        return address;
    }
    
    public void setAddress(String address) {
        this.address = address;
    }
    
    public String getPhone() {
        return phone;
    }
    
    public void setPhone(String phone) {
        this.phone = phone;
    }
    
    public LocalTime getOpenTime() {
        return openTime;
    }
    
    public void setOpenTime(LocalTime openTime) {
        this.openTime = openTime;
    }
    
    public LocalTime getCloseTime() {
        return closeTime;
    }
    
    public void setCloseTime(LocalTime closeTime) {
        this.closeTime = closeTime;
    }
    
    public Integer getSlotTime() {
        return slotTime;
    }
    
    public void setSlotTime(Integer slotTime) {
        this.slotTime = slotTime;
    }
    
    public Date getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }
    
    @Override
    public String toString() {
        return "Clinic{" +
                "clinicId=" + clinicId +
                ", name='" + name + '\'' +
                ", address='" + address + '\'' +
                ", phone='" + phone + '\'' +
                ", openTime=" + openTime +
                ", closeTime=" + closeTime +
                ", slotTime=" + slotTime +
                ", createdAt=" + createdAt +
                '}';
    }
}