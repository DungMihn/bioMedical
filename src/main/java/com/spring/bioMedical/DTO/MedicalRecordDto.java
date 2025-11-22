/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.spring.bioMedical.dto;

import java.util.List;

public class MedicalRecordDto {
    private Long recordId;
    private Long appointmentId;
    private String diagnosis;
    private String symptoms;
    private String treatment;
    private String notes;

    // 👇 tên field phải TRÙNG với JS: data.prescriptionsCollection
    private List<PrescriptionDto> prescriptionsCollection;

    // getter/setter

    public Long getRecordId() {
        return recordId;
    }

    public void setRecordId(Long recordId) {
        this.recordId = recordId;
    }

    public Long getAppointmentId() {
        return appointmentId;
    }

    public void setAppointmentId(Long appointmentId) {
        this.appointmentId = appointmentId;
    }

    public String getDiagnosis() {
        return diagnosis;
    }

    public void setDiagnosis(String diagnosis) {
        this.diagnosis = diagnosis;
    }

    public String getSymptoms() {
        return symptoms;
    }

    public void setSymptoms(String symptoms) {
        this.symptoms = symptoms;
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

    public List<PrescriptionDto> getPrescriptionsCollection() {
        return prescriptionsCollection;
    }

    public void setPrescriptionsCollection(List<PrescriptionDto> prescriptionsCollection) {
        this.prescriptionsCollection = prescriptionsCollection;
    }
}

