package com.spring.bioMedical.entity;

import java.time.LocalDateTime;
import java.time.LocalTime;   // 👈 thêm import này
import javax.persistence.*;

@Entity
@Table(name = "Clinics")
public class Clinic {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "clinic_id")
    private Long clinicId;

    private String name;
    private String address;
    private String phone;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    // ====== THÊM 3 FIELD KHỚP SCHEMA ======
    @Column(name = "open_time")              // TIME NOT NULL DEFAULT '08:00'
    private LocalTime openTime;

    @Column(name = "close_time")             // TIME NOT NULL DEFAULT '17:00'
    private LocalTime closeTime;

    @Column(name = "slot_time")              // INT NOT NULL DEFAULT 60 (phút)
    private Integer slotTime;

    // --- Getters & Setters ---

    public Long getClinicId() { return clinicId; }
    public void setClinicId(Long clinicId) { this.clinicId = clinicId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    // ====== GET/SET mới ======
    public LocalTime getOpenTime() { return openTime; }
    public void setOpenTime(LocalTime openTime) { this.openTime = openTime; }

    public LocalTime getCloseTime() { return closeTime; }
    public void setCloseTime(LocalTime closeTime) { this.closeTime = closeTime; }

    public Integer getSlotTime() { return slotTime; }
    public void setSlotTime(Integer slotTime) { this.slotTime = slotTime; }
}
