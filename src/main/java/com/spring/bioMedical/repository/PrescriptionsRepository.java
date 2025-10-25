package com.spring.bioMedical.repository;

import com.spring.bioMedical.entity.Prescriptions;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PrescriptionsRepository extends JpaRepository<Prescriptions, Long> {
    
    List<Prescriptions> findByRecordId(Long recordId);
    
    @Query("DELETE FROM Prescriptions p WHERE p.recordId = :recordId")
    void deleteByRecordId(@Param("recordId") Long recordId);
}