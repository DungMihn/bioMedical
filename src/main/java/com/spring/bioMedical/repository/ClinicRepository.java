package com.spring.bioMedical.repository;

import com.spring.bioMedical.entity.Clinics;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ClinicRepository extends JpaRepository<Clinics, Long> {
}
