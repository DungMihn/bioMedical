package com.spring.bioMedical.entity;

import javax.persistence.*;

@Entity
@Table(name = "Prescriptions")
public class Prescriptions {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "prescription_id")
    private Long prescriptionId;
    
    @Column(name = "record_id", nullable = false)
    private Long recordId;
    
    @Column(name = "medication_name", nullable = false, length = 255)
    private String medicationName;
    
    @Column(name = "dosage", length = 100)
    private String dosage;
    
    @Column(name = "frequency", length = 100)
    private String frequency;
    
    @Column(name = "duration", length = 100)
    private String duration;
    
    @Column(name = "notes", length = 500)
    private String notes;
    
    // Getters and Setters
    public Long getPrescriptionId() { return prescriptionId; }
    public void setPrescriptionId(Long prescriptionId) { this.prescriptionId = prescriptionId; }
    
    public Long getRecordId() { return recordId; }
    public void setRecordId(Long recordId) { this.recordId = recordId; }
    
    public String getMedicationName() { return medicationName; }
    public void setMedicationName(String medicationName) { this.medicationName = medicationName; }
    
    public String getDosage() { return dosage; }
    public void setDosage(String dosage) { this.dosage = dosage; }
    
    public String getFrequency() { return frequency; }
    public void setFrequency(String frequency) { this.frequency = frequency; }
    
    public String getDuration() { return duration; }
    public void setDuration(String duration) { this.duration = duration; }
    
    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
}