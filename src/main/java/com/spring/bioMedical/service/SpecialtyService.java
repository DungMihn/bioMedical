package com.spring.bioMedical.service;

import com.spring.bioMedical.entity.Specialty;
import java.util.List;

public interface SpecialtyService {
    List<Specialty> getAllSpecialties();
    Specialty getSpecialtyById(Long id);
    Specialty saveSpecialty(Specialty specialty);
    void deleteSpecialty(Long id);
}
