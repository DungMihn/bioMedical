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

/**
 *
 * @author Admin
 */
@Entity
@Table(name = "Clinics")
@XmlRootElement
@NamedQueries({
    @NamedQuery(name = "Clinics.findAll", query = "SELECT c FROM Clinics c"),
    @NamedQuery(name = "Clinics.findByClinicId", query = "SELECT c FROM Clinics c WHERE c.clinicId = :clinicId"),
    @NamedQuery(name = "Clinics.findByName", query = "SELECT c FROM Clinics c WHERE c.name = :name"),
    @NamedQuery(name = "Clinics.findByAddress", query = "SELECT c FROM Clinics c WHERE c.address = :address"),
    @NamedQuery(name = "Clinics.findByPhone", query = "SELECT c FROM Clinics c WHERE c.phone = :phone"),
    @NamedQuery(name = "Clinics.findByCreatedAt", query = "SELECT c FROM Clinics c WHERE c.createdAt = :createdAt")})
public class Clinics implements Serializable {

    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "clinic_id")
    private Long clinicId;
    @Basic(optional = false)
    @NotNull
    @Size(min = 1, max = 255)
    @Column(name = "name")
    private String name;
    @Size(max = 500)
    @Column(name = "address")
    private String address;
    // @Pattern(regexp="^\\(?(\\d{3})\\)?[- ]?(\\d{3})[- ]?(\\d{4})$", message="Invalid phone/fax format, should be as xxx-xxx-xxxx")//if the field contains phone or fax number consider using this annotation to enforce field validation
    @Size(max = 50)
    @Column(name = "phone")
    private String phone;
    @Column(name = "created_at")
    @Temporal(TemporalType.TIMESTAMP)
    private Date createdAt;
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "clinicId")
    private Collection<AppointmentSlots> appointmentSlotsCollection;
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "clinicId")
    private Collection<Doctors> doctorsCollection;

    public Clinics() {
    }

    public Clinics(Long clinicId) {
        this.clinicId = clinicId;
    }

    public Clinics(Long clinicId, String name) {
        this.clinicId = clinicId;
        this.name = name;
    }

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

    @XmlTransient
    public Collection<Doctors> getDoctorsCollection() {
        return doctorsCollection;
    }

    public void setDoctorsCollection(Collection<Doctors> doctorsCollection) {
        this.doctorsCollection = doctorsCollection;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (clinicId != null ? clinicId.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof Clinics)) {
            return false;
        }
        Clinics other = (Clinics) object;
        if ((this.clinicId == null && other.clinicId != null) || (this.clinicId != null && !this.clinicId.equals(other.clinicId))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "com.spring.bioMedical.entity.Clinics[ clinicId=" + clinicId + " ]";
    }
    
}
