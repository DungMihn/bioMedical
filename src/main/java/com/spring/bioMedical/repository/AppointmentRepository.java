package com.spring.bioMedical.repository;

import com.spring.bioMedical.entity.Appointments;
import com.spring.bioMedical.entity.Users;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AppointmentRepository extends JpaRepository<Appointments, Long> {

    List<Appointments> findByUserId(Users user);
}
