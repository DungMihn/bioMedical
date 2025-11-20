package com.spring.bioMedical.entity;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "Appointments")
public class AppointmentBooking {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "appointment_id")
    private Long appointmentId;

    // ✅ SỬA: Dùng relationship thay vì ID trực tiếp
    @ManyToOne(optional = false)
    @JoinColumn(name = "user_id", referencedColumnName = "user_id")
    private Users user;

    @ManyToOne(optional = false)
    @JoinColumn(name = "slot_id", referencedColumnName = "slot_id")
    private AppointmentSlots slot;

    @Column(name = "status", nullable = false, length = 50)
    private String status = "PENDING";

    @Column(name = "notes", length = 500)
    private String notes;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    // Getters and Setters
    public Long getAppointmentId() { return appointmentId; }
    public void setAppointmentId(Long appointmentId) { this.appointmentId = appointmentId; }

    public Users getUser() { return user; }
    public void setUser(Users user) { this.user = user; }

    public AppointmentSlots getSlot() { return slot; }
    public void setSlot(AppointmentSlots slot) { this.slot = slot; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    // ✅ Helper methods cho backward compatibility
    public Long getUserId() {
        return this.user != null ? this.user.getUserId() : null;
    }

    public Long getSlotId() {
        return this.slot != null ? this.slot.getSlotId() : null;
    }

    public void setUserId(Long userId) {
        if (userId != null) {
            Users user = new Users();
            user.setUserId(userId);
            this.user = user;
        }
    }

    public void setSlotId(Long slotId) {
        if (slotId != null) {
            AppointmentSlots slot = new AppointmentSlots();
            slot.setSlotId(slotId);
            this.slot = slot;
        }
    }

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }
}