/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.spring.bioMedical.DTO;

import com.spring.bioMedical.entity.Doctors;
import com.spring.bioMedical.entity.MedicalRecords;
import com.spring.bioMedical.entity.Prescriptions;
import com.spring.bioMedical.entity.Users;
import java.util.List;

/**
 *
 * @author Admin
 */
public class MedicalRecordDetail {
    private MedicalRecords medicalRecord;
    private Users patient;
    private Doctors doctor;
    private List<Prescriptions> prescriptions;
    
    // Constructors, getters và setters
    public MedicalRecordDetail() {}
    
    public MedicalRecords getMedicalRecord() {
        return medicalRecord;
    }
    
    public void setMedicalRecord(MedicalRecords medicalRecord) {
        this.medicalRecord = medicalRecord;
    }
    
    public Users getPatient() {
        return patient;
    }
    
    public void setPatient(Users patient) {
        this.patient = patient;
    }
    
    public Doctors getDoctor() {
        return doctor;
    }
    
    public void setDoctor(Doctors doctor) {
        this.doctor = doctor;
    }
    
    public List<Prescriptions> getPrescriptions() {
        return prescriptions;
    }
    
    public void setPrescriptions(List<Prescriptions> prescriptions) {
        this.prescriptions = prescriptions;
    }
}
