package com.spring.bioMedical.repository;

import com.spring.bioMedical.entity.Specialties;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SpecialtyRepository extends JpaRepository<Specialties, Long> {
}
