package com.spring.bioMedical.service;

import com.spring.bioMedical.entity.Doctor;
import java.util.List;

public interface DoctorService {
    Doctor getDoctorByUserId(Long userId);
    List<Doctor> getAllDoctors();
}
