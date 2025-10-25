package com.spring.bioMedical.entity;

import javax.persistence.*;
import java.util.Date;
import java.util.List;

@Entity
@Table(name = "MedicalRecords")
public class MedicalRecords {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "record_id")
    private Long recordId;
    
    @Column(name = "appointment_id", nullable = false)
    private Long appointmentId;
    
    @Column(name = "symptoms", length = 1000)
    private String symptoms;
    
    @Column(name = "diagnosis", length = 1000)
    private String diagnosis;
    
    @Column(name = "treatment", length = 1000)
    private String treatment;
    
    @Column(name = "notes", length = 2000)
    private String notes;
    
    @Column(name = "created_at")
    @Temporal(TemporalType.TIMESTAMP)
    private Date createdAt;
    
    @Transient
    private List<Prescriptions> prescriptions;
    
    @Transient
    private Appointments appointment;
    
    @Transient
    private Users patient;
    
    // Getters and Setters
    public Long getRecordId() { return recordId; }
    public void setRecordId(Long recordId) { this.recordId = recordId; }
    
    public Long getAppointmentId() { return appointmentId; }
    public void setAppointmentId(Long appointmentId) { this.appointmentId = appointmentId; }
    
    public String getSymptoms() { return symptoms; }
    public void setSymptoms(String symptoms) { this.symptoms = symptoms; }
    
    public String getDiagnosis() { return diagnosis; }
    public void setDiagnosis(String diagnosis) { this.diagnosis = diagnosis; }
    
    public String getTreatment() { return treatment; }
    public void setTreatment(String treatment) { this.treatment = treatment; }
    
    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
    
    public Date getCreatedAt() { return createdAt; }
    public void setCreatedAt(Date createdAt) { this.createdAt = createdAt; }
    
    public List<Prescriptions> getPrescriptions() { return prescriptions; }
    public void setPrescriptions(List<Prescriptions> prescriptions) { this.prescriptions = prescriptions; }
    
    public Appointments getAppointment() { return appointment; }
    public void setAppointment(Appointments appointment) { this.appointment = appointment; }
    
    public Users getPatient() { return patient; }
    public void setPatient(Users patient) { this.patient = patient; }
}