package com.spring.bioMedical.repository;

import com.spring.bioMedical.entity.Doctors;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DoctorRepository extends JpaRepository<Doctors, Long> {
}
