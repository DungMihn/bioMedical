/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.spring.bioMedical.DTO;

import com.spring.bioMedical.entity.Appointments;

/**
 *
 * @author Admin
 */
public class AppointmentHistoryDTO {

    private Appointments appointment;
    private String timeAgo;
    private boolean canViewMedicalRecord;
    private boolean canCreateMedicalRecord;
    private String statusBadgeClass;

    // Constructor
    public AppointmentHistoryDTO(Appointments appointment, String timeAgo,
            boolean canViewMedicalRecord, boolean canCreateMedicalRecord,
            String statusBadgeClass) {
        this.appointment = appointment;
        this.timeAgo = timeAgo;
        this.canViewMedicalRecord = canViewMedicalRecord;
        this.canCreateMedicalRecord = canCreateMedicalRecord;
        this.statusBadgeClass = statusBadgeClass;
    }

    // Getters and Setters
    public Appointments getAppointment() {
        return appointment;
    }

    public void setAppointment(Appointments appointment) {
        this.appointment = appointment;
    }

    public String getTimeAgo() {
        return timeAgo;
    }

    public void setTimeAgo(String timeAgo) {
        this.timeAgo = timeAgo;
    }

    public boolean isCanViewMedicalRecord() {
        return canViewMedicalRecord;
    }

    public void setCanViewMedicalRecord(boolean canViewMedicalRecord) {
        this.canViewMedicalRecord = canViewMedicalRecord;
    }

    public boolean isCanCreateMedicalRecord() {
        return canCreateMedicalRecord;
    }

    public void setCanCreateMedicalRecord(boolean canCreateMedicalRecord) {
        this.canCreateMedicalRecord = canCreateMedicalRecord;
    }

    public String getStatusBadgeClass() {
        return statusBadgeClass;
    }

    public void setStatusBadgeClass(String statusBadgeClass) {
        this.statusBadgeClass = statusBadgeClass;
    }
}
