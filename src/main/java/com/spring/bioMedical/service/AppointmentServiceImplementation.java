package com.spring.bioMedical.service;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.spring.bioMedical.entity.Appointment;
import com.spring.bioMedical.repository.AppointmentRepository;
import java.util.Collections;

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
    return clinicId == null ? Collections.emptyList()
                            : appointmentRepository.findByClinic_ClinicId(clinicId);
}


}
