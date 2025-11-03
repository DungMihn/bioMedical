/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.spring.bioMedical.entity;

import java.io.Serializable;
import java.util.Collection;
import java.util.Date;
import javax.persistence.Basic;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.validation.constraints.Size;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.fasterxml.jackson.annotation.JsonBackReference;


/**
 *
 * @author macbookprom1
 */
@Entity
@Table(name = "Doctors")
@XmlRootElement
@NamedQueries({
    @NamedQuery(name = "Doctors.findAll", query = "SELECT d FROM Doctors d"),
    @NamedQuery(name = "Doctors.findByDoctorId", query = "SELECT d FROM Doctors d WHERE d.doctorId = :doctorId"),
    @NamedQuery(name = "Doctors.findByBio", query = "SELECT d FROM Doctors d WHERE d.bio = :bio"),
    @NamedQuery(name = "Doctors.findByCreatedAt", query = "SELECT d FROM Doctors d WHERE d.createdAt = :createdAt")})
public class Doctors implements Serializable {

    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "doctor_id")
    private Long doctorId;
    @Size(max = 1000)
    @Column(name = "bio")
    private String bio;
    @Column(name = "created_at")
    @Temporal(TemporalType.TIMESTAMP)
    private Date createdAt;
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "doctorId")
    private Collection<AppointmentSlots> appointmentSlotsCollection;
@JoinColumn(name = "clinic_id", referencedColumnName = "clinic_id")
@ManyToOne(optional = false)
@JsonBackReference
private Clinics clinicId;

    @JoinColumn(name = "specialty_id", referencedColumnName = "specialty_id")
    @ManyToOne(optional = false)
    private Specialties specialtyId;
    @JoinColumn(name = "user_id", referencedColumnName = "user_id")
    @ManyToOne(optional = false)
    private Users userId;

    public Doctors() {
    }

    public Doctors(Long doctorId) {
        this.doctorId = doctorId;
    }

    public Long getDoctorId() {
        return doctorId;
    }

    public void setDoctorId(Long doctorId) {
        this.doctorId = doctorId;
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

    @XmlTransient
    public Collection<AppointmentSlots> getAppointmentSlotsCollection() {
        return appointmentSlotsCollection;
    }

    public void setAppointmentSlotsCollection(Collection<AppointmentSlots> appointmentSlotsCollection) {
        this.appointmentSlotsCollection = appointmentSlotsCollection;
    }

    public Clinics getClinicId() {
        return clinicId;
    }

    public void setClinicId(Clinics clinicId) {
        this.clinicId = clinicId;
    }

    public Specialties getSpecialtyId() {
        return specialtyId;
    }

    public void setSpecialtyId(Specialties specialtyId) {
        this.specialtyId = specialtyId;
    }

    public Users getUserId() {
        return userId;
    }

    public void setUserId(Users userId) {
        this.userId = userId;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (doctorId != null ? doctorId.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof Doctors)) {
            return false;
        }
        Doctors other = (Doctors) object;
        if ((this.doctorId == null && other.doctorId != null) || (this.doctorId != null && !this.doctorId.equals(other.doctorId))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "com.spring.bioMedical.entity.Doctors[ doctorId=" + doctorId + " ]";
    }
    
}
