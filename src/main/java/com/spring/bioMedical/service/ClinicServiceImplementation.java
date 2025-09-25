// service/ClinicServiceImplementation.java
package com.spring.bioMedical.service;

import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import com.spring.bioMedical.repository.ClinicRepository;
import com.spring.bioMedical.entity.Clinic;
import java.util.List;

@Service
public class ClinicServiceImplementation implements ClinicService {

    @Autowired
    private ClinicRepository clinicRepository;

    @Override
    public List<Clinic> findAll() {
        return clinicRepository.findAll();
    }

    @Override
    public Clinic findById(Long id) {
        return clinicRepository.findById(id).orElse(null);
    }

    @Override
    public Clinic save(Clinic clinic) {
        return clinicRepository.save(clinic);
    }

    @Override
    public void deleteById(Long id) {
        clinicRepository.deleteById(id);
    }
}
