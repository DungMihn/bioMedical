package com.spring.bioMedical.service;

import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import java.util.List;
import com.spring.bioMedical.entity.Doctor;
import com.spring.bioMedical.repository.DoctorRepository;

@Service
public class DoctorServiceImplementation implements DoctorService {

    private final DoctorRepository doctorRepository;

    @Autowired
    public DoctorServiceImplementation(DoctorRepository doctorRepository) {
        this.doctorRepository = doctorRepository;
    }

    @Override
    public Doctor getDoctorByUserId(Long userId) {
        return doctorRepository.findByUser_UserId(userId);
    }

    @Override
    public List<Doctor> getAllDoctors() {
        return doctorRepository.findAll();
    }
}
