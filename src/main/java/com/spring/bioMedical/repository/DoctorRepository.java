package com.spring.bioMedical.repository;

import com.spring.bioMedical.entity.Doctors;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface DoctorRepository extends JpaRepository<Doctors, Long> {

    Optional<Doctors> findByUserId(Long userId);

    @Query("SELECT d FROM Doctors d WHERE d.userId = :userId")
    Optional<Doctors> findDoctorByUserId(@Param("userId") Long userId);
}
