package com.spring.bioMedical.service;

import com.spring.bioMedical.entity.Specialties;
import com.spring.bioMedical.repository.SpecialtyRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class SpecialtyService {

    @Autowired
    private SpecialtyRepository specialtyRepository;

    public List<Specialties> getAllSpecialties() {
        return specialtyRepository.findAll();
    }


    public Specialties saveSpecialty(Specialties specialty) {
        return specialtyRepository.save(specialty);
    }
    
    public Specialties getSpecialtyById(Long id) {
    if (id == null) {
        return null;
    }
    try {
        Optional<Specialties> specialty = specialtyRepository.findById(id);
        System.out.println("[DEBUG] Specialty lookup - ID: " + id + ", Found: " + specialty.isPresent());
        return specialty.orElse(null);
    } catch (Exception e) {
        System.err.println("Error in getSpecialtyById: " + e.getMessage());
        return null;
    }
}
}