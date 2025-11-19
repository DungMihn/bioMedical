package com.spring.bioMedical.service;

import com.spring.bioMedical.entity.Clinics;
import com.spring.bioMedical.repository.ClinicRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class ClinicService {
    
    @Autowired
    private ClinicRepository clinicRepository;
    
    // Lấy tất cả clinics
    public List<Clinics> getAllClinics() {
        return clinicRepository.findAllByOrderByNameAsc();
    }
    
    // Lấy clinic theo ID
    public Clinics getClinicById(Long clinicId) {
        Optional<Clinics> clinic = clinicRepository.findById(clinicId);
        return clinic.orElseThrow(() -> new RuntimeException("Clinic not found with id: " + clinicId));
    }
    
    // Lấy clinic theo ID (trả về Optional)
    public Optional<Clinics> findClinicById(Long clinicId) {
        return clinicRepository.findById(clinicId);
    }
    
    // Tạo clinic mới
    public Clinics createClinic(Clinics clinic) {
        // Validate
        if (clinic.getName() == null || clinic.getName().trim().isEmpty()) {
            throw new RuntimeException("Clinic name is required");
        }
        
        // Check if clinic with same name already exists
        if (clinicRepository.existsByName(clinic.getName())) {
            throw new RuntimeException("Clinic with name '" + clinic.getName() + "' already exists");
        }
        
        // Set default values if not provided
        if (clinic.getOpenTime() == null) {
            clinic.setOpenTime(LocalTime.of(8, 0));
        }
        if (clinic.getCloseTime() == null) {
            clinic.setCloseTime(LocalTime.of(17, 0));
        }
        if (clinic.getSlotTime() == null) {
            clinic.setSlotTime(60);
        }
        
        return clinicRepository.save(clinic);
    }
    
    // Cập nhật clinic
    public Clinics updateClinic(Long clinicId, Clinics clinicDetails) {
        Clinics clinic = getClinicById(clinicId);
        
        // Update fields
        if (clinicDetails.getName() != null && !clinicDetails.getName().trim().isEmpty()) {
            clinic.setName(clinicDetails.getName());
        }
        if (clinicDetails.getAddress() != null) {
            clinic.setAddress(clinicDetails.getAddress());
        }
        if (clinicDetails.getPhone() != null) {
            clinic.setPhone(clinicDetails.getPhone());
        }
        if (clinicDetails.getOpenTime() != null) {
            clinic.setOpenTime(clinicDetails.getOpenTime());
        }
        if (clinicDetails.getCloseTime() != null) {
            clinic.setCloseTime(clinicDetails.getCloseTime());
        }
        if (clinicDetails.getSlotTime() != null) {
            clinic.setSlotTime(clinicDetails.getSlotTime());
        }
        
        return clinicRepository.save(clinic);
    }
    
    // Xóa clinic
    public void deleteClinic(Long clinicId) {
        Clinics clinic = getClinicById(clinicId);
        clinicRepository.delete(clinic);
    }
    
    // Tìm kiếm clinics theo tên
    public List<Clinics> searchClinicsByName(String name) {
        if (name == null || name.trim().isEmpty()) {
            return getAllClinics();
        }
        return clinicRepository.findByNameContainingIgnoreCase(name.trim());
    }
    
    // Lấy clinic theo số điện thoại
    public Optional<Clinics> getClinicByPhone(String phone) {
        return clinicRepository.findByPhone(phone);
    }
    
    // Kiểm tra xem clinic có tồn tại không
    public boolean clinicExists(Long clinicId) {
        return clinicRepository.existsById(clinicId);
    }
    
    // Đếm số lượng clinics
    public long getClinicsCount() {
        return clinicRepository.countClinics();
    }
    
    // Lấy clinics có slot time cụ thể
    public List<Clinics> getClinicsBySlotTime(Integer slotTime) {
        return clinicRepository.findBySlotTime(slotTime);
    }
    
    // Validate clinic working hours
    public boolean isValidWorkingHours(LocalTime openTime, LocalTime closeTime) {
        return openTime != null && closeTime != null && openTime.isBefore(closeTime);
    }
    
    // Get available slot times for a clinic
    public List<LocalTime> getAvailableSlots(Long clinicId) {
        Clinics clinic = getClinicById(clinicId);
        List<LocalTime> slots = new java.util.ArrayList<>();
        
        LocalTime current = clinic.getOpenTime();
        while (current.isBefore(clinic.getCloseTime())) {
            slots.add(current);
            current = current.plusMinutes(clinic.getSlotTime());
        }
        
        return slots;
    }
}