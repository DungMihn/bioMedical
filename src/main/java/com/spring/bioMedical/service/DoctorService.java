package com.spring.bioMedical.service;

import com.spring.bioMedical.entity.Doctors;
import com.spring.bioMedical.repository.DoctorRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class DoctorService {

    @Autowired
    private DoctorRepository doctorRepository;

    @Autowired
    private SpecialtyService specialtyService;

    public Optional<Doctors> getDoctorByUserId(Long userId) {
        return doctorRepository.findByUserUserId(userId);
    }

    public Doctors getDoctorById(Long doctorId) {
        return doctorRepository.findById(doctorId).orElse(null);
    }

    public Doctors findByUserId(Long userId) {
        return doctorRepository.findByUserUserId(userId)
                .orElse(null);
    }

//    // Thêm method để lấy thông tin đầy đủ bao gồm tên chuyên khoa
//    public String getDoctorSpecialtyName(Long doctorId) {
//        Doctors doctor = getDoctorById(doctorId);
//        if (doctor != null) {
//            return specialtyService.getSpecialtyName(doctor.getSpecialtyId());
//        }
//        return "Đa khoa";
//    }
//    
//    public Doctors saveDoctor(Doctors doctor) {
//        return doctorRepository.save(doctor);
//    }
}
