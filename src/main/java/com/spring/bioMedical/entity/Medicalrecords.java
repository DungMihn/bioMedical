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

/**
 *
 * @author macbookprom1
 */
@Entity
@Table(name = "Medicalrecords")
@XmlRootElement
@NamedQueries({
    @NamedQuery(name = "Medicalrecords.findAll", query = "SELECT m FROM Medicalrecords m"),
    @NamedQuery(name = "Medicalrecords.findByRecordId", query = "SELECT m FROM Medicalrecords m WHERE m.recordId = :recordId"),
    @NamedQuery(name = "Medicalrecords.findBySymptoms", query = "SELECT m FROM Medicalrecords m WHERE m.symptoms = :symptoms"),
    @NamedQuery(name = "Medicalrecords.findByDiagnosis", query = "SELECT m FROM Medicalrecords m WHERE m.diagnosis = :diagnosis"),
    @NamedQuery(name = "Medicalrecords.findByTreatment", query = "SELECT m FROM Medicalrecords m WHERE m.treatment = :treatment"),
    @NamedQuery(name = "Medicalrecords.findByNotes", query = "SELECT m FROM Medicalrecords m WHERE m.notes = :notes"),
    @NamedQuery(name = "Medicalrecords.findByCreatedAt", query = "SELECT m FROM Medicalrecords m WHERE m.createdAt = :createdAt")})
public class Medicalrecords implements Serializable {

    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "record_id")
    private Long recordId;
    @Size(max = 1000)
    @Column(name = "symptoms")
    private String symptoms;
    @Size(max = 1000)
    @Column(name = "diagnosis")
    private String diagnosis;
    @Size(max = 1000)
    @Column(name = "treatment")
    private String treatment;
    @Size(max = 2000)
    @Column(name = "notes")
    private String notes;
    @Column(name = "created_at")
    @Temporal(TemporalType.TIMESTAMP)
    private Date createdAt;
    @JoinColumn(name = "appointment_id", referencedColumnName = "appointment_id")
    @ManyToOne(optional = false)
    @com.fasterxml.jackson.annotation.JsonBackReference
    private Appointments appointmentId;
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "recordId")
    @com.fasterxml.jackson.annotation.JsonManagedReference
    private Collection<Prescriptions> prescriptionsCollection;

    public Medicalrecords() {
    }

    public Medicalrecords(Long recordId) {
        this.recordId = recordId;
    }

    public Long getRecordId() {
        return recordId;
    }

    public void setRecordId(Long recordId) {
        this.recordId = recordId;
    }

    public String getSymptoms() {
        return symptoms;
    }

    public void setSymptoms(String symptoms) {
        this.symptoms = symptoms;
    }

    public String getDiagnosis() {
        return diagnosis;
    }

    public void setDiagnosis(String diagnosis) {
        this.diagnosis = diagnosis;
    }

    public String getTreatment() {
        return treatment;
    }

    public void setTreatment(String treatment) {
        this.treatment = treatment;
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

    public Appointments getAppointmentId() {
        return appointmentId;
    }

    public void setAppointmentId(Appointments appointmentId) {
        this.appointmentId = appointmentId;
    }

    @XmlTransient
    public Collection<Prescriptions> getPrescriptionsCollection() {
        return prescriptionsCollection;
    }

    public void setPrescriptionsCollection(Collection<Prescriptions> prescriptionsCollection) {
        this.prescriptionsCollection = prescriptionsCollection;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (recordId != null ? recordId.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof Medicalrecords)) {
            return false;
        }
        Medicalrecords other = (Medicalrecords) object;
        if ((this.recordId == null && other.recordId != null) || (this.recordId != null && !this.recordId.equals(other.recordId))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "com.spring.bioMedical.entity.Medicalrecords[ recordId=" + recordId + " ]";
    }
    
}
