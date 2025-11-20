// service/ClinicService.java
package com.spring.bioMedical.service;

import java.util.List;
import com.spring.bioMedical.entity.Clinics;

public interface ClinicService {
    List<Clinics> findAll();
    Clinics findById(Long id);
    Clinics save(Clinics clinic);
    void deleteById(Long id);
}
