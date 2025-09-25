// service/ClinicService.java
package com.spring.bioMedical.service;

import java.util.List;
import com.spring.bioMedical.entity.Clinic;

public interface ClinicService {
    List<Clinic> findAll();
    Clinic findById(Long id);
    Clinic save(Clinic clinic);
    void deleteById(Long id);
}
