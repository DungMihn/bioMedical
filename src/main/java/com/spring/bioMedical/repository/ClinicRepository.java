// repository/ClinicRepository.java
package com.spring.bioMedical.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.spring.bioMedical.entity.Clinics;
import java.util.List;

public interface ClinicRepository extends JpaRepository<Clinics, Long> {

    List<Clinics> findByNameContainingIgnoreCase(String namePart);
}
