// service/ClinicServiceImplementation.java
package com.spring.bioMedical.service;

import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import com.spring.bioMedical.repository.ClinicRepository;
import com.spring.bioMedical.entity.Clinics;
import java.util.List;
import java.util.Optional;

@Service
public class ClinicServiceImplementation implements ClinicService {

    @Autowired
    private ClinicRepository clinicRepository;

    @Override
    public List<Clinics> findAll() {
        return clinicRepository.findAll();
    }

    @Override
    public Clinics findById(Long id) {
        return clinicRepository.findById(id).orElse(null);
    }

    @Override
    public Clinics save(Clinics clinic) {
        return clinicRepository.save(clinic);
    }

    @Override
    public void deleteById(Long id) {
        clinicRepository.deleteById(id);
    }
    
    //Diệu
     // Lấy clinic theo ID
    public Clinics getClinicById(Long clinicId) {
        Optional<Clinics> clinic = clinicRepository.findById(clinicId);
        return clinic.orElseThrow(() -> new RuntimeException("Clinic not found with id: " + clinicId));
    }
}
