/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.spring.bioMedical.entity;

import java.io.Serializable;
import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import javax.xml.bind.annotation.XmlRootElement;

/**
 *
 * @author macbookprom1
 */
@Entity
@Table(name = "Prescriptions")
@XmlRootElement
@NamedQueries({
    @NamedQuery(name = "Prescriptions.findAll", query = "SELECT p FROM Prescriptions p"),
    @NamedQuery(name = "Prescriptions.findByPrescriptionId", query = "SELECT p FROM Prescriptions p WHERE p.prescriptionId = :prescriptionId"),
    @NamedQuery(name = "Prescriptions.findByMedicationName", query = "SELECT p FROM Prescriptions p WHERE p.medicationName = :medicationName"),
    @NamedQuery(name = "Prescriptions.findByDosage", query = "SELECT p FROM Prescriptions p WHERE p.dosage = :dosage"),
    @NamedQuery(name = "Prescriptions.findByFrequency", query = "SELECT p FROM Prescriptions p WHERE p.frequency = :frequency"),
    @NamedQuery(name = "Prescriptions.findByDuration", query = "SELECT p FROM Prescriptions p WHERE p.duration = :duration"),
    @NamedQuery(name = "Prescriptions.findByNotes", query = "SELECT p FROM Prescriptions p WHERE p.notes = :notes")})
public class Prescriptions implements Serializable {

    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "prescription_id")
    private Long prescriptionId;
    @Basic(optional = false)
    @NotNull
    @Size(min = 1, max = 255)
    @Column(name = "medication_name")
    private String medicationName;
    @Size(max = 100)
    @Column(name = "dosage")
    private String dosage;
    @Size(max = 100)
    @Column(name = "frequency")
    private String frequency;
    @Size(max = 100)
    @Column(name = "duration")
    private String duration;
    @Size(max = 500)
    @Column(name = "notes")
    private String notes;
    @JoinColumn(name = "record_id", referencedColumnName = "record_id")
    @ManyToOne(optional = false)
    @com.fasterxml.jackson.annotation.JsonBackReference
    private Medicalrecords recordId;

    public Prescriptions() {
    }

    public Prescriptions(Long prescriptionId) {
        this.prescriptionId = prescriptionId;
    }

    public Prescriptions(Long prescriptionId, String medicationName) {
        this.prescriptionId = prescriptionId;
        this.medicationName = medicationName;
    }

    public Long getPrescriptionId() {
        return prescriptionId;
    }

    public void setPrescriptionId(Long prescriptionId) {
        this.prescriptionId = prescriptionId;
    }

    public String getMedicationName() {
        return medicationName;
    }

    public void setMedicationName(String medicationName) {
        this.medicationName = medicationName;
    }

    public String getDosage() {
        return dosage;
    }

    public void setDosage(String dosage) {
        this.dosage = dosage;
    }

    public String getFrequency() {
        return frequency;
    }

    public void setFrequency(String frequency) {
        this.frequency = frequency;
    }

    public String getDuration() {
        return duration;
    }

    public void setDuration(String duration) {
        this.duration = duration;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public Medicalrecords getRecordId() {
        return recordId;
    }

    public void setRecordId(Medicalrecords recordId) {
        this.recordId = recordId;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (prescriptionId != null ? prescriptionId.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof Prescriptions)) {
            return false;
        }
        Prescriptions other = (Prescriptions) object;
        if ((this.prescriptionId == null && other.prescriptionId != null) || (this.prescriptionId != null && !this.prescriptionId.equals(other.prescriptionId))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "com.spring.bioMedical.entity.Prescriptions[ prescriptionId=" + prescriptionId + " ]";
    }
    
}
