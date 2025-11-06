package com.spring.bioMedical.repository;

import java.util.List;
import java.util.Optional;
import java.time.LocalDate;

import com.spring.bioMedical.entity.Appointment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository("appointmentRepository")
public interface AppointmentRepository extends JpaRepository<Appointment, Long> {

    List<Appointment> findByClinic_ClinicId(Long clinicId);
    List<Appointment> findByClinic_ClinicIdAndDateGreaterThanEqual(Long clinicId, String today);
    Optional<Appointment> findByIdAndClinic_ClinicId(Long id, Long clinicId);
}
