package com.spring.bioMedical.service;

import com.spring.bioMedical.entity.AppointmentSlots;
import com.spring.bioMedical.repository.AppointmentSlotRepository;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class AppointmentSlotService {

    @Autowired
    private AppointmentSlotRepository appointmentSlotRepository;

    public AppointmentSlots getSlotById(Long slotId) {
        try {
            return appointmentSlotRepository.findById(slotId).orElse(null);
        } catch (Exception e) {
            return null;
        }
    }

    public List<AppointmentSlots> getSlotsByDoctorId(Long doctorId) {
        try {
            return appointmentSlotRepository.findByDoctorId(doctorId);
        } catch (Exception e) {
            return new ArrayList<>();
        }
    }

    public List<AppointmentSlots> getTodaySlotsByDoctorId(Long doctorId) {
        try {
            return appointmentSlotRepository.findByDoctorIdAndSlotDate(doctorId, LocalDate.now());
        } catch (Exception e) {
            return new ArrayList<>();
        }
    }
    // Thêm phương thức saveSlot

    public AppointmentSlots saveSlot(AppointmentSlots slot) {
        try {
            return appointmentSlotRepository.save(slot);
        } catch (Exception e) {
            System.err.println("Error saving slot: " + e.getMessage());
            return null;
        }
    }

}
