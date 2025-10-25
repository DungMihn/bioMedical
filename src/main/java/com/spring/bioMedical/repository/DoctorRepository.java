package com.spring.bioMedical.repository;

import com.spring.bioMedical.entity.Doctors;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DoctorRepository extends JpaRepository<Doctors, Long> {
    
    // Sửa query để tìm doctor bằng userId (không dùng join)
    @Query("SELECT d FROM Doctors d WHERE d.userId = :userId")
    Optional<Doctors> findByUserId(@Param("userId") Long userId);
    
    @Query("SELECT d FROM Doctors d WHERE d.specialtyId = :specialtyId")
    List<Doctors> findBySpecialtyId(@Param("specialtyId") Long specialtyId);
    
    @Query("SELECT d FROM Doctors d WHERE d.clinicId = :clinicId")
    List<Doctors> findByClinicId(@Param("clinicId") Long clinicId);
}