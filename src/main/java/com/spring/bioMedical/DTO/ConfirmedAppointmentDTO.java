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
public class ConfirmedAppointmentDTO {

    private Appointments appointment;
    private String timeIndicatorClass;
    private String timeIndicatorText;
    private boolean todayOrPast;
    private boolean past;
    private boolean canCreateMedicalRecord; // THÊM TRƯỜNG MỚI

    // Constructor
    public ConfirmedAppointmentDTO(Appointments appointment, String timeIndicatorClass,
            String timeIndicatorText, boolean todayOrPast,
            boolean past, boolean canCreateMedicalRecord) {
        this.appointment = appointment;
        this.timeIndicatorClass = timeIndicatorClass;
        this.timeIndicatorText = timeIndicatorText;
        this.todayOrPast = todayOrPast;
        this.past = past;
        this.canCreateMedicalRecord = canCreateMedicalRecord;
    }

    // Getters and Setters
    public Appointments getAppointment() {
        return appointment;
    }

    public void setAppointment(Appointments appointment) {
        this.appointment = appointment;
    }

    public String getTimeIndicatorClass() {
        return timeIndicatorClass;
    }

    public void setTimeIndicatorClass(String timeIndicatorClass) {
        this.timeIndicatorClass = timeIndicatorClass;
    }

    public String getTimeIndicatorText() {
        return timeIndicatorText;
    }

    public void setTimeIndicatorText(String timeIndicatorText) {
        this.timeIndicatorText = timeIndicatorText;
    }

    public boolean isTodayOrPast() {
        return todayOrPast;
    }

    public void setTodayOrPast(boolean todayOrPast) {
        this.todayOrPast = todayOrPast;
    }

    public boolean isPast() {
        return past;
    }

    public void setPast(boolean past) {
        this.past = past;
    }

    public boolean isCanCreateMedicalRecord() {
        return canCreateMedicalRecord;
    }

    public void setCanCreateMedicalRecord(boolean canCreateMedicalRecord) {
        this.canCreateMedicalRecord = canCreateMedicalRecord;
    }
}
