package com.spring.bioMedical.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import javax.persistence.*;
import java.io.Serializable;

@Entity
@Table(name = "Prescriptions")
@NamedQueries({
    @NamedQuery(name = "Prescriptions.findAll", query = "SELECT p FROM Prescriptions p"),
    @NamedQuery(name = "Prescriptions.findByPrescriptionId", query = "SELECT p FROM Prescriptions p WHERE p.prescriptionId = :prescriptionId"),
    @NamedQuery(name = "Prescriptions.findByMedicationName", query = "SELECT p FROM Prescriptions p WHERE p.medicationName = :medicationName"),
    @NamedQuery(name = "Prescriptions.findByDosage", query = "SELECT p FROM Prescriptions p WHERE p.dosage = :dosage"),
    @NamedQuery(name = "Prescriptions.findByFrequency", query = "SELECT p FROM Prescriptions p WHERE p.frequency = :frequency"),
    @NamedQuery(name = "Prescriptions.findByDuration", query = "SELECT p FROM Prescriptions p WHERE p.duration = :duration"),
    @NamedQuery(name = "Prescriptions.findByNotes", query = "SELECT p FROM Prescriptions p WHERE p.notes = :notes")
})
public class Prescriptions implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "prescription_id")
    private Long prescriptionId;

    @Basic(optional = false)
    @Column(name = "medication_name", length = 255)
    private String medicationName;

    @Column(name = "dosage", length = 100)
    private String dosage;

    @Column(name = "frequency", length = 100)
    private String frequency;

    @Column(name = "duration", length = 100)
    private String duration;

    @Column(name = "notes", length = 500)
    private String notes;

    // ✅ SỬA: Dùng relationship thay vì ID trực tiếp
    @ManyToOne(optional = false)
    @JsonIgnore
    @JoinColumn(name = "record_id", referencedColumnName = "record_id")
    private MedicalRecords medicalRecord;

    // Constructors
    public Prescriptions() {
    }

    public Prescriptions(Long prescriptionId) {
        this.prescriptionId = prescriptionId;
    }

    public Prescriptions(String medicationName, String dosage, String frequency, String duration) {
        this.medicationName = medicationName;
        this.dosage = dosage;
        this.frequency = frequency;
        this.duration = duration;
    }

    // Getters and Setters với naming convention thống nhất
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

    // ✅ QUAN TRỌNG: Đặt tên theo relationship - dùng "medicalRecord" thay vì "recordId"
    public MedicalRecords getMedicalRecord() { 
        return medicalRecord; 
    }
    
    public void setMedicalRecord(MedicalRecords medicalRecord) { 
        this.medicalRecord = medicalRecord; 
    }

    // ✅ Helper methods cho backward compatibility
    /**
     * For backward compatibility - get recordId as Long
     */
    public Long getRecordId() {
        return this.medicalRecord != null ? this.medicalRecord.getRecordId() : null;
    }

    /**
     * For backward compatibility - set medicalRecord từ ID
     */
    public void setRecordId(Long recordId) {
        if (recordId != null) {
            MedicalRecords record = new MedicalRecords();
            record.setRecordId(recordId);
            this.medicalRecord = record;
        }
    }

    // ✅ Business methods
    public String getFullPrescription() {
        StringBuilder sb = new StringBuilder();
        sb.append(medicationName);
        if (dosage != null) sb.append(" - ").append(dosage);
        if (frequency != null) sb.append(" - ").append(frequency);
        if (duration != null) sb.append(" - ").append(duration);
        return sb.toString();
    }

    public boolean isComplete() {
        return medicationName != null && !medicationName.trim().isEmpty();
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (prescriptionId != null ? prescriptionId.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        if (!(object instanceof Prescriptions)) {
            return false;
        }
        Prescriptions other = (Prescriptions) object;
        if ((this.prescriptionId == null && other.prescriptionId != null) || 
            (this.prescriptionId != null && !this.prescriptionId.equals(other.prescriptionId))) {
            return false;
        }
        return true;
    }

}