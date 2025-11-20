/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.spring.bioMedical.service;

import com.spring.bioMedical.entity.Clinics;
import com.spring.bioMedical.entity.Doctors;
import com.spring.bioMedical.entity.Specialties;
import com.spring.bioMedical.entity.Users;
import com.spring.bioMedical.repository.DoctorRepository;
import java.util.HashMap;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 *
 * @author Admin
 */
@Service
@Transactional
public class DoctorProfileService {

    @Autowired
    private DoctorRepository doctorsRepository;

    @Autowired
    private UsersService usersService;

    @Autowired
    private SpecialtyService specialtyService;

    @Autowired
    private ClinicServiceImplementation clinicService;

    public Doctors getDoctorProfile(Long userId) {
        return doctorsRepository.findByUserUserId(userId)
                .orElseThrow(() -> new RuntimeException("Doctor not found"));
    }

    public Doctors updateDoctorBio(Long doctorId, String bio) {
        Doctors doctor = doctorsRepository.findById(doctorId)
                .orElseThrow(() -> new RuntimeException("Doctor not found"));
        doctor.setBio(bio);
        return doctorsRepository.save(doctor);
    }

    // Lấy thông tin đầy đủ để hiển thị profile
    public Map<String, Object> getDoctorProfileWithDetails(Long userId) {
        Map<String, Object> profileData = new HashMap<>();

        Doctors doctor = getDoctorProfile(userId);
        Users user = usersService.findById(userId);

        // Lấy thông tin chuyên khoa và phòng khám
        String specialtyName = "Đa khoa";
        String clinicName = "Không xác định";

        try {
            if (doctor.getSpecialtyId() != null) {
                Specialties specialty = specialtyService.getSpecialtyById(doctor.getSpecialtyId());
                specialtyName = specialty != null ? specialty.getName() : "Đa khoa";
            }
            if (doctor.getClinicId() != null) {
                Clinics clinic = clinicService.getClinicById(doctor.getClinicId());
                clinicName = clinic != null ? clinic.getName() : "Không xác định";
            }
        } catch (Exception e) {
            // Ignore errors, use default values
        }

        profileData.put("doctor", doctor);
        profileData.put("user", user);
        profileData.put("specialtyName", specialtyName);
        profileData.put("clinicName", clinicName);

        return profileData;
    }
}
