/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.spring.bioMedical.Mapper;

import com.spring.bioMedical.entity.Appointments;
import org.springframework.stereotype.Component;

/**
 *
 * @author Admin
 */
@Component
public class AppointmentMapper {
    
    // Chuyển từ entity cũ (dùng ID) sang entity chuẩn
    public Appointments convertFromOldEntity(Appointments oldAppointment) {
        Appointments standard = new Appointments();
        standard.setAppointmentId(oldAppointment.getAppointmentId());
        standard.setStatus(oldAppointment.getStatus());
        standard.setNotes(oldAppointment.getNotes());
        standard.setCreatedAt(oldAppointment.getCreatedAt());
        
        // Các transient fields
        standard.setPatientName(oldAppointment.getPatientName());
        standard.setPatientEmail(oldAppointment.getPatientEmail());
        standard.setAppointmentDate(oldAppointment.getAppointmentDate());
        standard.setAppointmentTime(oldAppointment.getAppointmentTime());
        
        return standard;
    }
    
    // Populate transient fields từ relationship
    public void populateTransientFields(Appointments appointment) {
        if (appointment.getUser() != null) {
            appointment.setPatientName(appointment.getUser().getFullName());
            appointment.setPatientEmail(appointment.getUser().getEmail());
        }
        if (appointment.getSlot() != null) {
            appointment.setAppointmentDate(appointment.getSlot().getSlotDate().toString());
            appointment.setAppointmentTime(appointment.getSlot().getSlotTime().toString());
        }
    }
}
