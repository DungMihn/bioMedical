package com.spring.bioMedical.repository;

import com.spring.bioMedical.entity.Prescriptions;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
public interface PrescriptionsRepository extends JpaRepository<Prescriptions, Long> {
    
    // ✅ SỬA: Dùng relationship thay vì recordId trực tiếp
    @Query("SELECT p FROM Prescriptions p WHERE p.medicalRecord.recordId = :recordId")
    List<Prescriptions> findByRecordId(@Param("recordId") Long recordId);
    
    // ✅ SỬA: Dùng relationship trong delete query
    @Modifying
    @Transactional
    @Query("DELETE FROM Prescriptions p WHERE p.medicalRecord.recordId = :recordId")
    void deleteByRecordId(@Param("recordId") Long recordId);
    
    // ✅ THÊM MỚI: Derived queries với relationship
    List<Prescriptions> findByMedicalRecordRecordId(Long recordId);
    
    // ✅ THÊM MỚI: Tìm prescriptions theo medication name
    @Query("SELECT p FROM Prescriptions p WHERE LOWER(p.medicationName) LIKE LOWER(CONCAT('%', :medicationName, '%'))")
    List<Prescriptions> findByMedicationNameContainingIgnoreCase(@Param("medicationName") String medicationName);
    
    // ✅ THÊM MỚI: Đếm số prescriptions theo record
    @Query("SELECT COUNT(p) FROM Prescriptions p WHERE p.medicalRecord.recordId = :recordId")
    Long countByRecordId(@Param("recordId") Long recordId);
    
    // ✅ THÊM MỚI: Tìm prescriptions có notes
    @Query("SELECT p FROM Prescriptions p WHERE p.notes IS NOT NULL AND p.notes <> ''")
    List<Prescriptions> findPrescriptionsWithNotes();
    
    // ✅ THÊM MỚI: Xóa nhiều prescriptions bằng record ids
    @Modifying
    @Transactional
    @Query("DELETE FROM Prescriptions p WHERE p.medicalRecord.recordId IN :recordIds")
    void deleteByRecordIds(@Param("recordIds") List<Long> recordIds);
}