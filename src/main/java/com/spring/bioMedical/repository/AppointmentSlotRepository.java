/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package com.spring.bioMedical.repository;

import com.spring.bioMedical.entity.AppointmentSlots;
import java.time.LocalDate;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 *
 * @author Admin
 */
@Repository
public interface AppointmentSlotRepository extends JpaRepository<AppointmentSlots, Long> {
    
    @Query("SELECT s FROM AppointmentSlots s WHERE s.doctorId = :doctorId")
    List<AppointmentSlots> findByDoctorId(@Param("doctorId") Long doctorId);
    
    @Query("SELECT s FROM AppointmentSlots s WHERE s.doctorId = :doctorId AND s.slotDate = :date")
    List<AppointmentSlots> findByDoctorIdAndSlotDate(@Param("doctorId") Long doctorId, @Param("date") LocalDate date);
    
    @Query("SELECT s FROM AppointmentSlots s WHERE s.doctorId = :doctorId AND s.slotDate >= CURRENT_DATE")
    List<AppointmentSlots> findUpcomingSlotsByDoctorId(@Param("doctorId") Long doctorId);
}
