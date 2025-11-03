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
import javax.validation.constraints.NotNull;
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
@Table(name = "Appointmentslots")
@XmlRootElement
@NamedQueries({
    @NamedQuery(name = "AppointmentSlots.findAll", query = "SELECT a FROM AppointmentSlots a"),
    @NamedQuery(name = "AppointmentSlots.findBySlotId", query = "SELECT a FROM AppointmentSlots a WHERE a.slotId = :slotId"),
    @NamedQuery(name = "AppointmentSlots.findBySlotDate", query = "SELECT a FROM AppointmentSlots a WHERE a.slotDate = :slotDate"),
    @NamedQuery(name = "AppointmentSlots.findBySlotTime", query = "SELECT a FROM AppointmentSlots a WHERE a.slotTime = :slotTime"),
    @NamedQuery(name = "AppointmentSlots.findByStatus", query = "SELECT a FROM AppointmentSlots a WHERE a.status = :status"),
    @NamedQuery(name = "AppointmentSlots.findByCreatedAt", query = "SELECT a FROM AppointmentSlots a WHERE a.createdAt = :createdAt")})
public class AppointmentSlots implements Serializable {

    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "slot_id")
    private Long slotId;
    @Basic(optional = false)
    @NotNull
    @Column(name = "slot_date")
    @Temporal(TemporalType.DATE)
    private Date slotDate;
    @Basic(optional = false)
    @NotNull
    @Column(name = "slot_time")
    @Temporal(TemporalType.TIME)
    private Date slotTime;
    @Size(max = 20)
    @Column(name = "status")
    private String status;
    @Column(name = "created_at")
    @Temporal(TemporalType.TIMESTAMP)
    private Date createdAt;
@JoinColumn(name = "clinic_id", referencedColumnName = "clinic_id")
@ManyToOne(optional = false)
@JsonBackReference
private Clinics clinicId;

@JoinColumn(name = "doctor_id", referencedColumnName = "doctor_id")
@ManyToOne(optional = false)
@JsonBackReference
private Doctors doctorId;

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "slotId")
    private Collection<Appointments> appointmentsCollection;

    public AppointmentSlots() {
    }

    public AppointmentSlots(Long slotId) {
        this.slotId = slotId;
    }

    public AppointmentSlots(Long slotId, Date slotDate, Date slotTime) {
        this.slotId = slotId;
        this.slotDate = slotDate;
        this.slotTime = slotTime;
    }

    public Long getSlotId() {
        return slotId;
    }

    public void setSlotId(Long slotId) {
        this.slotId = slotId;
    }

    public Date getSlotDate() {
        return slotDate;
    }

    public void setSlotDate(Date slotDate) {
        this.slotDate = slotDate;
    }

    public Date getSlotTime() {
        return slotTime;
    }

    public void setSlotTime(Date slotTime) {
        this.slotTime = slotTime;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    public Clinics getClinicId() {
        return clinicId;
    }

    public void setClinicId(Clinics clinicId) {
        this.clinicId = clinicId;
    }

    public Doctors getDoctorId() {
        return doctorId;
    }

    public void setDoctorId(Doctors doctorId) {
        this.doctorId = doctorId;
    }

    @XmlTransient
    public Collection<Appointments> getAppointmentsCollection() {
        return appointmentsCollection;
    }

    public void setAppointmentsCollection(Collection<Appointments> appointmentsCollection) {
        this.appointmentsCollection = appointmentsCollection;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (slotId != null ? slotId.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof AppointmentSlots)) {
            return false;
        }
        AppointmentSlots other = (AppointmentSlots) object;
        if ((this.slotId == null && other.slotId != null) || (this.slotId != null && !this.slotId.equals(other.slotId))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "com.spring.bioMedical.entity.AppointmentSlots[ slotId=" + slotId + " ]";
    }
    
}
