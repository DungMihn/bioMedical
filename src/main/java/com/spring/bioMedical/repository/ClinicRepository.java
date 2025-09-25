// repository/ClinicRepository.java
package com.spring.bioMedical.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.spring.bioMedical.entity.Clinic;

public interface ClinicRepository extends JpaRepository<Clinic, Long> {
}
