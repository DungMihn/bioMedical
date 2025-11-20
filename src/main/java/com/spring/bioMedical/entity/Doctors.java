package com.spring.bioMedical.entity;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Date;
import java.util.Collection;

@Entity
@Table(name = "Doctors")
public class Doctors implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "doctor_id")
    private Long doctorId;

    // ✅ SỬA: Dùng relationship thay vì ID trực tiếp
    @ManyToOne(optional = false)
    @JoinColumn(name = "user_id", referencedColumnName = "user_id")
    private Users user;

    @ManyToOne(optional = false)
    @JoinColumn(name = "clinic_id", referencedColumnName = "clinic_id")
    private Clinics clinic;

    @ManyToOne(optional = false)
    @JoinColumn(name = "specialty_id", referencedColumnName = "specialty_id")
    private Specialties specialty;

    @Column(name = "bio", length = 1000)
    private String bio;

    @Column(name = "created_at")
    @Temporal(TemporalType.TIMESTAMP)
    private Date createdAt;

    // ✅ Relationships
    @OneToMany(mappedBy = "doctor")
    private Collection<AppointmentSlots> appointmentSlots;

    // Constructors
    public Doctors() {
    }

    public Doctors(Long doctorId) {
        this.doctorId = doctorId;
    }

    // Getters and Setters
    public Long getDoctorId() { 
        return doctorId; 
    }
    
    public void setDoctorId(Long doctorId) { 
        this.doctorId = doctorId; 
    }

    // ✅ QUAN TRỌNG: Đặt tên theo relationship
    public Users getUser() { 
        return user; 
    }
    
    public void setUser(Users user) { 
        this.user = user; 
    }

    public Clinics getClinic() { 
        return clinic; 
    }
    
    public void setClinic(Clinics clinic) { 
        this.clinic = clinic; 
    }

    public Specialties getSpecialty() { 
        return specialty; 
    }
    
    public void setSpecialty(Specialties specialty) { 
        this.specialty = specialty; 
    }

    public String getBio() { 
        return bio; 
    }
    
    public void setBio(String bio) { 
        this.bio = bio; 
    }

    public Date getCreatedAt() { 
        return createdAt; 
    }
    
    public void setCreatedAt(Date createdAt) { 
        this.createdAt = createdAt; 
    }

    public Collection<AppointmentSlots> getAppointmentSlots() { 
        return appointmentSlots; 
    }
    
    public void setAppointmentSlots(Collection<AppointmentSlots> appointmentSlots) { 
        this.appointmentSlots = appointmentSlots; 
    }

    // ✅ Helper methods cho backward compatibility
    public Long getUserId() {
        return this.user != null ? this.user.getUserId() : null;
    }

    public Long getClinicId() {
        return this.clinic != null ? this.clinic.getClinicId() : null;
    }

    public Long getSpecialtyId() {
        return this.specialty != null ? this.specialty.getSpecialtyId() : null;
    }

    public void setUserId(Long userId) {
        if (userId != null) {
            Users user = new Users();
            user.setUserId(userId);
            this.user = user;
        }
    }

    public void setClinicId(Long clinicId) {
        if (clinicId != null) {
            Clinics clinic = new Clinics();
            clinic.setClinicId(clinicId);
            this.clinic = clinic;
        }
    }

    public void setSpecialtyId(Long specialtyId) {
        if (specialtyId != null) {
            Specialties specialty = new Specialties();
            specialty.setSpecialtyId(specialtyId);
            this.specialty = specialty;
        }
    }

    // ✅ Business methods
    public String getDoctorFullName() {
        return this.user != null ? this.user.getFullName() : "Unknown Doctor";
    }

    public String getSpecialtyName() {
        return this.specialty != null ? this.specialty.getName() : "Unknown Specialty";
    }

    public String getClinicName() {
        return this.clinic != null ? this.clinic.getName() : "Unknown Clinic";
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (doctorId != null ? doctorId.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        if (!(object instanceof Doctors)) {
            return false;
        }
        Doctors other = (Doctors) object;
        if ((this.doctorId == null && other.doctorId != null) || 
            (this.doctorId != null && !this.doctorId.equals(other.doctorId))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "Doctors{" + 
               "doctorId=" + doctorId + 
               ", user=" + (user != null ? user.getUserId() : "null") +
               ", clinic=" + (clinic != null ? clinic.getClinicId() : "null") +
               ", specialty=" + (specialty != null ? specialty.getSpecialtyId() : "null") +
               '}';
    }
}