package com.spring.bioMedical.service;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.spring.bioMedical.entity.Appointment;
import com.spring.bioMedical.repository.AppointmentRepository;
import java.time.LocalDate;
import java.util.Collections;
import java.util.Optional;

@Service
public class AppointmentServiceImplementation  {

    private final AppointmentRepository appointmentRepository;

    @Autowired
    public AppointmentServiceImplementation(AppointmentRepository repo) {
        this.appointmentRepository = repo;
    }

    @Transactional
    public void save(Appointment app) {
        appointmentRepository.save(app);
    }

  public List<Appointment> findAll() {
        return appointmentRepository.findAll();
    }

  public List<Appointment> findAllByBranch(Long clinicId) {
    if (clinicId == null) return Collections.emptyList();

    String today = LocalDate.now().toString(); // "2025-11-04" kiểu yyyy-MM-dd
    return appointmentRepository.findByClinic_ClinicIdAndDateGreaterThanEqual(clinicId, today);
}
 /** LẤY APPOINTMENT THEO ID */
    public Optional<Appointment> findById(Long id) {
        return appointmentRepository.findById(id); // JpaRepository đã có sẵn
    }

}
