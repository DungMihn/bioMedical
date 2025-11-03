package com.spring.bioMedical.repository;

import com.spring.bioMedical.entity.Medicalrecords;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MedicalRecordRepository extends JpaRepository<Medicalrecords, Long> {
    Medicalrecords findByAppointmentId_AppointmentId(Long appointmentId);
}
