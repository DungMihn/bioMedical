// repository/ClinicRepository.java
package com.spring.bioMedical.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.spring.bioMedical.entity.Clinic;
import java.util.List;

public interface ClinicRepository extends JpaRepository<Clinic, Long> {
    List<Clinic> findByNameContainingIgnoreCase(String namePart);
}
