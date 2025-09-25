package com.spring.bioMedical.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.spring.bioMedical.entity.Doctor;

public interface DoctorRepository extends JpaRepository<Doctor, Long> {
    Doctor findByUser_UserId(Long userId);
}
