package com.spring.bioMedical.service;

import com.spring.bioMedical.entity.Doctors;
import com.spring.bioMedical.repository.DoctorRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class DoctorService {

    @Autowired
    private DoctorRepository doctorRepository;

    public Optional<Doctors> getDoctorByUserId(Long userId) {
        return doctorRepository.findByUserId(userId);
    }

    public Doctors getDoctorById(Long doctorId) {
        return doctorRepository.findById(doctorId).orElse(null);
    }

    public Optional<Doctors> getDoctorByIdOp(Long doctorId) {
        return doctorRepository.findByUserId(doctorId);
    }

    public List<Doctors> getDoctorsBySpecialty(Long specialtyId) {
        return doctorRepository.findBySpecialtyId(specialtyId);
    }

    public List<Doctors> getDoctorsByClinic(Long clinicId) {
        return doctorRepository.findByClinicId(clinicId);
    }

    public Doctors saveDoctor(Doctors doctor) {
        return doctorRepository.save(doctor);
    }
}
