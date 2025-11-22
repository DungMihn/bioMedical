package com.spring.bioMedical.entity;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Date;

@Entity
@Table(name = "Appointments")
@NamedQueries({
    @NamedQuery(name = "Appointments.findAll", query = "SELECT a FROM Appointments a"),
    @NamedQuery(name = "Appointments.findByAppointmentId", query = "SELECT a FROM Appointments a WHERE a.appointmentId = :appointmentId"),
    @NamedQuery(name = "Appointments.findByStatus", query = "SELECT a FROM Appointments a WHERE a.status = :status")
})
public class Appointments implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "appointment_id")
    private Long appointmentId;

    // Cách 1: Map relationship (như entity đầu)
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
    @Temporal(TemporalType.TIMESTAMP)
    private Date createdAt;

    // Transient fields for display (như entity thứ 2)
    @Transient
    private String patientName;

    @Transient
    private String patientEmail;

    @Transient
    private String appointmentDate;

    @Transient
    private String appointmentTime;
    

//    // ✅ THÊM: liên kết chi nhánh (Clinic)
//    @ManyToOne(fetch = FetchType.LAZY, optional = true)
//    @JoinColumn(name = "clinic_id") // cột FK trong bảng 'app'
//    private Clinics clinic;
//    
    

    // Constructors
    public Appointments() {
    }

//    public Clinics getClinic() {
//        return clinic;
//    }
//
//    public void setClinic(Clinics clinic) {
//        this.clinic = clinic;
//    }

    public Appointments(Long appointmentId) {
        this.appointmentId = appointmentId;
    }

    public Appointments(Long appointmentId, String status) {
        this.appointmentId = appointmentId;
        this.status = status;
    }

    // Getters and Setters với naming convention thống nhất
    public Long getAppointmentId() {
        return appointmentId;
    }

    public void setAppointmentId(Long appointmentId) {
        this.appointmentId = appointmentId;
    }

    // Quan trọng: Thống nhất naming - dùng "user" thay vì "userId"
    public Users getUser() {
        return user;
    }

    public void setUser(Users user) {
        this.user = user;
    }

    public AppointmentSlots getSlot() {
        return slot;
    }

    public void setSlot(AppointmentSlots slot) {
        this.slot = slot;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    // Transient fields
    public String getPatientName() {
        return patientName;
    }

    public void setPatientName(String patientName) {
        this.patientName = patientName;
    }

    public String getPatientEmail() {
        return patientEmail;
    }

    public void setPatientEmail(String patientEmail) {
        this.patientEmail = patientEmail;
    }

    public String getAppointmentDate() {
        return appointmentDate;
    }

    public void setAppointmentDate(String appointmentDate) {
        this.appointmentDate = appointmentDate;
    }

    public String getAppointmentTime() {
        return appointmentTime;
    }

    public void setAppointmentTime(String appointmentTime) {
        this.appointmentTime = appointmentTime;
    }

    // Helper methods để tương thích với code cũ
    /**
     * For backward compatibility với code dùng userId trực tiếp
     */
    public Long getUserId() {
        return this.user != null ? this.user.getUserId() : null;
    }

    /**
     * For backward compatibility với code dùng slotId trực tiếp
     */
    public Long getSlotId() {
        return this.slot != null ? this.slot.getSlotId() : null;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (appointmentId != null ? appointmentId.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        if (!(object instanceof Appointments)) {
            return false;
        }
        Appointments other = (Appointments) object;
        if ((this.appointmentId == null && other.appointmentId != null)
                || (this.appointmentId != null && !this.appointmentId.equals(other.appointmentId))) {
            return false;
        }
        return true;
    }

}
