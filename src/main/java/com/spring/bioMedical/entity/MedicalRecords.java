package com.spring.bioMedical.entity;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Date;
import java.util.List;

@Entity
@Table(name = "MedicalRecords")
@NamedQueries({
    @NamedQuery(name = "MedicalRecords.findAll", query = "SELECT m FROM MedicalRecords m"),
    @NamedQuery(name = "MedicalRecords.findByRecordId", query = "SELECT m FROM MedicalRecords m WHERE m.recordId = :recordId"),
    @NamedQuery(name = "MedicalRecords.findBySymptoms", query = "SELECT m FROM MedicalRecords m WHERE m.symptoms = :symptoms"),
    @NamedQuery(name = "MedicalRecords.findByDiagnosis", query = "SELECT m FROM MedicalRecords m WHERE m.diagnosis = :diagnosis"),
    @NamedQuery(name = "MedicalRecords.findByTreatment", query = "SELECT m FROM MedicalRecords m WHERE m.treatment = :treatment"),
    @NamedQuery(name = "MedicalRecords.findByNotes", query = "SELECT m FROM MedicalRecords m WHERE m.notes = :notes")
})
public class MedicalRecords implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "record_id")
    private Long recordId;

    // ✅ SỬA: Dùng relationship thay vì ID trực tiếp
    @ManyToOne(optional = false)
    @JoinColumn(name = "appointment_id", referencedColumnName = "appointment_id")
    private Appointments appointment;

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

    // ✅ SỬA: Dùng relationship với Prescriptions
    @OneToMany(mappedBy = "medicalRecord", cascade = CascadeType.ALL)
    private List<Prescriptions> prescriptions;

    // ✅ Transient fields cho frontend
    @Transient
    private Users patient;

    // Constructors
    public MedicalRecords() {
    }

    public MedicalRecords(Long recordId) {
        this.recordId = recordId;
    }

    // Getters and Setters với naming convention thống nhất
    public Long getRecordId() { 
        return recordId; 
    }
    
    public void setRecordId(Long recordId) { 
        this.recordId = recordId; 
    }

    // ✅ QUAN TRỌNG: Đặt tên theo relationship - dùng "appointment" thay vì "appointmentId"
    public Appointments getAppointment() { 
        return appointment; 
    }
    
    public void setAppointment(Appointments appointment) { 
        this.appointment = appointment; 
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

    public List<Prescriptions> getPrescriptions() { 
        return prescriptions; 
    }
    
    public void setPrescriptions(List<Prescriptions> prescriptions) { 
        this.prescriptions = prescriptions; 
    }

    public Users getPatient() { 
        return patient; 
    }
    
    public void setPatient(Users patient) { 
        this.patient = patient; 
    }

    // ✅ Helper methods cho backward compatibility
    /**
     * For backward compatibility - get appointmentId as Long
     */
    public Long getAppointmentId() {
        return this.appointment != null ? this.appointment.getAppointmentId() : null;
    }

    /**
     * For backward compatibility - set appointment từ ID
     */
    public void setAppointmentId(Long appointmentId) {
        if (appointmentId != null) {
            Appointments appt = new Appointments();
            appt.setAppointmentId(appointmentId);
            this.appointment = appt;
        }
    }

    // ✅ Business methods
    public boolean hasPrescriptions() {
        return prescriptions != null && !prescriptions.isEmpty();
    }

    public boolean isComplete() {
        return symptoms != null && diagnosis != null && treatment != null;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (recordId != null ? recordId.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        if (!(object instanceof MedicalRecords)) {
            return false;
        }
        MedicalRecords other = (MedicalRecords) object;
        if ((this.recordId == null && other.recordId != null) || 
            (this.recordId != null && !this.recordId.equals(other.recordId))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "MedicalRecords{" + 
               "recordId=" + recordId + 
               ", appointment=" + (appointment != null ? appointment.getAppointmentId() : "null") +
               ", diagnosis=" + diagnosis + 
               '}';
    }
}