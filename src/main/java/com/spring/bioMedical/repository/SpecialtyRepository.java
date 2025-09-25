// repository/SpecialtyRepository.java
package com.spring.bioMedical.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.spring.bioMedical.entity.Specialty;

public interface SpecialtyRepository extends JpaRepository<Specialty, Long> {
}
