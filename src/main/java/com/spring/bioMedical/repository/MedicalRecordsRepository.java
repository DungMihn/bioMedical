package com.spring.bioMedical.repository;

import com.spring.bioMedical.entity.MedicalRecords;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MedicalRecordsRepository extends JpaRepository<MedicalRecords, Long> {
    
    @Query("SELECT mr FROM MedicalRecords mr WHERE mr.appointmentId IN " +
           "(SELECT a.appointmentId FROM Appointments a WHERE a.slotId IN " +
           "(SELECT s.slotId FROM AppointmentSlots s WHERE s.doctorId = :doctorId))")
    List<MedicalRecords> findByDoctorId(@Param("doctorId") Long doctorId);
    
    @Query("SELECT mr FROM MedicalRecords mr WHERE mr.appointmentId IN " +
           "(SELECT a.appointmentId FROM Appointments a WHERE a.userId = :patientId)")
    List<MedicalRecords> findByPatientId(@Param("patientId") Long patientId);
    
    @Query("SELECT mr FROM MedicalRecords mr WHERE mr.appointmentId = :appointmentId")
    MedicalRecords findByAppointmentId(@Param("appointmentId") Long appointmentId);
}