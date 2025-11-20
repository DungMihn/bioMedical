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

    // ✅ SỬA: Dùng relationship trong derived query
    Optional<Doctors> findByUserUserId(Long userId);

    // ✅ SỬA: Dùng relationship trong @Query
    @Query("SELECT d FROM Doctors d WHERE d.user.userId = :userId")
    Optional<Doctors> findDoctorByUserId(@Param("userId") Long userId);

    // ✅ SỬA: Dùng relationship clinic
    Optional<Doctors> findTopByClinicClinicIdOrderByDoctorIdAsc(Long clinicId);

    // ✅ THÊM MỚI: Derived queries
    List<Doctors> findByClinicClinicId(Long clinicId);
    List<Doctors> findBySpecialtySpecialtyId(Long specialtyId);
    List<Doctors> findByClinicClinicIdAndSpecialtySpecialtyId(Long clinicId, Long specialtyId);
    
    // ✅ THÊM MỚI: Tìm doctors theo tên (thông qua user)
    @Query("SELECT d FROM Doctors d WHERE LOWER(d.user.fullName) LIKE LOWER(CONCAT('%', :name, '%'))")
    List<Doctors> findByUserNameContainingIgnoreCase(@Param("name") String name);
    
    // ✅ THÊM MỚI: Tìm doctors theo clinic và specialty name
    @Query("SELECT d FROM Doctors d WHERE d.clinic.clinicId = :clinicId AND LOWER(d.specialty.name) LIKE LOWER(CONCAT('%', :specialtyName, '%'))")
    List<Doctors> findByClinicAndSpecialtyName(@Param("clinicId") Long clinicId, @Param("specialtyName") String specialtyName);
    
    // ✅ THÊM MỚI: Đếm số doctors theo clinic
    @Query("SELECT COUNT(d) FROM Doctors d WHERE d.clinic.clinicId = :clinicId")
    Long countByClinicId(@Param("clinicId") Long clinicId);
    
    // ✅ THÊM MỚI: Kiểm tra user đã là doctor chưa
    @Query("SELECT COUNT(d) > 0 FROM Doctors d WHERE d.user.userId = :userId")
    boolean existsByUserId(@Param("userId") Long userId);
    
    // ✅ THÊM MỚI: Tìm doctors có slot available
    @Query("SELECT DISTINCT d FROM Doctors d JOIN d.appointmentSlots s WHERE s.status = 'AVAILABLE' AND s.slotDate >= CURRENT_DATE")
    List<Doctors> findDoctorsWithAvailableSlots();
}