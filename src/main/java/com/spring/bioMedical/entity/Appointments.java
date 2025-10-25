package com.spring.bioMedical.entity;

import javax.persistence.*;
import java.util.Date;

@Entity
@Table(name = "Appointments")
public class Appointments {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "appointment_id")
    private Long appointmentId;
    
    @Column(name = "user_id", nullable = false)
    private Long userId;
    
    @Column(name = "slot_id", nullable = false)
    private Long slotId;
    
    @Column(name = "status", nullable = false)
    private String status = "PENDING";
    
    @Column(name = "notes", length = 500)
    private String notes;
    
    @Column(name = "created_at")
    @Temporal(TemporalType.TIMESTAMP)
    private Date createdAt;
    
    // Getters and Setters
    public Long getAppointmentId() { return appointmentId; }
    public void setAppointmentId(Long appointmentId) { this.appointmentId = appointmentId; }
    
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    
    public Long getSlotId() { return slotId; }
    public void setSlotId(Long slotId) { this.slotId = slotId; }
    
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    
    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
    
    public Date getCreatedAt() { return createdAt; }
    public void setCreatedAt(Date createdAt) { this.createdAt = createdAt; }
    
    @Transient
private String patientName;

@Transient
private String patientEmail;

@Transient
private String appointmentDate;

@Transient
private String appointmentTime;

// Thêm getters và setters
public String getPatientName() { return patientName; }
public void setPatientName(String patientName) { this.patientName = patientName; }

public String getPatientEmail() { return patientEmail; }
public void setPatientEmail(String patientEmail) { this.patientEmail = patientEmail; }

public String getAppointmentDate() { return appointmentDate; }
public void setAppointmentDate(String appointmentDate) { this.appointmentDate = appointmentDate; }

public String getAppointmentTime() { return appointmentTime; }
public void setAppointmentTime(String appointmentTime) { this.appointmentTime = appointmentTime; }

    @Override
    public String toString() {
        return "Appointments{" + "appointmentId=" + appointmentId + ", userId=" + userId + ", slotId=" + slotId + ", status=" + status + ", notes=" + notes + ", createdAt=" + createdAt + ", patientName=" + patientName + ", patientEmail=" + patientEmail + ", appointmentDate=" + appointmentDate + ", appointmentTime=" + appointmentTime + '}';
    }



}